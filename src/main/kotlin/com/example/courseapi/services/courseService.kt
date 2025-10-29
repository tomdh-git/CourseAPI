package com.example.courseapi.services

import com.example.courseapi.exceptions.QueryException
import com.example.courseapi.models.course.Course
import com.example.courseapi.models.input.ValidCampuses
import com.example.courseapi.models.input.ValidDays
import com.example.courseapi.models.input.ValidDeliveryTypes
import com.example.courseapi.models.input.ValidLevels
import com.example.courseapi.models.input.ValidSubjects
import com.example.courseapi.models.input.ValidWaitListTypes
import com.example.courseapi.models.schedule.Schedule
import com.example.courseapi.repos.CourseRepo
import org.springframework.stereotype.Service

@Service
class CourseService(private val repo: CourseRepo, private val parseService: ParseService) {
    suspend fun getCourseByInfo(subject: List<String>? = null, courseNum: String? = null, campus: List<String>, attributes: List<String>? = null, delivery: List<String>? = null, term: String, openWaitlist: String? = null, crn: Int? = null, partOfTerm: List<String>? = null, level: String? = null, courseTitle: String? = null, daysFilter: List<String>? = null, creditHours: Int? = null, startEndTime: List<String>? = null): List<Course>{
        if (campus.isEmpty() || !campus.all{it in ValidCampuses }) throw IllegalArgumentException("Campuses empty or invalid")
        if (term.isEmpty()) throw IllegalArgumentException("Term cannot be empty")
        if (!((subject.isNullOrEmpty()) || (subject.isNotEmpty() && subject.all{it in ValidSubjects }))) throw IllegalArgumentException("Invalid subjects field")
        if (!delivery.isNullOrEmpty() && !delivery.all{it in ValidDeliveryTypes }) throw IllegalArgumentException("Delivery types empty or invalid")
        if (!startEndTime.isNullOrEmpty() && startEndTime.size != 2) throw IllegalArgumentException("StartEndTime empty or doesnt have size 2")
        if (!((openWaitlist.isNullOrEmpty()) || (openWaitlist.isNotEmpty() && openWaitlist in ValidWaitListTypes))) throw IllegalArgumentException("Invalid openWaitlist field")
        if (!((level.isNullOrEmpty()) || (level.isNotEmpty() && level in ValidLevels))) throw IllegalArgumentException("Invalid level field")
        if (!((daysFilter.isNullOrEmpty()) || (daysFilter.isNotEmpty() && daysFilter.all{it in ValidDays }))) throw IllegalArgumentException("Invalid daysFilter field")
        val res = repo.getCourseByInfo(subject, courseNum, campus, attributes, delivery, term, openWaitlist, crn, partOfTerm, level, courseTitle, daysFilter, creditHours, startEndTime)
        return res.ifEmpty { throw QueryException("Desired course does not exist or no courses found") }
    }

    suspend fun getCourseByCRN(crn: Int? = 0, term: String = ""): List<Course> {
        return getCourseByInfo(crn = crn, term = term, campus = listOf("All"))
    }

    suspend fun getScheduleByCourses(courses: List<String>, campus: List<String>, term: String, optimizeFreeTime: Boolean? = false, preferredStart: String?, preferredEnd: String?): List<Schedule> {
        if (campus.isEmpty() || !campus.all{it in ValidCampuses }) throw IllegalArgumentException("Campuses empty or invalid")
        if (term.isEmpty()) throw IllegalArgumentException("Term cannot be empty")
        if ((!preferredStart.isNullOrEmpty()&&preferredEnd.isNullOrEmpty())||(!preferredEnd.isNullOrEmpty() && preferredStart.isNullOrEmpty())) throw IllegalArgumentException("Preferred start and end fields must be specified together")
        if (parseService.toMinutes(preferredStart?:"12:01am")>=parseService.toMinutes(preferredEnd?:"12:00am")) throw IllegalArgumentException("Preferred start must be before preferred end")
        return repo.getScheduleByCourses(courses, campus, term, optimizeFreeTime, preferredStart, preferredEnd)
    }

    suspend fun getFillerByAttributes(attributes: List<String>, schedule: Schedule, campus: List<String>, term: String, preferredStart: String? = null, preferredEnd: String? = null): List<Schedule>{
        return emptyList()
    }
}
