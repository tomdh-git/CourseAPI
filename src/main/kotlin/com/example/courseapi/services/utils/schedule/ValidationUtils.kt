package com.example.courseapi.services.utils.schedule

import com.example.courseapi.models.dto.schedule.ScheduleByCourseInput
import com.example.courseapi.repos.field.FieldRepo
import com.example.courseapi.repos.utils.schedule.toMinutes

suspend fun validateScheduleFields(input: ScheduleByCourseInput, fieldRepo: FieldRepo, attributes: List<String>?=null) {
    val fields = fieldRepo.getOrFetchValidFields()
    if (input.campus.isEmpty() || !input.campus.all { it in fields.campuses }) throw IllegalArgumentException("Campuses empty or invalid")
    if (input.term.isEmpty() || input.term !in fields.terms) throw IllegalArgumentException("Term is empty or invalid")
    if (!input.delivery.isNullOrEmpty() && !input.delivery.all { it in fields.deliveryTypes }) throw IllegalArgumentException("Delivery types invalid")
    if (!attributes.isNullOrEmpty() && !attributes.all { it in fields.attributes })  throw IllegalArgumentException("Attributes field invalid")
    if ((!input.preferredStart.isNullOrEmpty()&&input.preferredEnd.isNullOrEmpty())
        ||(!input.preferredEnd.isNullOrEmpty() && input.preferredStart.isNullOrEmpty())) throw IllegalArgumentException("Preferred start and end fields must be specified together")
    if ((input.preferredStart!=null && input.preferredEnd!=null)&&
        toMinutes(input.preferredStart)>=toMinutes(input.preferredEnd)) throw IllegalArgumentException("Preferred start must be before preferred end")
}