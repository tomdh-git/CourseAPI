package com.example.courseapi.resolvers.utils.course

import com.example.courseapi.models.course.*
import com.example.courseapi.resolvers.utils.common.safeExecute

suspend fun courseSafe(action: suspend () -> List<Course>): CourseResult =
    safeExecute(
        action,
        { SuccessCourse(it) },
        { code, msg -> ErrorCourse(code, msg) }
    )