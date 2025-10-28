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
    suspend fun getCourseByInfo(@Argument subject: List<String>?, @Argument courseNum: Int?, @Argument campus: List<String>, @Argument attributes: List<String>?, @Argument delivery: List<String>?, @Argument term: String, @Argument openWaitlist: String?, @Argument crn: Int?, @Argument partOfTerm: List<String>?, @Argument level: String?, @Argument courseTitle: String?, @Argument daysFilter: List<String>?, @Argument creditHours: Int?, @Argument startEndTime: List<String>?): CourseResult {
        return safeExecute<List<Course>,CourseResult> ({ service.getCourseByInfo(subject, courseNum, campus, attributes, delivery, term, openWaitlist, crn, partOfTerm, level, courseTitle, daysFilter, creditHours, startEndTime) }, {CourseResult.Success(it)})
    }

    @QueryMapping
    suspend fun getCourseByCRN(@Argument crn: Int?, @Argument term: String): CourseResult {
        return safeExecute<List<Course>,CourseResult> ({ service.getCourseByCRN(crn, term) }, {CourseResult.Success(it)})
    }

//    @QueryMapping
//    suspend fun getScheduleByCourses(@Argument courses: List<String>?): ScheduleResult {
//        return safeExecute<List<Schedule>,ScheduleResult> ({ service.getScheduleByCourses(courses) }, {ScheduleResult.Success(it)})
//    }
    //getScheduleByCourses, getFillerByAttributes,

}

private suspend fun <T, R> safeExecute(action: suspend () -> T, wrap: (T) -> R): R {
    fun wrapError(code: String, message: String?): Any = CourseResult.Error(code, message)
    val logger: Logger = LoggerFactory.getLogger(CourseResolver::class.java)
    return try { wrap(action()) }
    catch (e: TokenException) { logger.error("Token Exception in ${e.stackTrace[1].methodName}: Couldn't fetch token at ${e.stackTraceToString()}"); @Suppress("UNCHECKED_CAST")(wrapError("TOKEN EXCEPTION", e.message) as R) }
    catch (e: QueryException) { logger.error("Query Exception in ${e.stackTrace[1].methodName}: Query returned too many results at ${e.stackTraceToString()}"); @Suppress("UNCHECKED_CAST") (wrapError("QUERY EXCEPTION", e.message) as R) }
    catch (e: IllegalArgumentException) { logger.error("Illegal Argument Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); @Suppress("UNCHECKED_CAST") (wrapError("ILLEGAL ARGUMENT EXCEPTION", e.message) as R) }
    catch (e: TimeoutCancellationException) { logger.error("Timeout Cancellation Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); @Suppress("UNCHECKED_CAST") (wrapError("TIMEOUT EXCEPTION", e.message) as R) }
    catch (e: NullPointerException) { logger.error("Null Pointer Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); @Suppress("UNCHECKED_CAST") (wrapError("NULL POINTER EXCEPTION", e.message) as R) }
    catch (e: Exception) { when (e) { is IOException, is ClientRequestException, is ServerResponseException -> { logger.error("Network Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); @Suppress("UNCHECKED_CAST") (wrapError("NETWORK EXCEPTION", e.message) as R) }
            else -> { logger.error("Unexpected Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); @Suppress("UNCHECKED_CAST") (wrapError("UNKNOWN EXCEPTION", e.message) as R) } } }
}
