package com.example.courseapi.services.utils.course

import com.example.courseapi.models.dto.course.CourseByInfoInput
import com.example.courseapi.models.field.ValidFields

fun validateCourseFields(input: CourseByInfoInput, fields: ValidFields){
    if (input.campus.isEmpty() || !input.campus.all { it in fields.campuses }) throw IllegalArgumentException("Campuses empty or invalid")
    if (input.term.isEmpty() || input.term !in fields.terms) throw IllegalArgumentException("Term is empty or invalid")
    if (!(input.subject.isNullOrEmpty() || input.subject.all { it in fields.subjects })) throw IllegalArgumentException("Invalid subjects field")
    if (!input.courseNum.isNullOrEmpty()) {
        if (input.subject.isNullOrEmpty()) { throw IllegalArgumentException("Course num is specified without a subject") }
        if (input.subject.size > 1) { throw IllegalArgumentException("Course num inputted with too many subjects") }
    }
    if (!input.attributes.isNullOrEmpty() && !input.attributes.all { it in fields.attributes })  throw IllegalArgumentException("Attributes field invalid")
    if (!input.delivery.isNullOrEmpty() && !input.delivery.all { it in fields.deliveryTypes }) throw IllegalArgumentException("Delivery types invalid")
    if (!input.startEndTime.isNullOrEmpty() && input.startEndTime.size != 2) throw IllegalArgumentException("StartEndTime empty or doesnt have size 2")
    if (!(input.openWaitlist.isNullOrEmpty() || (input.openWaitlist.isNotEmpty() && input.openWaitlist in fields.waitlistTypes))) throw IllegalArgumentException("Invalid openWaitlist field")
    if (!(input.level.isNullOrEmpty() || (input.level.isNotEmpty() && input.level in fields.levels))) throw IllegalArgumentException("Invalid level field")
    if (!(input.daysFilter.isNullOrEmpty() || (input.daysFilter.isNotEmpty() && input.daysFilter.all { it in fields.days }))) throw IllegalArgumentException("Invalid daysFilter field")
}