package com.example.courseapi.services.utils.course

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import kotlinx.coroutines.reactor.awaitSingle

@Component
class RequestUtils(private val webClient: WebClient) {
    @Volatile private var lastToken: String? = null
    @Volatile private var lastTokenTs: Long = 0
    private val tokenTimeout = 45_000L
    private val refreshThreshold = 35_000L
    private val tokenLock = Mutex()
    @Volatile private var refreshJob: Job? = null
    private val refreshScope = CoroutineScope(Dispatchers.IO)
    @Volatile private var cachedHtml: String? = null
    @Volatile private var cachedHtmlTs: Long = 0
    private val htmlCacheTimeout = 30_000L
    private val htmlCacheLock = Mutex()
    private val tokenRegex = Regex("""<input[^>]*name="_token"[^>]*value="([^"]+)"""")
    
    data class HttpTextResponse(val status: Int, val body: String)

    private val cookies = java.util.concurrent.ConcurrentHashMap<String, String>()

    @PostConstruct
    fun warmUpConnection() {
        refreshScope.launch {
            try { getCourseList() }
            catch (e: Exception) { /* maybe just log it **/ }
        }
    }

    suspend fun getCourseList(): String {
        val now = System.currentTimeMillis()
        val cached = cachedHtml
        val age = now - cachedHtmlTs

        val requestFresh = cached != null && age < htmlCacheTimeout
        if (requestFresh) return cached

        return htmlCacheLock.withLock {
            val againNow = System.currentTimeMillis()
            val againCached = cachedHtml
            val againAge = againNow - cachedHtmlTs

            val requestFreshAgain = againCached != null && againAge < htmlCacheTimeout
            if (requestFreshAgain) {
                return againCached
            }
            
            val result = webClient.get()
                .uri("https://www.apps.miamioh.edu/courselist/")
                .header("Accept", "text/html")
                .header("User-Agent", "Mozilla/5.0")
                .exchangeToMono { response ->
                    response.cookies().forEach { (name, cookieList) ->
                        if (cookieList.isNotEmpty()) {
                            cookies[name] = cookieList[0].value
                        }
                    }
                    response.bodyToMono(String::class.java)
                }.awaitSingle()

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

        val inWindow = cached != null && age >= refreshThreshold && age < tokenTimeout
        if (inWindow) {
            val currentJob = refreshJob
            val jobNotActive = currentJob == null || !currentJob.isActive
            if (jobNotActive) {
                refreshJob = refreshScope.launch {
                    tokenLock.withLock {
                        val freshToken = getToken()
                        lastToken = freshToken
                        lastTokenTs = System.currentTimeMillis()
                    }
                }
            }
            return cached
        }

        val requestFresh = cached != null && age < tokenTimeout
        if (requestFresh) return cached

        return tokenLock.withLock {
            val againNow = System.currentTimeMillis()
            val lastTokenValid = lastToken != null && againNow - lastTokenTs < tokenTimeout
            if (lastTokenValid) return lastToken!!

            val token = getToken()
            lastToken = token; lastTokenTs = againNow; token
        }
    }

    suspend fun postResultResponse(formBody: String): HttpTextResponse {
        val postResponse = getPostResponse(formBody)
        var resultHtml = postResponse.body ?: ""
        
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
            postResponse.statusCode.value(),
            resultHtml
        )
    }

    private val activeRequests = java.util.concurrent.atomic.AtomicInteger(0)
    private val isBusy = java.util.concurrent.atomic.AtomicBoolean(false)

    private suspend fun getPostResponse(formBody: String): ResponseEntity<String> {
        if (isBusy.get()) {
            throw com.example.courseapi.exceptions.ServerBusyException("Server is busy, please try again later")
        }

        activeRequests.incrementAndGet()

        return try {
            kotlinx.coroutines.withTimeout(28_000L) {
                webClient.post()
                    .uri("https://www.apps.miamioh.edu/courselist/")
                    .header("Accept", "text/html")
                    .header("Accept-Encoding", "gzip, deflate")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Origin", "https://www.apps.miamioh.edu")
                    .header("Referer", "https://www.apps.miamioh.edu/courselist/")
                    .cookies { map -> cookies.forEach { (k, v) -> map.add(k, v) } }
                    .bodyValue(formBody)
                    .retrieve()
                    .toEntity(String::class.java)
                    .awaitSingle()
            }
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            isBusy.set(true)
            throw com.example.courseapi.exceptions.ServerBusyException("Request timed out. Please try again later.")
        } catch (e: org.springframework.web.reactive.function.client.WebClientResponseException) {
            ResponseEntity.status(e.statusCode).body(e.responseBodyAsString)
        } finally {
            val noMoreRequests = activeRequests.decrementAndGet() == 0
            if (noMoreRequests) { isBusy.set(false) }
        }
    }

    private fun determineRedirect(redirectUrl: String): String {
        val redirectIsHttp = redirectUrl.startsWith("http")
        return if (redirectIsHttp) redirectUrl
        else {
            val isPath = redirectUrl.startsWith("/")
            val path = if (isPath) redirectUrl
            else "/courselist/$redirectUrl"
            "https://www.apps.miamioh.edu$path"
        }
    }

    private suspend fun getRedirectResponseHtml(redirectUrl: String): String {
        return webClient.get()
            .uri(determineRedirect(redirectUrl))
            .header("Referer", "https://www.apps.miamioh.edu/courselist/")
            .cookies { map -> cookies.forEach { (k, v) -> map.add(k, v) } }
            .retrieve()
            .bodyToMono(String::class.java)
            .awaitSingle()
    }
}
