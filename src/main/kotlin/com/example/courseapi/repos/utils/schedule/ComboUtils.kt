package com.example.courseapi.repos.utils.schedule

import com.example.courseapi.models.course.Course
import com.example.courseapi.models.dto.schedule.ScheduleByCourseInput
import com.example.courseapi.models.schedule.Schedule

fun <T> cartesianProduct(lists: List<List<T>>): List<List<T>> {
    if (lists.isEmpty() || lists.any { it.isEmpty() }) return emptyList()
    var size = 1
    for (list in lists) {
        size *= list.size
        if (size > 100000) return emptyList()
    }
    val result = ArrayList<List<T>>(size)
    val indices = IntArray(lists.size)
    while (true) {
        val combo = ArrayList<T>(lists.size)
        for (i in lists.indices) {
            combo.add(lists[i][indices[i]])
        }
        result.add(combo)
        var pos = lists.size - 1
        while (pos >= 0) {
            indices[pos]++
            if (indices[pos] < lists[pos].size) break
            indices[pos] = 0
            pos--
        }
        if (pos < 0) break
    }
    return result
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

fun getValidCombos(combos: List<List<Course>>, input: ScheduleByCourseInput):List<List<Course>>{
    return combos.asSequence()
        .filter {
                combo ->
            !timeConflicts(
                combo.asSequence().map { it.delivery },
                toMinutes(input.preferredStart ?: "12:00am"),
                toMinutes(input.preferredEnd ?: "11:59pm"))
        }.toList()
}

fun getSchedules(validCombos: List<List<Course>>): List<Schedule>{
    return validCombos.map {
        Schedule(
            it,
            freeTimeForSchedule(it)
        ) }
}