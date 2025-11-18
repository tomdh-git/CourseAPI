package com.example.courseapi.services.schedule

import com.example.courseapi.models.input.ValidAttributes
import com.example.courseapi.models.input.ValidCampuses
import com.example.courseapi.models.schedule.Schedule
import com.example.courseapi.repos.schedule.ScheduleRepo
import org.springframework.stereotype.Service

@Service
class ScheduleService(private val repo: ScheduleRepo){
    suspend fun getScheduleByCourses(courses: List<String>, campus: List<String>, term: String, optimizeFreeTime: Boolean? = false, preferredStart: String?=null, preferredEnd: String?=null): List<Schedule> {
        if (campus.isEmpty() || !campus.all{it in ValidCampuses }) throw IllegalArgumentException("Campuses empty or invalid")
        if (term.isEmpty()) throw IllegalArgumentException("Term cannot be empty")
        if ((!preferredStart.isNullOrEmpty()&&preferredEnd.isNullOrEmpty())||(!preferredEnd.isNullOrEmpty() && preferredStart.isNullOrEmpty())) throw IllegalArgumentException("Preferred start and end fields must be specified together")
        if ((preferredStart!=null && preferredEnd!=null)&&repo.toMinutes(preferredStart)>=repo.toMinutes(preferredEnd)) throw IllegalArgumentException("Preferred start must be before preferred end")
        return repo.getScheduleByCourses(courses, campus, term, optimizeFreeTime, preferredStart, preferredEnd)
    }

    suspend fun getFillerByAttributes(attributes: List<String>, courses: List<String>, campus: List<String>, term: String, preferredStart: String? = null, preferredEnd: String? = null, ignoreWeb: Boolean? = false): List<Schedule>{
        if (campus.isEmpty() || !campus.all{it in ValidCampuses }) throw IllegalArgumentException("Campuses empty or invalid")
        if (term.isEmpty()) throw IllegalArgumentException("Term cannot be empty")
        if ((!preferredStart.isNullOrEmpty()&&preferredEnd.isNullOrEmpty())||(!preferredEnd.isNullOrEmpty() && preferredStart.isNullOrEmpty())) throw IllegalArgumentException("Preferred start and end fields must be specified together")
        if ((preferredStart!=null && preferredEnd!=null)&&repo.toMinutes(preferredStart)>=repo.toMinutes(preferredEnd)) throw IllegalArgumentException("Preferred start must be before preferred end")
        if (!((attributes.isNotEmpty() && attributes.all{it in ValidAttributes }))) throw IllegalArgumentException("Invalid attributes field")
        return repo.getFillerByAttributes(attributes, courses, campus, term, preferredStart, preferredEnd, ignoreWeb)
    }
}