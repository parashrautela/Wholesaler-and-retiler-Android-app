package com.jewelindia.app.feature.wholesaler

import com.jewelindia.app.data.api.WholesalerApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReprocessPollTest {

    @Test
    fun testPollingSequencePendingToBackgroundRemovedToDone() = runTest {
        var callCount = 0
        val statuses = mutableListOf<String>()

        val fakeTick: suspend (String, Boolean) -> WholesalerApi.ReprocessPollTick = { id, bg ->
            callCount++
            when (callCount) {
                1 -> WholesalerApi.ReprocessPollTick.Pending(backgroundRemoved = false)
                2 -> WholesalerApi.ReprocessPollTick.Pending(backgroundRemoved = true)
                3 -> WholesalerApi.ReprocessPollTick.Done(listOf("https://cdn.example.com/render1.jpg", "https://cdn.example.com/render2.jpg"))
                else -> WholesalerApi.ReprocessPollTick.Done(emptyList())
            }
        }

        val results = pollReprocess(
            productId = "prod_123",
            onStatusChange = { statuses.add(it) },
            timeoutMillis = 300_000L,
            pollIntervalMillis = 5_000L,
            pollTickProvider = fakeTick
        )

        assertEquals(listOf("https://cdn.example.com/render1.jpg", "https://cdn.example.com/render2.jpg"), results)
        assertEquals(3, callCount)
        assertEquals(listOf("Background removed, enhancing..."), statuses)
    }

    @Test
    fun testPollingTimesOutAfter300Seconds() = runTest {
        val fakeTick: suspend (String, Boolean) -> WholesalerApi.ReprocessPollTick = { _, _ ->
            WholesalerApi.ReprocessPollTick.Pending(backgroundRemoved = false)
        }

        try {
            pollReprocess(
                productId = "prod_timeout",
                onStatusChange = {},
                timeoutMillis = 300_000L,
                pollIntervalMillis = 5_000L,
                pollTickProvider = fakeTick
            )
            fail("Expected pollReprocess to throw timeout exception")
        } catch (e: WholesalerApi.PipelineException) {
            assertTrue(e.message?.contains("Timed out waiting for processed images (5 min)") == true)
        }
    }

    @Test
    fun testPollingCancellationStopsExecution() = runTest {
        var callCount = 0
        val fakeTick: suspend (String, Boolean) -> WholesalerApi.ReprocessPollTick = { _, _ ->
            callCount++
            WholesalerApi.ReprocessPollTick.Pending(backgroundRemoved = false)
        }

        val job: Job = launch {
            pollReprocess(
                productId = "prod_cancel",
                onStatusChange = {},
                timeoutMillis = 300_000L,
                pollIntervalMillis = 5_000L,
                pollTickProvider = fakeTick
            )
        }

        runCurrent()
        advanceTimeBy(10_001L) // should have ticked ~2 times
        val callsBeforeCancel = callCount
        assertTrue(callsBeforeCancel >= 2)

        job.cancel()
        runCurrent()
        advanceTimeBy(30_000L) // advance more time

        // Call count must not have incremented after cancellation
        assertEquals(callsBeforeCancel, callCount)
    }
}
