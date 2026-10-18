package com.jewelindia.app.feature.wholesaler

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jewelindia.app.design.JewelWeight
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.manrope

@Composable
internal fun ColumnScope.CatalogErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.WarningAmber,
            contentDescription = null,
            tint = Color(0xFFDC2626),
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = manrope(14f, JewelWeight.Medium),
            color = Palette.muted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Palette.cream)
                .clickable(onClick = onRetry)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Try Again",
                style = manrope(14f, JewelWeight.Semibold),
                color = Palette.dark
            )
        }
    }
}

@Composable
internal fun ColumnScope.CatalogEmptyView(
    hasFilter: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.GridView,
            contentDescription = null,
            tint = Palette.muted,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "No Products Found",
            style = manrope(16f, JewelWeight.Bold),
            color = Palette.dark
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (hasFilter) {
                "Try adjusting your search or category filter."
            } else {
                "Tap + above to upload your first jewellery design."
            },
            style = manrope(13f, JewelWeight.Regular),
            color = Palette.muted,
            textAlign = TextAlign.Center
        )
    }
}
