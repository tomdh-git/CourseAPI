package com.example.courseapi.resolvers

import com.example.courseapi.models.course.*
import com.example.courseapi.resolvers.utils.course.courseSafe
import com.example.courseapi.services.course.CourseService
import org.springframework.graphql.data.method.annotation.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin

@Controller
@CrossOrigin(origins = ["*"])
class CourseResolver(private val cs: CourseService) {
    @QueryMapping
    suspend fun getCourseByInfo(
        @Argument subject: List<String>?, @Argument courseNum: String?, @Argument campus: List<String>, @Argument attributes: List<String>?, @Argument delivery: List<String>?, @Argument term: String, @Argument openWaitlist: String?, @Argument crn: Int?, @Argument partOfTerm: List<String>?, @Argument level: String?, @Argument courseTitle: String?, @Argument daysFilter: List<String>?, @Argument creditHours: Int?, @Argument startEndTime: List<String>?
    ): CourseResult = courseSafe { cs.getCourseByInfo(subject, courseNum, campus, attributes, delivery, term, openWaitlist, crn, partOfTerm, level, courseTitle, daysFilter, creditHours, startEndTime) }

    @QueryMapping
    suspend fun getCourseByCRN(@Argument crn: Int?, @Argument term: String
    ): CourseResult = courseSafe { cs.getCourseByCRN(crn, term) }
}

