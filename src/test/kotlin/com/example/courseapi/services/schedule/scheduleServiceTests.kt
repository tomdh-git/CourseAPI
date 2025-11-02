//package com.example.courseapi.services.schedule
//
//import com.example.courseapi.models.schedule.Schedule
//import com.example.courseapi.repos.schedule.ScheduleRepo
//import kotlinx.coroutines.runBlocking
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.assertThrows
//import org.mockito.kotlin.mock
//import org.mockito.kotlin.whenever
//
//class ScheduleServiceValidationTest {
//
//    private val repo: ScheduleRepo = mock()
//    private val service = ScheduleService(repo)
//
//    private val dummySchedule = Schedule(emptyList(), 0)
//
//    // getScheduleByCourses
//
//    @Test
//    fun `throws exception when campus empty`() {
//        whenever(runBlocking { service.getScheduleByCourses(listOf("CSE 101"), emptyList(), "202620") })
//            .thenCallRealMethod()
//        assertThrows<IllegalArgumentException> {
//            runBlocking { service.getScheduleByCourses(listOf("CSE 101"), emptyList(), "202620") }
//        }
//    }
//
//    @Test
//    fun `throws exception when campus invalid`() {
//        whenever(runBlocking { service.getScheduleByCourses(listOf("CSE 101"), listOf("INVALID"), "202620") })
//            .thenCallRealMethod()
//        assertThrows<IllegalArgumentException> {
//            runBlocking { service.getScheduleByCourses(listOf("CSE 101"), listOf("INVALID"), "202620") }
//        }
//    }
//
//    @Test
//    fun `throws exception when term empty`()  {
//        whenever(runBlocking { service.getScheduleByCourses(listOf("CSE 101"), listOf("Main"), "") })
//            .thenCallRealMethod()
//        assertThrows<IllegalArgumentException> {
//            runBlocking { service.getScheduleByCourses(listOf("CSE 101"), listOf("Main"), "") }
//        }
//    }
//
//    @Test
//    fun `throws exception when only preferredStart specified`()  {
//        whenever(runBlocking { service.getScheduleByCourses(listOf("CSE 101"), listOf("Main"), "202620", preferredStart = "08:00") })
//            .thenCallRealMethod()
//        assertThrows<IllegalArgumentException> {
//            runBlocking { service.getScheduleByCourses(listOf("CSE 101"), listOf("Main"), "202620", preferredStart = "08:00") }
//        }
//    }
//
//    @Test
//    fun `throws exception when only preferredEnd specified`()  {
//        whenever(runBlocking { service.getScheduleByCourses(listOf("CSE 101"), listOf("Main"), "202620", preferredEnd = "17:00") })
//            .thenCallRealMethod()
//        assertThrows<IllegalArgumentException> {
//            runBlocking { service.getScheduleByCourses(listOf("CSE 101"), listOf("Main"), "202620", preferredEnd = "17:00") }
//        }
//    }
//
//    @Test
//    fun `throws exception when preferredStart after preferredEnd`()  {
//        whenever(repo.toMinutes("17:00")).thenReturn(1020)
//        whenever(repo.toMinutes("08:00")).thenReturn(480)
//        whenever(runBlocking { service.getScheduleByCourses(listOf("CSE 101"), listOf("Main"), "202620", preferredStart = "17:00", preferredEnd = "08:00") })
//            .thenCallRealMethod()
//        assertThrows<IllegalArgumentException> {
//            runBlocking { service.getScheduleByCourses(listOf("CSE 101"), listOf("Main"), "202620", preferredStart = "17:00", preferredEnd = "08:00") }
//        }
//    }
//
//    // getFillerByAttributes
//
//    @Test
//    fun `throws exception when attributes empty`()  {
//        whenever(runBlocking { service.getFillerByAttributes(emptyList(), listOf("CSE 101"), listOf("Main"), "202620") })
//            .thenCallRealMethod()
//        assertThrows<IllegalArgumentException> {
//            runBlocking { service.getFillerByAttributes(emptyList(), listOf("CSE 101"), listOf("Main"), "202620") }
//        }
//    }
//
//    @Test
//    fun `throws exception when attributes invalid`()  {
//        whenever(runBlocking { service.getFillerByAttributes(listOf("INVALID"), listOf("CSE 101"), listOf("Main"), "202620") })
//            .thenCallRealMethod()
//        assertThrows<IllegalArgumentException> {
//            runBlocking { service.getFillerByAttributes(listOf("INVALID"), listOf("CSE 101"), listOf("Main"), "202620") }
//        }
//    }
//
//    @Test
//    fun `throws exception when campus empty for filler`()  {
//        whenever(runBlocking { service.getFillerByAttributes(listOf("PA1C"), listOf("CSE 101"), emptyList(), "202620") })
//            .thenCallRealMethod()
//        assertThrows<IllegalArgumentException> {
//            runBlocking { service.getFillerByAttributes(listOf("PA1C"), listOf("CSE 101"), emptyList(), "202620") }
//        }
//    }
//
//    @Test
//    fun `throws exception when term empty for filler`()  {
//        whenever(runBlocking { service.getFillerByAttributes(listOf("PA1C"), listOf("CSE 101"), listOf("Main"), "") })
//            .thenCallRealMethod()
//        assertThrows<IllegalArgumentException> {
//            runBlocking { service.getFillerByAttributes(listOf("PA1C"), listOf("CSE 101"), listOf("Main"), "") }
//        }
//    }
//
//    @Test
//    fun `throws exception when only preferredStart specified for filler`()  {
//        whenever(runBlocking { service.getFillerByAttributes(listOf("PA1C"), listOf("CSE 101"), listOf("Main"), "202620", preferredStart = "08:00") })
//            .thenCallRealMethod()
//        assertThrows<IllegalArgumentException> {
//            runBlocking { service.getFillerByAttributes(listOf("PA1C"), listOf("CSE 101"), listOf("Main"), "202620", preferredStart = "08:00") }
//        }
//    }
//
//    @Test
//    fun `throws exception when only preferredEnd specified for filler`()  {
//        whenever(runBlocking { service.getFillerByAttributes(listOf("PA1C"), listOf("CSE 101"), listOf("Main"), "202620", preferredEnd = "17:00") })
//            .thenCallRealMethod()
//        assertThrows<IllegalArgumentException> {
//            runBlocking { service.getFillerByAttributes(listOf("PA1C"), listOf("CSE 101"), listOf("Main"), "202620", preferredEnd = "17:00") }
//        }
//    }
//
//    @Test
//    fun `throws exception when preferredStart after preferredEnd for filler`()  {
//        whenever(repo.toMinutes("17:00")).thenReturn(1020)
//        whenever(repo.toMinutes("08:00")).thenReturn(480)
//        whenever(runBlocking { service.getFillerByAttributes(listOf("PA1C"), listOf("CSE 101"), listOf("Main"), "202620", preferredStart = "17:00", preferredEnd = "08:00") })
//            .thenCallRealMethod()
//        assertThrows<IllegalArgumentException> {
//            runBlocking { service.getFillerByAttributes(listOf("PA1C"), listOf("CSE 101"), listOf("Main"), "202620", preferredStart = "17:00", preferredEnd = "08:00") }
//        }
//    }
//}
