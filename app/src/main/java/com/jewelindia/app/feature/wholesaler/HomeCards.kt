// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerHomeView.swift (HomeCards)
package com.jewelindia.app.feature.wholesaler

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography

@Composable
fun HeroUploadCard(
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Palette.dark)
            .padding(22.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STUDIO PIPELINE",
                    style = Typography.santoshi(11.sp),
                    fontWeight = FontWeight.Bold,
                    color = Palette.goldPrimary
                )
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = Palette.goldPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Transform Photos with AI",
                style = Typography.cirka(22.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.light
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Upload jewellery photos to remove backgrounds, generate studio models, and publish directly to retailers.",
                style = Typography.santoshi(13.sp),
                color = Palette.muted
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onUploadClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Palette.goldPrimary),
                modifier = Modifier.fillMaxWidth().height(46.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Palette.light
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Upload New Design",
                    style = Typography.santoshi(14.sp),
                    fontWeight = FontWeight.Bold,
                    color = Palette.light
                )
            }
        }
    }
}

@Composable
fun ChamakPromoCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Palette.cream)
            .border(1.dp, Palette.border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Palette.goldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Palette.goldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Chamak Studio",
                        style = Typography.cirka(16.sp),
                        fontWeight = FontWeight.Bold,
                        color = Palette.dark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "AI set creation, model styling & exports",
                        style = Typography.santoshi(13.sp),
                        color = Palette.muted
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Palette.muted
            )
        }
    }
}

@Composable
fun KpiMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.cream)
            .border(1.dp, Palette.border, RoundedCornerShape(14.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = Typography.santoshi(11.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = Palette.muted
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Palette.goldPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = Typography.cirka(22.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark
            )
        }
    }
}

@Composable
fun CategoryGridItem(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Palette.cream)
            .border(1.dp, Palette.border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = Typography.santoshi(13.sp),
            fontWeight = FontWeight.SemiBold,
            color = Palette.dark
        )
    }
}
