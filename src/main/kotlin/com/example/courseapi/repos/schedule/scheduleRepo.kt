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
    // Cache for attribute lists (campus/term/attributes/delivery -> courses)
    private data class AttributeCacheKey(
        val campus: List<String>,
        val term: String?,
        val attributes: Set<String>?,
        val delivery: List<String>?
    )
    
    private val attributeCache = java.util.concurrent.ConcurrentHashMap<AttributeCacheKey, Pair<Long, List<Course>>>()
    private val cacheTimeout = 5 * 60 * 1000L // 5 minutes
    
    suspend fun getScheduleByCourses(input: ScheduleByCourseInput): List<Schedule> {
        val fetched = fetchCourses(parseCourses(input.courses), input)
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
        val totalStart = System.nanoTime()
        
        // PARALLEL EXECUTION: Fetch attributes and generate schedules simultaneously
        val attributesDeferred = async {
            val start = System.nanoTime()
            
            // Check cache first
            val cacheKey = AttributeCacheKey(
                input.campus,
                input.term,
                input.attributes?.toSet(),
                input.delivery
            )
            val now = System.currentTimeMillis()
            val cached = attributeCache[cacheKey]
            
            val result = if (cached != null && now - cached.first < cacheTimeout) {
                println("[FILLER-PROFILE] Fetch attributes: CACHED (${cached.second.size} courses)")
                cached.second
            } else {
                val startEndTime = if (input.preferredStart != null && input.preferredEnd != null) {
                    listOf(input.preferredStart, input.preferredEnd)
                } else null
                
                val courses = course.getCourseByInfo(CourseByInfoInput(
                    campus = input.campus,
                    term = input.term,
                    attributes = input.attributes,
                    delivery = input.delivery,
                    startEndTime = startEndTime
                ))
                
                // Cache the result
                attributeCache[cacheKey] = now to courses
                
                println("[FILLER-PROFILE] Fetch attributes: ${(System.nanoTime() - start) / 1_000_000.0}ms (${courses.size} courses)")
                courses
            }
            result
        }
        
        val schedulesDeferred = async {
            val start = System.nanoTime()
            val result = getScheduleByCourses(input.toScheduleInput())
            println("[FILLER-PROFILE] Generate schedules: ${(System.nanoTime() - start) / 1_000_000.0}ms (${result.size} schedules)")
            result
        }
        
        // Await both results
        val attributesList = attributesDeferred.await()
        val schedules = schedulesDeferred.await()
        
        val startMin = toMinutes(input.preferredStart ?: "12:00am")
        val endMin = toMinutes(input.preferredEnd ?: "11:59pm")
        
        val mapStart = System.nanoTime()
        val result = schedules.map { s ->
            val existingIntervalsByDay = getExistingIntervalsByDay(s)
            val compatible = getCompatibleCourse(attributesList, startMin, endMin, existingIntervalsByDay)
            if (compatible.isEmpty()) return@map s
            val best = getBestFit(compatible, s, startMin, endMin)
            s.copy(courses = s.courses + best)
        }
        val mapTime = (System.nanoTime() - mapStart) / 1_000_000.0
        println("[FILLER-PROFILE] Map schedules: ${mapTime}ms")
        
        val totalTime = (System.nanoTime() - totalStart) / 1_000_000.0
        println("[FILLER-PROFILE] Total getFillerByAttributes: ${totalTime}ms")
        
        result
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

