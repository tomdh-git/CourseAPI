package com.example.courseapi.resolvers

import com.example.courseapi.exceptions.*
import org.slf4j.*
import io.ktor.client.plugins.*
import com.example.courseapi.models.*
import com.example.courseapi.services.CourseService
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.TimeoutCancellationException
import org.springframework.graphql.data.method.annotation.*
import org.springframework.stereotype.Controller

@Controller
class CourseResolver(private val service: CourseService) {
    @QueryMapping
    suspend fun getCourseByInfo(@Argument subject: List<String>?, @Argument courseNum: String?, @Argument campus: List<String>, @Argument attributes: List<String>?, @Argument delivery: List<String>?, @Argument term: String, @Argument openWaitlist: String?, @Argument crn: Int?, @Argument partOfTerm: List<String>?, @Argument level: String?, @Argument courseTitle: String?, @Argument daysFilter: List<String>?, @Argument creditHours: Int?, @Argument startEndTime: List<String>?): CourseResult {
        return safeExecute<List<Course>,CourseResult> ({ service.getCourseByInfo(subject, courseNum, campus, attributes, delivery, term, openWaitlist, crn, partOfTerm, level, courseTitle, daysFilter, creditHours, startEndTime) }, {SuccessCourse(it)}, { code, msg -> ErrorCourse(code, msg) })
    }

    @QueryMapping
    suspend fun getCourseByCRN(@Argument crn: Int?, @Argument term: String): CourseResult {
        return safeExecute<List<Course>,CourseResult> ({ service.getCourseByCRN(crn, term) }, {SuccessCourse(it)}, { code, msg -> ErrorCourse(code, msg) })
    }

    @QueryMapping
    suspend fun getScheduleByCourses(@Argument courses: List<String>, @Argument campus: List<String>, @Argument term: String, @Argument optimizeFreeTime: Boolean?, @Argument preferredStart: String?, @Argument preferredEnd: String?): ScheduleResult {
        return safeExecute<List<Schedule>,ScheduleResult> ({ service.getScheduleByCourses(courses, campus, term, optimizeFreeTime, preferredStart, preferredEnd) }, {SuccessSchedule(it)}, { code, msg -> ErrorSchedule(code, msg) })
    }
//   getFillerByAttributes,

}

private suspend fun <T, R> safeExecute(action: suspend () -> T, wrap: (T) -> R, makeError: (String, String?) -> R): R {
    val logger: Logger = LoggerFactory.getLogger(CourseResolver::class.java)
    return try { wrap(action()) }
    catch (e: TokenException) { logger.error("Token Exception in ${e.stackTrace[1].methodName}: Couldn't fetch token at ${e.stackTraceToString()}"); (makeError("TOKEN EXCEPTION", e.message)) }
    catch (e: QueryException) { logger.error("Query Exception in ${e.stackTrace[1].methodName}: Query returned too many results at ${e.stackTraceToString()}"); (makeError("QUERY EXCEPTION", e.message)) }
    catch (e: IllegalArgumentException) { logger.error("Illegal Argument Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); (makeError("ILLEGAL ARGUMENT EXCEPTION", e.message)) }
    catch (e: TimeoutCancellationException) { logger.error("Timeout Cancellation Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); (makeError("TIMEOUT EXCEPTION", e.message)) }
    catch (e: NullPointerException) { logger.error("Null Pointer Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); (makeError("NULL POINTER EXCEPTION", e.message)) }
    catch (e: Exception) { when (e) { is IOException, is ClientRequestException, is ServerResponseException -> { logger.error("Network Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); (makeError("NETWORK EXCEPTION", e.message)) }
            else -> { logger.error("Unexpected Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); (makeError("UNKNOWN EXCEPTION", e.message) ) } } }
}
