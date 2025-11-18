package com.example.courseapi.resolvers

import com.example.courseapi.exceptions.*
import org.slf4j.*
import io.ktor.client.plugins.*
import com.example.courseapi.models.course.*
import com.example.courseapi.models.misc.*
import com.example.courseapi.models.schedule.*
import com.example.courseapi.services.course.CourseService
import com.example.courseapi.services.schedule.ScheduleService
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.TimeoutCancellationException
import org.springframework.graphql.data.method.annotation.*
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin

@Controller
@CrossOrigin(origins = ["*"]) // allow all origins
class CourseResolver(private val cs: CourseService, private val ss: ScheduleService) {
    @QueryMapping
    suspend fun getCourseByInfo(@Argument subject: List<String>?, @Argument courseNum: String?, @Argument campus: List<String>, @Argument attributes: List<String>?, @Argument delivery: List<String>?, @Argument term: String, @Argument openWaitlist: String?, @Argument crn: Int?, @Argument partOfTerm: List<String>?, @Argument level: String?, @Argument courseTitle: String?, @Argument daysFilter: List<String>?, @Argument creditHours: Int?, @Argument startEndTime: List<String>?): CourseResult {
        return safeExecute ({ cs.getCourseByInfo(subject, courseNum, campus, attributes, delivery, term, openWaitlist, crn, partOfTerm, level, courseTitle, daysFilter, creditHours, startEndTime) }, { SuccessCourse(it) }, { code, msg -> ErrorCourse(code, msg) })
    }

    @QueryMapping
    suspend fun getCourseByCRN(@Argument crn: Int?, @Argument term: String): CourseResult {
        return safeExecute ({ cs.getCourseByCRN(crn, term) }, { SuccessCourse(it) }, { code, msg -> ErrorCourse(code, msg) })
    }

    @QueryMapping
    suspend fun getScheduleByCourses(@Argument courses: List<String>, @Argument campus: List<String>, @Argument term: String, @Argument optimizeFreeTime: Boolean?, @Argument preferredStart: String?, @Argument preferredEnd: String?): ScheduleResult {
        return safeExecute ({ ss.getScheduleByCourses(courses, campus, term, optimizeFreeTime, preferredStart, preferredEnd) }, { SuccessSchedule(it) }, { code, msg -> ErrorSchedule(code, msg) })
    }

    @QueryMapping
    suspend fun getFillerByAttributes(@Argument attributes: List<String>, @Argument courses: List<String>, @Argument campus: List<String>, @Argument term: String, @Argument preferredStart: String?, @Argument preferredEnd: String?, @Argument ignoreWeb: Boolean?): ScheduleResult{
        return safeExecute ({ ss.getFillerByAttributes(attributes, courses, campus, term, preferredStart, preferredEnd, ignoreWeb) }, { SuccessSchedule(it) }, { code, msg -> ErrorSchedule(code, msg) })
    }

    @QueryMapping
    suspend fun getTerms(): FieldResult {
        return safeExecute({ cs.getTerms() }, { SuccessField(it) }, { code, msg -> ErrorField(code, msg) })
    }

//    @QueryMapping
//    suspend fun getCampuses(): FieldResult {
//        return safeExecute({ cs.getTerms() }, { SuccessField(it) }, { code, msg -> ErrorField(code, msg) })
//    }
}

private suspend fun <T, R> safeExecute(action: suspend () -> T, wrap: (T) -> R, makeError: (String, String?) -> R): R {
    val logger: Logger = LoggerFactory.getLogger(CourseResolver::class.java)
    return try { wrap(action()) }
    catch (e: APIException) { logger.error("API Exception in ${e.stackTrace[1].methodName}: Theres a problem with the Miami Course List. Threw at ${e.stackTraceToString()}"); (makeError("API EXCEPTION", e.message)) }
    catch (e: QueryException) { logger.error("Query Exception in ${e.stackTrace[1].methodName}: Query returned too many results at ${e.stackTraceToString()}"); (makeError("QUERY EXCEPTION", e.message)) }
    catch (e: IllegalArgumentException) { logger.error("Illegal Argument Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); (makeError("ILLEGAL ARGUMENT EXCEPTION", e.message)) }
    catch (e: TimeoutCancellationException) { logger.error("Timeout Cancellation Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); (makeError("TIMEOUT EXCEPTION", e.message)) }
    catch (e: NullPointerException) { logger.error("Null Pointer Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); (makeError("NULL POINTER EXCEPTION", e.message)) }
    catch (e: ServerBusyException) { logger.error("Server Busy Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); (makeError("SERVER BUSY EXCEPTION", e.message)) }
    catch (e: Exception) { when (e) { is IOException, is ClientRequestException, is ServerResponseException -> { logger.error("Network Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); (makeError("NETWORK EXCEPTION", e.message)) }
            else -> { logger.error("Unexpected Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); (makeError("UNKNOWN EXCEPTION", e.message) ) } } }
}
