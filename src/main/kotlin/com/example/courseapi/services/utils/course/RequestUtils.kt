package com.example.courseapi.services.utils.course

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import jakarta.annotation.PostConstruct

@Component
class RequestUtils(private val client: HttpClient) {
    @Volatile private var lastToken: String? = null
    @Volatile private var lastTokenTs: Long = 0
    private val tokenTimeout = 45_000L
    private val refreshThreshold = 35_000L
    private val tokenLock = Mutex()
    @Volatile private var refreshJob: Job? = null
    private val refreshScope = CoroutineScope(Dispatchers.IO)
    
    // HTML caching for the token page
    @Volatile private var cachedHtml: String? = null
    @Volatile private var cachedHtmlTs: Long = 0
    private val htmlCacheTimeout = 30_000L
    private val htmlCacheLock = Mutex()
    
    // Precompiled regex for token extraction
    private val tokenRegex = Regex("""<input[^>]*name="_token"[^>]*value="([^"]+)"""")
    
    data class HttpTextResponse(val status: Int, val body: String)

    @PostConstruct
    fun warmUpConnection() {
        refreshScope.launch {
            try {
                getCourseList()
            } catch (e: Exception) {
                //maybe just log it
            }
        }
    }

    suspend fun getCourseList(): String {
        val now = System.currentTimeMillis()
        val cached = cachedHtml
        val age = now - cachedHtmlTs
        
        // Return cached HTML if fresh
        if (cached != null && age < htmlCacheTimeout) {
            return cached
        }
        
        // Fetch fresh HTML
        return htmlCacheLock.withLock {
            // Double-check after acquiring lock
            val againNow = System.currentTimeMillis()
            val againCached = cachedHtml
            val againAge = againNow - cachedHtmlTs
            
            if (againCached != null && againAge < htmlCacheTimeout) {
                return againCached
            }
            
            val initialResponse: HttpResponse = client.get(
                "https://www.apps.miamioh.edu/courselist/")
            { headers {
                append("Accept", "text/html")
                append("User-Agent", "Mozilla/5.0")
            } }
            val result = initialResponse.bodyAsText()
            
            // Cache the HTML
            cachedHtml = result
            cachedHtmlTs = System.currentTimeMillis()
            result
        }
    }

    suspend fun getToken(): String {
        val html = getCourseList()
        return tokenRegex.find(html)?.groupValues?.get(1) ?: ""
    }

    suspend fun getOrFetchToken(): String {
        val now = System.currentTimeMillis()
        val cached = lastToken
        val age = now - lastTokenTs
        
        // If token is in refresh window (35s-45s), start background refresh
        if (cached != null && age >= refreshThreshold && age < tokenTimeout) {
            val currentJob = refreshJob
            if (currentJob == null || !currentJob.isActive) {
                refreshJob = refreshScope.launch {
                    tokenLock.withLock {
                        val freshToken = getToken()
                        lastToken = freshToken
                        lastTokenTs = System.currentTimeMillis()
                    }
                }
            }
            return cached // Return current token immediately
        }
        
        // If token is still valid and not in refresh window, return it
        if (cached != null && age < tokenTimeout) {
            return cached
        }
        
        // If token is expired, fetch synchronously
        return tokenLock.withLock {
            val againNow = System.currentTimeMillis()
            if (lastToken != null && againNow - lastTokenTs < tokenTimeout) return lastToken!!
            val token = getToken()
            lastToken = token; lastTokenTs = againNow; token
        }
    }

    suspend fun postResultResponse(formBody: String): HttpTextResponse {
        val postResponse = getPostResponse(formBody)
        var resultHtml = postResponse.bodyAsText()
        
        val hasRedirect = resultHtml.contains("meta http-equiv=\"refresh\"")
        if (hasRedirect) {
            val redirectUrl = """content=\s*"\s*\d+;\s*url='([^']+)'\s*""""
                .toRegex()
                .find(resultHtml)?.groupValues?.get(1)
            if (redirectUrl != null) {
                resultHtml = getRedirectResponseHtml(redirectUrl)
            }
        }
        return HttpTextResponse(
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

