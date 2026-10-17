// Port of: wholesaler ios/JewelIndia/Sources/Features/Employee/EmployeeFilters.swift
package com.jewelindia.app.feature.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.data.api.CloudinaryArt
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography

data class EmployeeCategoryItem(
    val id: String,
    val title: String,
    val iconUrl: String? = null
)

object EmployeeCategories {
    val items = listOf(
        EmployeeCategoryItem("all", "All Designs"),
        EmployeeCategoryItem("necklace", "Necklaces", CloudinaryArt.url("v1778318369/emp_static1_ywv9ro.svg", 160)),
        EmployeeCategoryItem("earrings", "Earrings", CloudinaryArt.url("v1778318368/emp_static2_vijtdd.svg", 160)),
        EmployeeCategoryItem("bangles", "Bangles", CloudinaryArt.url("v1778318368/emp_static3_xdapmt.svg", 160)),
        EmployeeCategoryItem("rings", "Rings", CloudinaryArt.url("v1778318368/emp_static4_q0ysjt.svg", 160)),
        EmployeeCategoryItem("mangalsutra", "Mangalsutra"),
        EmployeeCategoryItem("pendant", "Pendants"),
        EmployeeCategoryItem("bridal", "Bridal Sets"),
        EmployeeCategoryItem("chain", "Chains"),
        EmployeeCategoryItem("haram", "Haram")
    )

    val purities = listOf("All", "22KT", "18KT", "14KT", "24KT")
    val stockOptions = listOf("All", "Ready to ship", "Made to order")
}

@Composable
fun EmployeeCategoryBar(
    selectedCategoryId: String,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(EmployeeCategories.items, key = { it.id }) { cat ->
            val isSelected = cat.id == selectedCategoryId

            Surface(
                onClick = { onSelectCategory(cat.id) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) Palette.goldPrimary else Palette.surfaceElevated,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) Palette.goldPrimary else Palette.surfaceBorder
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cat.iconUrl?.let { icon ->
                        CachedImage(
                            url = icon,
                            contentDescription = cat.title,
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Text(
                        text = cat.title,
                        style = Typography.santoshi(12.sp, if (isSelected) FontWeight.Bold else FontWeight.Medium),
                        color = if (isSelected) Color.White else Palette.textPrimary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EmployeeFilterRow(
    selectedPurity: String,
    onSelectPurity: (String) -> Unit,
    inStockOnly: Boolean,
    onToggleInStock: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // In Stock pill
        Surface(
            onClick = { onToggleInStock(!inStockOnly) },
            shape = RoundedCornerShape(16.dp),
            color = if (inStockOnly) Palette.goldPrimary.copy(alpha = 0.15f) else Palette.surfaceElevated,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (inStockOnly) Palette.goldPrimary else Palette.surfaceBorder
            )
        ) {
            Text(
                text = if (inStockOnly) "✓ Ready to Ship" else "Ready to Ship",
                style = Typography.santoshi(12.sp, if (inStockOnly) FontWeight.Bold else FontWeight.Medium),
                color = if (inStockOnly) Palette.goldPrimary else Palette.textSecondary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // Purity Pills
        EmployeeCategories.purities.forEach { purity ->
            val isSelected = purity == selectedPurity
            Surface(
                onClick = { onSelectPurity(purity) },
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) Palette.goldPrimary.copy(alpha = 0.15f) else Palette.surfaceElevated,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSelected) Palette.goldPrimary else Palette.surfaceBorder
                )
            ) {
                Text(
                    text = purity,
                    style = Typography.santoshi(12.sp, if (isSelected) FontWeight.Bold else FontWeight.Medium),
                    color = if (isSelected) Palette.goldPrimary else Palette.textSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
