package com.example.courseapi.services.schedule

import com.example.courseapi.models.schedule.Schedule
import com.example.courseapi.repos.course.CourseRepo
import com.example.courseapi.repos.schedule.ScheduleRepo
import org.springframework.stereotype.Service

@Service
class ScheduleService(private val repo: ScheduleRepo, private val fields: CourseRepo){
    suspend fun getScheduleByCourses(delivery: List<String>?=null, courses: List<String>, campus: List<String>, term: String, optimizeFreeTime: Boolean? = false, preferredStart: String?=null, preferredEnd: String?=null): List<Schedule> {
        validateScheduleFields(delivery = delivery, campus = campus, term = term, preferredStart = preferredStart, preferredEnd = preferredEnd, repo = repo, fieldRepo = fields)
        return repo.getScheduleByCourses(delivery, courses, campus, term, optimizeFreeTime, preferredStart, preferredEnd)
    }

    suspend fun getFillerByAttributes(delivery: List<String>? = null, attributes: List<String>, courses: List<String>, campus: List<String>, term: String, preferredStart: String? = null, preferredEnd: String? = null): List<Schedule>{
        validateScheduleFields(delivery = delivery, attributes = attributes, campus = campus, term = term, preferredStart = preferredStart, preferredEnd = preferredEnd, repo = repo, fieldRepo = fields)
        return repo.getFillerByAttributes(delivery,attributes, courses, campus, term, preferredStart, preferredEnd)
    }
}

private suspend fun validateScheduleFields(delivery:List<String>?=null, attributes: List<String>? = null, campus:List<String>, term:String, preferredStart: String?=null, preferredEnd: String?=null, repo: ScheduleRepo, fieldRepo: CourseRepo) {
    val fields = fieldRepo.getOrFetchValidFields()
    if (campus.isEmpty() || !campus.all { it in fields.campuses }) throw IllegalArgumentException("Campuses empty or invalid")
    if (term.isEmpty() || term !in fields.terms) throw IllegalArgumentException("Term is empty or invalid")
    if (!delivery.isNullOrEmpty() && !delivery.all { it in fields.deliveryTypes }) throw IllegalArgumentException("Delivery types invalid")
    if (!attributes.isNullOrEmpty() && !attributes.all { it in fields.attributes })  throw IllegalArgumentException("Attributes field invalid")
    if ((!preferredStart.isNullOrEmpty()&&preferredEnd.isNullOrEmpty())||(!preferredEnd.isNullOrEmpty() && preferredStart.isNullOrEmpty())) throw IllegalArgumentException("Preferred start and end fields must be specified together")
    if ((preferredStart!=null && preferredEnd!=null)&&repo.toMinutes(preferredStart)>=repo.toMinutes(preferredEnd)) throw IllegalArgumentException("Preferred start must be before preferred end")
}
