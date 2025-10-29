package com.example.courseapi.models.course

sealed interface CourseResult
data class SuccessCourse(
    val courses: List<Course>
): CourseResult
data class ErrorCourse(
    val error: String = "",
    val message: String? = ""
): CourseResult


