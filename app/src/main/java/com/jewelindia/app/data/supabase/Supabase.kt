// Port of: wholesaler ios/JewelIndia/Sources/Networking/SupabaseManager.swift
package com.jewelindia.app.data.supabase

import android.net.Uri
import com.jewelindia.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.functions.Functions
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import io.github.jan.supabase.storage.Storage
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

val JewelJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
    explicitNulls = false
    isLenient = true
}

object JewelSupabase {
    val client: SupabaseClient by lazy { createJewelSupabase() }
}

fun createJewelSupabase(): SupabaseClient = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_ANON_KEY,
) {
    defaultSerializer = KotlinXSerializer(JewelJson)
    httpEngine = OkHttp.create {
        config {
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
            callTimeout(120, TimeUnit.SECONDS)
        }
    }
    install(Auth) {
        scheme = "jewelindia"
        host = "auth"
        flowType = FlowType.PKCE
        alwaysAutoRefresh = true
        autoLoadFromStorage = true
    }
    install(Postgrest)
    install(Storage)
    install(Functions)
}

val projectRef: String
    get() = Uri.parse(BuildConfig.SUPABASE_URL).host?.substringBefore('.') ?: ""
