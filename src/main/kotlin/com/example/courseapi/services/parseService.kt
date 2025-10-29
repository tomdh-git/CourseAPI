package com.example.courseapi.services

import com.example.courseapi.models.Course
import org.jsoup.Jsoup
import org.springframework.stereotype.Service
import kotlin.collections.map

@Service
class ParseService{
    enum class Day { M, T, W, R, F, S, U }
    data class Interval(val day: Day, val start: Int, val end: Int)

    fun parseCourses(html: String): List<Course> {
        val doc = Jsoup.parse(html)
        val rows = doc.select("tr.resultrow")
        val list = mutableListOf<Course>()
        for (tr in rows) {
            val tds = tr.select("td")
            if (tds.size < 9) continue
            val subject = tds[0].ownText().trim()
            val courseNum = tds[1].text().trim()
            val title = tds[2].text().trim()
            val section = tds[3].text().trim()
            val crn = tds[4].text().trim().filter { it.isDigit() }.toIntOrNull() ?: 0
            val campus = tds[5].text().trim()
            val credits = tds[6].text().trim().toIntOrNull() ?: 0
            val capacity = tds[7].text().trim()
            val requests = tds[8].text().trim()
            val delivery = tds.getOrNull(9)?.text()?.trim() ?: ""
            if (subject.isEmpty() && courseNum == "" && title.isEmpty()) continue
            list.add(Course(subject, courseNum, title, section, crn, campus, credits, capacity, requests, delivery)
            )
        }
        return list
    }

    fun toMinutes(t: String): Int {
        val (hStr, mStr, period) = (Regex("^(\\d{1,2}):(\\d{2})(am|pm)", RegexOption.IGNORE_CASE).matchEntire(t.trim()) ?: return 0).destructured
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

    fun parseTimeSlot(slot: String): List<Interval> {
        val regex = Regex("""([MTWRFSU]+)\s+(\d{1,2}:\d{2}[ap]m)-(\d{1,2}:\d{2}[ap]m)""", RegexOption.IGNORE_CASE)
        val matches = regex.findAll(slot)
        return matches.flatMap { m ->
            val (daysStr, startStr, endStr) = m.destructured
            val start = toMinutes(startStr)
            val end = toMinutes(endStr)
            val days = daysStr.mapNotNull { charToDay(it) }
            days.map { Interval(it, start, end) }
        }.toList()
    }

    fun timeConflicts(times: List<String>, preferredStartMin: Int, preferredEndMin: Int): Boolean {
        val intervals = times.flatMap(::parseTimeSlot)

        // conflict if class outside preferred hours
        if (intervals.any { it.start < preferredStartMin || it.end > preferredEndMin }) return true

        // check overlapping classes on same day
        return intervals.groupBy { it.day }.values.any { ivs ->
            val sorted = ivs.sortedBy { it.start }
            sorted.zipWithNext().any { (a, b) -> b.start < a.end }
        }
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

    fun <T> cartesianProduct(lists: List<List<T>>): List<List<T>> {
        if (lists.isEmpty() || lists.any { it.isEmpty() }) return emptyList()
        return lists.fold(listOf(listOf<T>())) { acc, list -> acc.flatMap { a -> list.map { a + it } } }
    }
}