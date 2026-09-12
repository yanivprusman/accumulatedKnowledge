package com.automatelinux.localKnowledge.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * The Android half of [httpRequest], on the JDK's own client.
 *
 * No HTTP library on purpose: this app makes four request shapes against one host
 * on the WireGuard overlay, and a dependency would be more code to keep in
 * version-lockstep with Compose than it would replace.
 */
actual suspend fun httpRequest(
    method: String,
    url: String,
    token: String,
    jsonBody: String?,
): HttpResult = withContext(Dispatchers.IO) {
    var conn: HttpURLConnection? = null
    try {
        conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = method
            // Short, because the honest answer out of coverage is "no connection"
            // arriving quickly, not a spinner that outlasts the user's patience.
            connectTimeout = 6_000
            readTimeout = 15_000
            setRequestProperty("Authorization", "Bearer $token")
            setRequestProperty("Accept", "application/json")
            if (jsonBody != null) {
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                outputStream.use { it.write(jsonBody.toByteArray(Charsets.UTF_8)) }
            }
        }
        val code = conn.responseCode
        // A 4xx/5xx body carries the reason; errorStream is where it lives.
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
        HttpResult(code, body)
    } catch (e: IOException) {
        // Code 0 means the request never reached anyone — no signal, or the VPN
        // being down. The UI says so in words rather than showing a number.
        HttpResult(0, "", e.message ?: "no connection")
    } finally {
        conn?.disconnect()
    }
}
