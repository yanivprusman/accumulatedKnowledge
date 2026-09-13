package com.automatelinux.accumulatedKnowledge.platform

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.automatelinux.accumulatedKnowledge.data.Fix
import com.automatelinux.accumulatedKnowledge.data.LocationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Where the phone is, through the platform's own [LocationManager].
 *
 * Not Play Services' fused provider: this asks for one modest thing — a position
 * good to a few tens of metres — and LocationManager delivers it on any Android
 * device, including one with no Play Services and an emulator being fed
 * `adb emu geo fix`. Following brownSigns, which learned the same thing.
 */
class AndroidLocationProvider(private val context: Context) : LocationProvider {

    private val _fix = MutableStateFlow<Fix?>(null)
    override val fix: StateFlow<Fix?> = _fix.asStateFlow()

    private val manager: LocationManager
        get() = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private var listening = false

    override fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    private var best: Location? = null

    private val listener = LocationListener { offer(it) }

    @SuppressLint("MissingPermission")
    override fun start() {
        if (listening || !hasPermission()) return
        val lm = manager
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { runCatching { lm.isProviderEnabled(it) }.getOrDefault(false) }
        if (providers.isEmpty()) return

        // A remembered fix makes the list correct immediately instead of after the
        // first satellite lock — which outdoors can be half a minute.
        providers.forEach { offer(runCatching { lm.getLastKnownLocation(it) }.getOrNull()) }
        providers.forEach {
            lm.requestLocationUpdates(it, UPDATE_INTERVAL_MS, UPDATE_DISTANCE_M, listener, Looper.getMainLooper())
        }
        listening = true
    }

    fun stop() {
        if (!listening) return
        runCatching { manager.removeUpdates(listener) }
        listening = false
    }

    private fun offer(candidate: Location?) {
        if (candidate == null) return
        if (!candidate.isBetterThan(best)) return
        best = candidate
        _fix.value = Fix(
            lat = candidate.latitude,
            lon = candidate.longitude,
            accuracyM = if (candidate.hasAccuracy()) candidate.accuracy else null,
        )
    }

    /** Newer wins, unless the newer fix is markedly vaguer than a still-fresh one:
     *  a 2 km cell-tower estimate should not displace a 10 m GPS fix from a minute ago. */
    private fun Location.isBetterThan(other: Location?): Boolean {
        if (other == null) return true
        val newerBy = time - other.time
        if (newerBy > STALE_AFTER_MS) return true
        if (newerBy < 0) return false
        if (!hasAccuracy()) return !other.hasAccuracy()
        if (!other.hasAccuracy()) return true
        return accuracy <= other.accuracy * 2f
    }

    private companion object {
        // Finer than this buys nothing: the app ranks places metres-to-kilometres
        // apart, and a capture takes the fix once, when the screen opens.
        const val UPDATE_INTERVAL_MS = 5_000L
        const val UPDATE_DISTANCE_M = 15f
        const val STALE_AFTER_MS = 60_000L
    }
}
