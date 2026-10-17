// Port of: wholesaler ios/JewelIndia/Sources/Networking/WishlistAPI.swift (Models)
package com.jewelindia.app.data.api

import com.jewelindia.app.core.config.AppConfig
import com.jewelindia.app.core.model.Product
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StoreCustomer(
    val id: String,
    val name: String,
    val phone: String? = null,
    val note: String? = null,
    @SerialName("customer_boards") val boards: List<CustomerBoardContainer> = emptyList()
) {
    val designCount: Int
        get() = boards.flatMap { it.items.map { item -> item.productId } }.toSet().size

    @Serializable
    data class CustomerBoardContainer(
        @SerialName("customer_board_items") val items: List<CustomerBoardItem> = emptyList()
    )

    @Serializable
    data class CustomerBoardItem(
        @SerialName("product_id") val productId: String
    )
}

data class CustomerBoard(
    val id: String,
    val title: String,
    var products: List<Product> = emptyList()
)

@Serializable
data class WishlistShare(
    val id: String,
    @SerialName("max_viewers") val maxViewers: Int,
    @SerialName("views_used") val viewsUsed: Int,
    @SerialName("expires_at") val expiresAt: String,
    @SerialName("revoked_at") val revokedAt: String? = null,
    @SerialName("link_path") val linkPath: String? = null
) {
    val url: String?
        get() = linkPath?.let { path ->
            val base = AppConfig.siteURL.trimEnd('/')
            val relative = path.trimStart('/')
            "$base/$relative"
        }
}
