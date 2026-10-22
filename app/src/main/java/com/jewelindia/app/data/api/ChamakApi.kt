// Port of: wholesaler ios/JewelIndia/Sources/Networking/ChamakAPI.swift
package com.jewelindia.app.data.api

import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.core.consent.AIConsent
import com.jewelindia.app.core.model.Product
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.data.net.AuthToken
import com.jewelindia.app.data.net.HttpClients
import com.jewelindia.app.data.net.JewelNetwork
import com.jewelindia.app.data.supabase.JewelJson
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.CancellationException
import com.jewelindia.app.core.images.StorageImageURL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import kotlin.time.Duration.Companion.hours

class ChamakException(message: String) : RuntimeException(message)

class InsufficientCreditsException(
    val required: Int,
    val balance: Int,
    val shortBy: Int
) : RuntimeException("You need $shortBy more credits.")

object ChamakApi {
    private val db get() = JewelSupabase.client

    private suspend fun requireLiveSession(matchingWholesalerId: String) {
        val currentUserId = db.auth.currentUserOrNull()?.id
        if (currentUserId == null || !currentUserId.equals(matchingWholesalerId, ignoreCase = true)) {
            throw ChamakException("Your session isn't active on this device, so this can't be authorised. Please sign out and sign in again.")
        }
    }

    private suspend fun authorizedRequest(url: String, idempotencyKey: String? = null): Request.Builder {
        val token = AuthToken.fresh()

        val builder = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $token")

        if (idempotencyKey != null) {
            builder.header("Idempotency-Key", idempotencyKey)
        }
        return builder
    }

    // MARK: - Products for Picker

    suspend fun fetchWholesalerProducts(wholesalerId: String): List<Product> {
        val rows = db.from("products").select {
            filter {
                eq("wholesaler_id", wholesalerId)
            }
            order("created_at", order = Order.DESCENDING)
        }.decodeList<Product>()

        return rows.filter { product ->
            val url = product.displaySource
            !url.isNullOrEmpty()
        }
    }

    suspend fun fetchStoreProducts(): List<Product> {
        @Serializable
        data class Row(@SerialName("product_id") val productId: String)

        val shortlist = try {
            db.from("retailer_selections").select {}.decodeList<Row>()
        } catch (e: CancellationException) { throw e } catch (e: Exception) { throw ChamakException("Couldn't load your store designs. Please retry.") }

        val boards = try {
            db.from("customer_board_items").select {}.decodeList<Row>()
        } catch (e: CancellationException) { throw e } catch (e: Exception) { throw ChamakException("Couldn't load your store designs. Please retry.") }

        val ids = (shortlist + boards).map { it.productId }.toSet()
        if (ids.isEmpty()) return emptyList()

        val rows = db.from("products").select {
            filter {
                eq("is_published", true)
                isIn("id", ids.toList())
            }
            order("created_at", order = Order.DESCENDING)
        }.decodeList<Product>()

        return rows.filter { product ->
            val url = product.displaySource
            !url.isNullOrEmpty()
        }
    }

    // MARK: - Direct Source Upload

    suspend fun uploadSourceImage(
        wholesalerId: String,
        imageData: ByteArray,
        slot: Int,
        mode: ChamakMode = ChamakMode.Fusion
    ): String {
        AIConsent.requireGranted(wholesalerId)
        requireLiveSession(wholesalerId)
        val uid = wholesalerId.lowercase()
        val stamp = System.currentTimeMillis()
        val prefix = if (mode == ChamakMode.SetCreation) "setcreation" else "chamak"
        val path = "raw/$uid/${prefix}_${slot}_$stamp.jpg"
        return try {
            WholesalerApi.upload(
                bucket = "plant-images",
                path = path,
                data = imageData,
                contentType = "image/jpeg",
                upsert = false
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            throw ChamakException("Failed to upload source image $slot: ${e.message}")
        }
    }

    // MARK: - Stage 1: Create & Analyze

    @Serializable
    private data class CreateGenerationPayload(
        val id: String,
        @SerialName("wholesaler_id") val wholesalerId: String,
        @SerialName("source_image_1_url") val sourceImage1URL: String,
        @SerialName("source_image_2_url") val sourceImage2URL: String,
        @SerialName("source_image_3_url") val sourceImage3URL: String? = null,
        @SerialName("source_image_4_url") val sourceImage4URL: String? = null,
        val status: String,
        @SerialName("prompt_version") val promptVersion: String,
        val mode: String
    )

    suspend fun createGeneration(
        wholesalerId: String,
        source1URL: String,
        source2URL: String,
        extraSourceURLs: List<String> = emptyList(),
        mode: ChamakMode = ChamakMode.Fusion,
        generationId: String
    ): ChamakGeneration {
        AIConsent.requireGranted(wholesalerId)
        requireLiveSession(wholesalerId)

        val payload = CreateGenerationPayload(
            id = generationId,
            wholesalerId = wholesalerId.lowercase(),
            sourceImage1URL = StorageImageURL.resolve(source1URL),
            sourceImage2URL = StorageImageURL.resolve(source2URL),
            sourceImage3URL = extraSourceURLs.firstOrNull()?.let { StorageImageURL.resolve(it) },
            sourceImage4URL = extraSourceURLs.drop(1).firstOrNull()?.let { StorageImageURL.resolve(it) },
            status = "queued",
            promptVersion = "v1.0-chamak",
            mode = if (mode == ChamakMode.SetCreation) "set_creation" else "fusion"
        )

        return JewelNetwork.withRetry {
            try {
                db.from("chamak_generations").insert(payload) { select() }.decodeSingle<ChamakGeneration>()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // An accepted insert may lose its response. Re-read the stable ID; never overwrite a running row.
                try { fetchGeneration(generationId) }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { throw e }
            }
        }
    }

    @Serializable
    private data class SourceLinks(
        @SerialName("source_image_1_url") val first: String,
        @SerialName("source_image_2_url") val second: String,
        @SerialName("source_image_3_url") val third: String?,
        @SerialName("source_image_4_url") val fourth: String?
    )

    private suspend fun refreshSourceLinks(generationId: String, ownerId: String) {
        val generation = fetchGeneration(generationId)
        if (!generation.wholesalerId.equals(ownerId, ignoreCase = true)) {
            throw ChamakException("This generation doesn't belong to your account.")
        }
        val before = listOf(generation.sourceImage1URL, generation.sourceImage2URL, generation.sourceImage3URL, generation.sourceImage4URL)
        val after = before.map { it?.let { source -> StorageImageURL.resolve(source) } }
        if (before != after) {
            db.from("chamak_generations").update(
                SourceLinks(requireNotNull(after[0]), requireNotNull(after[1]), after[2], after[3])
            ) {
                filter { eq("id", generationId); eq("wholesaler_id", generation.wholesalerId) }
            }
        }
    }

    suspend fun triggerStage1Analysis(
        generationId: String,
        wholesalerId: String,
        idempotencyKey: String? = null
    ) = withContext(Dispatchers.IO) {
        AIConsent.requireGranted(wholesalerId)
        requireLiveSession(wholesalerId)
        refreshSourceLinks(generationId, wholesalerId)
        val url = "${AppConfig.aiPipelineURL.trimEnd('/')}/api/chamak/analyze"
        val request = authorizedRequest(url, idempotencyKey)
            .post(
                buildJsonObject {
                    put("generation_id", generationId)
                }.toString().toRequestBody("application/json".toMediaType())
            )
            .build()

        val response = try {
            HttpClients.pipeline.newCall(request).execute()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            throw ChamakException("Couldn't reach the analysis service. Check your connection and try again.")
        }

        response.use { resp ->
            val bodyString = resp.body.string()
            if (resp.isSuccessful) return@withContext

            if (resp.code == 402) {
                val detail = try {
                    Json.parseToJsonElement(bodyString).jsonObject["detail"]?.jsonObject
                } catch (_: Exception) { null }
                val required = detail?.get("required")?.jsonPrimitive?.intOrNull ?: 0
                val balance = detail?.get("balance")?.jsonPrimitive?.intOrNull ?: 0
                val shortBy = detail?.get("short_by")?.jsonPrimitive?.intOrNull ?: maxOf(0, required - balance)
                throw InsufficientCreditsException(required, balance, shortBy)
            }

            if (resp.code == 401) {
                throw ChamakException("Your session has expired. Please sign out and sign in again.")
            }

            val message = try {
                val json = Json.parseToJsonElement(bodyString).jsonObject
                val detail = json["detail"]
                if (detail is JsonObject) {
                    detail["message"]?.jsonPrimitive?.content
                } else {
                    detail?.jsonPrimitive?.content ?: json["message"]?.jsonPrimitive?.content
                }
            } catch (_: Exception) { null } ?: "The analysis service didn't accept this request."

            throw ChamakException(message)
        }
    }

    suspend fun fetchGeneration(generationId: String): ChamakGeneration {
        return db.from("chamak_generations").select {
            filter {
                eq("id", generationId)
            }
            limit(1)
        }.decodeSingle<ChamakGeneration>()
    }

    suspend fun updateStatus(generationId: String, status: ChamakStatus) {
        @Serializable
        data class Patch(val status: String)

        val rawStatus = when (status) {
            ChamakStatus.Queued -> "queued"
            ChamakStatus.Analyzing -> "analyzing"
            ChamakStatus.AwaitingInput -> "awaiting_input"
            ChamakStatus.Generating -> "generating"
            ChamakStatus.Done -> "done"
            ChamakStatus.Failed -> "failed"
        }

        db.from("chamak_generations").update(Patch(rawStatus)) {
            filter {
                eq("id", generationId)
            }
        }
    }

    // MARK: - Stage 3 & 4: Submit Form and Generate

    suspend fun submitFormAndGenerate(
        generationId: String,
        wholesalerId: String,
        formInput: WholesalerFormInput,
        note: String?,
        idempotencyKey: String? = null
    ) = withContext(Dispatchers.IO) {
        AIConsent.requireGranted(wholesalerId)
        requireLiveSession(wholesalerId)
        refreshSourceLinks(generationId, wholesalerId)

        @Serializable
        data class Payload(
            @SerialName("wholesaler_form_json") val wholesalerFormJson: WholesalerFormInput,
            @SerialName("note_text") val noteText: String?,
            val status: String
        )

        JewelNetwork.withRetry {
            db.from("chamak_generations").update(
                Payload(formInput, note, "generating")
            ) {
                filter {
                    eq("id", generationId)
                }
            }
        }

        val url = "${AppConfig.aiPipelineURL.trimEnd('/')}/api/chamak/generate-v2"
        val request = authorizedRequest(url, idempotencyKey)
            .post(
                buildJsonObject {
                    put("generation_id", generationId)
                }.toString().toRequestBody("application/json".toMediaType())
            )
            .build()

        HttpClients.pipeline.newCall(request).execute().use { resp ->
            val bodyString = resp.body.string()
            if (resp.isSuccessful) return@withContext

            if (resp.code == 402) {
                val detail = try {
                    Json.parseToJsonElement(bodyString).jsonObject["detail"]?.jsonObject
                } catch (_: Exception) { null }
                val required = detail?.get("required")?.jsonPrimitive?.intOrNull ?: 0
                val balance = detail?.get("balance")?.jsonPrimitive?.intOrNull ?: 0
                val shortBy = detail?.get("short_by")?.jsonPrimitive?.intOrNull ?: maxOf(0, required - balance)
                throw InsufficientCreditsException(required, balance, shortBy)
            }

            if (resp.code == 401) {
                throw ChamakException("Your session has expired. Please sign out and sign in again.")
            }

            val message = try {
                val json = Json.parseToJsonElement(bodyString).jsonObject
                val detail = json["detail"]
                if (detail is JsonObject) {
                    detail["message"]?.jsonPrimitive?.content
                } else {
                    detail?.jsonPrimitive?.content ?: json["message"]?.jsonPrimitive?.content
                }
            } catch (_: Exception) { null } ?: "Chamak didn't accept this request."

            throw ChamakException(message)
        }
    }

    suspend fun submitSetAndGenerate(
        generationId: String,
        wholesalerId: String,
        backdrop: SetBackdrop,
        note: String?,
        idempotencyKey: String? = null
    ) = withContext(Dispatchers.IO) {
        AIConsent.requireGranted(wholesalerId)
        requireLiveSession(wholesalerId)
        refreshSourceLinks(generationId, wholesalerId)

        @Serializable
        data class Payload(
            @SerialName("wholesaler_form_json") val wholesalerFormJson: SetCreationInput,
            @SerialName("note_text") val noteText: String?,
            @SerialName("set_backdrop") val setBackdrop: String,
            val status: String
        )

        JewelNetwork.withRetry {
            db.from("chamak_generations").update(
                Payload(SetCreationInput(backdrop, note), note, backdrop.rawValue, "generating")
            ) {
                filter {
                    eq("id", generationId)
                }
            }
        }

        val url = "${AppConfig.aiPipelineURL.trimEnd('/')}/api/set-creation/generate"
        val request = authorizedRequest(url, idempotencyKey)
            .post(
                buildJsonObject {
                    put("generation_id", generationId)
                }.toString().toRequestBody("application/json".toMediaType())
            )
            .build()

        HttpClients.pipeline.newCall(request).execute().use { resp ->
            val bodyString = resp.body.string()
            if (resp.isSuccessful) return@withContext

            if (resp.code == 402) {
                val detail = try {
                    Json.parseToJsonElement(bodyString).jsonObject["detail"]?.jsonObject
                } catch (_: Exception) { null }
                val required = detail?.get("required")?.jsonPrimitive?.intOrNull ?: 0
                val balance = detail?.get("balance")?.jsonPrimitive?.intOrNull ?: 0
                val shortBy = detail?.get("short_by")?.jsonPrimitive?.intOrNull ?: maxOf(0, required - balance)
                throw InsufficientCreditsException(required, balance, shortBy)
            }

            if (resp.code == 401) {
                throw ChamakException("Your session has expired. Please sign out and sign in again.")
            }

            val message = try {
                val json = Json.parseToJsonElement(bodyString).jsonObject
                val detail = json["detail"]
                if (detail is JsonObject) {
                    detail["message"]?.jsonPrimitive?.content
                } else {
                    detail?.jsonPrimitive?.content ?: json["message"]?.jsonPrimitive?.content
                }
            } catch (_: Exception) { null } ?: "The set creation pipeline didn't accept this request."

            throw ChamakException(message)
        }
    }

    // MARK: - Signed Storage URLs

    suspend fun getSignedURL(path: String): String = StorageImageURL.resolve(path, "chamak-outputs")

    suspend fun getSignedURLs(paths: List<String>): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for (path in paths.toSet()) {
            try {
                result[path] = getSignedURL(path)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                // Other images can still be displayed; missing links remain retryable.
            }
        }
        return result
    }

    // MARK: - Feedback

    suspend fun submitFeedback(
        generationId: String,
        satisfied: Boolean,
        whatWentWrong: String?
    ) {
        val payload = ChamakFeedback(
            chamakGenerationId = generationId,
            satisfied = satisfied,
            whatWentWrongText = whatWentWrong
        )
        JewelNetwork.withRetry {
            db.from("chamak_feedback").insert(payload)
        }
    }

    // MARK: - Gallery

    suspend fun fetchWholesalerGallery(wholesalerId: String): List<ChamakGeneration> {
        val rows = db.from("chamak_generations").select {
            filter {
                eq("wholesaler_id", wholesalerId)
            }
            order("created_at", order = Order.DESCENDING)
        }.decodeList<ChamakGeneration>()

        if (rows.isEmpty()) {
            val sessionUserId = db.auth.currentUserOrNull()?.id
            if (sessionUserId == null || !sessionUserId.equals(wholesalerId, ignoreCase = true)) {
                throw ChamakException("Your session isn't active on this device, so your gallery can't be read. Please sign out and sign in again.")
            }
        }

        return rows
    }
}
