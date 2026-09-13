package com.automatelinux.accumulatedKnowledge

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue

sealed interface Route {
    data object Home : Route
    /** Null id = a new finding at the current position; an id = editing one. */
    data class Capture(val findingId: String?) : Route
    data class Detail(val findingId: String) : Route
}

/**
 * A hand-rolled back stack wired to the Activity's back dispatcher.
 *
 * Full-screen routes rather than bottom sheets: writing down what worked is the
 * main event, not an aside, and a sheet that can be half-dismissed while you are
 * typing a paragraph loses the paragraph.
 */
class Navigator {
    private val stack = mutableStateListOf<Route>(Route.Home)

    val current: Route get() = stack.last()
    val canGoBack: Boolean get() = stack.size > 1

    fun go(route: Route) { stack.add(route) }

    fun back(): Boolean {
        if (stack.size <= 1) return false
        stack.removeAt(stack.lastIndex)
        return true
    }

    /** After saving, the capture screen should not be somewhere back returns to. */
    fun backToHome() { while (stack.size > 1) stack.removeAt(stack.lastIndex) }
}
