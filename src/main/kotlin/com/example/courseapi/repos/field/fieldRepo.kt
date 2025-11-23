package com.example.courseapi.repos.field

import com.example.courseapi.exceptions.APIException
import com.example.courseapi.models.misc.Field
import com.example.courseapi.services.course.ParseService
import com.example.courseapi.services.course.RequestService
import org.springframework.stereotype.Repository

@Repository
class FieldRepo(private val requests: RequestService, private val parse: ParseService){
    suspend fun getTerms(): List<Field>{
        val termsRaw = requests.getTokenResponse()
        if (termsRaw.isEmpty()) throw APIException("Empty terms")
        return parse.parseTerms(termsRaw)
    }
}
