package com.example.courseapi.repos.utils.course

import com.example.courseapi.models.dto.course.CourseByInfoInput
import io.ktor.http.encodeURLParameter

fun formRequest(formParts: ArrayList<String>, input: CourseByInfoInput, token: String): ArrayList<String> {
    formParts.add("_token=${token.encodeURLParameter()}")
    formParts.add("term=${input.term.encodeURLParameter()}")
    input.campus.forEach { formParts.add("campusFilter%5B%5D=${it.encodeURLParameter()}") }
    input.subject?.forEach { formParts.add("subject%5B%5D=${it.encodeURLParameter()}") }
    formParts.add("courseNumber=${input.courseNum ?: ""}")
    formParts.add("openWaitlist=${input.openWaitlist ?: ""}")
    formParts.add("crnNumber=${input.crn ?: ""}")
    formParts.add("level=${input.level ?: ""}")
    formParts.add("courseTitle=${input.courseTitle ?: ""}")
    formParts.add("instructor=")
    formParts.add("instructorUid=")
    formParts.add("creditHours=${input.creditHours ?: ""}")
    input.startEndTime?.forEach { formParts.add("startEndTime%5B%5D=${it.encodeURLParameter()}") }
        ?: formParts.addAll(listOf("startEndTime%5B%5D=", "startEndTime%5B%5D="))
    formParts.add("courseSearch=Find")
    input.delivery?.forEach { formParts.add("sectionAttributes%5B%5D=${it.encodeURLParameter()}") }
    input.attributes?.forEach { formParts.add("sectionFilterAttributes%5B%5D=${it.encodeURLParameter()}") }
    input.partOfTerm?.forEach { formParts.add("partOfTerm%5B%5D=${it.encodeURLParameter()}") }
    input.daysFilter?.forEach { formParts.add("daysFilter%5B%5D=${it.encodeURLParameter()}") }
    return formParts
}