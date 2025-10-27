package com.example.courseapi.repos

import com.example.courseapi.exceptions.*
import com.example.courseapi.models.Course
import io.ktor.http.encodeURLParameter
import org.springframework.stereotype.Repository
import com.example.courseapi.services.*

@Repository
class CourseRepo(private val requests: RequestService, private val parse: ParseService){
    suspend fun getCourseByInfo(subject: List<String>, courseNum: Int? = 0, campus: List<String>, attributes: List<String>? = emptyList(), delivery: List<String>? = emptyList(), term: String, openWaitlist: String? = "", crn: Int? = 0, partOfTerm: List<String>? = emptyList(), level: String? = "", courseTitle: String? = "", daysFilter: List<String>? = emptyList(), creditHours: Int? = 0, startEndTime: List<String>? = emptyList()): List<Course> {
        // get or reuse token (saves one GET on warm requests)
        val token = requests.getOrFetchToken()
        if (token.isEmpty()) throw TokenException("Empty Token")

        // build form
        val formParts = ArrayList<String>(24)
        //mandatory
        formParts.add("_token=${token.encodeURLParameter()}")
        formParts.add("term=${term.encodeURLParameter()}")
        if (campus.isNotEmpty()) { campus.forEach { formParts.add("campusFilter%5B%5D=${it.encodeURLParameter()}") } }
        subject.forEach { formParts.add("subject%5B%5D=${it.encodeURLParameter()}") }
        if (courseNum != null && courseNum > 0) { formParts.add("courseNumber=${courseNum}") } else { formParts.add("courseNumber=") }
        if (!openWaitlist.isNullOrEmpty()) { formParts.add("openWaitlist=$openWaitlist") } else { formParts.add("openWaitlist=") }
        if (crn != null) formParts.add("crnNumber=${crn}") else formParts.add("crnNumber=")
        if (level != null) formParts.add("level=$level") else formParts.add("level=")
        if (courseTitle != null) formParts.add("courseTitle=$courseTitle") else formParts.add("courseTitle=")
        formParts.add("instructor=")
        formParts.add("instructorUid=")
        if (creditHours!=null) formParts.add("creditHours=$creditHours") else formParts.add("creditHours=")
        if (!startEndTime.isNullOrEmpty()) { startEndTime.forEach { formParts.add("startEndTime%5B%5D=${it.encodeURLParameter()}") } } else { formParts.add("startEndTime%5B%5D="); formParts.add("startEndTime%5B%5D=") }
        formParts.add("courseSearch=Find")
        //not mandatory
        if (!delivery.isNullOrEmpty()) { delivery.forEach { formParts.add("sectionAttributes%5B%5D=${it.encodeURLParameter()}") } } //could be empty
        if (!attributes.isNullOrEmpty()) { attributes.forEach{formParts.add("sectionFilterAttributes%5B%5D=${it.encodeURLParameter()}")} } //could be empty
        if (!partOfTerm.isNullOrEmpty()) { partOfTerm.forEach { formParts.add("partOfTerm%5B%5D=${it.encodeURLParameter()}") } }
        if (!daysFilter.isNullOrEmpty()) { daysFilter.forEach { formParts.add("daysFilter%5B%5D=${it.encodeURLParameter()}") } }
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
        if (resp.body.contains("Your query returned too many results.", ignoreCase = true)) { throw QueryException("Query returned too many results.") }
        // parse
        return parse.parseCourses(resp.body)
    }
}
