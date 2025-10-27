package com.example.courseapi.repos

import com.example.courseapi.exceptions.QueryException
import com.example.courseapi.exceptions.TokenException
import com.example.courseapi.models.Course
import io.ktor.http.encodeURLParameter
import org.springframework.stereotype.Repository
import com.example.courseapi.services.RequestService
import com.example.courseapi.services.ParseService

@Repository
class CourseRepo(private val requests: RequestService, private val parse: ParseService){
    suspend fun getCourseByInfo(subject: List<String>, courseNum: Int?, campus: List<String>?, attributes: List<String>?, delivery: List<String>?, term: String?, crn: Int = 0): List<Course> {
        // get or reuse token (saves one GET on warm requests)
        val token = requests.getOrFetchToken()
        if (token.isEmpty()) throw TokenException("Empty Token")

        // build form
        val formParts = ArrayList<String>(24)
        formParts.add("_token=${token.encodeURLParameter()}")
        if (!delivery.isNullOrEmpty()) { delivery.forEach { formParts.add("sectionAttributes%5B%5D=${it.encodeURLParameter()}") } }
        if (!attributes.isNullOrEmpty()) { attributes.forEach{formParts.add("sectionFilterAttributes%5B%5D=${it.encodeURLParameter()}")} }
        if (!term.isNullOrEmpty()) { formParts.add("term=${term.encodeURLParameter()}") }
        if (!campus.isNullOrEmpty()) { campus.forEach { formParts.add("campusFilter%5B%5D=${it.encodeURLParameter()}") } } else { formParts.add("campusFilter%5B%5D=All") }
        subject.forEach { formParts.add("subject%5B%5D=${it.encodeURLParameter()}") }
        if (courseNum != null && courseNum > 0) { formParts.add("courseNumber=${courseNum}") } else { formParts.add("courseNumber=") }
        formParts.add("courseSearch=Find")
        formParts.add("openWaitlist=")
        if (crn != 0) { formParts.add("crnNumber==${crn}") } else { formParts.add("crnNumber=") }
        formParts.add("crnNumber=")
        formParts.add("level=")
        formParts.add("courseTitle=")
        formParts.add("instructor=")
        formParts.add("instructorUid=")
        formParts.add("creditHours=")
        formParts.add("startEndTime%5B%5D=")
        formParts.add("startEndTime%5B%5D=")
        val formBody = formParts.joinToString("&")

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
        if (resp.body.contains("Your query returned too many results.",ignoreCase = true)) { throw QueryException("Query returned too many results.") }
        // parse
        return parse.parseCourses(resp.body)
    }
}
