// Port of: wholesaler ios/JewelIndia/Sources/Networking/SSRSessionBridge.swift
package com.jewelindia.app.data.net

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Cookie
import okhttp3.Headers
import okhttp3.HttpUrl
import java.io.ByteArrayOutputStream
import java.util.Base64

/**
 * Extracts a Supabase session from the cookies that `@supabase/ssr` sets on a
 * Next.js response. Pure Kotlin (no android.net.Uri, no android.util.Base64),
 * unit-testable on JVM.
 */
object SsrSessionBridge {

    data class Tokens(
        val accessToken: String,
        val refreshToken: String
    )

    fun tokens(headers: Headers, url: HttpUrl, projectRef: String): Tokens? {
        val cookies = Cookie.parseAll(url, headers)
        return tokens(cookies, projectRef)
    }

    fun tokens(cookies: List<Cookie>, projectRef: String): Tokens? {
        val base = "sb-$projectRef-auth-token"

        // Unchunked first
        val single = cookies.firstOrNull { it.name == base }
        if (single != null) {
            val tokens = decode(single.value)
            if (tokens != null) return tokens
        }

        // Chunked: <base>.0, <base>.1, ... concatenated in numeric order
        val chunks = cookies.mapNotNull { cookie ->
            if (cookie.name.startsWith("$base.")) {
                val indexStr = cookie.name.substring(base.length + 1)
                val index = indexStr.toIntOrNull()
                if (index != null) index to cookie.value else null
            } else null
        }

        if (chunks.isEmpty()) return null
        val joined = chunks.sortedBy { it.first }.joinToString("") { it.second }
        return decode(joined)
    }

    /**
     * Decodes one assembled cookie value into tokens.
     */
    fun decode(rawValue: String): Tokens? {
        var value = percentDecode(rawValue)

        if (value.startsWith("base64-")) {
            value = value.substring("base64-".length)
            val bytes = base64UrlDecode(value) ?: return null
            value = bytes.toString(Charsets.UTF_8)
        }

        return parseJson(value)
    }

    /**
     * Custom percent decoder that leaves '+' intact, avoiding URLDecoder's
     * conversion of '+' into a space.
     */
    fun percentDecode(input: String): String {
        val out = ByteArrayOutputStream()
        var i = 0
        while (i < input.length) {
            val c = input[i]
            if (c == '%' && i + 2 < input.length) {
                val hex = input.substring(i + 1, i + 3)
                val byteVal = hex.toIntOrNull(16)
                if (byteVal != null) {
                    out.write(byteVal)
                    i += 3
                    continue
                }
            }
            out.write(c.code)
            i++
        }
        return out.toByteArray().toString(Charsets.UTF_8)
    }

    internal fun base64UrlDecode(input: String): ByteArray? {
        var s = input.replace('-', '+').replace('_', '/')
        val remainder = s.length % 4
        if (remainder > 0) {
            s += "=".repeat(4 - remainder)
        }
        return try {
            Base64.getDecoder().decode(s)
        } catch (_: Exception) {
            null
        }
    }

    internal fun parseJson(jsonString: String): Tokens? {
        return try {
            val element = Json.parseToJsonElement(jsonString)
            when (element) {
                is JsonObject -> {
                    val access = element["access_token"]?.jsonPrimitive?.content
                    val refresh = element["refresh_token"]?.jsonPrimitive?.content
                    if (!access.isNullOrBlank() && !refresh.isNullOrBlank()) {
                        Tokens(accessToken = access, refreshToken = refresh)
                    } else null
                }
                is JsonArray -> {
                    if (element.size >= 2) {
                        val access = element[0].jsonPrimitive.content
                        val refresh = element[1].jsonPrimitive.content
                        if (access.isNotBlank() && refresh.isNotBlank()) {
                            Tokens(accessToken = access, refreshToken = refresh)
                        } else null
                    } else null
                }
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}
