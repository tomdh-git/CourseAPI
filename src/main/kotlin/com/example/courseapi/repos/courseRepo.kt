package com.example.courseapi.repos

import com.example.courseapi.exceptions.*
import com.example.courseapi.models.*
import io.ktor.http.encodeURLParameter
import org.springframework.stereotype.Repository
import com.example.courseapi.services.*

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

    suspend fun getScheduleByCourses(courses: List<String>, campus: List<String>, term: String, optimizeFreeTime: Boolean? = false, preferredStart: String?, preferredEnd: String?): List<Schedule> {
        val fetched = mutableMapOf<Pair<String, String>, List<Course>>()
        for ((subject, num) in courses.mapNotNull { val p = it.trim().split(" "); if (p.size == 2) p[0] to p[1] else null }) { val sections = getCourseByInfo(subject = listOf(subject), courseNum = num, campus = campus, term = term); fetched[subject to num] = sections }
        val valid = fetched.filterValues { it.isNotEmpty() }
        if (valid.isEmpty()) throw QueryException("No valid schedules found")
        val combos = parse.cartesianProduct(valid.values.toList())
        if (combos.isEmpty()) throw QueryException("No schedule combos found")
        val validCombos = combos.filter { combo -> !parse.timeConflicts(combo.map { it.delivery }, parse.toMinutes(preferredStart ?: "12:00am"), parse.toMinutes(preferredEnd ?: "11:59pm")) }
        if (validCombos.isEmpty()) throw QueryException("No valid schedule combos found")
        val schedules = validCombos.map { Schedule(it, parse.freeTimeForSchedule(it)) }
        val result = if (optimizeFreeTime == true) schedules.sortedByDescending { it.freeTime } else schedules
        return result.take(10)
    }
}
