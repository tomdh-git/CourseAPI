package com.example.courseapi.repos.field

import com.example.courseapi.exceptions.APIException
import com.example.courseapi.models.field.Field
import com.example.courseapi.models.field.ValidFields
import com.example.courseapi.repos.utils.field.*
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

        val requestFresh = cached != null && now - cacheTimestamp < fieldsCacheTimeout
        if (requestFresh) return cached

        val html = requests.getCourseList()
        if (html.isEmpty()) throw APIException("Empty response when fetching valid fields")

        val allFields = parseAllFields(html)
        val fields = getValidFields(allFields)
        cachedValidFields = fields
        cacheTimestamp = now
        fields
    }
}
