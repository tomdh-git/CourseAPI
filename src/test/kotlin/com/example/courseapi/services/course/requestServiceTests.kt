//package com.example.courseapi.services.course
//
//import io.ktor.client.*
//import io.ktor.client.request.*
//import io.ktor.client.statement.*
//import io.mockk.*
//import kotlinx.coroutines.runBlocking
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import kotlin.test.assertEquals
//import kotlin.test.assertTrue
//
//class RequestServiceTest {
//
//    private lateinit var client: HttpClient
//    private lateinit var service: RequestService
//
//    @BeforeEach
//    fun setup() {
//        client = mockk()
//        service = RequestService(client)
//    }
//
//    @Test
//    fun `getTokenResponse returns HTML substring`() = runBlocking {
//        val mockResponse = mockk<HttpResponse>()
//        every { runBlocking { mockResponse.bodyAsText() } } returns "<html>${"x".repeat(8000)}</html>"
//        coEvery { client.get(any<String>(), any()) } returns mockResponse
//
//        val tokenHtml = service.getTokenResponse()
//        assertTrue(tokenHtml.length in 1000..3000)
//    }
//
//    @Test
//    fun `getOrFetchToken fetches new token when none cached`() = runBlocking {
//        val html = """<input type="hidden" name="_token" value="abc123">"""
//        coEvery { client.get(any<String>(), any()) } returns mockk {
//            coEvery { bodyAsText() } returns html
//        }
//
//        val token = service.getOrFetchToken()
//        assertEquals("abc123", token)
//    }
//
//    @Test
//    fun `getOrFetchToken reuses cached token if not expired`() = runBlocking {
//        val html = """<input type="hidden" name="_token" value="firstToken">"""
//        coEvery { client.get(any<String>(), any()) } returns mockk {
//            coEvery { bodyAsText() } returns html
//        }
//
//        val first = service.getOrFetchToken()
//        val second = service.getOrFetchToken()
//        assertEquals(first, second)
//    }
//
////    @Test
////    fun `postResultResponse handles normal HTML`() = runBlocking {
////        val mockResponse = mockk<HttpResponse>()
////        coEvery { mockResponse.status.value } returns 200
////        coEvery { mockResponse.bodyAsText() } returns "<html><body>OK</body></html>"
////        coEvery { client.post(any<String>(), any()) } returns mockResponse
////
////        val result = service.postResultResponse("data=a")
////        assertEquals(200, result.status)
////        assertTrue(result.body.contains("OK"))
////    }
//
////    @Test
////    fun `postResultResponse follows meta refresh redirect`() = runBlocking {
////        val postResponse = mockk<HttpResponse>()
////        val redirectResponse = mockk<HttpResponse>()
////        coEvery { postResponse.status.value } returns 200
////        coEvery { postResponse.bodyAsText() } returns """<meta http-equiv="refresh" content="0; url='/courselist/next'">"""
////        coEvery { redirectResponse.bodyAsText() } returns "<html>redirected</html>"
////        coEvery { client.post(any<String>(), any()) } returns postResponse
////        coEvery { client.get("https://www.apps.miamioh.edu/courselist/next", any()) } returns redirectResponse
////
////        val result = service.postResultResponse("body=data")
////        assertTrue(result.body.contains("redirected"))
////    }
//
////    @Test
////    fun `postResultResponse handles redirect with absolute URL`() = runBlocking {
////        val postResponse = mockk<HttpResponse>()
////        val redirectResponse = mockk<HttpResponse>()
////        coEvery { postResponse.status.value } returns 200
////        coEvery { postResponse.bodyAsText() } returns """<meta http-equiv="refresh" content="0; url='https://example.com/page'">"""
////        coEvery { redirectResponse.bodyAsText() } returns "<html>absolute redirect</html>"
////        coEvery { client.post(any<String>(), any()) } returns postResponse
////        coEvery { client.get("https://example.com/page", any()) } returns redirectResponse
////
////        val result = service.postResultResponse("form=data")
////        assertTrue(result.body.contains("absolute redirect"))
////    }
//}
