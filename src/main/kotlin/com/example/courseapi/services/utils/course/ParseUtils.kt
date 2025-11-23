package com.example.courseapi.services.utils.course

import com.example.courseapi.models.course.Course
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.springframework.stereotype.Component

@Component
class ParseUtils {
    private val digitRegex = Regex("\\d+")

    fun parseCourses(html: String): List<Course> {
        val doc = Jsoup.parse(html)
        val rows = doc.select("tr.resultrow")
        val list = ArrayList<Course>(rows.size)
        return populateCourses(list,rows)
    }

    private fun populateCourses(list: ArrayList<Course>, rows: Elements): List<Course> {
        for (tr in rows) {
            val tds = tr.select("td")
            if (tds.size < 9) continue
            val subject = tds[0].ownText().trim()
            if (subject.isEmpty()) continue
            val courseNum = tds[1].text().trim()
            val title = tds[2].text().trim()
            val section = tds[3].text().trim()
            val crn = digitRegex.find(tds[4].text())?.value?.toIntOrNull() ?: 0
            val campus = tds[5].text().trim()
            val credits = digitRegex.find(tds[6].text())?.value?.toIntOrNull() ?: 0
            val capacity = tds[7].text().trim()
            val requests = tds[8].text().trim()
            val delivery = tds.getOrNull(9)?.text()?.trim() ?: ""
            list.add(Course(
                subject, courseNum, title, section, crn,
                campus, credits, capacity, requests, delivery
            )
            )
        }
        return list
    }
}