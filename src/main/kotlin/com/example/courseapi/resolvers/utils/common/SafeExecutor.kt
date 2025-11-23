package com.example.courseapi.resolvers.utils.common

import com.example.courseapi.exceptions.*
import io.ktor.client.plugins.*
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.TimeoutCancellationException
import org.slf4j.LoggerFactory

suspend fun <T, R> safeExecute(
    action: suspend () -> T,
    wrap: (T) -> R,
    makeError: (String, String?) -> R
): R {
    val logger = LoggerFactory.getLogger("SafeExecutor")
    return try { wrap(action()) }
    catch (e: APIException) { logger.error("API Exception", e); makeError("API EXCEPTION", e.message) }
    catch (e: QueryException) { logger.error("Query Exception", e); makeError("QUERY EXCEPTION", e.message) }
    catch (e: IllegalArgumentException) { logger.error("Illegal Argument", e); makeError("ILLEGAL ARGUMENT EXCEPTION", e.message) }
    catch (e: TimeoutCancellationException) { logger.error("Timeout", e); makeError("TIMEOUT EXCEPTION", e.message) }
    catch (e: NullPointerException) { logger.error("Null Pointer", e); makeError("NULL POINTER EXCEPTION", e.message) }
    catch (e: ServerBusyException) { logger.error("Server Busy", e); makeError("SERVER BUSY EXCEPTION", e.message) }
    catch (e: Exception) {
        when(e) {
            is IOException, is ClientRequestException, is ServerResponseException -> { logger.error("Network", e); makeError("NETWORK EXCEPTION", e.message) }
            else -> { logger.error("Unknown", e); makeError("UNKNOWN EXCEPTION", e.message) }
        }
    }
}