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
    suspend fun getScheduleByCourses(courses: List<String>, campus: List<String>, term: String, optimizeFreeTime: Boolean? = false, preferredStart: String?, preferredEnd: String?): List<Schedule> = coroutineScope{
        val parsed = courses.mapNotNull {
            val p = it.trim().split(" ")
            if (p.size == 2) p[0] to p[1] else null
        }

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

        val validCombos = combos.asSequence().filter { combo ->
            // Pass a Sequence of deliveries, no asIterable or intermediate list
            !timeConflicts(combo.asSequence().map { it.delivery }, startMin, endMin)
        }.toList()

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

    enum class Day { M, T, W, R, F, S, U }
    data class Interval(val day: Day, val start: Int, val end: Int)
    val toMinutesRegex = Regex("^(\\d{1,2}):(\\d{2})(am|pm)", RegexOption.IGNORE_CASE)

    fun toMinutes(t: String): Int {
        val match = toMinutesRegex.matchEntire(t.trim()) ?: return 0
        val (hStr, mStr, period) = match.destructured
        var h = hStr.toInt()
        if (h == 12) h = 0
        if (period.lowercase() == "pm") h += 12
        return h * 60 + mStr.toInt()
    }

    fun charToDay(c: Char) = when (c) {
        'M' -> Day.M; 'T' -> Day.T; 'W' -> Day.W; 'R' -> Day.R
        'F' -> Day.F; 'S' -> Day.S; 'U' -> Day.U
        else -> null
    }

    val timeSlotRegex = Regex("""([MTWRFSU]+)\s+(\d{1,2}:\d{2}[ap]m)-(\d{1,2}:\d{2}[ap]m)""", RegexOption.IGNORE_CASE)

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
        val intervalsByDay = mutableMapOf<Day, MutableList<Interval>>()

        for (slot in times) {
            for (iv in parseTimeSlot(slot)) {
                // Conflict with preferred hours
                if (iv.start < preferredStartMin || iv.end > preferredEndMin) return true

                intervalsByDay.computeIfAbsent(iv.day) { mutableListOf() }.add(iv)
            }
        }

        // Check overlaps per day
        for ((_, ivs) in intervalsByDay) {
            // Small list per day; memory tiny
            ivs.sortBy { it.start }
            for (i in 0 until ivs.size - 1) {
                if (ivs[i + 1].start < ivs[i].end) return true
            }
        }

        return false
    }




    fun freeTimeForSchedule(courses: List<Course>): Int {
        val map = mutableMapOf<Day, MutableList<Pair<Int, Int>>>()
        for (c in courses) {
            val d = c.delivery
            for (iv in parseTimeSlot(d)) map.computeIfAbsent(iv.day) { mutableListOf() }.add(iv.start to iv.end)
        }
        var total = 0
        for (day in Day.entries) {
            val intervals = map[day]?.sortedBy { it.first } ?: emptyList()
            var last = 7 * 60
            for ((start, end) in intervals) {
                if (start > last) total += start - last
                last = maxOf(last, end)
            }
            total += (23 * 60) - last
        }
        return total
    }

    fun getFreeSlots(schedule: Schedule): Map<Day, List<Pair<Int, Int>>> {
        val occupied = mutableMapOf<Day, MutableList<Pair<Int, Int>>>()
        for (c in schedule.courses) {
            for (iv in parseTimeSlot(c.delivery))
                occupied.computeIfAbsent(iv.day) { mutableListOf() }.add(iv.start to iv.end)
        }

        val free = mutableMapOf<Day, MutableList<Pair<Int, Int>>>()
        for ((day, blocks) in occupied) {
            val sorted = blocks.sortedBy { it.first }
            var prevEnd = 0
            val dailyFree = mutableListOf<Pair<Int, Int>>()
            for ((start, end) in sorted) {
                if (start > prevEnd) dailyFree.add(prevEnd to start)
                prevEnd = end
            }
            if (prevEnd < 1440) dailyFree.add(prevEnd to 1440)
            free[day] = dailyFree
        }
        return free
    }

    fun <T> cartesianProduct(lists: List<List<T>>): List<List<T>> {
        if (lists.isEmpty() || lists.any { it.isEmpty() }) return emptyList()
        return lists.fold(listOf(listOf<T>())) { acc, list -> acc.flatMap { a -> list.map { a + it } } }
    }
}

