package com.example.courseapi.resolvers

import com.example.courseapi.models.misc.FieldResult
import com.example.courseapi.resolvers.utils.field.fieldSafe
import com.example.courseapi.services.field.FieldService
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.CrossOrigin

@Controller
@CrossOrigin(origins = ["*"])
class FieldResolver(private val service: FieldService){
    @QueryMapping
    suspend fun getTerms(): FieldResult = fieldSafe{service.getTerms()}
}