// Port of: wholesaler ios fresh token handling (auth.session)
package com.jewelindia.app.data.net

import com.jewelindia.app.data.supabase.JewelSupabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

object AuthToken {
    suspend fun fresh(supabase: SupabaseClient = JewelSupabase.client): String {
        val s = supabase.auth.currentSessionOrNull()
            ?: throw ApiError(401, "Session expired. Please sign in again.")
        val expiresAt = s.expiresAt
        val now = Clock.System.now()
        if (expiresAt <= now + 60.seconds) {
            try {
                supabase.auth.refreshCurrentSession()
            } catch (_: Exception) {
                // Fall back to current if refresh failed transiently
            }
        }
        return supabase.auth.currentSessionOrNull()?.accessToken
            ?: throw ApiError(401, "Session expired. Please sign in again.")
    }
}
