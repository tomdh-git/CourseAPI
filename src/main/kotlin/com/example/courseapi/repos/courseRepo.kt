package com.example.courseapi.repos

import com.example.courseapi.models.Course
import io.ktor.http.encodeURLParameter
import org.springframework.stereotype.Repository

@Repository
class CourseRepo{
    suspend fun getCourses(
        subject: List<String>?,
        courseNum: Int?,
        campus: List<String>?,
        attributes: List<String>?,
        delivery: List<String>?,
        term: String?,
    ): List<Course> {
        //get token
        val token = """<input[^>]*name=\"_token\"[^>]*value=\"([^\"]+)\"""".toRegex().find(getTokenResponse())?.groupValues?.get(1)
        if (token == null) { client.close(); return emptyList() }

        // build form
        val formParts = mutableListOf<String>()
        formParts.add("_token=${token.encodeURLParameter()}")
        if (!delivery.isNullOrEmpty()) { delivery.forEach { formParts.add("sectionAttributes%5B%5D=${it.encodeURLParameter()}") } }
        if (!attributes.isNullOrEmpty()) { attributes.forEach{formParts.add("sectionFilterAttributes%5B%5D=${it.encodeURLParameter()}")} }
        if (!term.isNullOrEmpty()) { formParts.add("term=${term.encodeURLParameter()}") }
        if (!campus.isNullOrEmpty()) { campus.forEach { formParts.add("campusFilter%5B%5D=${it.encodeURLParameter()}") } } else { formParts.add("campusFilter%5B%5D=All") }
        if (!subject.isNullOrEmpty()) { subject.forEach { formParts.add("subject%5B%5D=${it.encodeURLParameter()}") } }
        if (courseNum != null && courseNum > 0) { formParts.add("courseNumber=${courseNum}") } else { formParts.add("courseNumber=") }
        formParts.add("courseSearch=Find")
        formParts.add("openWaitlist=")
        formParts.add("crnNumber=")
        formParts.add("level=")
        formParts.add("courseTitle=")
        formParts.add("instructor=")
        formParts.add("instructorUid=")
        formParts.add("creditHours=")
        formParts.add("startEndTime%5B%5D=")
        formParts.add("startEndTime%5B%5D=")
        val formBody = formParts.joinToString("&")

        //send post request
        val resultHtml = postResultResponse(formBody)

        // parse
        return parseCourses(resultHtml)
    }
}
