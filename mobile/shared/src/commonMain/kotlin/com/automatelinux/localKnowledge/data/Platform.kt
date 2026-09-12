package com.automatelinux.localKnowledge.data

import kotlinx.coroutines.flow.StateFlow

/**
 * The three things the shared UI needs from the device it is running on.
 *
 * They are interfaces rather than `expect` declarations so the platform supplies
 * them at the top (MainActivity builds them and hands them to `App()`), which
 * keeps every screen testable with a fake and keeps the Android module a launcher.
 */

/** Somewhere small and durable to keep the cache and the outbox. */
interface KeyValueStore {
    fun get(key: String): String?
    fun put(key: String, value: String)
}

/** Where the phone is, as it changes. Null until the first fix arrives — and a
 *  null that persists is a state the UI names rather than papers over. */
interface LocationProvider {
    val fix: StateFlow<Fix?>
    /** False when the user has not granted location. The UI asks; it does not assume. */
    fun hasPermission(): Boolean
    fun start()
}

/** Handing a place to whatever the phone actually navigates with. */
interface PlatformActions {
    /** A `geo:` URI, so the phone offers Waze or Maps or whatever is installed —
     *  rather than this app deciding for the person holding it. */
    fun navigateTo(lat: Double, lon: Double, label: String)
}
