package com.example.courseapi.models

sealed interface ScheduleResult {
    data class Success(
        val courses: List<Schedule>
    ): ScheduleResult
    data class Error(
        val error: String = "",
        val message: String? = ""
    ): ScheduleResult
}