// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/WholesalerHomeView.swift (HomeCards)
package com.jewelindia.app.feature.wholesaler

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.R
import com.jewelindia.app.design.JewelWeight
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import com.jewelindia.app.design.cirka
import com.jewelindia.app.design.components.SvgImage
import com.jewelindia.app.design.manrope

@Composable
fun HeroUploadCard(
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UploadDesignCard(onUploadClick = onUploadClick, modifier = modifier)
}

@Composable
fun UploadDesignCard(
    onUploadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(158.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFFFFBF2))
            .border(0.6.dp, Color(0xFFFAF1F1), RoundedCornerShape(14.dp))
            .clickable(onClick = onUploadClick)
    ) {
        // Decorative Artwork Layer
        Box(modifier = Modifier.fillMaxSize()) {
            // Left Hand
            Image(
                painter = painterResource(R.drawable.upload_design_left_hand),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 170.dp, height = 110.dp)
                    .offset(x = (-30).dp, y = 20.dp)
                    .rotate(-18f)
                    .alpha(0.54f),
                contentScale = ContentScale.Fit
            )

            // Wash banner at bottom
            SvgImage(
                name = "UploadDesignWash",
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter),
                contentScale = ContentScale.FillWidth
            )

            // Right Hand
            Image(
                painter = painterResource(R.drawable.upload_design_right_hand),
                contentDescription = null,
                modifier = Modifier
                    .size(width = 160.dp, height = 110.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = 10.dp)
                    .rotate(12f)
                    .alpha(0.54f),
                contentScale = ContentScale.Fit
            )

            // Coins
            Image(
                painter = painterResource(R.drawable.upload_design_coin),
                contentDescription = null,
                modifier = Modifier
                    .size(54.dp)
                    .offset(x = 10.dp, y = 105.dp)
                    .alpha(0.35f),
                contentScale = ContentScale.Fit
            )
            Image(
                painter = painterResource(R.drawable.upload_design_coin),
                contentDescription = null,
                modifier = Modifier
                    .size(54.dp)
                    .align(Alignment.TopCenter)
                    .offset(x = (-10).dp, y = (-20).dp)
                    .alpha(0.20f),
                contentScale = ContentScale.Fit
            )
            Image(
                painter = painterResource(R.drawable.upload_design_coin),
                contentDescription = null,
                modifier = Modifier
                    .size(54.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (-20).dp, y = (-10).dp)
                    .alpha(0.35f),
                contentScale = ContentScale.Fit
            )
        }

        // Foreground Content
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Upload New\nJewellery",
                style = cirka(24f, JewelWeight.Bold),
                color = Color(0xFF604C0D),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = "Reimagine Your Collection",
                style = manrope(10f, JewelWeight.Semibold),
                color = Color(0xFFA09D8A),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Upload Now dark capsule button
            Row(
                modifier = Modifier
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFF292826))
                    .border(0.5.dp, Color(0xFFE4CC8F), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                SvgImage(
                    name = "UploadDesignIcon",
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Upload Now",
                    style = manrope(14f, JewelWeight.Medium),
                    color = Color.White
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
    ChamakCard(onOpen = onClick, modifier = modifier)
}

@Composable
fun ChamakCard(
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(193.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFEAEAEA), Color(0xFFCDCDCD))
                )
            )
            .clickable(onClick = onOpen)
    ) {
        // Trailing Artwork
        Image(
            painter = painterResource(R.drawable.chamak_studio_artwork),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = 155.dp, height = 193.dp),
            contentScale = ContentScale.Fit
        )

        // Text Content
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp, end = 120.dp, top = 22.dp, bottom = 22.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Chamak Studio",
                    style = cirka(32f, JewelWeight.Bold),
                    color = Color(0xFF3E3E3E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Reimagine your jewellery.\nCreate something new.",
                    style = manrope(14f, JewelWeight.Semibold),
                    color = Color(0xFF494949).copy(alpha = 0.82f),
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Get started capsule
            Row(
                modifier = Modifier
                    .shadow(elevation = 2.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFB6B6B6), Color(0xFF9E9E9E))
                        )
                    )
                    .border(0.4.dp, Color(0xFFA0A0A0), CircleShape)
                    .padding(start = 14.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Get started",
                    style = manrope(14f, JewelWeight.Semibold),
                    color = Color.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    showsBadge: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.dp, Color(0xFFE5E5E5), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF374151),
                    modifier = Modifier.size(13.dp)
                )
                if (showsBadge) {
                    PingDot()
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = cirka(26f, JewelWeight.Medium),
                color = Color(0xFF111827),
                maxLines = 1
            )
            Text(
                text = title,
                style = manrope(11f, JewelWeight.Semibold),
                color = Color(0xFF374151),
                maxLines = 2,
                minLines = 2,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
fun PingDot(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "ping_dot")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ping_scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.75f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ping_alpha"
    )

    Box(
        modifier = modifier.size(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .background(Color(0xFFF87171), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color(0xFFEF4444), CircleShape)
        )
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
            style = Typography.cirka(14.sp),
            fontWeight = FontWeight.Bold,
            color = Palette.dark
        )
    }
}
