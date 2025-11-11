package com.example.courseapi.services.course

import com.example.courseapi.client.RequestLimiter
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Service

@Service
class RequestService(private val client: HttpClient, private val limiter: RequestLimiter) {
    @Volatile private var lastToken: String? = null
    @Volatile private var lastTokenTs: Long = 0
    private  val tokenTimeout = 120_000L
    private val tokenLock = Mutex()
    data class HttpTextResponse(val status: Int, val body: String)

    suspend fun getTokenResponse(): String = limiter.limit{
        val initialResponse: HttpResponse = client.get("https://www.apps.miamioh.edu/courselist/") { headers { append("Accept", "text/html"); append("Accept-Encoding", "gzip, deflate"); append("User-Agent", "Mozilla/5.0") } }
        return@limit initialResponse.bodyAsText()
    }

    suspend fun getOrFetchToken(): String {
        val now = System.currentTimeMillis()
        val cached = lastToken
        if (cached != null && now - lastTokenTs < tokenTimeout) return cached
        return tokenLock.withLock { val againNow = System.currentTimeMillis(); if (lastToken != null && againNow - lastTokenTs < tokenTimeout) return lastToken!! ; val html = getTokenResponse() ; val token = """<input[^>]*name="_token"[^>]*value="([^"]+)"""".toRegex().find(html)?.groupValues?.get(1) ?: ""; lastToken = token; lastTokenTs = againNow; token }
    }

    suspend fun postResultResponse(formBody: String): HttpTextResponse = limiter.limit{
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
        return@limit HttpTextResponse(postResponse.status.value, resultHtml)
    }


}

