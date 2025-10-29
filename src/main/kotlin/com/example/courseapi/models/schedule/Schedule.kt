package com.example.courseapi.models.schedule

import com.example.courseapi.models.course.Course

data class Schedule(
    val courses: List<Course>,
    val freeTime: Int
)
