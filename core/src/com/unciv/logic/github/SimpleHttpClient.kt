package com.unciv.logic.github

import com.unciv.UncivGame
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Simple HTTP client for iOS/RoboVM compatibility
 * 
 * Uses OkHttp directly without Ktor's reflection-based content negotiation.
 * This avoids the Kotlin reflection crash on RoboVM while still providing
 * HTTP functionality.
 */
object SimpleHttpClient {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .retryOnConnectionFailure(true)
        .build()
    
    /**
     * Perform a GET request and return the response body as a string
     * 
     * @param url The URL to fetch
     * @param headers Optional headers to include in the request
     * @param addGithubHeaders If true, adds GitHub API headers (User-Agent, bearer token)
     * @return The response body as a string
     * @throws IOException if the request fails
     */
    fun get(url: String, headers: Map<String, String> = emptyMap(), addGithubHeaders: Boolean = true): String {
        val requestBuilder = Request.Builder().url(url)
        
        // Add default GitHub headers if requested
        if (addGithubHeaders) {
            requestBuilder.addHeader("User-Agent", UncivGame.getUserAgent("Github"))
            requestBuilder.addHeader("X-GitHub-Api-Version", "2022-11-28")
            requestBuilder.addHeader("Accept", "application/vnd.github+json")
            if (GithubAPI.bearerToken.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer ${GithubAPI.bearerToken}")
            }
        }
        
        // Add custom headers (can override defaults)
        headers.forEach { (key, value) ->
            requestBuilder.header(key, value)  // Use header() to replace, not addHeader()
        }
        
        val request = requestBuilder.build()
        
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${response.message}")
            }
            return response.body?.string() ?: ""
        }
    }
    
    /**
     * Perform a GET request with retry logic
     * 
     * @param url The URL to fetch
     * @param headers Optional headers to include in the request
     * @param maxRetries Maximum number of retry attempts (default: 3)
     * @param addGithubHeaders If true, adds GitHub API headers (User-Agent, bearer token)
     * @return The response body as a string
     * @throws IOException if all retries fail
     */
    fun getWithRetry(
        url: String,
        headers: Map<String, String> = emptyMap(),
        maxRetries: Int = 3,
        addGithubHeaders: Boolean = true
    ): String {
        var lastException: IOException? = null
        
        repeat(maxRetries) { attempt ->
            try {
                return get(url, headers, addGithubHeaders)
            } catch (e: IOException) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    // Wait before retrying (exponential backoff)
                    Thread.sleep((1000L * (attempt + 1)))
                }
            }
        }
        
        throw lastException ?: IOException("Failed to fetch URL after $maxRetries attempts")
    }

    /**
     * Perform a GET request and return the response body as bytes
     * Useful for binary content (images, zips)
     */
    fun getBytes(url: String, headers: Map<String, String> = emptyMap(), addGithubHeaders: Boolean = true): ByteArray {
        val requestBuilder = Request.Builder().url(url)

        if (addGithubHeaders) {
            requestBuilder.addHeader("User-Agent", UncivGame.getUserAgent("Github"))
            requestBuilder.addHeader("X-GitHub-Api-Version", "2022-11-28")
            requestBuilder.addHeader("Accept", "application/vnd.github+json")
            if (GithubAPI.bearerToken.isNotBlank()) {
                requestBuilder.addHeader("Authorization", "Bearer ${GithubAPI.bearerToken}")
            }
        }

        headers.forEach { (key, value) -> requestBuilder.header(key, value) }

        val request = requestBuilder.build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${response.message}")
            }
            return response.body?.bytes() ?: ByteArray(0)
        }
    }

    fun getBytesWithRetry(
        url: String,
        headers: Map<String, String> = emptyMap(),
        maxRetries: Int = 3,
        addGithubHeaders: Boolean = true
    ): ByteArray {
        var lastException: IOException? = null

        repeat(maxRetries) { attempt ->
            try {
                return getBytes(url, headers, addGithubHeaders)
            } catch (e: IOException) {
                lastException = e
                if (attempt < maxRetries - 1) {
                    Thread.sleep((1000L * (attempt + 1)))
                }
            }
        }

        throw lastException ?: IOException("Failed to fetch URL after $maxRetries attempts")
    }
}
