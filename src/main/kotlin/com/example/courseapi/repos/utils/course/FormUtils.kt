package com.example.courseapi.repos.utils.course

import com.example.courseapi.models.dto.course.CourseByInfoInput
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private fun String.encode(): String = URLEncoder.encode(this, StandardCharsets.UTF_8)

fun formRequest(formParts: ArrayList<String>, input: CourseByInfoInput, token: String): ArrayList<String> {
    formParts.add("_token=${token.encode()}")
    formParts.add("term=${input.term.encode()}")
    input.campus.forEach { formParts.add("campusFilter%5B%5D=${it.encode()}") }
    input.subject?.forEach { formParts.add("subject%5B%5D=${it.encode()}") }
    formParts.add("courseNumber=${input.courseNum ?: ""}")
    formParts.add("openWaitlist=${input.openWaitlist ?: ""}")
    formParts.add("crnNumber=${input.crn ?: ""}")
    formParts.add("level=${input.level ?: ""}")
    formParts.add("courseTitle=${input.courseTitle ?: ""}")
    formParts.add("instructor=")
    formParts.add("instructorUid=")
    formParts.add("creditHours=${input.creditHours ?: ""}")
    input.startEndTime?.forEach { formParts.add("startEndTime%5B%5D=${it.encode()}") }
        ?: formParts.addAll(listOf("startEndTime%5B%5D=", "startEndTime%5B%5D="))
    formParts.add("courseSearch=Find")
    input.delivery?.forEach { formParts.add("sectionAttributes%5B%5D=${it.encode()}") }
    input.attributes?.forEach { formParts.add("sectionFilterAttributes%5B%5D=${it.encode()}") }
    input.partOfTerm?.forEach { formParts.add("partOfTerm%5B%5D=${it.encode()}") }
    input.daysFilter?.forEach { formParts.add("daysFilter%5B%5D=${it.encode()}") }
    return formParts
}