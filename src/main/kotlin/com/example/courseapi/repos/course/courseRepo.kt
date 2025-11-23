package com.example.courseapi.repos.course

import com.example.courseapi.exceptions.*
import com.example.courseapi.models.course.Course
import io.ktor.http.encodeURLParameter
import org.springframework.stereotype.Repository
import com.example.courseapi.services.course.ParseService
import com.example.courseapi.services.course.RequestService
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class ValidFields(
    val subjects: Set<String>,
    val campuses: Set<String>,
    val terms: Set<String>,
    val deliveryTypes: Set<String>,
    val levels: Set<String>,
    val days: Set<String>,
    val waitlistTypes: Set<String>,
    val attributes: Set<String>
)

@Repository
class CourseRepo(private val requests: RequestService, private val parse: ParseService){
    @Volatile private var cachedValidFields: ValidFields? = null
    @Volatile private var cacheTimestamp: Long = 0
    private val fieldsCacheLock = Mutex()
    private val fieldsCacheTimeout = 3_600_000L

    suspend fun getCourseByInfo(subject: List<String>? = null, courseNum: String? = null, campus: List<String>, attributes: List<String>? = null, delivery: List<String>? = null, term: String, openWaitlist: String? = null, crn: Int? = null, partOfTerm: List<String>? = null, level: String? = null, courseTitle: String? = null, daysFilter: List<String>? = null, creditHours: Int? = null, startEndTime: List<String>? = null
    ): List<Course> {
        val token = requests.getOrFetchToken()
        if (token.isEmpty()) throw APIException("Empty Token")
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
        var resp = requests.postResultResponse(formBody)
        if (resp.status == 419 ||
            resp.body.contains(
                "Page Expired",
                ignoreCase = true
            )) {
            val token = requests.getOrFetchToken()
            if (token.isNotEmpty()) {
                formParts[0] = "_token=${token.encodeURLParameter()}"
                val formBody = formParts.joinToString("&")
                resp = requests.postResultResponse(formBody)
            }
        }
        if (resp.body.contains(
                "Your query returned too many results.",
                ignoreCase = true
        )) { throw QueryException("Query returned too many results.") }
        return parse.parseCourses(resp.body)
    }

    suspend fun getOrFetchValidFields(): ValidFields = fieldsCacheLock.withLock {
        val now = System.currentTimeMillis()
        val cached = cachedValidFields
        if (cached != null && now - cacheTimestamp < fieldsCacheTimeout) return cached
        requests.getOrFetchToken()
        val html = requests.getTokenResponse()
        if (html.isEmpty()) throw APIException("Empty response when fetching valid fields")
        val allFields = parse.parseAllFields(html)
        val subjects = allFields["subjects"]?.map { it.name }?.toSet() ?: emptySet()
        val campuses = allFields["campuses"]?.map { it.name }?.toSet() ?: emptySet()
        val terms = allFields["terms"]?.map { it.name }?.toSet() ?: emptySet()
        val deliveryTypes = allFields["delivery"]?.map { it.name }?.toSet() ?: emptySet()
        val levels = allFields["levels"]?.map { it.name }?.toSet() ?: emptySet()
        val days = allFields["days"]?.map { it.name }?.toSet() ?: emptySet()
        val waitlistTypes = allFields["waitlist"]?.map { it.name }?.toSet() ?: emptySet()
        val attributes = allFields["attributes"]?.map { it.name }?.toSet() ?: emptySet()
        val fields = ValidFields(
            subjects = subjects,
            campuses = campuses,
            terms = terms,
            deliveryTypes = deliveryTypes,
            levels = levels,
            days = days,
            waitlistTypes = waitlistTypes,
            attributes = attributes
        )
        cachedValidFields = fields
        cacheTimestamp = now
        fields
    }
}
