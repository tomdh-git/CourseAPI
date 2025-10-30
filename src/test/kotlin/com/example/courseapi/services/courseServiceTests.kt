package com.example.courseapi.services

import com.example.courseapi.services.course.CourseService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CourseServiceValidationTest {

    private val service: CourseService = mock()

    @Test
    fun `throws exception when campus empty`() {
        whenever(runBlocking { service.getCourseByInfo(campus = emptyList(), term = "202620") })
            .thenCallRealMethod()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getCourseByInfo(campus = emptyList(), term = "202620") }
        }
    }

    @Test
    fun `throws exception when term empty`() {
        whenever(runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "") })
            .thenCallRealMethod()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "") }
        }
    }

    @Test
    fun `throws exception when subject invalid`() {
        whenever(runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", subject = listOf("INVALID")) })
            .thenCallRealMethod()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", subject = listOf("INVALID")) }
        }
    }

    @Test
    fun `throws exception when delivery invalid`() {
        whenever(runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", delivery = listOf("INVALID")) })
            .thenCallRealMethod()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", delivery = listOf("INVALID")) }
        }
    }

    @Test
    fun `throws exception when startEndTime invalid`() {
        whenever(runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", startEndTime = listOf("12:00 AM")) })
            .thenCallRealMethod()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", startEndTime = listOf("12:00 AM")) }
        }
    }

    @Test
    fun `throws exception when openWaitlist invalid`() {
        whenever(runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", openWaitlist = "INVALID") })
            .thenCallRealMethod()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", openWaitlist = "INVALID") }
        }
    }

    @Test
    fun `throws exception when level invalid`() {
        whenever(runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", level = "INVALID") })
            .thenCallRealMethod()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", level = "INVALID") }
        }
    }

    @Test
    fun `throws exception when daysFilter invalid`() {
        whenever(runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", daysFilter = listOf("X")) })
            .thenCallRealMethod()
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", daysFilter = listOf("X")) }
        }
    }
}
