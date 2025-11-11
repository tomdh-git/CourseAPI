package com.example.courseapi.models.misc

sealed interface TermResult
data class SuccessTerm(
    val terms: List<Term>
): TermResult
data class ErrorTerm(
    val error: String = "",
    val message: String? = ""
): TermResult