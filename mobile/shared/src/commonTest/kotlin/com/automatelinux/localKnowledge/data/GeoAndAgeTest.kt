package com.automatelinux.localKnowledge.data

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GeoAndAgeTest {

    // The real pair from the night this app came from: the gate where you park,
    // and the shower on the far side of it.
    private val gate = LatLon(32.388282, 34.867022)
    private val shower = LatLon(32.3865159, 34.8637763)

    @Test
    fun distanceFromTheGateToTheShowerIsTheWalkYouActuallyMake() {
        val metres = distanceMetres(gate, shower)
        assertTrue(metres in 330.0..400.0, "expected ~360 m, got $metres")
        assertEquals("360 מ׳", formatDistance(metres))
    }

    @Test
    fun theShowerIsSouthWestOfTheGate() {
        // 237°, not due west — which is why the record says south-west. Writing
        // "west" in a note you follow in the dark is how you walk past it.
        assertEquals("דרום-מערב", compassPoint(bearingDegrees(gate, shower)))
    }

    @Test
    fun distanceSwitchesUnitAtAKilometre() {
        assertEquals("990 מ׳", formatDistance(994.0))
        assertEquals("1 ק״מ", formatDistance(1_000.0))
        assertEquals("11.2 ק״מ", formatDistance(11_240.0))
        assertEquals("120 ק״מ", formatDistance(120_400.0))
    }

    private val now = Instant.parse("2026-09-12T09:00:00Z")
    private fun age(utc: String) = ageLabel(utc, now)

    @Test
    fun ageReadsInWordsAndRoundsDown() {
        assertEquals("היום", age("2026-09-12 02:00:00"))
        assertEquals("אתמול", age("2026-09-11 05:00:00"))
        assertEquals("לפני 3 ימים", age("2026-09-09 05:00:00"))
        assertEquals("לפני 2 שבועות", age("2026-08-28 09:00:00"))
        assertEquals("לפני 6 חודשים", age("2026-03-12 09:00:00"))
        assertEquals("לפני שנה", age("2025-09-12 09:00:00"))
        assertEquals("לפני 4 שנים", age("2022-09-01 09:00:00"))
    }

    @Test
    fun anUnparseableTimestampIsNamedAsUnknownNotDatedToTheEpoch() {
        // Reading a bad value as 1970 would label a fresh finding "לפני 56 שנים"
        // and quietly destroy the one signal the user judges trust by.
        assertEquals("בזמן לא ידוע", age("not a timestamp"))
        assertNull(parseUtc("not a timestamp"))
    }

    @Test
    fun utcRoundTrips() {
        val s = "2026-09-09 21:30:00"
        assertEquals(s, utcString(parseUtc(s)!!))
    }
}
