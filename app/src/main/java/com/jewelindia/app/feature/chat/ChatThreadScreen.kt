// Port of: wholesaler ios/JewelIndia/Sources/Features/Chat/ChatThreadView.swift
package com.jewelindia.app.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.model.ChatMessage
import com.jewelindia.app.core.time.ChatTime
import com.jewelindia.app.data.api.ChatApi
import com.jewelindia.app.data.api.ChatThread
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@Composable
fun ChatThreadScreen(
    thread: ChatThread,
    side: String, // "wholesaler", "retailer", "employee"
    title: String? = null,
    peekMessages: List<ChatMessage>? = null,
    onBack: () -> Unit
) {
    var messages by remember { mutableStateOf<List<ChatMessage>>(peekMessages ?: emptyList()) }
    var inputText by remember { mutableStateOf("") }
    var sending by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(peekMessages == null) }
    var showMenu by remember { mutableStateOf(false) }
    var reportingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var reportReason by remember { mutableStateOf("") }
    var reportDetails by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val displayTitle = title ?: thread.title

    suspend fun loadMessages() {
        if (peekMessages != null) {
            messages = peekMessages
            loading = false
            return
        }
        try {
            messages = ChatApi.fetchMessages(thread.id)
            ChatApi.markRead(thread.id)
        } catch (_: Exception) {}
    }

    LaunchedEffect(thread.id, peekMessages) {
        if (peekMessages != null) {
            messages = peekMessages
            loading = false
        } else {
            loading = true
            loadMessages()
            loading = false
        }
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }
    }

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isEmpty() || sending) return
        scope.launch {
            sending = true
            errorMessage = null
            try {
                if (peekMessages != null) {
                    messages = messages + ChatMessage(
                        id = "local-${System.currentTimeMillis()}",
                        conversationId = thread.id,
                        senderType = side,
                        content = text,
                        isRead = true,
                        createdAt = java.time.Instant.now().toString()
                    )
                } else {
                    ChatApi.send(thread.id, text)
                    loadMessages()
                }
                inputText = ""
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
        // iOS Navigation Bar: Close pill on left, Centered title, Ellipsis button on right
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                onClick = onBack,
                shape = CircleShape,
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .height(34.dp)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Close",
                        style = Typography.santoshi(14.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = Palette.dark
                    )
                }
            }

            Text(
                text = displayTitle,
                style = Typography.santoshi(16.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark,
                textAlign = TextAlign.Center
            )

            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                Surface(
                    onClick = { showMenu = true },
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = "Options",
                            tint = Palette.dark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Report latest message") },
                        onClick = {
                            showMenu = false
                            val latestIncoming = messages.lastOrNull { it.senderType != side }
                            reportingMessage = latestIncoming
                        }
                    )
                }
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = Color(0xFFE5E7EB)
        )

        // Error banner if any
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
                    color = Palette.dark
                )
            } else if (messages.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Say hello — ask about availability, purity or delivery.",
                        style = Typography.santoshi(13.sp),
                        color = Color(0xFF8E8E93),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isMine = msg.senderType == side
                        ChatBubble(
                            message = msg,
                            isMine = isMine,
                            onReport = { reportingMessage = msg }
                        )
                    }
                }
            }
        }

        HorizontalDivider(
            thickness = 0.5.dp,
            color = Color(0xFFE5E7EB)
        )

        // iOS Inset Rounded Composer Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Palette.background)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                if (inputText.isEmpty()) {
                    Text(
                        text = "Type a message…",
                        style = Typography.santoshi(14.sp),
                        color = Color(0xFF8E8E93)
                    )
                }
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    textStyle = Typography.santoshi(14.sp).copy(color = Palette.dark),
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { sendMessage() })
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { sendMessage() },
                enabled = inputText.isNotBlank() && !sending,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (inputText.isNotBlank()) Palette.dark else Color(0xFFC7C7CC))
            ) {
                if (sending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isMine) Palette.dark else Color.White)
                .then(
                    if (!isMine) Modifier.border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(16.dp))
                    else Modifier
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.content ?: "",
                style = Typography.santoshi(14.sp),
                color = if (isMine) Color.White else Palette.dark
            )
        }

        val fullTime = ChatTime.full(message.createdAt)
        if (fullTime != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = fullTime,
                style = Typography.santoshi(10.sp),
                color = Color(0xFF8E8E93)
            )
        }
    }
}
