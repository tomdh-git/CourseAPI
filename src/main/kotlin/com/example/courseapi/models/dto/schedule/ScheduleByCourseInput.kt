package com.example.courseapi.models.dto.schedule

data class ScheduleByCourseInput(
    val delivery: List<String>? = null,
    val courses: List<String>,
    val campus: List<String>,
    val term: String,
    val optimizeFreeTime: Boolean?=false,
    val preferredStart: String? = null,
    val preferredEnd: String? = null
)
