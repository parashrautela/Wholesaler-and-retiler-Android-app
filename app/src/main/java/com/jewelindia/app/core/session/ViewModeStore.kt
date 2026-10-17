// Port of: wholesaler ios/JewelIndia/Sources/Core/AuthRouter.swift (ViewModeStore)
package com.jewelindia.app.core.session

import android.content.Context
import android.content.SharedPreferences
import com.jewelindia.app.core.model.ViewMode

/**
 * `jewel_view_mode`, stored per user id.
 *
 * On the web this is a 7-day httpOnly cookie that is never cleared on sign-out,
 * so it leaks between accounts on a shared browser. Keying it by user id keeps
 * the same semantics without the leak.
 */
class ViewModeStore(private val prefs: SharedPreferences) {

    constructor(context: Context) : this(
        context.getSharedPreferences("jewel_view_mode_prefs", Context.MODE_PRIVATE)
    )

    private fun key(userId: String): String = "jewel_view_mode.$userId"

    /**
     * A verified retailer enters their own standalone marketplace by
     * default. Employee mode remains an explicit switch and is persisted.
     */
    fun mode(userId: String): ViewMode {
        val raw = prefs.getString(key(userId), null) ?: return ViewMode.retailer
        return try {
            ViewMode.valueOf(raw)
        } catch (_: IllegalArgumentException) {
            ViewMode.retailer
        }
    }

    fun set(mode: ViewMode, userId: String) {
        prefs.edit().putString(key(userId), mode.name).apply()
    }

    fun clear(userId: String) {
        prefs.edit().remove(key(userId)).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
