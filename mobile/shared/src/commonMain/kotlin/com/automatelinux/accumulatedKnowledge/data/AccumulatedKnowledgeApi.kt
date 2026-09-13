package com.automatelinux.accumulatedKnowledge.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** One HTTP round trip. Android supplies it with the JDK client; iOS will supply
 *  NSURLSession. Kept small so neither platform grows a second place where a
 *  request can be built differently. */
expect suspend fun httpRequest(
    method: String,
    url: String,
    token: String,
    jsonBody: String?,
): HttpResult

data class HttpResult(val code: Int, val body: String, val transportError: String? = null)

@Serializable
data class FindingsResponse(
    val ok: Boolean = false,
    val findings: List<Finding> = emptyList(),
    val error: String? = null,
)

@Serializable
private data class Ack(val ok: Boolean = false, val error: String? = null)

@Serializable
private data class ConfirmBody(val at: String)

/**
 * The app's whole conversation with the backend.
 *
 * Failures are values, never exceptions thrown at the UI. This app is used at the
 * edge of coverage by design — that is where tent spots are — so "no signal" has
 * to render as a sentence next to a retry, not as a crash or an endless spinner.
 */
class AccumulatedKnowledgeApi(baseUrl: String, private val token: String) {
    private val base = baseUrl.trimEnd('/')
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }

    val configured: Boolean get() = token.isNotEmpty()

    suspend fun list(): FindingsResponse {
        if (!configured) return FindingsResponse(error = NO_TOKEN)
        val r = httpRequest("GET", "$base/api/findings", token, null)
        return runCatching { json.decodeFromString(FindingsResponse.serializer(), r.body) }
            .getOrElse { FindingsResponse(error = describe(r)) }
    }

    /** Create or replace. The finding carries its own id, so sending it twice — which
     *  a flaky connection guarantees — leaves one row, not two. */
    suspend fun save(finding: Finding): String? =
        call("POST", "/api/findings", json.encodeToString(Finding.serializer(), finding))

    suspend fun confirm(id: String, at: String): String? =
        call("POST", "/api/findings/$id/confirm", json.encodeToString(ConfirmBody.serializer(), ConfirmBody(at)))

    suspend fun delete(id: String): String? = call("DELETE", "/api/findings/$id", null)

    /** Null means it worked; anything else is a sentence to put on screen. */
    private suspend fun call(method: String, path: String, body: String?): String? {
        if (!configured) return NO_TOKEN
        val r = httpRequest(method, "$base$path", token, body)
        val ack = runCatching { json.decodeFromString(Ack.serializer(), r.body) }.getOrNull()
        return if (ack?.ok == true) null else ack?.error ?: describe(r)
    }

    /** A status code is useless to someone holding a phone at the side of a road. */
    private fun describe(r: HttpResult): String = when (r.code) {
        401 -> "הגרסה הזו לא מאושרת בשרת"
        0 -> "אין חיבור — הרישום נשמר במכשיר וייסלח כשתהיה קליטה"
        404 -> "הרישום כבר לא קיים בשרת"
        in 500..599 -> "השרת לא הצליח להגיע לבסיס הנתונים"
        else -> "תשובה לא צפויה מהשרת (${r.code})"
    }

    private companion object {
        const val NO_TOKEN = "נבנה בלי מפתח גישה — בנה מחדש עם mobile/.env"
    }
}
