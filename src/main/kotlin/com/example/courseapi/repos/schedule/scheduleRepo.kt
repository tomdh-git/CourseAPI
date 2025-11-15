package com.example.courseapi.repos.schedule

import com.example.courseapi.exceptions.QueryException
import com.example.courseapi.models.course.Course
import com.example.courseapi.models.schedule.Schedule
import com.example.courseapi.repos.course.CourseRepo
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Repository
import kotlin.collections.iterator
import kotlin.collections.zipWithNext

@Repository
class ScheduleRepo(private val course: CourseRepo){
    enum class Day { M, T, W, R, F, S, U }
    data class Interval(val day: Day, val start: Int, val end: Int)
    val timeSlotRegex = Regex("""([MTWRFSU]+)\s+(\d{1,2}:\d{2}[ap]m)-(\d{1,2}:\d{2}[ap]m)""", RegexOption.IGNORE_CASE)

    suspend fun getScheduleByCourses(courses: List<String>, campus: List<String>, term: String, optimizeFreeTime: Boolean? = false, preferredStart: String?, preferredEnd: String?): List<Schedule> = coroutineScope{
        val parsed = courses.mapNotNull { val p = it.trim().split(" "); if (p.size == 2) p[0] to p[1] else null }
        val fetched = parsed.map { (subject, num) ->
            async {
                val sections = course.getCourseByInfo(
                    subject = listOf(subject),
                    courseNum = num,
                    campus = campus,
                    term = term
                )
                subject to num to sections
            }
        }.awaitAll().groupBy(
            { it.first },
            { it.second }
        ).mapValues { it.value.flatten() }

        val valid = fetched.filterValues { it.isNotEmpty() }
        if (valid.isEmpty()) throw QueryException("No valid schedules found")
        val combos = cartesianProduct(valid.values.toList())
        if (combos.isEmpty()) throw QueryException("No schedule combos found")
        val startMin = toMinutes(preferredStart ?: "12:00am")
        val endMin = toMinutes(preferredEnd ?: "11:59pm")
        val validCombos = combos.asSequence().filter { combo -> !timeConflicts(combo.asSequence().map { it.delivery }, startMin, endMin) }.toList()
        if (validCombos.isEmpty()) throw QueryException("No valid schedule combos found")
        val schedules = validCombos.map { Schedule(it, freeTimeForSchedule(it)) }
        if (optimizeFreeTime == true) schedules.sortedByDescending { it.freeTime } else schedules
    }

    suspend fun getFillerByAttributes(attributes: List<String>, courses: List<String>, campus: List<String>, term: String, preferredStart: String? = null, preferredEnd: String? = null): List<Schedule>{
        val attributesList = course.getCourseByInfo(campus = campus, term = term, attributes = attributes)
        val schedules = getScheduleByCourses(courses, campus, term, true, preferredStart, preferredEnd)
        val result = mutableListOf<Schedule>()
        for (s in schedules) {
            val freeSlots = getFreeSlots(s)
            val fillers = attributesList.filter { filler ->
                val slot = parseTimeSlot(filler.delivery)
                slot.all { iv -> freeSlots[iv.day]?.any { (fs, fe) -> iv.start >= fs && iv.end <= fe } == true }
            }
            if (fillers.isNotEmpty()) { result.add(s.copy(courses = s.courses + fillers.first())) }
        }
        return result
    }

    fun toMinutes(t: String): Int {
        var i = 0
        val s = t.trim()
        var h = 0
        var m = 0

        // Parse hours
        while (i < s.length && s[i].isDigit()) {
            h = h * 10 + (s[i] - '0')
            i++
        }

        if (i >= s.length || s[i] != ':') return 0
        i++ // skip ':'

        // Parse minutes
        while (i < s.length && s[i].isDigit()) {
            m = m * 10 + (s[i] - '0')
            i++
        }

        // Parse am/pm - avoid drop() and multiple allocations
        var isPm = false
        if (i < s.length - 1) {
            val ch1 = s[i]
            val ch2 = if (i + 1 < s.length) s[i + 1] else ' '
            isPm = (ch1 == 'p' || ch1 == 'P') && (ch2 == 'm' || ch2 == 'M')
        }

        if (h == 12) h = 0
        if (isPm) h += 12

        return h * 60 + m
    }

    fun charToDay(c: Char) = when (c) {
        'M' -> Day.M; 'T' -> Day.T; 'W' -> Day.W; 'R' -> Day.R
        'F' -> Day.F; 'S' -> Day.S; 'U' -> Day.U
        else -> null
    }

    fun parseTimeSlot(slot: String): Sequence<Interval> = sequence {
        val matches = timeSlotRegex.findAll(slot)
        for (m in matches) {
            val groupValues = m.groupValues
            val daysStr = groupValues[1]
            val start = toMinutes(groupValues[2])
            val end = toMinutes(groupValues[3])
            for (dayChar in daysStr) {
                val day = charToDay(dayChar) ?: continue
                yield(Interval(day, start, end))
            }
        }
    }

    fun timeConflicts(times: Sequence<String>, preferredStartMin: Int, preferredEndMin: Int): Boolean {
        val intervalsByDay = arrayOfNulls<MutableList<Interval>?>(7) // Fixed size array, faster than map
        for (slot in times) {
            for (iv in parseTimeSlot(slot)) {
                if (iv.start < preferredStartMin || iv.end > preferredEndMin) return true
                val dayIndex = iv.day.ordinal
                if (intervalsByDay[dayIndex] == null) intervalsByDay[dayIndex] = mutableListOf()
                intervalsByDay[dayIndex]!!.add(iv)
            }
        }
        // Check for overlaps
        for (dayIntervals in intervalsByDay) {
            if (dayIntervals == null) continue
            dayIntervals.sortBy { it.start }
            for (i in 0 until dayIntervals.size - 1) {
                if (dayIntervals[i + 1].start < dayIntervals[i].end) return true
            }
        }
        return false
    }

    fun freeTimeForSchedule(courses: List<Course>): Int {
        val map = arrayOfNulls<MutableList<Pair<Int, Int>>?>(7) // Fixed size array for O(1) access
        for (c in courses) {
            val d = c.delivery
            for (iv in parseTimeSlot(d)) {
                val dayIndex = iv.day.ordinal
                if (map[dayIndex] == null) map[dayIndex] = mutableListOf()
                map[dayIndex]!!.add(iv.start to iv.end)
            }
        }
        var total = 0
        for (dayIndex in 0 until 7) {
            val intervals = map[dayIndex]
            if (intervals != null && intervals.isNotEmpty()) {
                intervals.sortBy { it.first }
                var last = 7 * 60
                for ((start, end) in intervals) {
                    if (start > last) total += start - last
                    last = maxOf(last, end)
                }
                total += (23 * 60) - last
            } else {
                total += (23 * 60) - (7 * 60) // Full day free if no classes
            }
        }
        return total
    }

    fun getFreeSlots(schedule: Schedule): Map<Day, List<Pair<Int, Int>>> {
        val occupied = arrayOfNulls<MutableList<Pair<Int, Int>>?>(7)
        for (c in schedule.courses) {
            for (iv in parseTimeSlot(c.delivery)) {
                val dayIndex = iv.day.ordinal
                if (occupied[dayIndex] == null) occupied[dayIndex] = mutableListOf()
                occupied[dayIndex]!!.add(iv.start to iv.end)
            }
        }

        val free = mutableMapOf<Day, List<Pair<Int, Int>>>()
        for (dayIndex in 0 until 7) {
            val blocks = occupied[dayIndex]
            if (blocks != null && blocks.isNotEmpty()) {
                blocks.sortBy { it.first }
                var prevEnd = 0
                val dailyFree = mutableListOf<Pair<Int, Int>>()
                for ((start, end) in blocks) {
                    if (start > prevEnd) dailyFree.add(prevEnd to start)
                    prevEnd = end
                }
                if (prevEnd < 1440) dailyFree.add(prevEnd to 1440)
                free[Day.entries[dayIndex]] = dailyFree
            } else {
                free[Day.entries[dayIndex]] = listOf(0 to 1440)
            }
        }
        return free
    }

    // Optimized Cartesian product - avoids creating intermediate lists with '+' operator
    fun <T> cartesianProduct(lists: List<List<T>>): List<List<T>> {
        if (lists.isEmpty() || lists.any { it.isEmpty() }) return emptyList()
        // Calculate total result size upfront to pre-allocate
        var size = 1
        for (list in lists) {
            size *= list.size
            if (size > 100000) return emptyList() // Safeguard against memory explosion
        }
        
        val result = ArrayList<List<T>>(size)
        val indices = IntArray(lists.size)
        
        while (true) {
            val combo = ArrayList<T>(lists.size)
            for (i in lists.indices) {
                combo.add(lists[i][indices[i]])
            }
            result.add(combo)
            
            // Increment indices like an odometer
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
}

