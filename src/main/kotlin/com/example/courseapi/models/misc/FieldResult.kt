package com.example.courseapi.models.misc

sealed interface FieldResult
data class SuccessField(
    val terms: List<Field>
): FieldResult
data class ErrorField(
    val error: String = "",
    val message: String? = ""
): FieldResult