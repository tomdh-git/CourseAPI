package com.example.courseapi.repos.utils.schedule

import com.example.courseapi.models.course.Course
import com.example.courseapi.models.dto.course.CourseByInfoInput
import com.example.courseapi.models.dto.schedule.FillerByAttributesInput
import com.example.courseapi.repos.course.CourseRepo

private val cache = AttributeCache()

suspend fun fetchAttributes(input: FillerByAttributesInput, course: CourseRepo): List<Course> {
    val cacheKey = AttributeCache.CacheKey(
        campus = input.campus,
        term = input.term,
        attributes = input.attributes.toSet(),
        delivery = input.delivery
    )

    return cache.get(cacheKey) ?: run {
        val startAndEndTimeValid = input.preferredStart != null && input.preferredEnd != null
        val startEndTime = if (startAndEndTimeValid) {
            listOf(input.preferredStart, input.preferredEnd)
        } else null

        val courses = course.getCourseByInfo(CourseByInfoInput(
            campus = input.campus,
            term = input.term,
            attributes = input.attributes,
            delivery = input.delivery,
            startEndTime = startEndTime
        ))

        cache.put(cacheKey, courses)
        courses
    }
}