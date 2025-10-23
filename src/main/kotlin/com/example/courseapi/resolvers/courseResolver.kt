package com.example.courseapi.resolvers

import com.example.courseapi.models.Course
import com.example.courseapi.services.CourseService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class CourseResolver(private val service: CourseService) {
    @QueryMapping
    suspend fun getCourses(
        @Argument subject: List<String>?,
        @Argument courseNum: Int?,
        @Argument campus: List<String>?,
        @Argument attributes: List<String>?,
        @Argument delivery: List<String>?,
        @Argument term: String?,
    ): List<Course> {
        return service.getCourses(
            subject, courseNum,
            campus, attributes,
            delivery, term
        )
    }
}
