package com.example.courseapi.services.field

import com.example.courseapi.exceptions.APIException
import com.example.courseapi.models.field.Field
import com.example.courseapi.repos.field.FieldRepo
import org.springframework.stereotype.Service

@Service
class FieldService(private val repo: FieldRepo){
    suspend fun getTerms(): List<Field>{
        val res = repo.getTerms()
        return res.ifEmpty{throw APIException("getTerms returning no terms") }
    }
}