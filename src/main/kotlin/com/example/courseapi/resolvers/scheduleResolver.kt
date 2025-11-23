package com.example.courseapi.resolvers

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
    suspend fun getScheduleByCourses(@Argument delivery: List<String>?, @Argument courses: List<String>, @Argument campus: List<String>, @Argument term: String, @Argument optimizeFreeTime: Boolean?, @Argument preferredStart: String?, @Argument preferredEnd: String?
    ): ScheduleResult = scheduleSafe{ss.getScheduleByCourses(delivery,courses,campus,term,optimizeFreeTime,preferredStart,preferredEnd)}

    @QueryMapping
    suspend fun getFillerByAttributes(@Argument delivery: List<String>?, @Argument attributes: List<String>, @Argument courses: List<String>, @Argument campus: List<String>, @Argument term: String, @Argument preferredStart: String?, @Argument preferredEnd: String?
    ): ScheduleResult=scheduleSafe{ss.getFillerByAttributes(delivery,attributes,courses,campus,term,preferredStart,preferredEnd)}

}