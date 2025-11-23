package com.example.courseapi.models.field

data class ValidFields(
    val subjects: Set<String>,
    val campuses: Set<String>,
    val terms: Set<String>,
    val deliveryTypes: Set<String>,
    val levels: Set<String>,
    val days: Set<String>,
    val waitlistTypes: Set<String>,
    val attributes: Set<String>
)
