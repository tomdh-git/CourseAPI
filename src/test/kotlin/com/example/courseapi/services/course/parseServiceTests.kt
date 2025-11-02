package com.example.courseapi.services.course

import org.junit.jupiter.api.Test

class ParseServiceTest {

    private val parse = ParseService()

    @Test
    fun `parses empty HTML as empty list`() {
        val result = parse.parseCourses("<html><body></body></html>")
        assert(result.isEmpty())
    }

    @Test
    fun `skips rows with less than 9 columns`() {
        val html = """
            <table>
                <tr class="resultrow">
                    <td>CSE</td><td>101</td><td>Intro</td>
                </tr>
            </table>
        """.trimIndent()
        val result = parse.parseCourses(html)
        assert(result.isEmpty())
    }

    @Test
    fun `parses valid table rows`() {
        val html = """
            <table>
                <tr class="resultrow">
                    <td>CSE</td><td>101</td><td>Intro</td><td>A</td>
                    <td>12345</td><td>Main</td><td>3</td><td>30</td><td>10</td><td>Face2Face</td>
                </tr>
            </table>
        """.trimIndent()
        val result = parse.parseCourses(html)
        assert(result.size == 1)
        val c = result[0]
        assert(c.subject == "CSE")
        assert(c.courseNum == "101")
        assert(c.title == "Intro")
        assert(c.section == "A")
        assert(c.crn == 12345)
        assert(c.campus == "Main")
        assert(c.credits == 3)
        assert(c.capacity == "30")
        assert(c.requests == "10")
        assert(c.delivery == "Face2Face")
    }

    @Test
    fun `handles missing delivery field gracefully`() {
        val html = """
            <table>
                <tr class="resultrow">
                    <td>CSE</td><td>101</td><td>Intro</td><td>A</td>
                    <td>12345</td><td>Main</td><td>3</td><td>30</td><td>10</td>
                </tr>
            </table>
        """.trimIndent()
        val result = parse.parseCourses(html)
        assert(result.size == 1)
        assert(result[0].delivery == "")
    }

    @Test
    fun `handles non-numeric CRN and credit fields`() {
        val html = """
            <table>
                <tr class="resultrow">
                    <td>CSE</td><td>101</td><td>Intro</td><td>A</td>
                    <td>ABC</td><td>Main</td><td>X</td><td>30</td><td>10</td><td>Online</td>
                </tr>
            </table>
        """.trimIndent()
        val result = parse.parseCourses(html)
        assert(result.size == 1)
        assert(result[0].crn == 0)
        assert(result[0].credits == 0)
    }

    @Test
    fun `skips rows with empty subject and courseNum and title`() {
        val html = """
            <table>
                <tr class="resultrow">
                    <td></td><td></td><td></td><td>A</td>
                    <td>12345</td><td>Main</td><td>3</td><td>30</td><td>10</td><td>Online</td>
                </tr>
            </table>
        """.trimIndent()
        val result = parse.parseCourses(html)
        assert(result.isEmpty())
    }

    @Test
    fun `parses multiple valid rows`() {
        val html = """
            <table>
                <tr class="resultrow">
                    <td>CSE</td><td>101</td><td>Intro</td><td>A</td>
                    <td>11111</td><td>Main</td><td>3</td><td>30</td><td>10</td><td>Online</td>
                </tr>
                <tr class="resultrow">
                    <td>MTH</td><td>202</td><td>Calc II</td><td>B</td>
                    <td>22222</td><td>City</td><td>4</td><td>40</td><td>5</td><td>Hybrid</td>
                </tr>
            </table>
        """.trimIndent()
        val result = parse.parseCourses(html)
        assert(result.size == 2)
        assert(result[1].subject == "MTH")
        assert(result[1].courseNum == "202")
        assert(result[1].delivery == "Hybrid")
    }
}
