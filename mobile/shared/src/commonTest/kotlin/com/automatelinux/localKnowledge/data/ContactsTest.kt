package com.automatelinux.localKnowledge.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ContactsTest {

    @Test
    fun aNumberWrittenTheWayItWasReadOutBecomesTheDigitsWhatsAppWants() {
        assertEquals("972502225880", internationalDigits("050-2225880"))
        assertEquals("972502225880", internationalDigits("+972 50-222-5880"))
        assertEquals("972502225880", internationalDigits("00972502225880"))
        assertNull(internationalDigits("  "))
    }

    private fun finding(id: String, lat: Double?, lon: Double?, confirmedAt: String) = Finding(
        id = id, need = "ציוד להרחקת יונים", place = id, lat = lat, lon = lon,
        method = "…", foundAt = confirmedAt, confirmedAt = confirmedAt,
    )

    /** Hands the store a cache as if the app had synced before, and nothing else. */
    private class CachedOnly(findings: List<Finding>) : KeyValueStore {
        private val values = mutableMapOf(
            "cache.findings" to Json.encodeToString(ListSerializer(Finding.serializer()), findings),
        )
        override fun get(key: String): String? = values[key]
        override fun put(key: String, value: String) { values[key] = value }
    }

    @Test
    fun aSupplierYouPhoneFollowsThePlacesYouCanDriveTo() {
        // The supplier is the most recently confirmed record, which is exactly why it
        // must not be allowed to outrank the gate you are standing 360 m from.
        val store = KnowledgeStore(
            LocalKnowledgeApi("http://unused", ""),
            CachedOnly(
                listOf(
                    finding("supplier", null, null, "2026-09-12 08:00:00"),
                    finding("far", 33.0, 35.1, "2026-09-01 08:00:00"),
                    finding("gate", 32.388282, 34.867022, "2026-09-10 08:00:00"),
                ),
            ),
            CoroutineScope(Job()),
        )

        val list = store.located(Fix(32.3865159, 34.8637763, 10f), null)

        assertEquals(listOf("gate", "far", "supplier"), list.map { it.finding.id })
        assertNull(list.last().metres, "a finding with no spot has no distance to show")
    }
}
