package com.example.courseapi.client

import com.example.courseapi.exceptions.ServerBusyException
import org.springframework.stereotype.Component
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

@Component
class RequestLimiter {
    private val semaphore = Semaphore(8)
    suspend fun <T> limit(block: suspend () -> T): T {
        if (!semaphore.tryAcquire(30, TimeUnit.SECONDS)) {
            throw ServerBusyException("Server is busy. Try again later.")
        }
        return try {
            block()
        } finally {
            semaphore.release()
        }
    }
}
