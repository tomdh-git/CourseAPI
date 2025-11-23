package com.example.courseapi.services.schedule

import com.example.courseapi.models.dto.schedule.FillerByAttributesInput
import com.example.courseapi.models.dto.schedule.ScheduleByCourseInput
import com.example.courseapi.models.schedule.Schedule
import com.example.courseapi.repos.field.FieldRepo
import com.example.courseapi.repos.schedule.ScheduleRepo
import com.example.courseapi.services.utils.schedule.validateScheduleFields
import org.springframework.stereotype.Service

@Service
class ScheduleService(private val repo: ScheduleRepo, private val fields: FieldRepo){
    suspend fun getScheduleByCourses(input: ScheduleByCourseInput): List<Schedule> {
        validateScheduleFields(input, fields)
        return repo.getScheduleByCourses(input)
    }

    suspend fun getFillerByAttributes(input: FillerByAttributesInput): List<Schedule>{
        validateScheduleFields(input.toScheduleInput(),fields,attributes = input.attributes)
        return repo.getFillerByAttributes(input)
    }
}
