package com.jewelindia.app.core.images

import android.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.storage.storage
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import kotlin.time.Duration.Companion.hours

/** Refresh private links without exposing a bucket or changing access policies. */
object StorageImageURL {
    suspend fun resolve(value: String, defaultBucket: String? = null): String {
        val http = value.toHttpUrlOrNull()
        var reference = value.trimStart('/')
        if (http != null) {
            val ownHost = AppConfig.supabaseURL.toHttpUrlOrNull()?.host
            if (http.host != ownHost) return value
            val segments = http.pathSegments
            if (segments.take(3) != listOf("storage", "v1", "object") || segments.size < 6) return value
            if (segments[3] == "public") return value
            // Keep usable signed URLs stable so Coil can reuse its memory/disk cache.
            if (segments[3] == "sign") {
                val expiry = runCatching {
                    val payload = http.queryParameter("token")?.split('.')?.getOrNull(1) ?: return@runCatching null
                    val json = String(Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP), Charsets.UTF_8)
                    Json.parseToJsonElement(json).jsonObject["exp"]?.jsonPrimitive?.longOrNull
                }.getOrNull()
                if (expiry != null && expiry > System.currentTimeMillis() / 1000 + 60) return value
            }
            reference = segments.drop(4).joinToString("/")
        }
        val bucket = reference.substringBefore('/')
        val known = bucket in setOf("plant-images", "chamak-outputs")
        val selectedBucket = if (known) bucket else defaultBucket ?: return value
        val objectPath = if (known) reference.substringAfter('/') else reference
        require(objectPath.isNotBlank() && !objectPath.startsWith("http")) { "Image reference is invalid." }
        return JewelSupabase.client.storage.from(selectedBucket).createSignedUrl(objectPath, expiresIn = 1.hours)
    }
}
