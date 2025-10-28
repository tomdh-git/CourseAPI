package com.example.courseapi.services

import org.junit.jupiter.api.Test

class ParseServiceTest {
    private val parse = ParseService()

    @Test fun `parses empty HTML as empty list`() {
        val result = parse.parseCourses("<html><body></body></html>")
        assert(result.isEmpty())
    }

    @Test fun `parses valid table rows`() {
        val html = """
            <table>
                <tr class="resultrow">
                    <td>CSE</td><td>101</td><td>Intro</td><td>A</td><td>12345</td><td>Main</td><td>3</td><td>30</td><td>10</td><td>Face2Face</td>
                </tr>
            </table>
        """.trimIndent()
        val result = parse.parseCourses(html)
        assert(result.size == 1)
        assert(result[0].subject == "CSE")
        assert(result[0].courseNum == 101)
    }
}
