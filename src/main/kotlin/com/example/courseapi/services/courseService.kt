package com.example.courseapi.services

import com.example.courseapi.models.Course
import com.example.courseapi.models.ValidSubjects
import com.example.courseapi.repos.CourseRepo
import org.springframework.stereotype.Service

@Service
class CourseService(private val repo: CourseRepo) {
    suspend fun getCourseByInfo(subject: List<String>?, courseNum: Int?, campus: List<String>?, attributes: List<String>?, delivery: List<String>?, term: String?): List<Course> {
        if (subject.isNullOrEmpty() || subject.none{it in ValidSubjects}) throw IllegalArgumentException("Subjects empty or invalid")
//        if (!campus.isNullOrEmpty() || campus?.none{it in ValidSubjects} ?: false) throw IllegalArgumentException("Subjects empty or invalid")
        return repo.getCourseByInfo(subject, courseNum, campus, attributes, delivery, term)
    }
}
