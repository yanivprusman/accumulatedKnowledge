package com.automatelinux.accumulatedKnowledge.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Whether this place gave you the thing, or cost you the trip.
 *
 * AVOID is a first-class record, not an error state. "Free shower on the map;
 * the gate locks at night" is exactly as useful as a place that works — arguably
 * more so, because the map will keep telling you to go there.
 */
@Serializable
enum class Verdict { WORKS, AVOID }

/**
 * One thing learned, fixed to the spot where it was learned.
 *
 * `need` is what you were after; `method` is what actually works. The split
 * matters: you search by the need you have again, and what you read back is the
 * method, because the place alone is what every map already told you.
 *
 * `lat`/`lon` are both set or both null. Null is a finding with no spot — a
 * supplier you phone, whose number you wrote down wherever you happened to be
 * standing. Anchoring it there would navigate you to the wrong place.
 */
@Serializable
data class Finding(
    val id: String,
    val need: String,
    val place: String,
    val lat: Double? = null,
    val lon: Double? = null,
    @SerialName("accuracyM") val accuracyM: Int? = null,
    val verdict: Verdict = Verdict.WORKS,
    val method: String,
    /** Someone to call about it. Null for most findings — a shower has no number. */
    val phone: String? = null,
    val foundAt: String,
    val confirmedAt: String,
    val confirmedN: Int = 1,
    val updatedAt: String = "",
)

/** Where to drive to, or null for a finding that is a person rather than a place. */
val Finding.spot: LatLon?
    get() = if (lat != null && lon != null) LatLon(lat, lon) else null

/**
 * Needs seen often enough to be worth one tap.
 *
 * A suggestion list, never a closed set — the field the user types into accepts
 * anything. An app that could only record the needs its author thought of would
 * have had nowhere to put "a shop that sells good tomatoes".
 */
val COMMON_NEEDS = listOf(
    "מקלחת", "אוהל", "מים", "שירותים", "חניה", "חנות",
    "אוכל", "דלק", "תיקון", "רחצה", "נוף", "קליטה",
)
