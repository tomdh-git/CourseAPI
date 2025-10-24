package com.example.courseapi.repos

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

val client = HttpClient(CIO) {
    install(HttpCookies)
    followRedirects = true
    expectSuccess = false
}

@Volatile private var lastToken: String? = null
@Volatile private var lastTokenTs: Long = 0
private const val TOKEN_TTL_MS = 120_000L

suspend fun getTokenResponse(): String{
    val initialResponse: HttpResponse = client.get("https://www.apps.miamioh.edu/courselist/") {
        headers {
            append("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            append("Accept-Language", "en-US,en;q=0.9")
            append("Accept-Encoding", "gzip, deflate")
            append("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36")
            append("Cache-Control", "max-age=0")
            append("Upgrade-Insecure-Requests", "1")
        }
    }
    return initialResponse.bodyAsText()
}

suspend fun getOrFetchToken(): String {
    val now = System.currentTimeMillis()
    val cached = lastToken
    if (cached != null && now - lastTokenTs < TOKEN_TTL_MS) return cached
    val html = getTokenResponse()
    val token = """<input[^>]*name=\"_token\"[^>]*value=\"([^\"]+)\"""".toRegex().find(html)?.groupValues?.get(1)
    if (token != null) {
        lastToken = token
        lastTokenTs = now
        return token
    }
    return ""
}

data class HttpTextResponse(val status: Int, val body: String)

suspend fun postResultResponse(formBody: String): HttpTextResponse {
    val postResponse: HttpResponse = client.post("https://www.apps.miamioh.edu/courselist/") {
        headers {
            append("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            append("Accept-Language", "en-US,en;q=0.9")
            append("Accept-Encoding", "gzip, deflate")
            append("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            append("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36")
            append("Origin", "https://www.apps.miamioh.edu")
            append("Referer", "https://www.apps.miamioh.edu/courselist/")
            append("Cache-Control", "max-age=0")
        }
        setBody(formBody)
    }

    var resultHtml = postResponse.bodyAsText()

    // refresh check
    if (resultHtml.contains("meta http-equiv=\"refresh\"")) {
        val redirectUrl = """content=\s*\"\s*\d+;\s*url='([^']+)'\s*\"""".toRegex().find(resultHtml)?.groupValues?.get(1)
        if (redirectUrl != null) {
            val absolute = if (redirectUrl.startsWith("http")) redirectUrl else {
                val path = if (redirectUrl.startsWith("/")) redirectUrl else "/courselist/" + redirectUrl
                "https://www.apps.miamioh.edu" + path
            }
            val redirectResponse = client.get(absolute) {
                headers {
                    append("Referer", "https://www.apps.miamioh.edu/courselist/")
                }
            }
            resultHtml = redirectResponse.bodyAsText()
        }
    }
    return HttpTextResponse(postResponse.status.value, resultHtml)
}
