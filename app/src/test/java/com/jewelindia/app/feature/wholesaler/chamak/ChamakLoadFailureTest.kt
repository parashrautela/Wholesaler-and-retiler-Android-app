package com.jewelindia.app.feature.wholesaler.chamak

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChamakLoadFailureTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })
    }

    @After
    fun tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null)
        Dispatchers.resetMain()
    }

    @Test
    fun testLoadFailureDoesNotThrowAndSetsErrorMessage() = runTest(testDispatcher) {
        val vm = ChamakViewModel(testScope)
        // Calling load with an unauthenticated / invalid ID should trigger network/session refusal
        // and must NOT throw an uncaught exception out of the coroutine.
        try {
            vm.load("non-existent-or-invalid-id")
        } catch (e: Throwable) {
            org.junit.Assert.fail("vm.load threw an uncaught exception: ${e.message}")
        }

        assertFalse("isLoadingProducts should be reset to false in finally", vm.isLoadingProducts)
        assertTrue("catalogProducts should be empty on failure", vm.catalogProducts.isEmpty())
        assertTrue("galleryGenerations should be empty on failure", vm.galleryGenerations.isEmpty())
        assertNotNull("galleryErrorMessage should be set", vm.galleryErrorMessage)
        assertTrue(
            "galleryErrorMessage should match expected iOS copy",
            vm.galleryErrorMessage!!.contains("Couldn't load your gallery")
        )
    }

    @Test
    fun testRefreshGalleryFailureDoesNotThrow() = runTest(testDispatcher) {
        val vm = ChamakViewModel(testScope)
        try {
            vm.refreshGallery("non-existent-or-invalid-id")
        } catch (e: Throwable) {
            org.junit.Assert.fail("vm.refreshGallery threw an uncaught exception: ${e.message}")
        }

        assertFalse("isRefreshingGallery should be reset to false in finally", vm.isRefreshingGallery)
        assertTrue("galleryGenerations should be empty on failure", vm.galleryGenerations.isEmpty())
        assertNotNull("galleryErrorMessage should be set", vm.galleryErrorMessage)
        assertEquals(
            "Couldn't load your gallery. Check your connection and try again.",
            vm.galleryErrorMessage
        )
    }
}
