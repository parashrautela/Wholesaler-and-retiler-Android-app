// Port of: wholesaler ios/JewelIndia/Sources/Features/Auth/RoleChoiceView.swift (UnreachableView)
package com.jewelindia.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.session.SessionStore
import com.jewelindia.app.core.text.Copy
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Spacing
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@Composable
fun UnreachableScreen(
    sessionStore: SessionStore,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var retrying by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(Palette.background)
            .padding(Spacing.xxl)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.base)
        ) {
            Icon(
                imageVector = Icons.Default.WifiOff,
                contentDescription = null,
                tint = Palette.muted,
                modifier = Modifier.size(48.dp)
            )

            Text(
                text = Copy.unreachableTitle,
                fontFamily = Typography.Cirka,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Palette.foreground,
                textAlign = TextAlign.Center
            )

            Text(
                text = Copy.unreachableBody,
                fontFamily = Typography.Manrope,
                fontSize = 14.sp,
                color = Palette.muted,
                textAlign = TextAlign.Center
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(top = Spacing.sm)
                    .clip(CircleShape)
                    .background(Palette.dark)
                    .clickable(enabled = !retrying) {
                        scope.launch {
                            retrying = true
                            sessionStore.refreshDestination()
                            retrying = false
                        }
                    }
                    .padding(horizontal = 28.dp, vertical = 12.dp)
            ) {
                if (retrying) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = Copy.unreachableRetry,
                        fontFamily = Typography.Manrope,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Text(
                text = Copy.logoutConfirm,
                fontFamily = Typography.Manrope,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Palette.muted,
                modifier = Modifier
                    .clickable { scope.launch { sessionStore.signOut() } }
                    .padding(top = Spacing.sm)
            )
        }
    }
}
