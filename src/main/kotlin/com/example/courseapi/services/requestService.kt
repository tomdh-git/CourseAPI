package com.example.courseapi.services

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.headers
import org.springframework.stereotype.Service

@Service
class RequestService(private val client: HttpClient) {
    @Volatile private var lastToken: String? = null
    @Volatile private var lastTokenTs: Long = 0
    private  val tokenTimeout = 120_000L
    data class HttpTextResponse(val status: Int, val body: String)

    suspend fun getTokenResponse(): String{
        val initialResponse: HttpResponse = client.get("https://www.apps.miamioh.edu/courselist/") { headers { append("Accept", "text/html"); append("Accept-Encoding", "gzip, deflate"); append("User-Agent", "Mozilla/5.0") } }
        return initialResponse.bodyAsText().substring(4500,7000)
    }

    suspend fun getOrFetchToken(): String {
        val now = System.currentTimeMillis()
        val cached = lastToken
        if (cached != null && now - lastTokenTs < tokenTimeout) return cached
        val html = getTokenResponse()
        val token = """<input[^>]*name="_token"[^>]*value="([^"]+)"""".toRegex().find(html)?.groupValues?.get(1)
        if (token != null) { lastToken = token; lastTokenTs = now; return token }
        return ""
    }

    suspend fun postResultResponse(formBody: String): HttpTextResponse {
        val postResponse: HttpResponse = client.post("https://www.apps.miamioh.edu/courselist/") { headers { append("Accept", "text/html"); append("Accept-Encoding", "gzip, deflate"); append("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"); append("User-Agent", "Mozilla/5.0"); append("Origin", "https://www.apps.miamioh.edu"); append("Referer", "https://www.apps.miamioh.edu/courselist/") }; setBody(formBody) }
        var resultHtml = postResponse.bodyAsText()

        // refresh check
        if (resultHtml.contains("meta http-equiv=\"refresh\"")) {
            val redirectUrl = """content=\s*"\s*\d+;\s*url='([^']+)'\s*"""".toRegex().find(resultHtml)?.groupValues?.get(1)
            if (redirectUrl != null) {
                val absolute = if (redirectUrl.startsWith("http")) redirectUrl else { val path = if (redirectUrl.startsWith("/")) redirectUrl else "/courselist/$redirectUrl"; "https://www.apps.miamioh.edu$path" }
                val redirectResponse = client.get(absolute) { headers { append("Referer", "https://www.apps.miamioh.edu/courselist/") } }
                resultHtml = redirectResponse.bodyAsText()
            }
        }
        return HttpTextResponse(postResponse.status.value, resultHtml)
    }
}

