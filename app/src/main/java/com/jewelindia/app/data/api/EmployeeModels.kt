// Port of: wholesaler ios/JewelIndia/Sources/Networking/EmployeeAPI.swift (Models)
package com.jewelindia.app.data.api

import com.jewelindia.app.core.text.nilIfEmpty
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull

// MARK: - Employee Session

data class EmployeeSession(
    val identity: Identity,
    val retailerId: String,
    val isRetailer: Boolean,
    val storeName: String,
    val storeLogoURL: String?,
    val theme: EmployeeTheme
) {
    sealed interface Identity {
        data class Employee(val id: String) : Identity
        data object Store : Identity
    }

    val seedId: String
        get() = when (identity) {
            is Identity.Employee -> identity.id
            Identity.Store -> retailerId
        }

    val storageKey: String
        get() = "employee_orders_last_checked.$seedId"
}

// MARK: - Employee Theme

enum class EmployeeTheme(val rawValue: String) {
    Indian("indian"),
    Maharaja("maharaja");

    val heroBackground: String
        get() = when (this) {
            Indian -> CloudinaryArt.url("v1778318369/home_bg_ryyopk.svg", 1280)
            Maharaja -> CloudinaryArt.url("v1781085325/Maharaja_Theme_ymaqjt.svg", 1280)
        }

    fun detailBackground(landscape: Boolean): String = when (this) {
        Maharaja -> CloudinaryArt.url("v1781085326/Maharaja_theme_infoPage_zslwde.svg", 1280)
        Indian -> if (landscape) {
            CloudinaryArt.url("v1781118947/theme1_horizontal_hog6yy.svg", 1470)
        } else {
            CloudinaryArt.url("v1781118947/theme1_vertical_sclwlj.svg", 1280)
        }
    }

    companion object {
        const val LOCAL_KEY = "jewel_store_theme"

        fun normalized(raw: String?): EmployeeTheme =
            entries.find { it.rawValue.equals(raw, ignoreCase = true) } ?: Indian
    }
}

// MARK: - Cloudinary Art

object CloudinaryArt {
    fun url(asset: String, width: Int): String {
        return "https://res.cloudinary.com/dcs0vuzwg/image/upload/f_webp,w_${width},q_80/$asset"
    }

    val viewModeCard: String = url("v1778318370/home_fg_gtjkeu.svg", 900)
    const val jewelLogo: String = "https://res.cloudinary.com/dcs0vuzwg/image/upload/f_png,w_240/v1777013959/jewel_logo_rhgin9.svg"

    val collectionImages: List<String> = listOf(
        url("v1778318369/emp_static1_ywv9ro.svg", 800),
        url("v1778318368/emp_static2_vijtdd.svg", 800),
        url("v1778318368/emp_static3_xdapmt.svg", 800),
        url("v1778318368/emp_static4_q0ysjt.svg", 800)
    )
}

// MARK: - Retailer Design

/**
 * Custom serializer for Postgres `numeric` which may arrive as a number or string.
 */
@OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)
object FlexibleDoubleSerializer : KSerializer<Double?> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("FlexibleDouble", PrimitiveKind.DOUBLE)
    override fun serialize(encoder: Encoder, value: Double?) {
        if (value == null) encoder.encodeNull() else encoder.encodeDouble(value)
    }
    override fun deserialize(decoder: Decoder): Double? {
        val jsonDecoder = decoder as? JsonDecoder ?: return null
        val element = jsonDecoder.decodeJsonElement()
        if (element is JsonPrimitive) {
            return element.doubleOrNull ?: element.content.toDoubleOrNull()
        }
        return null
    }
}

@Serializable
data class RetailerDesign(
    val id: String,
    @SerialName("image_url") val imageURL: String? = null,
    val title: String? = null,
    val category: String? = null,
    val tags: List<String> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null,
    val size: String? = null,
    val purity: String? = null,
    @Serializable(with = FlexibleDoubleSerializer::class)
    @SerialName("net_weight") val netWeight: Double? = null,
    @Serializable(with = FlexibleDoubleSerializer::class)
    @SerialName("gross_weight") val grossWeight: Double? = null,
    @Serializable(with = FlexibleDoubleSerializer::class)
    @SerialName("stone_weight") val stoneWeight: Double? = null,
    val type: String? = null,
    @SerialName("style_aesthetic") val styleAesthetic: String? = null,
    @SerialName("is_in_stock") val isInStock: Boolean? = null,
    @SerialName("production_time_days") val productionTimeDays: Int? = null
) {
    val imageLink: String? get() = imageURL?.trim()?.nilIfEmpty
    val cardTitle: String get() = title?.trim()?.nilIfEmpty ?: "Untitled design"

    companion object {
        const val COLUMNS = "id, image_url, title, category, tags, is_archived, created_at, size, purity, net_weight, gross_weight, stone_weight, type, style_aesthetic, is_in_stock, production_time_days"
    }
}
