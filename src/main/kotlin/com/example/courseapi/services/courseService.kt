package com.example.courseapi.services

import com.example.courseapi.models.*
import com.example.courseapi.repos.CourseRepo
import org.springframework.stereotype.Service

@Service
class CourseService(private val repo: CourseRepo) {
    suspend fun getCourseByInfo(subject: List<String>? = emptyList(), courseNum: Int? = 0, campus: List<String>? = emptyList(), attributes: List<String>? = emptyList(), delivery: List<String>? = emptyList(), term: String? = "", openWaitlist: String? = "", crn: Int? = 0, partOfTerm: List<String>? = emptyList(), level: String? = "", courseTitle: String? = "", daysFilter: List<String>? = emptyList(), creditHours: Int? = 0, startEndTime: List<String>? = emptyList()): List<Course> {
        //mandatory
        if (campus.isNullOrEmpty() || campus.none{it in ValidCampuses}) throw IllegalArgumentException("Campuses empty or invalid")
        if (term.isNullOrEmpty()) throw IllegalArgumentException("Term cannot be empty")
        //not mandatory
        if (!((subject.isNullOrEmpty()) || (subject.isNotEmpty() && subject.any{it in ValidSubjects}))) throw IllegalArgumentException("Invalid subjects field")
        if (!delivery.isNullOrEmpty() && delivery.none{it in ValidDeliveryTypes}) throw IllegalArgumentException("Delivery types empty or invalid")
        if (!startEndTime.isNullOrEmpty() && startEndTime.size != 2) throw IllegalArgumentException("StartEndTime empty or doesnt have size 2")
        if (!((openWaitlist.isNullOrEmpty()) || (openWaitlist.isNotEmpty() && openWaitlist in ValidWaitListTypes))) throw IllegalArgumentException("Invalid openWaitlist field")
        if (!((level.isNullOrEmpty()) || (level.isNotEmpty() && level in ValidLevels))) throw IllegalArgumentException("Invalid level field")
        if (!((daysFilter.isNullOrEmpty()) || (daysFilter.isNotEmpty() && daysFilter.any{it in ValidDays}))) throw IllegalArgumentException("Invalid daysFilter field")
        return repo.getCourseByInfo(subject, courseNum, campus, attributes, delivery, term, openWaitlist, crn, partOfTerm, level, courseTitle, daysFilter, creditHours, startEndTime)
    }

    suspend fun getCourseByCRN(crn: Int? = 0, term: String? = ""): List<Course> {
        return getCourseByInfo(crn = crn, term = term, campus = listOf("All"))
    }
}
