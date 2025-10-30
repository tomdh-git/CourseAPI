package com.example.courseapi.repos.course

import com.example.courseapi.exceptions.*
import com.example.courseapi.models.course.Course
import io.ktor.http.encodeURLParameter
import org.springframework.stereotype.Repository
import com.example.courseapi.services.course.ParseService
import com.example.courseapi.services.course.RequestService

@Repository
class CourseRepo(private val requests: RequestService, private val parse: ParseService){
    suspend fun getCourseByInfo(subject: List<String>? = null, courseNum: String? = null, campus: List<String>, attributes: List<String>? = null, delivery: List<String>? = null, term: String, openWaitlist: String? = null, crn: Int? = null, partOfTerm: List<String>? = null, level: String? = null, courseTitle: String? = null, daysFilter: List<String>? = null, creditHours: Int? = null, startEndTime: List<String>? = null): List<Course> {
        // get or reuse token (saves one GET on warm requests)
        val token = requests.getOrFetchToken()
        if (token.isEmpty()) throw TokenException("Empty Token")

        // build form
        val formParts = ArrayList<String>(24)
        formParts.add("_token=${token.encodeURLParameter()}")
        formParts.add("term=${term.encodeURLParameter()}")
        campus.forEach { formParts.add("campusFilter%5B%5D=${it.encodeURLParameter()}") }
        subject?.forEach { formParts.add("subject%5B%5D=${it.encodeURLParameter()}") }
        formParts.add("courseNumber=${courseNum ?: ""}")
        formParts.add("openWaitlist=${openWaitlist ?: ""}")
        formParts.add("crnNumber=${crn ?: ""}")
        formParts.add("level=${level ?: ""}")
        formParts.add("courseTitle=${courseTitle ?: ""}")
        formParts.add("instructor=")
        formParts.add("instructorUid=")
        formParts.add("creditHours=${creditHours ?: ""}")
        startEndTime?.forEach { formParts.add("startEndTime%5B%5D=${it.encodeURLParameter()}") }
            ?: formParts.addAll(listOf("startEndTime%5B%5D=", "startEndTime%5B%5D="))
        formParts.add("courseSearch=Find")
        delivery?.forEach { formParts.add("sectionAttributes%5B%5D=${it.encodeURLParameter()}") }
        attributes?.forEach { formParts.add("sectionFilterAttributes%5B%5D=${it.encodeURLParameter()}") }
        partOfTerm?.forEach { formParts.add("partOfTerm%5B%5D=${it.encodeURLParameter()}") }
        daysFilter?.forEach { formParts.add("daysFilter%5B%5D=${it.encodeURLParameter()}") }
        val formBody = formParts.joinToString("&")

//        println(formParts.joinToString("\n"))

        // send post request
        var resp = requests.postResultResponse(formBody)
        // page expired check
        if (resp.status == 419 || resp.body.contains("Page Expired", ignoreCase = true)) {
            val token = requests.getOrFetchToken()
            if (token.isNotEmpty()) {
                formParts[0] = "_token=${token.encodeURLParameter()}"
                val formBody = formParts.joinToString("&")
                resp = requests.postResultResponse(formBody)
            }
        }
        //too many results
        if (resp.body.contains("Your query returned too many results.", ignoreCase = true)) { throw QueryException("Query returned too many results.") }
        // parse
        return parse.parseCourses(resp.body)
    }
}
