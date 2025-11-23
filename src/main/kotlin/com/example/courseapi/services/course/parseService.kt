package com.example.courseapi.services.course

import com.example.courseapi.models.course.Course
import com.example.courseapi.models.misc.Field
import org.jsoup.Jsoup
import org.springframework.stereotype.Service

@Service
class ParseService{
    private val digitRegex = Regex("\\d+")
    
    fun parseCourses(html: String): List<Course> {
        val doc = Jsoup.parse(html)
        val rows = doc.select("tr.resultrow")
        val list = ArrayList<Course>(rows.size)
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
                subject,
                courseNum,
                title,
                section,
                crn,
                campus,
                credits,
                capacity,
                requests,
                delivery)
            )
        }
        return list
    }

    fun parseAllFields(html: String): Map<String, List<Field>> {
        val doc = Jsoup.parse(html)
        val result = mutableMapOf<String, List<Field>>()
        result["attributes"] = doc.select("select#sectionFilterAttributes option[value]").map { opt ->
            Field(opt.attr("value").trim())
        }
        result["terms"] = doc.select("select#termFilter option[value]").map { opt ->
            Field(opt.attr("value").trim())
        }
        result["delivery"] = doc.select("input.deliveryTypeCheckBox[value]").map { input ->
            Field(input.attr("value").trim())
        }.filter { it.name.isNotEmpty() }
        result["campuses"] = doc.select("select#campusFilter option[value]").map { opt ->
            Field(opt.attr("value").trim())
        }.filter { it.name.isNotEmpty() }
        result["subjects"] = doc.select("select#subject option[value]").map { opt ->
            Field(opt.attr("value").trim())
        }.filter { it.name.isNotEmpty() }
        result["waitlist"] = doc.select("select#openWaitlist option[value]").map { opt ->
            Field(opt.attr("value").trim())
        }
        result["levels"] = doc.select("select#levelFilter option[value]").map { opt ->
            Field(opt.attr("value").trim())
        }
        result["days"] = doc.select("select#daysFilter option[value]").map { opt ->
            Field(opt.attr("value").trim())
        }
        return result
    }
    
    fun parseTerms(html: String): List<Field> {
        val doc = Jsoup.parse(html)
        return doc.select("select#termFilter option[value]").map { opt ->
            Field(opt.attr("value").trim())
        }
    }
}