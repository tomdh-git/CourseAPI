package com.example.courseapi.services.utils.course

import com.example.courseapi.client.RequestLimiter
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Component

@Component
class RequestUtils(private val client: HttpClient, private val limiter: RequestLimiter) {
    @Volatile private var lastToken: String? = null
    @Volatile private var lastTokenTs: Long = 0
    private val tokenTimeout = 120_000L
    private val tokenLock = Mutex()
    data class HttpTextResponse(val status: Int, val body: String)

    suspend fun getCourseList(): String = limiter.limit{
        val initialResponse: HttpResponse = client.get(
            "https://www.apps.miamioh.edu/courselist/")
        { headers {
            append("Accept", "text/html")
            append("Accept-Encoding","gzip, deflate")
            append("User-Agent", "Mozilla/5.0")
        } }
        return@limit initialResponse.bodyAsText()
    }

    suspend fun getToken(): String {
        val html = getCourseList()
        return  """<input[^>]*name="_token"[^>]*value="([^"]+)""""
            .toRegex()
            .find(html)?.groupValues?.get(1) ?: ""
    }

    suspend fun getOrFetchToken(): String {
        val now = System.currentTimeMillis()
        val cached = lastToken
        if (cached != null && now - lastTokenTs < tokenTimeout) return cached
        return tokenLock.withLock {
            val againNow = System.currentTimeMillis()
            if (lastToken != null && againNow - lastTokenTs < tokenTimeout) return lastToken!!
            val token = getToken()
            lastToken = token; lastTokenTs = againNow; token }
    }

    suspend fun postResultResponse(formBody: String): HttpTextResponse = limiter.limit{
        val postResponse = getPostResponse(formBody)
        var resultHtml = postResponse.bodyAsText()
        val hasRedirect = resultHtml.contains("meta http-equiv=\"refresh\"")
        if (hasRedirect) {
            val redirectUrl = """content=\s*"\s*\d+;\s*url='([^']+)'\s*""""
                .toRegex()
                .find(resultHtml)?.groupValues?.get(1)
            if (redirectUrl != null)
                resultHtml = getRedirectResponseHtml(redirectUrl)
        }
        return@limit HttpTextResponse(
            postResponse.status.value,
            resultHtml
        )
    }

    private suspend fun getPostResponse(formBody: String): HttpResponse {
        val postResponse: HttpResponse = client.post(
            "https://www.apps.miamioh.edu/courselist/"
        ) {
            headers {
                append("Accept","text/html")
                append("Accept-Encoding", "gzip, deflate")
                append("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                append("User-Agent", "Mozilla/5.0")
                append("Origin", "https://www.apps.miamioh.edu")
                append("Referer", "https://www.apps.miamioh.edu/courselist/")
            }
            setBody(formBody)
        }
        return postResponse
    }

    private fun determineRedirect(redirectUrl: String): String {
        return if (redirectUrl.startsWith("http")) redirectUrl
        else {
            val path = if (redirectUrl.startsWith("/")) redirectUrl
            else "/courselist/$redirectUrl"
            "https://www.apps.miamioh.edu$path"
        }
    }

    private suspend fun getRedirectResponseHtml(redirectUrl: String): String {
        val redirectResponse = client.get(determineRedirect(redirectUrl)) {
            headers {
                append("Referer", "https://www.apps.miamioh.edu/courselist/")
            }
        }
        return redirectResponse.bodyAsText()
    }
}

