package com.example.courseapi.services.course.utils

import com.example.courseapi.models.dto.course.CourseByInfoInput
import com.example.courseapi.repos.course.ValidFields

fun validate(input: CourseByInfoInput, fields: ValidFields){
    val subject = input.subject
    val courseNum = input.courseNum
    val campus = input.campus
    val attributes = input.attributes
    val delivery = input.delivery
    val term = input.term
    val openWaitlist = input.openWaitlist
    val level = input.level
    val daysFilter = input.daysFilter
    val startEndTime = input.startEndTime
    if (campus.isEmpty() || !campus.all { it in fields.campuses }) throw IllegalArgumentException("Campuses empty or invalid")
    if (term.isEmpty() || term !in fields.terms) throw IllegalArgumentException("Term is empty or invalid")
    if (!(subject.isNullOrEmpty() || subject.all { it in fields.subjects })) throw IllegalArgumentException("Invalid subjects field")
    if (!courseNum.isNullOrEmpty()) {
        if (subject.isNullOrEmpty()) { throw IllegalArgumentException("Course num is specified without a subject") }
        if (subject.size > 1) { throw IllegalArgumentException("Course num inputted with too many subjects") }
    }
    if (!attributes.isNullOrEmpty() && !attributes.all { it in fields.attributes })  throw IllegalArgumentException("Attributes field invalid")
    if (!delivery.isNullOrEmpty() && !delivery.all { it in fields.deliveryTypes }) throw IllegalArgumentException("Delivery types invalid")
    if (!startEndTime.isNullOrEmpty() && startEndTime.size != 2) throw IllegalArgumentException("StartEndTime empty or doesnt have size 2")
    if (!(openWaitlist.isNullOrEmpty() || (openWaitlist.isNotEmpty() && openWaitlist in fields.waitlistTypes))) throw IllegalArgumentException("Invalid openWaitlist field")
    if (!(level.isNullOrEmpty() || (level.isNotEmpty() && level in fields.levels))) throw IllegalArgumentException("Invalid level field")
    if (!(daysFilter.isNullOrEmpty() || (daysFilter.isNotEmpty() && daysFilter.all { it in fields.days }))) throw IllegalArgumentException("Invalid daysFilter field")
}