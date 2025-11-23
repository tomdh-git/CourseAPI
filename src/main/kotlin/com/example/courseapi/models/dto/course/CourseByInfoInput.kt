package com.example.courseapi.models.dto.course

data class CourseByInfoInput(
    val subject: List<String>?=null,
    val courseNum: String?=null,
    val campus: List<String>,
    val attributes: List<String>?=null,
    val delivery: List<String>?=null,
    val term: String,
    val openWaitlist: String?=null,
    val crn: Int?=null,
    val partOfTerm: List<String>?=null,
    val level: String?=null,
    val courseTitle: String?=null,
    val daysFilter: List<String>?=null,
    val creditHours: Int?=null,
    val startEndTime: List<String>?=null
)