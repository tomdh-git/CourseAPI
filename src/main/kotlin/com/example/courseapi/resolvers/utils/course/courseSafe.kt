package com.example.courseapi.resolvers.utils.course

import com.example.courseapi.models.course.Course
import com.example.courseapi.models.course.CourseResult
import com.example.courseapi.models.course.ErrorCourse
import com.example.courseapi.models.course.SuccessCourse
import com.example.courseapi.resolvers.utils.common.safeExecute

suspend fun courseSafe(action: suspend () -> List<Course>): CourseResult =
    safeExecute(
        action,
        { SuccessCourse(it) },
        { code, msg -> ErrorCourse(code, msg) }
    )