package com.example.courseapi.client

import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import java.util.concurrent.LinkedBlockingQueue
import com.example.courseapi.exceptions.ServerBusyException
import java.util.concurrent.Semaphore

@Component
class RequestLimiter {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val queue = LinkedBlockingQueue<suspend () -> Unit>(50) // backlog capacity
    private val semaphore = Semaphore(5) // concurrent workers


    init {
        repeat(semaphore.availablePermits()) {
            scope.launch {
                while (isActive) {
                    val job = queue.take()
                    semaphore.acquire()
                    try {
                        job()
                    } finally {
                        semaphore.release()
                    }
                }
            }
        }
    }

    suspend fun <T> limit(block: suspend () -> T): T {
        val deferred = CompletableDeferred<T>()
        val task: suspend () -> Unit = {
            try {
                deferred.complete(block())
            } catch (e: Exception) {
                deferred.completeExceptionally(e)
            }
        }

        if (!queue.offer(task)) {
            throw ServerBusyException("Server is busy. Try again later.")
        }

        return deferred.await()
    }
}
