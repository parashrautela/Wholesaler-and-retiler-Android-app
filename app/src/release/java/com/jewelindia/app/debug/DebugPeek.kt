package com.jewelindia.app.debug

import android.content.Intent
import androidx.compose.runtime.Composable

object DebugPeek {
    fun getPeekId(intent: Intent?): String? = null

    @Composable
    fun PeekScreen(id: String, onBack: () -> Unit = {}) {}
}
