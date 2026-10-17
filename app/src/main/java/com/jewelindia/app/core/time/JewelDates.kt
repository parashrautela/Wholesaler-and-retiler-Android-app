// Port of: wholesaler ios/JewelIndia/Sources/Core/Time & Networking/EmployeeAPI.swift & Features/Chat/ChatThreadsView.swift
package com.jewelindia.app.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object JewelDates {
    val indiaZone: ZoneId = ZoneId.of("Asia/Kolkata")
    val indiaLocale: Locale = Locale.Builder().setLanguage("en").setRegion("IN").build()

    /**
     * Parse ISO-8601 string with or without fractional seconds / microseconds.
     */
    fun parse(raw: String?): Instant? {
        if (raw.isNullOrBlank()) return null
        return try {
            OffsetDateTime.parse(raw).toInstant()
        } catch (_: Exception) {
            try {
                Instant.parse(raw)
            } catch (_: Exception) {
                null
            }
        }
    }
}

object ChatTime {
    private val timeFormat = DateTimeFormatter.ofPattern("h:mm a", JewelDates.indiaLocale)
        .withZone(JewelDates.indiaZone)
    private val dayMonthFormat = DateTimeFormatter.ofPattern("d MMM", JewelDates.indiaLocale)
        .withZone(JewelDates.indiaZone)
    private val fullDateTimeFormat = DateTimeFormatter.ofPattern("d MMM, h:mm a", JewelDates.indiaLocale)
        .withZone(JewelDates.indiaZone)

    fun date(raw: String?): Instant? = JewelDates.parse(raw)

    /**
     * Time today (e.g. "3:45 PM"), otherwise date (e.g. "17 Oct").
     */
    fun short(raw: String?): String? {
        val instant = date(raw) ?: return null
        val dateInZone = instant.atZone(JewelDates.indiaZone).toLocalDate()
        val today = LocalDate.now(JewelDates.indiaZone)
        return if (dateInZone == today) {
            timeFormat.format(instant)
        } else {
            dayMonthFormat.format(instant)
        }
    }

    /**
     * Time today, otherwise date and time.
     */
    fun full(raw: String?): String? {
        val instant = date(raw) ?: return null
        val dateInZone = instant.atZone(JewelDates.indiaZone).toLocalDate()
        val today = LocalDate.now(JewelDates.indiaZone)
        return if (dateInZone == today) {
            timeFormat.format(instant)
        } else {
            fullDateTimeFormat.format(instant)
        }
    }
}

object OrderTime {
    private val fullOrderFormat = DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", JewelDates.indiaLocale)
        .withZone(JewelDates.indiaZone)

    fun short(raw: String?): String? = ChatTime.short(raw)

    fun full(raw: String?): String? {
        val instant = JewelDates.parse(raw) ?: return null
        return fullOrderFormat.format(instant)
    }
}

object TreasureChestFormat {
    private val resetFormatter = DateTimeFormatter.ofPattern("EEE, d MMM 'at' h:mm a 'IST'", JewelDates.indiaLocale)
        .withZone(JewelDates.indiaZone)

    fun formatResetsAt(instant: Instant): String = resetFormatter.format(instant)
}
