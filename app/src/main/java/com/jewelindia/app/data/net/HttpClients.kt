// Port of: wholesaler ios/JewelIndia/Sources/Networking/NetworkResilience.swift & JewelAPI.session
package com.jewelindia.app.data.net

import android.app.Application
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class InMemoryCookieJar : CookieJar {
    private val cookieStore = mutableMapOf<String, MutableList<Cookie>>()

    @Synchronized
    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val list = cookieStore.getOrPut(url.host) { mutableListOf() }
        for (cookie in cookies) {
            list.removeAll { it.name == cookie.name && it.domain == cookie.domain && it.path == cookie.path }
            list.add(cookie)
        }
    }

    @Synchronized
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val list = cookieStore[url.host] ?: return emptyList()
        list.removeAll { it.expiresAt < now }
        return list.filter { it.matches(url) }
    }

    @Synchronized
    fun getCookies(host: String): List<Cookie> {
        return cookieStore[host]?.toList() ?: emptyList()
    }

    @Synchronized
    fun clear() {
        cookieStore.clear()
    }
}

class HttpClients(val app: Application) {
    val cookieJar: InMemoryCookieJar get() = Companion.cookieJar
    val site: OkHttpClient get() = Companion.site
    val pipeline: OkHttpClient get() = Companion.pipeline
    val plain: OkHttpClient get() = Companion.plain

    companion object {
        val cookieJar = InMemoryCookieJar()

        /**
         * Site API. Keeps cookies like a browser, because the signup sequence relies on
         * Supabase SSR cookies the Next server sets (JewelAPI.session in iOS).
         */
        val site: OkHttpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .build()

        /**
         * AI pipeline: 120 s call timeout for /process uploads.
         */
        val pipeline: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .callTimeout(120, TimeUnit.SECONDS)
            .build()

        /**
         * Everything else (HEAD probes, image search, edge functions): 30 s.
         */
        val plain: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
