package com.example.courseapi.resolvers

import com.example.courseapi.models.course.CourseResult
import com.example.courseapi.models.dto.course.*
import com.example.courseapi.resolvers.utils.course.courseSafe
import com.example.courseapi.services.course.CourseService
import org.springframework.graphql.data.method.annotation.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin

@Controller
@CrossOrigin(origins = ["*"])
class CourseResolver(private val service: CourseService) {
    @QueryMapping
    suspend fun getCourseByInfo(@Argument input: CourseByInfoInput): CourseResult =
        courseSafe { service.getCourseByInfo(input) }

    @QueryMapping
    suspend fun getCourseByCRN(@Argument input: CourseByCRNInput): CourseResult =
        courseSafe { service.getCourseByCRN(input) }
}

