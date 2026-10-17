// Port of: wholesaler ios/JewelIndia/Sources/Networking/SSRSessionBridge.swift (Tests)
package com.jewelindia.app.data.net

import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SsrSessionBridgeTest {

    @Test
    fun testPercentDecode() {
        assertEquals("hello world", SsrSessionBridge.percentDecode("hello%20world"))
        assertEquals("foo+bar", SsrSessionBridge.percentDecode("foo+bar"))
        assertEquals("a/b?c=d&e=f", SsrSessionBridge.percentDecode("a%2Fb%3Fc%3Dd%26e%3Df"))
    }

    @Test
    fun testBase64UrlDecode() {
        val input = "eyJhbGciOiJIUzI1NiJ9"
        val decoded = SsrSessionBridge.base64UrlDecode(input)
        assertNotNull(decoded)
        assertEquals("{\"alg\":\"HS256\"}", decoded?.let { String(it, Charsets.UTF_8) })
    }

    @Test
    fun testSingleCookieExtraction() {
        val jsonPayload = """{"access_token":"token_123","refresh_token":"refresh_abc"}"""
        val cookie = Cookie.Builder()
            .name("sb-projectref-auth-token")
            .value(jsonPayload)
            .domain("example.com")
            .build()
        val tokens = SsrSessionBridge.tokens(listOf(cookie), "projectref")
        assertNotNull(tokens)
        assertEquals("token_123", tokens?.accessToken)
        assertEquals("refresh_abc", tokens?.refreshToken)
    }

    @Test
    fun testChunkedCookieAssembly() {
        val jsonPayload = """{"access_token":"token_chunked","refresh_token":"refresh_chunked"}"""
        val chunk0 = jsonPayload.substring(0, 20)
        val chunk1 = jsonPayload.substring(20)

        val cookie0 = Cookie.Builder()
            .name("sb-ref-auth-token.0")
            .value(chunk0)
            .domain("example.com")
            .build()
        val cookie1 = Cookie.Builder()
            .name("sb-ref-auth-token.1")
            .value(chunk1)
            .domain("example.com")
            .build()

        val tokens = SsrSessionBridge.tokens(listOf(cookie1, cookie0), "ref")
        assertNotNull(tokens)
        assertEquals("token_chunked", tokens?.accessToken)
        assertEquals("refresh_chunked", tokens?.refreshToken)
    }
}
