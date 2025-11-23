package com.example.courseapi.resolvers.schedule

import com.example.courseapi.models.dto.schedule.*
import com.example.courseapi.models.schedule.ScheduleResult
import com.example.courseapi.resolvers.utils.schedule.scheduleSafe
import com.example.courseapi.services.schedule.ScheduleService
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin

@Controller
@CrossOrigin(origins = ["*"])
class ScheduleResolver(private val ss: ScheduleService) {
    @QueryMapping
    suspend fun getScheduleByCourses(@Argument input: ScheduleByCourseInput): ScheduleResult =
        scheduleSafe{ss.getScheduleByCourses(input)}

    @QueryMapping
    suspend fun getFillerByAttributes(@Argument input: FillerByAttributesInput): ScheduleResult=
        scheduleSafe{ss.getFillerByAttributes(input)}

}