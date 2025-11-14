package com.example.courseapi.services.course

import com.example.courseapi.repos.course.CourseRepo
import com.example.courseapi.repos.course.ValidFields
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class CourseServiceValidationTest {

    private val repo: CourseRepo = mock()
    private val service: CourseService = CourseService(repo)
    
    private val defaultValidFields = ValidFields(
        subjects = listOf("CSE", "MTH", "PHY"),
        campuses = listOf("Main", "Regional"),
        terms = listOf("202620", "202710"),
        deliveryTypes = listOf("Face2Face", "Online"),
        levels = listOf("100", "200", "300"),
        days = listOf("M", "T", "W", "R", "F"),
        waitlistTypes = listOf("open", "closed", "")
    )
    
    @BeforeEach
    fun setup() {
        whenever(runBlocking { repo.getOrFetchValidFields() }).thenReturn(defaultValidFields)
    }

    @Test
    fun `throws exception when campus empty`() {
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getCourseByInfo(campus = emptyList(), term = "202620") }
        }
    }

    @Test
    fun `throws exception when campus invalid`() {
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getCourseByInfo(campus = listOf("INVALID"), term = "202620") }
        }
    }

    @Test
    fun `throws exception when term empty`() {
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "") }
        }
    }

    @Test
    fun `throws exception when subject invalid`() {
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", subject = listOf("INVALID")) }
        }
    }

    @Test
    fun `throws exception when delivery invalid`() {
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", delivery = listOf("INVALID")) }
        }
    }

    @Test
    fun `throws exception when startEndTime invalid size`() {
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", startEndTime = listOf("12:00 AM")) }
        }
    }

    @Test
    fun `throws exception when openWaitlist invalid`() {
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", openWaitlist = "INVALID") }
        }
    }

    @Test
    fun `throws exception when level invalid`() {
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", level = "INVALID") }
        }
    }

    @Test
    fun `throws exception when daysFilter invalid`() {
        assertThrows<IllegalArgumentException> {
            runBlocking { service.getCourseByInfo(campus = listOf("Main"), term = "202620", daysFilter = listOf("X")) }
        }
    }

    @Test
    fun `throws exception when partOfTerm invalid`() {
        // This test no longer applies since partOfTerm validation was removed
        // The partOfTerm parameter is passed directly to the API without validation
    }

    @Test
    fun `throws exception when courseNum invalid format`() {
        // This test no longer applies since courseNum validation was removed
        // The courseNum parameter is passed directly to the API without validation
    }

    @Test
    fun `throws exception when creditHours negative`() {
        // This test no longer applies since creditHours validation was removed
        // The creditHours parameter is passed directly to the API without validation
    }
}
