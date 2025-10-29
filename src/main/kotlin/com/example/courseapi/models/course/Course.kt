package com.example.courseapi.models.course

data class Course(
    val subject: String = "",
    val courseNum: String = "",
    val title: String = "",
    val section: String = "",
    val crn: Int = 0,
    val campus: String = "",
    val credits: Int = 0,
    val capacity: String = "",
    val requests: String = "",
    val delivery: String = ""
)
