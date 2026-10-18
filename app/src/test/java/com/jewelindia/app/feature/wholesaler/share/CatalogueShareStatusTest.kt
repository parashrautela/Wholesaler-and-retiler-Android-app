package com.jewelindia.app.feature.wholesaler.share

import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.data.api.CatalogueShare
import com.jewelindia.app.data.api.CatalogueShareStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class CatalogueShareStatusTest {

    private val now = Instant.parse("2026-10-18T12:00:00Z")
    private val futureExpiry = "2026-10-19T12:00:00Z"
    private val pastExpiry = "2026-10-17T12:00:00Z"

    @Test
    fun testActiveShare() {
        val share = CatalogueShare(
            id = "s-1",
            maxViewers = 5,
            viewsUsed = 2,
            expiresAt = futureExpiry,
            revokedAt = null,
            linkPath = null // Note: URL is null for fetched list items
        )
        val status = share.status(now)
        assertEquals(CatalogueShareStatus.ACTIVE, status)
        assertEquals(Copy.CatalogueShare.statusActive, status.displayLabel)
        assertTrue("Active shares can always be revoked even without a URL", status.canRevoke)
    }

    @Test
    fun testRevokedTakesPrecedenceOverExpiredAndFull() {
        val share = CatalogueShare(
            id = "s-2",
            maxViewers = 5,
            viewsUsed = 10,
            expiresAt = pastExpiry,
            revokedAt = "2026-10-17T10:00:00Z"
        )
        val status = share.status(now)
        assertEquals(CatalogueShareStatus.REVOKED, status)
        assertEquals(Copy.CatalogueShare.statusRevoked, status.displayLabel)
        assertFalse(status.canRevoke)
    }

    @Test
    fun testExpiredTakesPrecedenceOverFull() {
        val share = CatalogueShare(
            id = "s-3",
            maxViewers = 5,
            viewsUsed = 5,
            expiresAt = pastExpiry,
            revokedAt = null
        )
        val status = share.status(now)
        assertEquals(CatalogueShareStatus.EXPIRED, status)
        assertEquals(Copy.CatalogueShare.statusExpired, status.displayLabel)
        assertFalse(status.canRevoke)
    }

    @Test
    fun testAllSlotsUsedStatus() {
        val share = CatalogueShare(
            id = "s-4",
            maxViewers = 3,
            viewsUsed = 3,
            expiresAt = futureExpiry,
            revokedAt = null
        )
        val status = share.status(now)
        assertEquals(CatalogueShareStatus.FULL, status)
        assertEquals(Copy.CatalogueShare.statusFull, status.displayLabel)
        assertFalse(status.canRevoke)
    }

    @Test
    fun testRevokeAvailableWithoutLinkUrl() {
        // Critical finding A-1 verification: Listed links from GET /api/catalogue-shares
        // have null link_path, but revoke button must remain functional
        val share = CatalogueShare(
            id = "s-5",
            maxViewers = 10,
            viewsUsed = 0,
            expiresAt = futureExpiry,
            revokedAt = null,
            linkPath = null
        )
        assertEquals(null, share.url)
        assertEquals(CatalogueShareStatus.ACTIVE, share.status(now))
        assertTrue(share.status(now).canRevoke)
    }
}
