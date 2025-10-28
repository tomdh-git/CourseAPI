package com.example.courseapi.models

data class Schedule(
    val courses: List<Course>,
    val freeTime: Int
)
