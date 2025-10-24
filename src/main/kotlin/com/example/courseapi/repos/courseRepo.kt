package com.example.courseapi.repos

import com.example.courseapi.models.Course
import io.ktor.http.encodeURLParameter
import org.springframework.stereotype.Repository
import kotlin.system.measureTimeMillis

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
        // get or reuse token (saves one GET on warm requests)
        val totalStart = System.nanoTime()
        var token = ""
        val tokenMs = measureTimeMillis {
            token = getOrFetchToken()
            if (token.isEmpty()) return emptyList()
        }
        println("token: $tokenMs ms")

        // build form
        var formBody = ""
        var formParts = ArrayList<String>(24)
        val formMs = measureTimeMillis {
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
            formBody = formParts.joinToString("&")
        }
        println("form: $formMs ms")

        // send post request
        var resp: HttpTextResponse
        val postMs = measureTimeMillis {
            resp = postResultResponse(formBody)
            // If token expired (e.g., 419/Page Expired), refresh once and retry
            if (resp.status == 419 || resp.body.contains("Page Expired", ignoreCase = true)) {
                token = getOrFetchToken()
                if (token.isNotEmpty()) {
                    formParts[0] = "_token=${token.encodeURLParameter()}"
                    formBody = formParts.joinToString("&")
                    resp = postResultResponse(formBody)
                }
            }
        }
        println("post: $postMs ms")

        // parse
        var res: List<Course>
        val parseMs = measureTimeMillis {
            res = parseCourses(resp.body)
        }
        println("parse: $parseMs ms")
        val totalMs = (System.nanoTime() - totalStart) / 1_000_000
        println("total: ${totalMs} ms")
        return res
    }
}
