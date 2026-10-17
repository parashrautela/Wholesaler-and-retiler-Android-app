// Port of: wholesaler ios/JewelIndia/Sources/Features/Wholesaler/InviteRetailerSheet.swift (InviteRetailerCard)
package com.jewelindia.app.feature.wholesaler

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.R
import com.jewelindia.app.design.Typography

@Composable
fun InviteRetailerCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(98.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFFF1F2F3), Color(0xFFFED77D))
                )
            )
            .border(1.dp, Color(0xFFD0D0D0), RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
    ) {
        // Artwork on right edge
        Image(
            painter = painterResource(id = R.drawable.invite_card_artwork),
            contentDescription = null,
            contentScale = ContentScale.FillHeight,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
                .clip(
                    RoundedCornerShape(
                        topStart = 0.dp,
                        bottomStart = 0.dp,
                        topEnd = 22.dp,
                        bottomEnd = 22.dp
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_invite_user_plus),
                contentDescription = null,
                modifier = Modifier.size(54.dp, 55.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.width(175.dp)
            ) {
                Text(
                    text = "Invite retailers",
                    style = Typography.cirka(24.sp),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E2D2A)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Invite Retailer to join your\nnetwork on JewelIndia",
                    style = Typography.santoshi(14.sp),
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF949494),
                    lineHeight = 17.5.sp
                )
            }
        }
    }
}
