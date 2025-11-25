package com.example.courseapi.repos.utils.schedule

import com.example.courseapi.models.course.Course
import java.util.concurrent.ConcurrentHashMap

/**
 * Cache for attribute course lists to avoid repeated API calls.
 * Uses a 5-minute timeout to prevent stale data.
 */
class AttributeCache {
    data class CacheKey(
        val campus: List<String>,
        val term: String?,
        val attributes: Set<String>?,
        val delivery: List<String>?
    )

    private data class CacheEntry(
        val timestamp: Long,
        val courses: List<Course>
    )

    private val cache = ConcurrentHashMap<CacheKey, CacheEntry>()
    private val cacheTimeout = 5 * 60 * 1000L // 5 minutes

    fun get(key: CacheKey): List<Course>? {
        val entry = cache[key] ?: return null
        val now = System.currentTimeMillis()

        return if (now - entry.timestamp < cacheTimeout) {
            entry.courses
        } else {
            cache.remove(key)
            null
        }
    }

    fun put(key: CacheKey, courses: List<Course>) {
        cache[key] = CacheEntry(System.currentTimeMillis(), courses)
    }

    fun size(): Int = cache.size
}