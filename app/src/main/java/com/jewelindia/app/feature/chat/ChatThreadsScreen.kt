// Port of: wholesaler ios/JewelIndia/Sources/Features/Chat/ChatThreadsView.swift
package com.jewelindia.app.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jewelindia.app.core.images.CachedImage
import com.jewelindia.app.data.api.ChatApi
import com.jewelindia.app.data.api.ChatThread
import com.jewelindia.app.design.Palette
import com.jewelindia.app.design.Typography
import kotlinx.coroutines.launch

@Composable
fun ChatThreadsScreen(
    title: String = "Queries & Chat",
    emptyTitle: String = "No Active Queries",
    emptyMessage: String = "When stores ask about your designs, their questions will appear here.",
    onBack: (() -> Unit)? = null,
    onOpenThread: (ChatThread) -> Unit,
    onUnreadChange: ((Int) -> Unit)? = null
) {
    var threads by remember { mutableStateOf<List<ChatThread>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun load() {
        scope.launch {
            loading = true
            errorMessage = null
            try {
                val list = ChatApi.fetchThreads()
                threads = list
                val totalUnread = list.sumOf { it.unread }
                onUnreadChange?.invoke(totalUnread)
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to load chats"
            } finally {
                loading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Palette.background)
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Palette.dark
                    )
                }
            }
            Text(
                text = title,
                style = Typography.cirka(22.sp),
                fontWeight = FontWeight.Bold,
                color = Palette.dark,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { load() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Palette.muted
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when {
                loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Palette.goldPrimary,
                            strokeWidth = 2.dp
                        )
                    }
                }
                errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            style = Typography.santoshi(14.sp),
                            color = Palette.accentRed,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        IconButton(onClick = { load() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Retry",
                                tint = Palette.dark
                            )
                        }
                    }
                }
                threads.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = Palette.muted.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = emptyTitle,
                            style = Typography.cirka(18.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = Palette.dark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = emptyMessage,
                            style = Typography.santoshi(14.sp),
                            color = Palette.muted,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(threads, key = { it.id }) { thread ->
                            ChatThreadRow(
                                thread = thread,
                                onClick = { onOpenThread(thread) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatThreadRow(
    thread: ChatThread,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val imgUrl = thread.imageURL
        if (!imgUrl.isNullOrBlank()) {
            CachedImage(
                url = imgUrl,
                contentDescription = thread.productTitle,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Palette.cream),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (thread.storeName?.firstOrNull() ?: thread.title.firstOrNull() ?: 'J').uppercase(),
                    style = Typography.cirka(18.sp),
                    fontWeight = FontWeight.Bold,
                    color = Palette.goldPrimary
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = thread.title,
                    style = Typography.santoshi(15.sp),
                    fontWeight = if (thread.unread > 0) FontWeight.Bold else FontWeight.Medium,
                    color = Palette.dark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (thread.unread > 0) {
                    Badge(
                        containerColor = Palette.goldPrimary,
                        contentColor = Palette.light,
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(text = "${thread.unread}")
                    }
                }
            }

            if (!thread.subtitle.isNullOrBlank()) {
                Text(
                    text = thread.subtitle ?: "",
                    style = Typography.santoshi(12.sp),
                    color = Palette.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!thread.lastMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = thread.lastMessage ?: "",
                    style = Typography.santoshi(13.sp),
                    color = if (thread.unread > 0) Palette.dark else Palette.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
