package com.example.courseapi.services

import com.example.courseapi.models.Course
import com.example.courseapi.repos.CourseRepo
import org.springframework.stereotype.Service

@Service
class CourseService(private val repo: CourseRepo) {
    suspend fun getCourses(subject: List<String>?, courseNum: Int?, campus: List<String>?, attributes: List<String>?, delivery: List<String>?, term: String?): List<Course> {
        return repo.getCourses(subject, courseNum, campus, attributes, delivery, term)
    }
}
