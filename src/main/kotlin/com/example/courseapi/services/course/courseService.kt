package com.example.courseapi.services.course

import com.example.courseapi.exceptions.APIException
import com.example.courseapi.exceptions.QueryException
import com.example.courseapi.models.course.Course
import com.example.courseapi.models.misc.Field
import com.example.courseapi.repos.course.CourseRepo
import org.springframework.stereotype.Service

@Service
class CourseService(private val repo: CourseRepo) {
    suspend fun getCourseByInfo(subject: List<String>? = null, courseNum: String? = null, campus: List<String>, attributes: List<String>? = null, delivery: List<String>? = null, term: String, openWaitlist: String? = null, crn: Int? = null, partOfTerm: List<String>? = null, level: String? = null, courseTitle: String? = null, daysFilter: List<String>? = null, creditHours: Int? = null, startEndTime: List<String>? = null): List<Course>{
        // Fetch and cache valid fields (also persists token via RequestService)
        val fields = repo.getOrFetchValidFields()

        if (campus.isEmpty() || !campus.all { it in fields.campuses }) throw IllegalArgumentException("Campuses empty or invalid")
        if (term.isEmpty() || term !in fields.terms) throw IllegalArgumentException("Term is empty or invalid")
        if (!(subject.isNullOrEmpty() || subject.all { it in fields.subjects })) throw IllegalArgumentException("Invalid subjects field")
        if (!courseNum.isNullOrEmpty()) {
            if (subject.isNullOrEmpty()) { throw IllegalArgumentException("Course num is specified without a subject") }
            if (subject.size > 1) { throw IllegalArgumentException("Course num inputted with too many subjects") }
        }
        if (!delivery.isNullOrEmpty() && !delivery.all { it in fields.deliveryTypes }) throw IllegalArgumentException("Delivery types empty or invalid")
        if (!startEndTime.isNullOrEmpty() && startEndTime.size != 2) throw IllegalArgumentException("StartEndTime empty or doesnt have size 2")
        if (!(openWaitlist.isNullOrEmpty() || (openWaitlist.isNotEmpty() && openWaitlist in fields.waitlistTypes))) throw IllegalArgumentException("Invalid openWaitlist field")
        if (!(level.isNullOrEmpty() || (level.isNotEmpty() && level in fields.levels))) throw IllegalArgumentException("Invalid level field")
        if (!(daysFilter.isNullOrEmpty() || (daysFilter.isNotEmpty() && daysFilter.all { it in fields.days }))) throw IllegalArgumentException("Invalid daysFilter field")

        val res = repo.getCourseByInfo(subject, courseNum, campus, attributes, delivery, term, openWaitlist, crn, partOfTerm, level, courseTitle, daysFilter, creditHours, startEndTime)
        return res.ifEmpty { throw QueryException("Desired course does not exist or no courses found") }
    }

    suspend fun getCourseByCRN(crn: Int? = 0, term: String = ""): List<Course> {
        return getCourseByInfo(crn = crn, term = term, campus = listOf("All"))
    }

    suspend fun getTerms(): List<Field>{
        val res = repo.getTerms()
        return res.ifEmpty{throw APIException("getTerms returning no terms") }
    }
}
