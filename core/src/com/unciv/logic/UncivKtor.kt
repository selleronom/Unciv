package com.unciv.logic

import com.unciv.UncivGame
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import com.unciv.logic.github.SimpleHttpClient

object UncivKtor {
    // Check whether platform supports Java 8 functional interfaces needed by some Ktor engines
    fun isKtorSupported(): Boolean {
        return try {
            Class.forName("java.util.function.Function")
            true
        } catch (e: Throwable) {
            false
        }
    }

    // Lazy-initialized Ktor client (only created on supported platforms)
    val client: HttpClient by lazy {
        if (!isKtorSupported()) throw IllegalStateException("Ktor HttpClient not supported on this platform")
        HttpClient(CIO) {
            followRedirects = true
            install(HttpRequestRetry) {
                maxRetries = 3
                retryOnException()
            }
            install(BodyProgress)

            defaultRequest {
                userAgent(UncivGame.getUserAgent())
            }
        }
    }

    /** Minimal response wrapper used to avoid exposing Ktor's HttpResponse on platforms where Ktor engines are unavailable */
    data class SimpleResponse(val status: HttpStatusCode, val headers: Map<String, String>, private val body: ByteArray) {
        fun bodyAsBytes(): ByteArray = body
        fun bodyAsText(): String = body.toString(Charsets.UTF_8)
    }

    /**
     * Wrapper for HTTP GET that returns `SimpleResponse` on success or `null` on failure.
     * Uses Ktor client on supported platforms and SimpleHttpClient on iOS/RoboVM.
     */
    suspend fun getOrNull(url: String, block: HttpRequestBuilder.() -> Unit = {}): SimpleResponse? {
        return try {
            if (isKtorSupported()) {
                val resp = client.get(url, block)
                val bytes = resp.bodyAsBytes()
                // Flatten headers to a simple map of key -> single string value
                val headersMap = resp.headers.entries().associate { it.key to it.value.joinToString(",") }
                SimpleResponse(resp.status, headersMap, bytes)
            } else {
                // Fallback for iOS/RoboVM: use SimpleHttpClient
                val bytes = SimpleHttpClient.getBytesWithRetry(url)
                SimpleResponse(HttpStatusCode.OK, emptyMap(), bytes)
            }
        } catch (_: Throwable) {
            null
        }
    }
}
