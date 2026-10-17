// Port of: wholesaler ios/JewelIndia/Sources/Features/Chat/ChatThreadView.swift
package com.jewelindia.app.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.core.model.ChatMessage
import com.jewelindia.app.data.api.ChatApi
import com.jewelindia.app.data.api.ChatThread
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@Composable
fun ChatThreadScreen(
    thread: ChatThread,
    side: String, // "wholesaler", "retailer", "employee"
    onBack: () -> Unit
) {
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var reportingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var reportReason by remember { mutableStateOf("") }
    var reportDetails by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    suspend fun loadMessages() {
        try {
            messages = ChatApi.fetchMessages(thread.id)
            ChatApi.markRead(thread.id)
        } catch (_: Exception) {}
    }

    LaunchedEffect(thread.id) {
        loading = true
        loadMessages()
        loading = false
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isEmpty() || sending) return
        scope.launch {
            sending = true
            errorMessage = null
            try {
                ChatApi.send(thread.id, text)
                inputText = ""
                loadMessages()
                if (messages.isNotEmpty()) {
                    listState.animateScrollToItem(messages.size - 1)
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to send message"
            } finally {
                sending = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // App bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Palette.light)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Palette.dark
                )
            }

            if (!thread.imageURL.isNullOrBlank()) {
                CachedImage(
                    url = thread.imageURL ?: "",
                    contentDescription = null,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = thread.title,
                    style = Typography.santoshi(16.sp),
                    fontWeight = FontWeight.Bold,
                    color = Palette.dark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!thread.subtitle.isNullOrBlank()) {
                    Text(
                        text = thread.subtitle ?: "",
                        style = Typography.santoshi(12.sp),
                        color = Palette.muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Palette.dark
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Block contact") },
                        onClick = {
                            showMenu = false
                            scope.launch {
                                try {
                                    ChatApi.block(thread.id)
                                    onBack()
                                } catch (_: Exception) {}
                            }
                        }
                    )
                }
            }
        }

        // Error banner
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Palette.accentRed.copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = errorMessage ?: "",
                    style = Typography.santoshi(13.sp),
                    color = Palette.accentRed
                )
            }
        }

        // Messages list
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Palette.goldPrimary
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isMine = when (side) {
                            "wholesaler" -> msg.senderType == "wholesaler"
                            else -> msg.senderType in listOf("store", "retailer", "employee")
                        }
                        ChatBubble(
                            message = msg,
                            isMine = isMine,
                            onReport = { reportingMessage = msg }
                        )
                    }
                }
            }
        }

        // Input row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Palette.light)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Type a message...", style = Typography.santoshi(14.sp), color = Palette.muted) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Palette.goldPrimary,
                    unfocusedBorderColor = Palette.border,
                    focusedContainerColor = Palette.cream.copy(alpha = 0.3f),
                    unfocusedContainerColor = Palette.cream.copy(alpha = 0.3f)
                ),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { sendMessage() })
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { sendMessage() },
                enabled = inputText.isNotBlank() && !sending,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) Palette.goldPrimary else Palette.muted.copy(alpha = 0.3f))
            ) {
                if (sending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Palette.light,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Palette.light,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    // Report Dialog
    reportingMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { reportingMessage = null },
            title = { Text("Report Message", style = Typography.cirka(18.sp), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Reason for reporting:", style = Typography.santoshi(14.sp))
                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        placeholder = { Text("e.g. Inappropriate content, spam") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = reportDetails,
                        onValueChange = { reportDetails = it },
                        placeholder = { Text("Additional details (optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val reason = reportReason.ifBlank { "Inappropriate" }
                        scope.launch {
                            try {
                                ChatApi.report(thread.id, msg.id, reason, reportDetails)
                            } catch (_: Exception) {}
                            reportingMessage = null
                            reportReason = ""
                            reportDetails = ""
                        }
                    }
                ) {
                    Text("Report", color = Palette.accentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { reportingMessage = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    isMine: Boolean,
    onReport: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .wrapContentWidth(if (isMine) Alignment.End else Alignment.Start)
        ) {
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMine) 16.dp else 4.dp,
                            bottomEnd = if (isMine) 4.dp else 16.dp
                        )
                    )
                    .background(if (isMine) Palette.goldPrimary else Palette.cream)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.content ?: "",
                    style = Typography.santoshi(14.sp),
                    color = if (isMine) Palette.light else Palette.dark
                )
            }
        }
    }
}

private fun Modifier.wrapContentWidth(align: Alignment.Horizontal): Modifier = this
