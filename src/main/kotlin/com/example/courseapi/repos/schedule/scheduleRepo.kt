package com.example.courseapi.repos.schedule

import com.example.courseapi.exceptions.QueryException
import com.example.courseapi.models.dto.schedule.*
import com.example.courseapi.models.schedule.Schedule
import com.example.courseapi.repos.course.CourseRepo
import com.example.courseapi.repos.utils.schedule.*
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Repository

@Repository
class ScheduleRepo(private val course: CourseRepo) {
    suspend fun getScheduleByCourses(input: ScheduleByCourseInput): List<Schedule> {
        val fetched = fetchCourses(parseCourses(input.courses), input,course)
        val valid = fetched.filterValues { it.isNotEmpty() }
        if (valid.isEmpty()) throw QueryException("No valid schedules found")
        
        val startMin = toMinutes(input.preferredStart ?: "12:00am")
        val endMin = toMinutes(input.preferredEnd ?: "11:59pm")
        
        val schedules = generateValidSchedules(
            valid.values.toList(), 
            startMin, 
            endMin,
            optimizeFreeTime = input.optimizeFreeTime == true,
            maxResults = 100
        )
        if (schedules.isEmpty()) throw QueryException("No valid schedule combos found")
        return schedules
    }

    suspend fun getFillerByAttributes(input: FillerByAttributesInput): List<Schedule> = coroutineScope {
        val (attributesList, schedules) = fetchAttributesAndSchedules(input)
        val startMin = toMinutes(input.preferredStart ?: "12:00am")
        val endMin = toMinutes(input.preferredEnd ?: "11:59pm")
        
        schedules.map { schedule ->
            addFillerCourse(schedule, attributesList, startMin, endMin)
        }
    }

    private suspend fun fetchAttributesAndSchedules(input: FillerByAttributesInput) = coroutineScope {
        val attributesDeferred = async { fetchAttributes(input,course) }
        val schedulesDeferred = async { getScheduleByCourses(input.toScheduleInput()) }

        attributesDeferred.await() to schedulesDeferred.await()
    }
}

