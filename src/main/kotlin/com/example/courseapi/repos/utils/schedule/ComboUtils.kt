package com.example.courseapi.repos.utils.schedule

import com.example.courseapi.models.course.Course
import com.example.courseapi.models.schedule.Schedule

fun generateValidSchedules(
    courseGroups: List<List<Course>>, 
    preferredStartMin: Int, 
    preferredEndMin: Int,
    optimizeFreeTime: Boolean = false,
    maxResults: Int = 100
): List<Schedule> {
    if (courseGroups.isEmpty()) return emptyList()
    
    val processedGroups = courseGroups.map { group ->
        group.mapNotNull { course ->
            val intervals = parseTimeSlot(course.delivery).toList()
            if (intervals.isEmpty()) {
                course to emptyList()
            } else if (intervals.any { it.start < preferredStartMin || it.end > preferredEndMin }) {
                null
            } else {
                course to intervals
            }
        }
    }

    if (processedGroups.any { it.isEmpty() }) return emptyList()

    val sortedGroups = processedGroups.sortedBy { it.size }

    val results = if (optimizeFreeTime) {
        java.util.PriorityQueue<Schedule>(maxResults, compareBy { it.freeTime })
    } else {
        null
    }
    val listResults = if (!optimizeFreeTime) ArrayList<Schedule>() else null
    
    val currentCourses = ArrayList<Course>(courseGroups.size)
    val currentIntervals = ArrayList<Interval>()
    
    backtrack(0, currentCourses, currentIntervals, sortedGroups, results, listResults, maxResults, optimizeFreeTime)
    
    val finalResults = if (optimizeFreeTime) {
        results!!.sortedByDescending { it.freeTime }
    } else {
        listResults!!
    }
    
    return finalResults
}

private fun backtrack(
    index: Int,
    currentCourses: MutableList<Course>,
    currentIntervals: MutableList<Interval>,
    groups: List<List<Pair<Course, List<Interval>>>>,
    priorityQueue: java.util.PriorityQueue<Schedule>?,
    listResults: MutableList<Schedule>?,
    maxResults: Int,
    optimizeFreeTime: Boolean
) {
    if (index == groups.size) {
        val schedule = if (optimizeFreeTime) {
            Schedule(ArrayList(currentCourses), freeTimeForSchedule(currentCourses))
        } else {
            Schedule(ArrayList(currentCourses), 0)
        }
        
        if (optimizeFreeTime) {
            if (priorityQueue!!.size < maxResults) {
                priorityQueue.add(schedule)
            } else if (schedule.freeTime > priorityQueue.peek()!!.freeTime) {
                priorityQueue.poll()
                priorityQueue.add(schedule)
            }
        } else {
            if (listResults!!.size < maxResults) {
                listResults.add(schedule)
            }
        }
        return
    }

    val currentResultCount = if (optimizeFreeTime) priorityQueue!!.size else listResults!!.size
    if (currentResultCount >= maxResults && !optimizeFreeTime) return

    val group = groups[index]
    for ((course, intervals) in group) {
        var hasConflict = false
        for (newIv in intervals) {
            for (exIv in currentIntervals) {
                if (newIv.day == exIv.day && newIv.start < exIv.end && newIv.end > exIv.start) {
                    hasConflict = true
                    break
                }
            }
            if (hasConflict) break
        }
        
        if (!hasConflict) {
            currentCourses.add(course)
            val addedCount = intervals.size
            currentIntervals.addAll(intervals)
            
            backtrack(index + 1, currentCourses, currentIntervals, groups, priorityQueue, listResults, maxResults, optimizeFreeTime)
            
            currentCourses.removeAt(currentCourses.lastIndex)
            repeat(addedCount) { currentIntervals.removeAt(currentIntervals.lastIndex) }
        }
    }
}

fun getCompatibleCourse(attributesList: List<Course>,startMin: Int, endMin: Int, existingIntervalsByDay: MutableMap<Day, MutableList<Interval>>): List<Course>{
    return attributesList.filter { filler ->
        val ivs = parseTimeSlot(filler.delivery).toList()
        if (ivs.isEmpty()) return@filter true
        ivs.all { iv ->
            val inWindow = iv.start >= startMin && iv.end <= endMin
            if (!inWindow) return@all false
            val existing = existingIntervalsByDay[iv.day] ?: emptyList()
            val conflict = existing.any {
                    e -> iv.start < e.end && iv.end > e.start
            }
            if (conflict) return@all false
            true
        }
    }
}

fun getExistingIntervalsByDay(s: Schedule): MutableMap<Day, MutableList<Interval>>{
    val existingIntervalsByDay = mutableMapOf<Day, MutableList<Interval>>()
    for (c in s.courses) {
        for (iv in parseTimeSlot(c.delivery)) {
            existingIntervalsByDay.computeIfAbsent(iv.day) { mutableListOf() }.add(iv)
        }
    }
    return existingIntervalsByDay
}

fun getBestFit(compatible: List<Course>, s: Schedule, startMin: Int, endMin: Int): Course{
    return compatible.maxByOrNull { filler ->
        val newCourses = s.courses + filler
        val newSchedule = Schedule(newCourses, freeTimeForSchedule(newCourses))
        val newFree = getFreeSlots(newSchedule, startMin, endMin)
        newFree.values.sumOf {
                slots -> slots.sumOf { it.second - it.first }
        }
    }!!
}