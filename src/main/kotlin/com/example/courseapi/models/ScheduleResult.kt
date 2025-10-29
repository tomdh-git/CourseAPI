package com.example.courseapi.models

sealed interface ScheduleResult

data class SuccessSchedule(
    val schedules: List<Schedule>
) : ScheduleResult

data class ErrorSchedule(
    val error: String,
    val message: String?
) : ScheduleResult
