package com.jewelindia.app.data.api

import com.jewelindia.app.core.config.AppConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CatalogueShareApiTest {

    private val validHash = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"

    @Test
    fun testLinkPathRegexAcceptsExact64LowercaseHex() {
        val share = CatalogueShare(
            id = "share-1",
            maxViewers = 1,
            expiresAt = "2026-10-19T10:00:00Z",
            linkPath = "/share/catalogue#$validHash"
        )
        val expectedBase = AppConfig.siteURL.trimEnd('/')
        assertEquals("$expectedBase/share/catalogue#$validHash", share.url)
    }

    @Test
    fun testLinkPathRegexRejectsUppercaseHex() {
        val uppercaseHash = validHash.uppercase()
        val share = CatalogueShare(
            id = "share-2",
            maxViewers = 1,
            expiresAt = "2026-10-19T10:00:00Z",
            linkPath = "/share/catalogue#$uppercaseHash"
        )
        assertNull("Uppercase hex must be rejected by CONTRACT regex", share.url)
    }

    @Test
    fun testLinkPathRegexRejectsWrongLengthOrPath() {
        // 63 chars
        val shortShare = CatalogueShare(
            id = "s-short",
            maxViewers = 1,
            expiresAt = "2026-10-19T10:00:00Z",
            linkPath = "/share/catalogue#" + validHash.substring(0, 63)
        )
        assertNull(shortShare.url)

        // 65 chars
        val longShare = CatalogueShare(
            id = "s-long",
            maxViewers = 1,
            expiresAt = "2026-10-19T10:00:00Z",
            linkPath = "/share/catalogue#" + validHash + "a"
        )
        assertNull(longShare.url)

        // Wishlist path instead of catalogue
        val wishlistShare = CatalogueShare(
            id = "s-wishlist",
            maxViewers = 1,
            expiresAt = "2026-10-19T10:00:00Z",
            linkPath = "/share/wishlist#$validHash"
        )
        assertNull(wishlistShare.url)

        // Null linkPath
        val nullShare = CatalogueShare(
            id = "s-null",
            maxViewers = 1,
            expiresAt = "2026-10-19T10:00:00Z",
            linkPath = null
        )
        assertNull(nullShare.url)
    }

    @Test
    fun testErrorParseHtml404RouteNotDeployedFallback() {
        val html404 = """
            <!DOCTYPE html>
            <html lang="en">
            <head><title>404: This page could not be found.</title></head>
            <body><div class="error">404 - Not Found</div></body>
            </html>
        """.trimIndent()

        val parsed = parseCatalogueShareError(404, html404)
        assertEquals(
            "Catalogue sharing isn't available on the server yet. Please try again later.",
            parsed.message
        )
        assertEquals(404, parsed.httpStatusCode)
    }

    @Test
    fun testErrorParseMessagePassthrough() {
        val serverErrors = listOf(
            Pair(
                """{"ok":false,"error":"UNAUTHORIZED","message":"Please sign in again to share designs."}""",
                "Please sign in again to share designs."
            ),
            Pair(
                """{"ok":false,"error":"NOT_VERIFIED","message":"Sharing is available once your wholesaler account is verified."}""",
                "Sharing is available once your wholesaler account is verified."
            ),
            Pair(
                """{"ok":false,"error":"INVALID_SETTINGS","message":"Choose 1–100 viewers and a validity between 5 minutes and 7 days."}""",
                "Choose 1–100 viewers and a validity between 5 minutes and 7 days."
            ),
            Pair(
                """{"ok":false,"error":"INVALID_SELECTION","message":"Select 1–50 of your published designs."}""",
                "Select 1–50 of your published designs."
            ),
            Pair(
                """{"ok":false,"error":"TOO_MANY_LINKS","message":"Revoke an unused link before creating another."}""",
                "Revoke an unused link before creating another."
            ),
            Pair(
                """{"ok":false,"error":"RATE_LIMITED","message":"Too many attempts. Please wait a minute and try again."}""",
                "Too many attempts. Please wait a minute and try again."
            ),
            Pair(
                """{"ok":false,"error":"NOT_FOUND","message":"Link not found."}""",
                "Link not found."
            ),
            Pair(
                """{"ok":false,"error":"UNAVAILABLE","message":"Sharing is temporarily unavailable. Please try again."}""",
                "Sharing is temporarily unavailable. Please try again."
            )
        )

        for ((json, expectedMsg) in serverErrors) {
            val parsed = parseCatalogueShareError(400, json)
            assertEquals("Apps show server message as-is", expectedMsg, parsed.message)
        }
    }

    @Test
    fun testErrorParseFallbackWhenNotJsonOrNoMessage() {
        val nonJson = "Bad Gateway"
        val parsed502 = parseCatalogueShareError(502, nonJson)
        assertEquals("Sharing is temporarily unavailable. Please try again.", parsed502.message)

        val jsonNoMessage = """{"ok":false,"error":"SOMETHING_ELSE"}"""
        val parsedNoMsg = parseCatalogueShareError(500, jsonNoMessage)
        assertEquals("Sharing is temporarily unavailable. Please try again.", parsedNoMsg.message)
    }
}
