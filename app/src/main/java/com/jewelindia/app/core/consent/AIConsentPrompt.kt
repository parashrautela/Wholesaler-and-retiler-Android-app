package com.jewelindia.app.core.consent

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/** Defers the requested action until the account explicitly allows AI processing. */
@Composable
fun rememberAIConsentRequest(userId: String): (() -> Unit) -> Unit {
    var pendingAction by remember(userId) { mutableStateOf<(() -> Unit)?>(null) }

    if (pendingAction != null) {
        AlertDialog(
            onDismissRequest = { pendingAction = null },
            title = { Text("Allow AI image processing?") },
            text = {
                Text(
                    "Your jewellery photos will be sent to third-party AI providers " +
                        "(${AIConsent.PROVIDERS}) to analyze images and generate designs. " +
                        "Your permission is saved for this account on this device."
                )
            },
            confirmButton = {
                TextButton(
                    enabled = userId.isNotBlank(),
                    onClick = {
                        AIConsent.grant(userId)
                        val action = pendingAction
                        pendingAction = null
                        action?.invoke()
                    }
                ) { Text("Allow and continue") }
            },
            dismissButton = {
                TextButton(onClick = { pendingAction = null }) { Text("Not now") }
            }
        )
    }

    return { action ->
        if (AIConsent.isGranted(userId)) action() else pendingAction = action
    }
}
