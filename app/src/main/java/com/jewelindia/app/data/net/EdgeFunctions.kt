// Port of: wholesaler ios edge functions invoke pattern
package com.jewelindia.app.data.net

import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.data.supabase.JewelJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

object EdgeFunctions {
    suspend inline fun <reified T : Any> invoke(
        name: String,
        body: Any? = null,
        token: String? = null,
        fallbackErrorMessage: String = "Operation failed. Please try again."
    ): T = withContext(Dispatchers.IO) {
        val authToken = token ?: AuthToken.fresh()
        val url = "${AppConfig.supabaseURL.trimEnd('/')}/functions/v1/$name"

        val jsonBody = when (body) {
            null -> "{}"
            is String -> body
            is JsonObject -> body.toString()
            else -> JewelJson.encodeToString(body)
        }

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $authToken")
            .addHeader("apikey", AppConfig.supabaseAnonKey)
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        HttpClients.plain.newCall(request).execute().use { response ->
            val bodyString = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val errorMsg = try {
                    val elem = Json.parseToJsonElement(bodyString).jsonObject
                    elem["message"]?.jsonPrimitive?.content
                        ?: elem["error"]?.jsonPrimitive?.content
                        ?: fallbackErrorMessage
                } catch (_: Exception) {
                    if (bodyString.isNotBlank() && bodyString.length < 200) bodyString else fallbackErrorMessage
                }
                throw ApiError(status = response.code, message = errorMsg)
            }

            if (T::class == String::class) {
                bodyString as T
            } else if (T::class == JsonObject::class) {
                Json.parseToJsonElement(bodyString).jsonObject as T
            } else {
                JewelJson.decodeFromString<T>(bodyString)
            }
        }
    }
}
