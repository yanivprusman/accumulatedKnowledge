package com.automatelinux.localKnowledge.data

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class LatLon(val lat: Double, val lon: Double)

/** Where the phone thinks it is, and how well it knows. */
data class Fix(val lat: Double, val lon: Double, val accuracyM: Float?)

private const val EARTH_RADIUS_M = 6_371_000.0
private fun Double.toRadians() = this * PI / 180.0
private fun Double.toDegrees() = this * 180.0 / PI

/** Great-circle distance in metres. */
fun distanceMetres(from: LatLon, to: LatLon): Double {
    val dLat = (to.lat - from.lat).toRadians()
    val dLon = (to.lon - from.lon).toRadians()
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(from.lat.toRadians()) * cos(to.lat.toRadians()) * sin(dLon / 2) * sin(dLon / 2)
    return 2 * EARTH_RADIUS_M * asin(sqrt(a.coerceIn(0.0, 1.0)))
}

/** Initial bearing from `from` to `to`, degrees clockwise from true north. */
fun bearingDegrees(from: LatLon, to: LatLon): Double {
    val dLon = (to.lon - from.lon).toRadians()
    val lat1 = from.lat.toRadians()
    val lat2 = to.lat.toRadians()
    val y = sin(dLon) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon)
    return (atan2(y, x).toDegrees() + 360.0) % 360.0
}

/** "360 מ׳", "11.2 ק״מ" */
fun formatDistance(metres: Double): String = when {
    metres < 1_000 -> "${(metres / 10).roundToInt() * 10} מ׳"
    metres < 100_000 -> {
        val rounded = (metres / 100).roundToInt() / 10.0
        val figure =
            if (abs(rounded - rounded.roundToInt()) < 0.05) "${rounded.roundToInt()}"
            else "$rounded"
        "$figure ק״מ"
    }
    else -> "${(metres / 1_000).roundToInt()} ק״מ"
}

private val COMPASS =
    listOf("צפון", "צפון-מזרח", "מזרח", "דרום-מזרח", "דרום", "דרום-מערב", "מערב", "צפון-מערב")

/** The Hebrew compass point a bearing falls in. */
fun compassPoint(bearing: Double): String =
    COMPASS[(((bearing % 360.0 + 360.0) % 360.0) / 45.0).roundToInt() % 8]
