package com.jewelindia.app.core.session

import android.content.Context
import android.content.SharedPreferences
import com.jewelindia.app.AppGraph
import com.jewelindia.app.core.model.ViewMode
import com.jewelindia.app.data.api.LikeBook
import com.jewelindia.app.feature.retailer.MarketplaceCatalogueStore
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.lang.reflect.Proxy

class SignOutCleanupTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun testViewModeStoreClear() {
        val fakePrefs = FakeSharedPreferences()
        val store = ViewModeStore(fakePrefs)

        store.set(ViewMode.employee, "user_123")
        assertEquals(ViewMode.employee, store.mode("user_123"))

        store.clear()
        // Should fall back to default retailer mode after clear
        assertEquals(ViewMode.retailer, store.mode("user_123"))
    }

    @Test
    fun testMarketplaceCatalogueStoreClear() {
        val rootDir = tempFolder.newFolder("app_files")
        val cacheDir = File(rootDir, "MarketplaceCatalogueCache").apply { mkdirs() }
        val testFile = File(cacheDir, "user_123.json").apply { writeText("{}") }

        assertTrue(testFile.exists())

        val context = object : android.content.ContextWrapper(null) {
            override fun getFilesDir(): File = rootDir
        }

        MarketplaceCatalogueStore.clear(context)
        assertFalse(testFile.exists())
        assertFalse(cacheDir.exists())
    }

    @Test
    fun testLikeBookClear() {
        LikeBook.clear()
        assertFalse(LikeBook.isLiked("prod_1"))
    }

    @Test
    fun testPerformSignOutCleanupOrchestration() {
        val rootDir = tempFolder.newFolder("app_files_orch")
        val cacheDir = File(rootDir, "MarketplaceCatalogueCache").apply { mkdirs() }
        val testFile = File(cacheDir, "user_456.json").apply { writeText("{}") }

        val fakePrefs = FakeSharedPreferences()
        val viewModeStore = ViewModeStore(fakePrefs)
        viewModeStore.set(ViewMode.employee, "user_456")

        val context = object : android.content.ContextWrapper(null) {
            override fun getFilesDir(): File = rootDir
        }

        AppGraph.performSignOutCleanup(
            context = context,
            viewModeStore = viewModeStore,
            imageLoader = null
        )

        assertEquals(ViewMode.retailer, viewModeStore.mode("user_456"))
        assertFalse(testFile.exists())
        assertFalse(LikeBook.isLiked("any_prod"))
    }
}
