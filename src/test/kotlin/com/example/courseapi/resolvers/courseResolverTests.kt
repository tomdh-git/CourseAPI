package com.example.courseapi.resolvers

import com.example.courseapi.exceptions.*
import com.example.courseapi.models.course.*
import com.example.courseapi.models.schedule.*
import com.example.courseapi.services.course.CourseService
import com.example.courseapi.services.schedule.ScheduleService
import io.ktor.client.plugins.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class CourseResolverTests {

    private val cs: CourseService = mock()
    private val ss: ScheduleService = mock()
    private val resolver = CourseResolver(cs, ss)

    private fun sampleCourse() = Course(
        "CSE", "101", "Intro", "A", 12345,
        "Main", 3, "30", "10", "Face2Face"
    )

    private fun sampleSchedule() = Schedule(
        listOf(sampleCourse()), 0)

    @Test
    fun `getCourseByInfo returns Success`() = runBlocking {
        val courses = listOf(sampleCourse())
        whenever(cs.getCourseByInfo(
            anyOrNull(), anyOrNull(), any(), anyOrNull(), anyOrNull(),
            any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(),
            anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()
        )).thenReturn(courses)

        val result = resolver.getCourseByInfo(
            listOf("CSE"), null, listOf("All"), null,
            listOf("Face2Face"), "202620", "open", null, null,
            "GR", null, listOf("M"), 3, listOf("12:00 AM", "11:59 PM")
        )

        assertTrue(result is SuccessCourse)
        assertEquals(courses, (result as SuccessCourse).courses)
    }

    @Test
    fun `getCourseByCRN returns Success`() = runBlocking {
        val courses = listOf(sampleCourse())
        whenever(cs.getCourseByCRN(any(), any())).thenReturn(courses)

        val result = resolver.getCourseByCRN(12345, "202620")

        assertTrue(result is SuccessCourse)
        assertEquals(courses, (result as SuccessCourse).courses)
    }

    @Test
    fun `getScheduleByCourses returns Success`() = runBlocking {
        val schedules = listOf(sampleSchedule())
        whenever(ss.getScheduleByCourses(any(), any(), any(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn(schedules)

        val result = resolver.getScheduleByCourses(listOf("CSE101"), listOf("Main"), "202620", true, "08:00 AM", "06:00 PM")

        assertTrue(result is SuccessSchedule)
        assertEquals(schedules, (result as SuccessSchedule).schedules)
    }

    @Test
    fun `getFillerByAttributes returns Success`() = runBlocking {
        val schedules = listOf(sampleSchedule())
        whenever(ss.getFillerByAttributes(any(), any(), any(), any(), anyOrNull(), anyOrNull()))
            .thenReturn(schedules)

        val result = resolver.getFillerByAttributes(listOf("WRIT"), listOf("CSE101"), listOf("Main"), "202620", "08:00 AM", "06:00 PM")

        assertTrue(result is SuccessSchedule)
        assertEquals(schedules, (result as SuccessSchedule).schedules)
    }

    @Test
    fun `handles APIException`() = runBlocking {
        whenever(cs.getCourseByCRN(any(), any())).thenThrow(APIException("Token empty"))
        val result = resolver.getCourseByCRN(1, "202620")
        assertTrue(result is ErrorCourse)
        assertEquals("API EXCEPTION", (result as ErrorCourse).error)
    }

    @Test
    fun `handles QueryException`() = runBlocking {
        whenever(cs.getCourseByCRN(any(), any())).thenThrow(QueryException("Too many results"))
        val result = resolver.getCourseByCRN(1, "202620")
        assertTrue(result is ErrorCourse)
        assertEquals("QUERY EXCEPTION", (result as ErrorCourse).error)
    }

    @Test
    fun `handles IllegalArgumentException`() = runBlocking {
        whenever(cs.getCourseByCRN(any(), any())).thenThrow(IllegalArgumentException("Invalid"))
        val result = resolver.getCourseByCRN(1, "202620")
        assertTrue(result is ErrorCourse)
        assertEquals("ILLEGAL ARGUMENT EXCEPTION", (result as ErrorCourse).error)
    }

    @Test
    fun `handles NullPointerException`() = runBlocking {
        whenever(cs.getCourseByCRN(any(), any())).thenThrow(NullPointerException("null"))
        val result = resolver.getCourseByCRN(1, "202620")
        assertTrue(result is ErrorCourse)
        assertEquals("NULL POINTER EXCEPTION", (result as ErrorCourse).error)
    }

    @Test
    fun `handles TimeoutCancellationException`() = runBlocking {
        whenever(cs.getCourseByCRN(any(), any())).thenAnswer {
            runBlocking { withTimeout(1) { delay(100) } }
        }
        val result = resolver.getCourseByCRN(1, "202620")
        assertTrue(result is ErrorCourse)
        assertEquals("TIMEOUT EXCEPTION", (result as ErrorCourse).error)
    }

    @Test
    fun `handles unknown exceptions`() = runBlocking {
        whenever(cs.getCourseByCRN(any(), any())).thenThrow(RuntimeException("unknown"))
        val result = resolver.getCourseByCRN(1, "202620")
        assertTrue(result is ErrorCourse)
        assertEquals("UNKNOWN EXCEPTION", (result as ErrorCourse).error)
    }
}
