package com.jewelindia.app.feature.retailer

import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.data.api.WishlistShare
import com.jewelindia.app.data.api.WishlistShareError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class WishlistShareTest {

    private fun share(path: String?): WishlistShare {
        return WishlistShare(
            id = "test",
            maxViewers = 1,
            viewsUsed = 0,
            expiresAt = "2026-10-09T00:00:00Z",
            revokedAt = null,
            linkPath = path
        )
    }

    @Test
    fun testTrustedHostLinkGeneration() {
        val token = "b".repeat(64)
        val path = "/share/wishlist#$token"
        val expected = AppConfig.siteURL.trimEnd('/') + path

        assertEquals(expected, share(path).url)
    }

    @Test
    fun testMalformedAndExternalLinksRejected() {
        val token = "b".repeat(64)
        val path = "/share/wishlist#$token"

        val invalidPaths = listOf(
            null,
            "",
            "/share/wishlist",
            "/share/wishlist#abc",
            "//evil.example/share/wishlist#$token",
            "https://evil.example$path",
            "$path?other=1",
            "$path\n",
            "/different#$token"
        )

        for (invalid in invalidPaths) {
            assertNull("Path '$invalid' should be rejected", share(invalid).url)
        }
    }

    @Test
    fun testExpiryDateParsing() {
        val token = "b".repeat(64)
        val path = "/share/wishlist#$token"
        val s = share(path)

        assertNotNull(s.expiry)
    }

    @Test
    fun testWishlistShareErrorMessages() {
        val errors = listOf(
            WishlistShareError.NotDeployed,
            WishlistShareError.InvalidLink,
            WishlistShareError.Unavailable
        )

        for (err in errors) {
            assertFalse(err.message.isNullOrEmpty())
        }
    }
}
