package com.example.courseapi.repos.schedule

import com.example.courseapi.exceptions.QueryException
import com.example.courseapi.models.course.Course
import com.example.courseapi.models.dto.course.CourseByInfoInput
import com.example.courseapi.models.dto.schedule.*
import com.example.courseapi.models.schedule.Schedule
import com.example.courseapi.repos.course.CourseRepo
import com.example.courseapi.repos.utils.schedule.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Repository
import kotlin.collections.plus

@Repository
class ScheduleRepo(private val course: CourseRepo){
    suspend fun getScheduleByCourses(input: ScheduleByCourseInput): List<Schedule>{
        val fetched = fetchCourses(parseCourses(input.courses), input)
        val valid = fetched.filterValues { it.isNotEmpty() }
        if (valid.isEmpty()) throw QueryException("No valid schedules found")
        val combos = cartesianProduct(valid.values.toList())
        if (combos.isEmpty()) throw QueryException("No schedule combos found")
        val validCombos = getValidCombos(combos, input)
        if (validCombos.isEmpty()) throw QueryException("No valid schedule combos found")
        val schedules = getSchedules(validCombos)
        return if (input.optimizeFreeTime == true) schedules.sortedByDescending { it.freeTime }
        else schedules
    }

    suspend fun getFillerByAttributes(input: FillerByAttributesInput): List<Schedule> {
        val attributesList = course.getCourseByInfo(CourseByInfoInput(campus = input.campus, term = input.term, attributes = input.attributes, delivery = input.delivery))
        val schedules = getScheduleByCourses(input.toScheduleInput())
        val startMin = toMinutes(input.preferredStart ?: "12:00am")
        val endMin = toMinutes(input.preferredEnd ?: "11:59pm")
        return schedules.map { s ->
            val existingIntervalsByDay = getExistingIntervalsByDay(s)
            val compatible = getCompatibleCourse(attributesList, startMin, endMin, existingIntervalsByDay)
            if (compatible.isEmpty()) return@map s
            val best = getBestFit(compatible, s, startMin, endMin)
            s.copy(courses = s.courses + best)
        }
    }

    private suspend fun fetchCourses(parsed: List<Pair<String,String>>, input: ScheduleByCourseInput): Map<Pair<String, String>, List<Course>> = coroutineScope{
        return@coroutineScope parsed.map { (subject, num) ->
            async {
                val sections = course.getCourseByInfo(
                    CourseByInfoInput(delivery = input.delivery,subject = listOf(subject), courseNum = num, campus = input.campus, term = input.term)
                )
                subject to num to sections
            }
        }.awaitAll().groupBy(
            { it.first },
            { it.second }
        ).mapValues { it.value.flatten() }
    }
}

