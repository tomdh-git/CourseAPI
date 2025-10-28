package com.example.courseapi.resolvers

import com.example.courseapi.exceptions.*
import org.slf4j.*
import io.ktor.client.plugins.*
import com.example.courseapi.models.CourseResult
import com.example.courseapi.services.CourseService
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.TimeoutCancellationException
import org.springframework.graphql.data.method.annotation.*
import org.springframework.stereotype.Controller

@Controller
class CourseResolver(private val service: CourseService, ) {
    @QueryMapping
    suspend fun getCourseByInfo(@Argument subject: List<String>?, @Argument courseNum: Int?, @Argument campus: List<String>?, @Argument attributes: List<String>?, @Argument delivery: List<String>?, @Argument term: String?, @Argument openWaitlist: String?, @Argument crn: Int?, @Argument partOfTerm: List<String>?, @Argument level: String?, @Argument courseTitle: String?, @Argument daysFilter: List<String>?, @Argument creditHours: Int?, @Argument startEndTime: List<String>?): CourseResult {
        return safeExecute { service.getCourseByInfo(subject, courseNum, campus, attributes, delivery, term, openWaitlist, crn, partOfTerm, level, courseTitle, daysFilter, creditHours, startEndTime) }
    }

    @QueryMapping
    suspend fun getCourseByCRN(@Argument crn: Int?, @Argument term: String?): CourseResult {
        return safeExecute { service.getCourseByCRN(crn, term) }
    }

//    @QueryMapping
//    suspend fun getScheduleByCourses(@Argument courses: List<String>?){
//
//    }
    //getScheduleByCourses, getFillerByAttributes,

}

private suspend fun safeExecute(action: suspend () -> List<com.example.courseapi.models.Course>): CourseResult {
    val logger: Logger = LoggerFactory.getLogger(CourseResolver::class.java)
    return try { CourseResult.Success(action()) }
    catch (e: TokenException) { logger.error("Token Exception in ${e.stackTrace[1].methodName}: Couldn't fetch token (returned empty) at ${e.stackTraceToString()}"); CourseResult.Error("TOKEN EXCEPTION", e.message) }
    catch (e: QueryException) { logger.error("Query Exception in ${e.stackTrace[1].methodName}: Query returned too many results at ${e.stackTraceToString()}"); CourseResult.Error("QUERY EXCEPTION", e.message) }
    catch (e: IllegalArgumentException) { logger.error("Illegal Argument Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); CourseResult.Error("ILLEGAL ARGUMENT EXCEPTION", e.message) }
    catch (e: TimeoutCancellationException) { logger.error("Timeout Cancellation Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); CourseResult.Error("TIMEOUT EXCEPTION", e.message) }
    catch (e: NullPointerException) { logger.error("Null Pointer Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()} (problably because of HTML parsing failure)"); CourseResult.Error("NULL POINTER EXCEPTION", e.message)}
    catch (e: Exception) { when (e) { is IOException, is ClientRequestException, is ServerResponseException -> { logger.error("Network Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()} (bad response or network layer error)"); CourseResult.Error("NETWORK EXCEPTION", e.message) }
        else -> { logger.error("Unexpected Exception in ${e.stackTrace[1].methodName}: ${e.message} at ${e.stackTraceToString()}"); CourseResult.Error("UNKNOWN EXCEPTION", e.message) } }
    }
}
