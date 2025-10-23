package com.example.courseapi.repos

import com.example.courseapi.models.Course
import org.springframework.stereotype.Repository
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup

@Repository
class CourseRepo {
    fun getCourses(
        subject: String?,
        courseNum: Int?,
        title: String?,
        section: String?,
        crn: Int?,
        campus: String?,
        credits: Int?,
        capacity: String?,
        requests: String?,
        delivery: String?
    ): List<Course> = runBlocking {
        val client = HttpClient(CIO) {
            install(HttpCookies)
            followRedirects = true
            expectSuccess = false
        }

        // Step 1: Get the initial page to extract the token
        val initialResponse: HttpResponse = client.get("https://www.apps.miamioh.edu/courselist/") {
            headers {
                append("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                append("Accept-Language", "en-US,en;q=0.9")
                append("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36")
                append("Cache-Control", "max-age=0")
                append("Upgrade-Insecure-Requests", "1")
            }
        }
        val html = initialResponse.bodyAsText()

        // Extract token from HTML
        val tokenRegex = """<input[^>]*name=\"_token\"[^>]*value=\"([^\"]+)\"""".toRegex()
        val token = tokenRegex.find(html)?.groupValues?.get(1)

        if (token == null) {
            client.close()
            return@runBlocking emptyList()
        }

        // Step 2: Build form body manually with proper URL encoding
        val formParts = mutableListOf<String>()
        formParts.add("_token=${token.encodeURLParameter()}")

        // Add section attributes if delivery is specified
        if (!delivery.isNullOrEmpty()) {
            formParts.add("sectionAttributes%5B%5D=${delivery.encodeURLParameter()}")
        }

        // Required fields
        formParts.add("term=202620")
        if (campus.isNullOrEmpty()) {
            // Campus is required by the form; default to 'All' if none provided
            formParts.add("campusFilter%5B%5D=All")
        } else {
            formParts.add("campusFilter%5B%5D=${campus.encodeURLParameter()}")
        }

        // Optional filters
        if (!subject.isNullOrEmpty()) {
            formParts.add("subject%5B%5D=${subject.encodeURLParameter()}")
        }

        if (courseNum != null && courseNum > 0) {
            formParts.add("courseNumber=${courseNum}")
        } else {
            formParts.add("courseNumber=")
        }

        // Add other optional fields
        formParts.add("openWaitlist=")
        // Only include CRN if it's a positive integer; otherwise leave blank
        formParts.add("crnNumber=${if (crn != null && crn > 0) crn.toString() else ""}")
        formParts.add("level=")
        formParts.add("courseTitle=${title?.encodeURLParameter() ?: ""}")
        formParts.add("instructor=")
        formParts.add("instructorUid=")
        formParts.add("creditHours=${if (credits != null && credits > 0) credits.toString() else ""}")
        formParts.add("startEndTime%5B%5D=")
        formParts.add("startEndTime%5B%5D=")
        // Include submit value
        formParts.add("courseSearch=Find")

        val formBody = formParts.joinToString("&")

        val postResponse: HttpResponse = client.post("https://www.apps.miamioh.edu/courselist/") {
            headers {
                append("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                append("Accept-Language", "en-US,en;q=0.9")
                append("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                append("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36")
                append("Origin", "https://www.apps.miamioh.edu")
                append("Referer", "https://www.apps.miamioh.edu/courselist/")
                append("Cache-Control", "max-age=0")
            }
            setBody(formBody)
        }

        var resultHtml = postResponse.bodyAsText()

        // Manual follow for HTTP redirects
        if (postResponse.status.value in 300..399) {
            val loc = postResponse.headers[HttpHeaders.Location]
            if (!loc.isNullOrBlank()) {
                val redirectUrl = if (loc.startsWith("http")) loc else "https://www.apps.miamioh.edu" + (if (loc.startsWith("/")) loc else "/courselist/" + loc)
                val redirectResp = client.get(redirectUrl) {
                    headers {
                        append("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        append("Accept-Language", "en-US,en;q=0.9")
                        append("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36")
                        append("Referer", "https://www.apps.miamioh.edu/courselist/")
                    }
                }
                resultHtml = redirectResp.bodyAsText()
            }
        }

        // Meta refresh redirect
        if (resultHtml.contains("meta http-equiv=\"refresh\"")) {
            val redirectRegex = """content=\s*\"\s*\d+;\s*url='([^']+)'\s*\"""".toRegex()
            val redirectUrl = redirectRegex.find(resultHtml)?.groupValues?.get(1)
            if (redirectUrl != null) {
                val absolute = if (redirectUrl.startsWith("http")) redirectUrl else {
                    val path = if (redirectUrl.startsWith("/")) redirectUrl else "/courselist/" + redirectUrl
                    "https://www.apps.miamioh.edu" + path
                }
                val redirectResponse = client.get(absolute) {
                    headers {
                        append("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                        append("Accept-Language", "en-US,en;q=0.9")
                        append("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 Safari/537.36")
                        append("Referer", "https://www.apps.miamioh.edu/courselist/")
                    }
                }
                resultHtml = redirectResponse.bodyAsText()
            }
        }

        client.close()

        // Parse the results table into Course objects
        return@runBlocking parseCourses(resultHtml)
    }

    private fun parseCourses(html: String): List<Course> {
        val doc = Jsoup.parse(html)
        val rows = doc.select("tr.resultrow")
        val list = mutableListOf<Course>()
        for (tr in rows) {
            val tds = tr.select("td")
            if (tds.size < 9) continue
            val subject = tds[0].ownText().trim()
            val courseNum = tds[1].text().trim().toIntOrNull() ?: 0
            val title = tds[2].text().trim()
            val section = tds[3].text().trim()
            val crn = tds[4].text().trim().filter { it.isDigit() }.toIntOrNull() ?: 0
            val campus = tds[5].text().trim()
            val credits = tds[6].text().trim().toIntOrNull() ?: 0
            val capacity = tds[7].text().trim()
            val requests = tds[8].text().trim()
            val delivery = tds.getOrNull(9)?.text()?.trim() ?: ""

            if (subject.isEmpty() && courseNum == 0 && title.isEmpty()) continue
            list.add(
                Course(
                    subject = subject,
                    courseNum = courseNum,
                    title = title,
                    section = section,
                    crn = crn,
                    campus = campus,
                    credits = credits,
                    capacity = capacity,
                    requests = requests,
                    delivery = delivery
                )
            )
        }
        return list
    }
}
