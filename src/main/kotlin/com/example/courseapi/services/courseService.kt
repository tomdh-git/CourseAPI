package com.example.courseapi.services

import com.example.courseapi.models.*
import com.example.courseapi.repos.CourseRepo
import org.springframework.stereotype.Service

@Service
class CourseService(private val repo: CourseRepo) {
    suspend fun getCourseByInfo(subject: List<String>? = null, courseNum: Int? = null, campus: List<String>, attributes: List<String>? = null, delivery: List<String>? = null, term: String, openWaitlist: String? = null, crn: Int? = null, partOfTerm: List<String>? = null, level: String? = null, courseTitle: String? = null, daysFilter: List<String>? = null, creditHours: Int? = null, startEndTime: List<String>? = null): List<Course>{
    //mandatory
        if (campus.isEmpty() || campus.none{it in ValidCampuses}) throw IllegalArgumentException("Campuses empty or invalid")
        if (term.isEmpty()) throw IllegalArgumentException("Term cannot be empty")
        //not mandatory
        if (!((subject.isNullOrEmpty()) || (subject.isNotEmpty() && subject.any{it in ValidSubjects}))) throw IllegalArgumentException("Invalid subjects field")
        if (!delivery.isNullOrEmpty() && delivery.none{it in ValidDeliveryTypes}) throw IllegalArgumentException("Delivery types empty or invalid")
        if (!startEndTime.isNullOrEmpty() && startEndTime.size != 2) throw IllegalArgumentException("StartEndTime empty or doesnt have size 2")
        if (!((openWaitlist.isNullOrEmpty()) || (openWaitlist.isNotEmpty() && openWaitlist in ValidWaitListTypes))) throw IllegalArgumentException("Invalid openWaitlist field")
        if (!((level.isNullOrEmpty()) || (level.isNotEmpty() && level in ValidLevels))) throw IllegalArgumentException("Invalid level field")
        if (!((daysFilter.isNullOrEmpty()) || (daysFilter.isNotEmpty() && daysFilter.any{it in ValidDays}))) throw IllegalArgumentException("Invalid daysFilter field")
        return repo.getCourseByInfo(subject, courseNum, campus, attributes, delivery, term, openWaitlist, crn, partOfTerm, level, courseTitle, daysFilter, creditHours, startEndTime)
    }

    suspend fun getCourseByCRN(crn: Int? = 0, term: String = ""): List<Course> {
        return getCourseByInfo(crn = crn, term = term, campus = listOf("All"))
    }

//    suspend fun getScheduleByCourses(courses: List<String>? = emptyList()): List<Schedule> {
//        return repo.getScheduleByInfo(courses)
//    }
}
