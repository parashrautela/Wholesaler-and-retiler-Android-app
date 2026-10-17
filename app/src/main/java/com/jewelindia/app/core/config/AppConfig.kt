// Port of: wholesaler ios/JewelIndia/Sources/Core/AppConfig.swift
package com.jewelindia.app.core.config

import com.jewelindia.app.BuildConfig
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl

object AppConfig {
    val supabaseUrl: String = BuildConfig.SUPABASE_URL
    val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY
    val aiPipelineUrl: String = BuildConfig.AI_PIPELINE_URL
    val siteUrl: String = BuildConfig.SITE_URL

    val supabaseURL: String get() = supabaseUrl
    val aiPipelineURL: String get() = aiPipelineUrl
    val siteURL: String get() = siteUrl

    val supabaseHttpUrl: HttpUrl = supabaseUrl.toHttpUrl()
    val aiPipelineHttpUrl: HttpUrl = aiPipelineUrl.toHttpUrl()
    val siteHttpUrl: HttpUrl = siteUrl.toHttpUrl()

    const val authCallbackScheme: String = "jewelindia"
    val googleWebClientId: String = BuildConfig.GOOGLE_WEB_CLIENT_ID

    val supportsNativeGoogleSignIn: Boolean
        get() = googleWebClientId.isNotBlank()
}
