package com.jewelindia.app.data.api

import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.core.time.JewelDates
import com.jewelindia.app.data.supabase.JewelJson
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * Status derived by the client per CONTRACT.md §3.2.
 */
enum class CatalogueShareStatus {
    ACTIVE,
    REVOKED,
    EXPIRED,
    FULL;

    val displayLabel: String
        get() = when (this) {
            ACTIVE -> Copy.CatalogueShare.statusActive
            REVOKED -> Copy.CatalogueShare.statusRevoked
            EXPIRED -> Copy.CatalogueShare.statusExpired
            FULL -> Copy.CatalogueShare.statusFull
        }

    val canRevoke: Boolean
        get() = this == ACTIVE
}

/**
 * Catalogue share entity matching CONTRACT.md §3.1 and §3.2.
 */
@Serializable
data class CatalogueShare(
    val id: String,
    val title: String? = null,
    @SerialName("item_count") val itemCount: Int = 0,
    @SerialName("max_viewers") val maxViewers: Int,
    @SerialName("views_used") val viewsUsed: Int = 0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("revoked_at") val revokedAt: String? = null,
    @SerialName("link_path") val linkPath: String? = null
) {
    /**
     * Complete URL built with AppConfig.siteURL and validated against CONTRACT §2 regex:
     * ^/share/catalogue#[a-f0-9]{64}$
     */
    val url: String?
        get() {
            val path = linkPath ?: return null
            if (!Regex("""^/share/catalogue#[a-f0-9]{64}${'$'}""").matches(path)) return null
            val base = AppConfig.siteURL.trimEnd('/')
            return "$base$path"
        }

    /**
     * Parsed Instant of expiresAt.
     */
    val expiry: Instant?
        get() = JewelDates.parse(expiresAt)

    /**
     * Derives status matching CONTRACT.md §3.2:
     * revoked_at != null -> Revoked
     * else expires_at <= now -> Expired
     * else views_used >= max_viewers -> All slots used
     * else Active
     */
    fun status(now: Instant = Instant.now()): CatalogueShareStatus {
        if (revokedAt != null) return CatalogueShareStatus.REVOKED
        val exp = expiry
        if (exp != null && !exp.isAfter(now)) return CatalogueShareStatus.EXPIRED
        if (viewsUsed >= maxViewers) return CatalogueShareStatus.FULL
        return CatalogueShareStatus.ACTIVE
    }

    /**
     * Formats expiry date for display in ready and list views.
     */
    fun formattedExpiry(): String {
        val instant = expiry ?: return expiresAt
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", JewelDates.indiaLocale)
            .withZone(JewelDates.indiaZone)
        return formatter.format(instant)
    }
}

class CatalogueShareException(
    override val message: String,
    val errorCode: String? = null,
    val httpStatusCode: Int = 0
) : RuntimeException(message)

/**
 * Parses API error response according to CONTRACT.md §3:
 * - Every error body is { "ok": false, "error": "<CODE>", "message": "<user-facing text>" }. Apps show message as-is.
 * - On 404 with HTML body (route not deployed yet): "Catalogue sharing isn't available on the server yet. Please try again later."
 * - Fallback when not JSON or no message: "Sharing is temporarily unavailable. Please try again."
 */
fun parseCatalogueShareError(statusCode: Int, body: String): CatalogueShareException {
    val trimmed = body.trim()
    if (statusCode == 404) {
        val isHtml = trimmed.startsWith("<!DOCTYPE", ignoreCase = true) ||
                trimmed.startsWith("<html", ignoreCase = true) ||
                trimmed.contains("<body", ignoreCase = true) ||
                !trimmed.startsWith("{")
        if (isHtml) {
            return CatalogueShareException(
                message = "Catalogue sharing isn't available on the server yet. Please try again later.",
                httpStatusCode = 404
            )
        }
    }

    return try {
        val json = JewelJson.parseToJsonElement(body).jsonObject
        val msg = json["message"]?.jsonPrimitive?.content
        val err = json["error"]?.jsonPrimitive?.content
        if (!msg.isNullOrBlank()) {
            CatalogueShareException(message = msg, errorCode = err, httpStatusCode = statusCode)
        } else {
            CatalogueShareException(
                message = "Sharing is temporarily unavailable. Please try again.",
                errorCode = err,
                httpStatusCode = statusCode
            )
        }
    } catch (_: Exception) {
        if (statusCode == 404) {
            CatalogueShareException(
                message = "Catalogue sharing isn't available on the server yet. Please try again later.",
                httpStatusCode = 404
            )
        } else {
            CatalogueShareException(
                message = "Sharing is temporarily unavailable. Please try again.",
                httpStatusCode = statusCode
            )
        }
    }
}
