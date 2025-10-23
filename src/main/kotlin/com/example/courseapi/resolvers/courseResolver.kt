package com.example.courseapi.resolvers

import com.example.courseapi.models.Course
import com.example.courseapi.services.CourseService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class CourseResolver(private val service: CourseService) {
    @QueryMapping
    fun getCourses(
        @Argument subject: String?,
        @Argument courseNum: Int?,
        @Argument title: String?,
        @Argument section: String?,
        @Argument crn: Int?,
        @Argument campus: String?,
        @Argument credits: Int?,
        @Argument capacity: String?,
        @Argument requests: String?,
        @Argument delivery: String?
    ): List<Course> {
        return service.getCourses(
            subject, courseNum, title, section, crn,
            campus, credits, capacity, requests, delivery
        )
    }
}
