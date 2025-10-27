package com.example.courseapi.models

sealed interface CourseResult {
    data class Success(
        val courses: List<Course>
    ): CourseResult
    data class Error(
        val error: String = "",
        val message: String? = ""
    ): CourseResult
}


