package com.example.courseapi.repos.field

import com.example.courseapi.exceptions.APIException
import com.example.courseapi.models.field.Field
import com.example.courseapi.models.field.ValidFields
import com.example.courseapi.repos.utils.field.parseAllFields
import com.example.courseapi.repos.utils.field.parseTerms
import com.example.courseapi.services.utils.course.RequestUtils
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.springframework.stereotype.Repository

@Repository
class FieldRepo(private val requests: RequestUtils){
    @Volatile private var cachedValidFields: ValidFields? = null
    @Volatile private var cacheTimestamp: Long = 0
    private val fieldsCacheLock = Mutex()
    private val fieldsCacheTimeout = 3_600_000L

    suspend fun getTerms(): List<Field>{
        val termsRaw = requests.getCourseList()
        if (termsRaw.isEmpty()) throw APIException("Empty terms")
        return parseTerms(termsRaw)
    }

    suspend fun getOrFetchValidFields(): ValidFields = fieldsCacheLock.withLock {
        val now = System.currentTimeMillis()
        val cached = cachedValidFields
        if (cached != null && now - cacheTimestamp < fieldsCacheTimeout) return cached
        val html = requests.getCourseList()
        if (html.isEmpty()) throw APIException("Empty response when fetching valid fields")
        val allFields = parseAllFields(html)
        val subjects = allFields["subjects"]?.map { it.name }?.toSet() ?: emptySet()
        val campuses = allFields["campuses"]?.map { it.name }?.toSet() ?: emptySet()
        val terms = allFields["terms"]?.map { it.name }?.toSet() ?: emptySet()
        val deliveryTypes = allFields["delivery"]?.map { it.name }?.toSet() ?: emptySet()
        val levels = allFields["levels"]?.map { it.name }?.toSet() ?: emptySet()
        val days = allFields["days"]?.map { it.name }?.toSet() ?: emptySet()
        val waitlistTypes = allFields["waitlist"]?.map { it.name }?.toSet() ?: emptySet()
        val attributes = allFields["attributes"]?.map { it.name }?.toSet() ?: emptySet()
        val fields = ValidFields(
            subjects = subjects,
            campuses = campuses,
            terms = terms,
            deliveryTypes = deliveryTypes,
            levels = levels,
            days = days,
            waitlistTypes = waitlistTypes,
            attributes = attributes
        )
        cachedValidFields = fields
        cacheTimestamp = now
        fields
    }
}
