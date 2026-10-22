package com.jewelindia.app.feature.manufacturing

import com.jewelindia.app.core.model.CreateManufacturingRequestParams
import com.jewelindia.app.data.api.ManufacturingApi
import com.jewelindia.app.data.supabase.JewelJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ManufacturingContractTest {

    private val testDispatcher = StandardTestDispatcher()

    private val jsonWithExplicitNulls = Json {
        explicitNulls = true
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createRequestParams_encodesExactJsonKeySetMatchingIos() {
        val params = CreateManufacturingRequestParams(
            assetId = "test-asset-uuid",
            category = "Ring",
            minWeightGrams = 10.0,
            maxWeightGrams = 15.0,
            material = "Gold",
            purity = "22KT",
            gemstonePreference = "Ruby",
            quantity = 2,
            makingBudgetMode = "per_gram",
            makingBudgetAmount = 450.0,
            currency = "INR",
            metalRateSnapshot = 7200.0,
            metalRateBasis = "MCX",
            deliveryNeededDate = "2026-11-01",
            notes = "Custom engraving inside band",
            supersededRequestId = "previous-req-uuid",
            quotationWindowHours = 24
        )

        val json = jsonWithExplicitNulls.encodeToString(params)
        val encodedKeys = (jsonWithExplicitNulls.parseToJsonElement(json).jsonObject.keys).sorted()

        val expectedKeys = listOf(
            "asset_id",
            "category",
            "currency",
            "delivery_needed_date",
            "gemstone_preference",
            "making_budget_amount",
            "making_budget_mode",
            "material",
            "max_weight_grams",
            "metal_rate_basis",
            "metal_rate_snapshot",
            "min_weight_grams",
            "notes",
            "purity",
            "quantity",
            "quotation_window_hours",
            "superseded_request_id"
        ).sorted()

        assertEquals(expectedKeys, encodedKeys)
    }

    @Test
    fun decodeCreateRequestResponse() {
        val responseJson = """
            {
                "ok": true,
                "request_id": "11111111-1111-1111-1111-111111111111",
                "state": "collecting",
                "active_offer_id": "22222222-2222-2222-2222-222222222222",
                "expires_at": "2026-10-20T12:00:00Z"
            }
        """.trimIndent()

        val decoded = JewelJson.decodeFromString<ManufacturingApi.CreateRequestResponse>(responseJson)
        assertTrue(decoded.ok)
        assertEquals("11111111-1111-1111-1111-111111111111", decoded.requestId)
        assertEquals("collecting", decoded.state)
        assertEquals("22222222-2222-2222-2222-222222222222", decoded.activeOfferId)
        assertEquals("2026-10-20T12:00:00Z", decoded.expiresAt)
    }

    @Test
    fun decodeAssetUploadResponse() {
        val responseJson = """
            {
                "ok": true,
                "asset_id": "33333333-3333-3333-3333-333333333333",
                "preview_url": "https://example.com/asset.jpg",
                "width": 800,
                "height": 600,
                "byte_size": 123456
            }
        """.trimIndent()

        val decoded = JewelJson.decodeFromString<ManufacturingApi.AssetUploadResponse>(responseJson)
        assertTrue(decoded.ok)
        assertEquals("33333333-3333-3333-3333-333333333333", decoded.assetId)
        assertEquals("https://example.com/asset.jpg", decoded.previewUrl)
        assertEquals(800, decoded.width)
        assertEquals(600, decoded.height)
        assertEquals(123456L, decoded.byteSize)
    }

    @Test
    fun acceptOfferPath_selectsQuotesForParallelAndAcceptOtherwise() {
        val offerId = "44444444-4444-4444-4444-444444444444"
        val parallelPath = ManufacturingApi.acceptOfferPath(offerId, parallel = true)
        val directPath = ManufacturingApi.acceptOfferPath(offerId, parallel = false)

        assertEquals("/api/wholesaler/manufacturing-offers/$offerId/quotes", parallelPath)
        assertEquals("/api/wholesaler/manufacturing-offers/$offerId/accept", directPath)
    }

    @Test
    fun decodeOfferWithParallelBroadcastMode_routesToQuotes() {
        val offerJson = """
        {
            "id": "offer-parallel-123",
            "request_id": "req-parallel-456",
            "rank": 1,
            "status": "active",
            "offered_at": "2026-10-22T00:00:00Z",
            "expires_at": "2026-10-23T00:00:00Z",
            "request": {
                "id": "req-parallel-456",
                "category": "Ring",
                "material": "Gold",
                "min_weight_grams": 5.0,
                "max_weight_grams": 10.0,
                "delivery_needed_date": "2026-11-01",
                "broadcast_mode": "parallel"
            }
        }
        """.trimIndent()

        val offer = JewelJson.decodeFromString(com.jewelindia.app.core.model.ManufacturingOffer.serializer(), offerJson)
        val isParallel = offer.request?.broadcastMode == "parallel"
        assertTrue(isParallel)

        val path = ManufacturingApi.acceptOfferPath(offer.id, parallel = isParallel)
        assertTrue(path.endsWith("/quotes"))
        assertEquals("/api/wholesaler/manufacturing-offers/offer-parallel-123/quotes", path)
    }
}
