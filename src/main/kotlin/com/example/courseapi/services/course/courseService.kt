package com.example.courseapi.services.course

import com.example.courseapi.exceptions.*
import com.example.courseapi.models.course.Course
import com.example.courseapi.models.dto.course.*
import com.example.courseapi.repos.course.CourseRepo
import com.example.courseapi.repos.field.FieldRepo
import com.example.courseapi.services.utils.course.validateCourseFields
import org.springframework.stereotype.Service

@Service
class CourseService(private val field: FieldRepo, private val repo: CourseRepo) {
    suspend fun getCourseByInfo(input: CourseByInfoInput): List<Course>{
        validateCourseFields(input,field.getOrFetchValidFields())
        val res = repo.getCourseByInfo(input)
        return res.ifEmpty { throw QueryException("Desired course does not exist or no courses found") }
    }

    suspend fun getCourseByCRN(input: CourseByCRNInput): List<Course> =
        getCourseByInfo(CourseByInfoInput(crn = input.crn, term= input.term, campus = listOf("All")))
}
