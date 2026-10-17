// Port of: wholesaler ios/JewelIndia/Sources/Networking/OrdersChatAPI.swift (ChatAPI)
package com.jewelindia.app.data.api

import com.jewelindia.app.core.model.ChatMessage
import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class ChatException(message: String) : RuntimeException(message)

object ChatApi {
    private val db get() = JewelSupabase.client

    @Serializable
    private data class Reply(
        val ok: Boolean,
        val error: String? = null,
        @SerialName("conversation_id") val conversationId: String? = null
    )

    @Serializable
    private data class ReportReply(
        val ok: Boolean,
        val error: String? = null,
        @SerialName("already_reported") val alreadyReported: Boolean? = null
    )

    @Serializable
    data class BlockState(
        val ok: Boolean,
        @SerialName("blocked_by_me") val blockedByMe: Boolean? = null,
        @SerialName("blocked_by_them") val blockedByThem: Boolean? = null
    )

    suspend fun fetchThreads(): List<ChatThread> {
        return db.postgrest.rpc("chat_threads").decodeList<ChatThread>()
    }

    suspend fun open(productId: String): String {
        val reply = db.postgrest.rpc(
            "chat_open",
            buildJsonObject { put("p_product", productId) }
        ).decodeAs<Reply>()

        if (reply.ok && reply.conversationId != null) {
            return reply.conversationId
        }

        val message = if (reply.error == "NO_STORE") {
            "Only a store can start a conversation about a design."
        } else {
            "Couldn't start the conversation. Please try again."
        }
        throw ChatException(message)
    }

    suspend fun fetchMessages(conversationId: String): List<ChatMessage> {
        return db.from("messages").select {
            filter {
                eq("conversation_id", conversationId)
            }
            order("created_at", order = Order.ASCENDING)
        }.decodeList<ChatMessage>()
    }

    suspend fun send(conversationId: String, content: String) {
        val reply = try {
            db.postgrest.rpc(
                "chat_send",
                buildJsonObject {
                    put("p_conversation", conversationId)
                    put("p_content", content)
                }
            ).decodeAs<Reply>()
        } catch (e: Exception) {
            val text = e.message ?: ""
            if (text.contains("CHAT_CONTENT_REJECTED")) {
                throw ChatException("This message contains language that isn't allowed on Jewel India. Please rephrase it.")
            }
            if (text.contains("CHAT_BLOCKED")) {
                throw ChatException("You can't send messages in this conversation.")
            }
            throw e
        }

        if (!reply.ok) {
            throw ChatException("Your message didn't send. Please try again.")
        }
    }

    suspend fun report(conversationId: String, messageId: String, reason: String, details: String) {
        val reply = db.postgrest.rpc(
            "chat_report_message",
            buildJsonObject {
                put("p_conversation", conversationId)
                put("p_message", messageId)
                put("p_reason", reason)
                put("p_details", details.take(1000))
            }
        ).decodeAs<ReportReply>()

        if (!reply.ok && reply.alreadyReported != true) {
            val msg = if (reply.error == "NOT_FOUND") {
                "That message is no longer available to report."
            } else {
                "Couldn't send your report. Please try again."
            }
            throw ChatException(msg)
        }
    }

    suspend fun blockState(conversationId: String): BlockState {
        return db.postgrest.rpc(
            "chat_block_state",
            buildJsonObject { put("p_conversation", conversationId) }
        ).decodeAs<BlockState>()
    }

    suspend fun block(conversationId: String) {
        val reply = db.postgrest.rpc(
            "chat_block",
            buildJsonObject { put("p_conversation", conversationId) }
        ).decodeAs<Reply>()

        if (!reply.ok) throw ChatException("Couldn't block this contact. Please try again.")
    }

    suspend fun unblock(conversationId: String) {
        val reply = db.postgrest.rpc(
            "chat_unblock",
            buildJsonObject { put("p_conversation", conversationId) }
        ).decodeAs<Reply>()

        if (!reply.ok) throw ChatException("Couldn't unblock this contact. Please try again.")
    }

    suspend fun fetchBlockedThreadIDs(): Set<String> {
        val ids = db.postgrest.rpc("chat_my_blocked_threads").decodeList<String>()
        return ids.map { it.lowercase() }.toSet()
    }

    suspend fun markRead(conversationId: String) {
        try {
            db.postgrest.rpc(
                "chat_mark_read",
                buildJsonObject { put("p_conversation", conversationId) }
            )
        } catch (_: Exception) {
            // Best effort
        }
    }
}
