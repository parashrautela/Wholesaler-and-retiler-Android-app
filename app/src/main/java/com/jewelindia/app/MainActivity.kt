// Port of: wholesaler ios/JewelIndia/Sources/JewelIndiaApp.swift + RootView.onOpenURL
package com.jewelindia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.jewelindia.app.design.JewelTheme
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.cirka

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JewelTheme {
                LaunchScreen()
            }
        }
    }
}

/**
 * Launch state while the persisted session is resolved. Deliberately quiet —
 * brand mark only, no spinner flash. (iOS: LaunchView in RootView.swift)
 */
@Composable
fun LaunchScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Jewel India",
            style = cirka(32f),
            color = Palette.foreground
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LaunchScreenPreview() {
    JewelTheme {
        LaunchScreen()
    }
}
