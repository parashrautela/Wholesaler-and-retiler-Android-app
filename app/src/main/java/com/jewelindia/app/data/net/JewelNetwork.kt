// Port of: wholesaler ios/JewelIndia/Sources/Networking/NetworkResilience.swift
package com.jewelindia.app.data.net

import io.github.jan.supabase.exceptions.HttpRequestException
import kotlinx.coroutines.delay
import okhttp3.internal.http2.StreamResetException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLHandshakeException
import kotlin.math.max

object JewelNetwork {
    /**
     * Errors that mean "the pipe broke", as opposed to "the server said no".
     * Only these are worth retrying — a 4xx will fail identically every time.
     */
    fun isTransient(e: Throwable): Boolean {
        if (e is HttpRequestException) {
            val cause = e.cause
            if (cause != null && isTransient(cause)) return true
        }
        return when (e) {
            is SocketTimeoutException,
            is ConnectException,
            is UnknownHostException,
            is SSLHandshakeException,
            is InterruptedIOException,
            is StreamResetException -> true
            is IOException -> true
            else -> false
        }
    }

    /**
     * Runs [operation], retrying transient transport failures with backoff.
     */
    suspend fun <T> withRetry(
        attempts: Int = 3,
        initialDelayMs: Long = 400L,
        factor: Double = 2.0,
        operation: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var lastError: Throwable? = null

        for (attempt in 1..max(1, attempts)) {
            try {
                return operation()
            } catch (e: Throwable) {
                lastError = e
                if (!isTransient(e) || attempt >= attempts) {
                    throw e
                }
                delay(currentDelay)
                currentDelay = (currentDelay * factor).toLong()
            }
        }
        throw lastError ?: IOException("Network operation failed after $attempts retries")
    }
}
