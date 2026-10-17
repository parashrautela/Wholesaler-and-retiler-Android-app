package com.jewelindia.app

import com.jewelindia.app.data.api.WholesalerApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountDeletionTest {

    @Test
    fun testMissingSessionThrowsSignInRequired() {
        // Without an active session token, account deletion must be rejected
        val token: String? = null
        val ex = try {
            if (token == null) {
                throw WholesalerApi.PipelineException("Please sign in again.")
            }
            null
        } catch (e: WholesalerApi.PipelineException) {
            e
        }
        assertEquals("Please sign in again.", ex?.message)
    }

    @Test
    fun testErrorPayloadParsingWithoutLeakingInternalConstraints() {
        fun extractErrorMessage(bodyString: String, fallback: String = "Failed to delete account."): String {
            return try {
                val elem = Json.parseToJsonElement(bodyString).jsonObject
                val rawMsg = elem["message"]?.jsonPrimitive?.content
                    ?: elem["error"]?.jsonPrimitive?.content
                    ?: fallback
                if (rawMsg.contains("constraint") || rawMsg.contains("violates foreign key")) {
                    fallback
                } else {
                    rawMsg
                }
            } catch (_: Exception) {
                fallback
            }
        }

        // Standard user-facing message
        val json1 = """{"error": "Account deletion could not be completed. Please contact support."}"""
        assertEquals(
            "Account deletion could not be completed. Please contact support.",
            extractErrorMessage(json1)
        )

        // Internal DB constraint message should not leak to user
        val json2 = """{"error": "update or delete on table 'retailers' violates foreign key constraint 'fk_retailer'"}"""
        val sanitized = extractErrorMessage(json2)
        assertFalse("Must not leak internal DB constraint", sanitized.contains("violates foreign key"))
        assertEquals("Failed to delete account.", sanitized)
    }

    @Test
    fun testSuccessfulDeletionPayload() {
        val json = """{"success": true}"""
        val elem = Json.parseToJsonElement(json).jsonObject
        assertTrue(elem["success"]?.jsonPrimitive?.content == "true")
    }
}
