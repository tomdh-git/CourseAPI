package com.example.courseapi.models.dto.schedule

data class FillerByAttributesInput(
    val delivery: List<String>?=null,
    val attributes: List<String>,
    val courses: List<String>,
    val campus: List<String>,
    val term: String,
    val preferredStart: String? = null,
    val preferredEnd: String? = null
){
    fun toScheduleInput(): ScheduleByCourseInput {
        return ScheduleByCourseInput(delivery,courses,campus,term,true,preferredStart,preferredEnd)
    }
}
