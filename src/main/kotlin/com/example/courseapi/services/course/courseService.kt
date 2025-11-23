package com.example.courseapi.services.course

import com.example.courseapi.exceptions.*
import com.example.courseapi.models.course.Course
import com.example.courseapi.models.dto.course.*
import com.example.courseapi.repos.course.CourseRepo
import com.example.courseapi.services.course.utils.validate
import org.springframework.stereotype.Service

@Service
class CourseService(private val repo: CourseRepo) {
    suspend fun getCourseByInfo(input: CourseByInfoInput): List<Course>{
        val fields = repo.getOrFetchValidFields()
        validate(input,fields)
        val res = repo.getCourseByInfo(input.subject,input.courseNum,input.campus,input.attributes,input.delivery,
            input.term,input.openWaitlist,input.crn,input.partOfTerm,input.level,input.courseTitle,input.daysFilter,
            input.creditHours,input.startEndTime)
        return res.ifEmpty { throw QueryException("Desired course does not exist or no courses found") }
    }

    suspend fun getCourseByCRN(input: CourseByCRNInput): List<Course> =
        getCourseByInfo(CourseByInfoInput(crn = input.crn, term= input.term, campus = listOf("All")))
}
