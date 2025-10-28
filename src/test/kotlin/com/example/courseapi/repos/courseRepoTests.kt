import com.example.courseapi.exceptions.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*
import com.example.courseapi.services.*
import com.example.courseapi.repos.*

class CourseRepoTest {
    private val requests: RequestService = mock()
    private val parse: ParseService = mock()
    private val repo = CourseRepo(requests, parse)

    @Test fun `throws TokenException when token empty`() {
        whenever(runBlocking { requests.getOrFetchToken() }).thenReturn("")
        assertThrows<TokenException> { runBlocking { repo.getCourseByInfo(campus = listOf("Main"), term = "202620") } }
    }

    @Test fun `throws QueryException when too many results`() {
        whenever(runBlocking { requests.getOrFetchToken() }).thenReturn("token")
        whenever(runBlocking { requests.postResultResponse(any()) }).thenReturn(RequestService.HttpTextResponse(200,"Your query returned too many results."))
        assertThrows<QueryException> { runBlocking { repo.getCourseByInfo(campus = listOf("Main"), term = "202620") } }
    }
}
