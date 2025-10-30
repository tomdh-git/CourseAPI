package com.example.courseapi.services.course

import com.example.courseapi.models.course.Course
import org.jsoup.Jsoup
import org.springframework.stereotype.Service

@Service
class ParseService{
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
}