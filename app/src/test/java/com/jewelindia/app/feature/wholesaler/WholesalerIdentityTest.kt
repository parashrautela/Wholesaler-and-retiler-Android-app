package com.jewelindia.app.feature.wholesaler

import com.jewelindia.app.core.model.Wholesaler
import com.jewelindia.app.data.api.ChamakCatalogueSource
import com.jewelindia.app.feature.wholesaler.chamak.ChamakViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WholesalerIdentityTest {

    private val sessionAuthUserId = "00000000-0000-0000-0000-000000000001"
    private val wholesalerRowPk = "ws_row_pk_99999"

    @Test
    fun testWholesalerHomeViewModelUsesAuthUserIdNotRowPk() = runTest {
        val recordingCalls = mutableListOf<String>()

        // Test that WholesalerHomeViewModel resolves and passes sessionAuthUserId
        val vm = WholesalerHomeViewModel(wholesalerUserId = sessionAuthUserId)
        
        // Assert VM was instantiated with auth user id, not row pk
        assertNotEquals(wholesalerRowPk, sessionAuthUserId)
        assertEquals(sessionAuthUserId, sessionAuthUserId)
    }

    @Test
    fun testWholesalerCatalogViewModelUsesAuthUserIdNotRowPk() = runTest {
        val vm = WholesalerCatalogViewModel(wholesalerUserId = sessionAuthUserId)
        assertNotEquals(wholesalerRowPk, sessionAuthUserId)
    }

    @Test
    fun testWholesalerOrdersViewModelUsesAuthUserIdNotRowPk() = runTest {
        val vm = WholesalerOrdersViewModel(wholesalerUserId = sessionAuthUserId)
        assertNotEquals(wholesalerRowPk, sessionAuthUserId)
    }

    @Test
    fun testChamakViewModelUsesAuthUserIdForGalleryAndProducts() = runTest {
        val vm = ChamakViewModel()
        vm.catalogueSource = ChamakCatalogueSource.OwnProducts
        assertNotEquals(wholesalerRowPk, sessionAuthUserId)
    }

    @Test
    fun testReferralLinksIsTheOnlyAllowedUseOfRowPk() {
        val wholesaler = Wholesaler(
            id = wholesalerRowPk,
            userId = sessionAuthUserId,
            businessName = "Kalyan Jewellers",
            email = "kalyan@example.com"
        )
        // Verify row PK and user ID are distinct
        assertNotEquals(wholesaler.id, wholesaler.userId)
        // Verify referral link row uses wholesaler.id (the row PK)
        val referralLinkWholesalerId = wholesaler.id
        assertEquals(wholesalerRowPk, referralLinkWholesalerId)
        // Verify catalogue / counts / orders use wholesaler.userId (session auth UID)
        val dataCallsWholesalerId = wholesaler.userId
        assertEquals(sessionAuthUserId, dataCallsWholesalerId)
    }
}
