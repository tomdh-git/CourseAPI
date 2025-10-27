package com.example.courseapi.resolvers

import com.example.courseapi.exceptions.TokenException
import com.example.courseapi.models.CourseResult
import com.example.courseapi.services.CourseService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class CourseResolver(
    private val service: CourseService,
    private val logger: Logger = LoggerFactory.getLogger(CourseResolver::class.java),
) {
    @QueryMapping
    suspend fun getCourses(
        @Argument subject: List<String>?,
        @Argument courseNum: Int?,
        @Argument campus: List<String>?,
        @Argument attributes: List<String>?,
        @Argument delivery: List<String>?,
        @Argument term: String?,
    ): CourseResult {
        return try{
            CourseResult.Success(service.getCourses(subject, courseNum, campus, attributes, delivery, term))
        }catch(e: TokenException){
            logger.error("Token Exception: Couldnt fetch token (returned empty)")
            CourseResult.Error("TOKEN EXCEPTION", e.message)
        }
    }
}
