package com.automatelinux.localKnowledge

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.automatelinux.localKnowledge.data.KnowledgeStore
import com.automatelinux.localKnowledge.data.LocationProvider
import com.automatelinux.localKnowledge.data.PlatformActions
import com.automatelinux.localKnowledge.data.spot
import com.automatelinux.localKnowledge.ui.CaptureScreen
import com.automatelinux.localKnowledge.ui.DetailScreen
import com.automatelinux.localKnowledge.ui.HomeScreen
import com.automatelinux.localKnowledge.ui.theme.AppTheme

/**
 * The whole app, shared between Android and (on a Mac) iOS.
 *
 * The platform supplies three things — storage, where the phone is, and how to
 * hand a coordinate or a phone number to the app that deals with it — and
 * everything else is here.
 */
@Composable
fun App(
    store: KnowledgeStore,
    location: LocationProvider,
    actions: PlatformActions,
    navigator: Navigator,
) {
    AppTheme {
        val synced by store.synced.collectAsState()
        val outbox by store.outbox.collectAsState()
        val busy by store.busy.collectAsState()
        val error by store.error.collectAsState()
        val fix by location.fix.collectAsState()
        var need by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) { location.start(); store.sync() }

        val all = remember(synced, outbox, fix, need) { store.located(fix, need) }
        val needs = remember(synced, outbox) { store.needsInUse() }
        fun find(id: String) = store.located(fix, null).firstOrNull { it.finding.id == id }

        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Box(Modifier.fillMaxSize()) {
                AnimatedContent(targetState = navigator.current, label = "route") { route ->
                    when (route) {
                        is Route.Home -> HomeScreen(
                            items = all,
                            needs = needs,
                            selectedNeed = need,
                            fix = fix,
                            hasLocationPermission = location.hasPermission(),
                            busy = busy,
                            outboxCount = outbox.size,
                            onSelectNeed = { need = it },
                            onOpen = { navigator.go(Route.Detail(it)) },
                            onCapture = { navigator.go(Route.Capture(null)) },
                            onSync = { store.sync() },
                        )

                        is Route.Capture -> CaptureScreen(
                            existing = route.findingId?.let { find(it)?.finding },
                            fix = fix,
                            knownNeeds = needs,
                            onSave = { store.save(it); navigator.backToHome() },
                            onBack = { navigator.back() },
                        )

                        is Route.Detail -> {
                            val located = find(route.findingId)
                            // The record can vanish under this screen — a delete on
                            // another device, or a refresh that no longer lists it.
                            if (located == null) {
                                LaunchedEffect(route.findingId) { navigator.back() }
                            } else {
                                val finding = located.finding
                                DetailScreen(
                                    located = located,
                                    onBack = { navigator.back() },
                                    onEdit = { navigator.go(Route.Capture(finding.id)) },
                                    onConfirm = { store.confirm(finding.id) },
                                    onDelete = { store.delete(finding.id); navigator.back() },
                                    onNavigate = {
                                        finding.spot?.let { actions.navigateTo(it.lat, it.lon, finding.place) }
                                    },
                                    onCall = { finding.phone?.let(actions::call) },
                                    onWhatsApp = { finding.phone?.let(actions::openWhatsApp) },
                                )
                            }
                        }
                    }
                }

                // Failures are sentences, and they stay until dismissed: one that
                // fades after four seconds is one the user misses while driving.
                error?.let {
                    Snackbar(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        action = { TextButton(onClick = { store.dismissError() }) { Text("סגור") } },
                    ) { Text(it) }
                }
            }
        }
    }
}
