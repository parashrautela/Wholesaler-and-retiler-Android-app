// Port of: wholesaler ios/JewelIndia/Sources/Networking/StoreActivity.swift
package com.jewelindia.app.data.api

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

object StoreActivity {
    private val db get() = JewelSupabase.client
    private val scope = CoroutineScope(Dispatchers.IO)

    enum class Event(val rawValue: String) {
        DesignViewed("design.viewed"),
        DesignShortlisted("design.shortlisted"),
        DesignSavedToBoard("wishlist.design_saved"),
        CustomerAdded("customer.added"),
        OrderRequested("order.requested"),
        ImageExported("chamak.exported")
    }

    @Serializable
    private data class Row(
        val event: String,
        @SerialName("product_id") val productId: String? = null,
        @SerialName("customer_id") val customerId: String? = null
    )

    fun log(event: Event, productId: String? = null, customerId: String? = null) {
        scope.launch {
            try {
                db.from("store_events").insert(
                    Row(event = event.rawValue, productId = productId, customerId = customerId)
                )
            } catch (_: Exception) {
                // Drop silently
            }
        }
    }

    fun registerDevice(context: Context) {
        val deviceId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: return

        val pInfo = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (_: Exception) { null }
        val versionName = pInfo?.versionName ?: "1.0"
        val model = "${Build.MANUFACTURER} ${Build.MODEL}"

        scope.launch {
            try {
                db.postgrest.rpc(
                    "register_device",
                    buildJsonObject {
                        put("p_device_id", deviceId)
                        put("p_model", model)
                        put("p_app_version", versionName)
                    }
                )
            } catch (_: Exception) {
                // Drop silently
            }
        }
    }
}
