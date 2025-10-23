package com.example.courseapi.services

import com.example.courseapi.models.Course
import com.example.courseapi.repos.CourseRepo
import org.springframework.stereotype.Service

@Service
class CourseService(private val repo: CourseRepo) {
    fun getCourses(
        subject: String?,
        courseNum: Int?,
        title: String?,
        section: String?,
        crn: Int?,
        campus: String?,
        credits: Int?,
        capacity: String?,
        requests: String?,
        delivery: String?
    ): List<Course> {
        return repo.getCourses(
            subject,
            courseNum,
            title,
            section,
            crn,
            campus,
            credits,
            capacity,
            requests,
            delivery
        )
    }
}
