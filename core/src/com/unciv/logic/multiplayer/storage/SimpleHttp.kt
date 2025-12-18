package com.unciv.logic.multiplayer.storage

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Net
import com.unciv.UncivGame
import com.unciv.utils.Log
import com.unciv.utils.debug
import java.net.DatagramSocket
import java.net.InetAddress

private typealias SendRequestCallback = (success: Boolean, result: String, code: Int?)->Unit

object SimpleHttp {
    fun sendGetRequest(url: String, timeout: Int = 5000, header: Map<String, String>? = null, action: SendRequestCallback) {
        sendRequest(Net.HttpMethods.GET, url, "", timeout, header, action)
    }

    fun sendRequest(method: String, url: String, content: String, timeout: Int = 5000, header: Map<String, String>? = null, action: SendRequestCallback) {
        // Avoid java.net URL handlers on iOS (RoboVM) by using LibGDX's cross-platform Net API.
        val fullUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "http://$url"

        val request = Net.HttpRequest().apply {
            this.method = method
            this.url = fullUrl
            this.timeOut = timeout
            if (content.isNotEmpty()) this.content = content

            // Standard headers
            headers["Content-Type"] = "text/plain"
            headers["User-Agent"] = if (UncivGame.isCurrentInitialized())
                "Unciv/${UncivGame.VERSION.toNiceString()}-GNU-Terry-Pratchett"
            else "Unciv/Turn-Checker-GNU-Terry-Pratchett"

            // Custom headers
            for ((key, value) in header.orEmpty()) headers[key] = value
        }

        try {
            Gdx.net.sendHttpRequest(request, object : Net.HttpResponseListener {
                override fun handleHttpResponse(httpResponse: Net.HttpResponse) {
                    val status = httpResponse.status.statusCode
                    val text = try { httpResponse.resultAsString } catch (t: Throwable) { "" }
                    action(true, text, status)
                }

                override fun failed(t: Throwable) {
                    debug("Error during HTTP request", t)
                    action(false, t.message ?: "HTTP request failed", null)
                }

                override fun cancelled() {
                    action(false, "HTTP request cancelled", null)
                }
            })
        } catch (t: Throwable) {
            Log.debug("Bad URL", t)
            action(false, "Bad URL", null)
        }
    }

    fun getIpAddress(): String? {
        DatagramSocket().use { socket ->
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002)
            return socket.localAddress.hostAddress
        }
    }
}
