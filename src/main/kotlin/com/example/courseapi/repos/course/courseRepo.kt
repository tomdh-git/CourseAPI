package com.example.courseapi.repos.course

import com.example.courseapi.exceptions.*
import com.example.courseapi.models.course.Course
import com.example.courseapi.models.dto.course.CourseByInfoInput
import com.example.courseapi.repos.utils.course.formRequest
import com.example.courseapi.services.utils.course.ParseUtils
import io.ktor.http.encodeURLParameter
import org.springframework.stereotype.Repository
import com.example.courseapi.services.utils.course.RequestUtils

@Repository
class CourseRepo(private val requests: RequestUtils,private val parse: ParseUtils){
    suspend fun getCourseByInfo(input: CourseByInfoInput): List<Course> {
        val token: String = requests.getOrFetchToken()
        if (token.isEmpty()) throw APIException("Empty Token")
        val formParts = ArrayList<String>(24)
        val formBody = formRequest(formParts, input, token).joinToString("&")
        var resp = requests.postResultResponse(formBody)
        val isExpired = resp.status == 419 || resp.body.contains("Page Expired", ignoreCase = true)
        if (isExpired) {
            val token = requests.getToken()
            if (token.isNotEmpty()) {
                formParts[0] = "_token=${token.encodeURLParameter()}"
                val formBody = formParts.joinToString("&")
                resp = requests.postResultResponse(formBody)
            }
        }
        val hasTooManyResults = resp.body.contains("Your query returned too many results.", ignoreCase = true)
        if (hasTooManyResults) { throw QueryException("Query returned too many results.") }
        return parse.parseCourses(resp.body)
    }
}
