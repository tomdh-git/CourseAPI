package com.example.courseapi.resolvers

import com.example.courseapi.exceptions.*
import com.example.courseapi.models.course.Course
import com.example.courseapi.models.course.ErrorCourse
import com.example.courseapi.models.course.SuccessCourse
import com.example.courseapi.services.course.CourseService
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*

class CourseResolverTests {

    private val service: CourseService = mock()
    private val resolver = CourseResolver(service)

    private fun sampleCourse() = Course(
        "CSE", 101, "Intro", "A", 12345,
        "Main", 3, "30", "10", "Face2Face"
    )

    @Test
    fun `getCourseByInfo returns Success`() = runBlocking {
        val courses = listOf(sampleCourse())
        whenever(service.getCourseByInfo(
            anyOrNull(), anyOrNull(), any(), anyOrNull(), anyOrNull(),
            any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull(),
            anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()
        )).thenReturn(courses)

        val result = resolver.getCourseByInfo(
            listOf("CSE"), null, listOf("All"), null,
            listOf("Face2Face"), "202620", "open", null, null,
            "GR", null, listOf("M"), 3, listOf("12:00 AM","11:59 PM")
        )

        assertTrue(result is SuccessCourse)
        assertEquals(courses, (result as SuccessCourse).courses)
    }

    @Test
    fun `getCourseByCRN returns Success`() = runBlocking {
        val courses = listOf(sampleCourse())
        whenever(service.getCourseByCRN(any(), any())).thenReturn(courses)

        val result = resolver.getCourseByCRN(12345, "202620")

        assertTrue(result is SuccessCourse)
        assertEquals(courses, (result as SuccessCourse).courses)
    }

    @Test
    fun `safeExecute handles TokenException`() = runBlocking {
        whenever(service.getCourseByCRN(any(), any())).thenThrow(TokenException("Token empty"))
        val result = resolver.getCourseByCRN(123, "202620")
        assertTrue(result is ErrorCourse)
        assertEquals("TOKEN EXCEPTION", (result as ErrorCourse).error)
    }

    @Test
    fun `safeExecute handles QueryException`() = runBlocking {
        whenever(service.getCourseByCRN(any(), any())).thenThrow(QueryException("Too many results"))
        val result = resolver.getCourseByCRN(123, "202620")
        assertTrue(result is ErrorCourse)
        assertEquals("QUERY EXCEPTION", (result as ErrorCourse).error)
    }

    @Test
    fun `safeExecute handles IllegalArgumentException`() = runBlocking {
        whenever(service.getCourseByCRN(any(), any())).thenThrow(IllegalArgumentException("Invalid input"))
        val result = resolver.getCourseByCRN(123, "202620")
        assertTrue(result is ErrorCourse)
        assertEquals("ILLEGAL ARGUMENT EXCEPTION", (result as ErrorCourse).error)
    }

    @Test
    fun `safeExecute handles NullPointerException`() = runBlocking {
        whenever(service.getCourseByCRN(any(), any())).thenThrow(NullPointerException("null"))
        val result = resolver.getCourseByCRN(123, "202620")
        assertTrue(result is ErrorCourse)
        assertEquals("NULL POINTER EXCEPTION", (result as ErrorCourse).error)
    }

    @Test
    fun `safeExecute handles TimeoutCancellationException`() = runBlocking {
        whenever(service.getCourseByCRN(any(), any())).thenAnswer {
            runBlocking { withTimeout(1) { delay(100); emptyList<Course>() } } // triggers TimeoutCancellationException
        }

        val result = resolver.getCourseByCRN(123, "202620")
        assertTrue(result is ErrorCourse)
        assertEquals("TIMEOUT EXCEPTION", (result as ErrorCourse).error)
    }

    @Test
    fun `safeExecute handles network exceptions`() = runBlocking {
        whenever(service.getCourseByCRN(any(), any())).thenAnswer { throw IOException("IO error") }
        val result = resolver.getCourseByCRN(123, "202620")
        assertTrue(result is ErrorCourse)
        assertEquals("NETWORK EXCEPTION", (result as ErrorCourse).error)
    }

    @Test
    fun `safeExecute handles unknown exceptions`() = runBlocking {
        whenever(service.getCourseByCRN(any(), any())).thenThrow(RuntimeException("unknown"))
        val result = resolver.getCourseByCRN(123, "202620")
        assertTrue(result is ErrorCourse)
        assertEquals("UNKNOWN EXCEPTION", (result as ErrorCourse).error)
    }
}
