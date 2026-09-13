package com.automatelinux.accumulatedKnowledge

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.automatelinux.accumulatedKnowledge.data.KnowledgeStore
import com.automatelinux.accumulatedKnowledge.data.AccumulatedKnowledgeApi
import com.automatelinux.accumulatedKnowledge.platform.AndroidActions
import com.automatelinux.accumulatedKnowledge.platform.AndroidKeyValueStore
import com.automatelinux.accumulatedKnowledge.platform.AndroidLocationProvider

/**
 * Thin Android launcher — every screen lives in the shared commonMain `App()`.
 *
 * All this does is build the three platform pieces, ask for location once, and
 * hand back presses to the shared navigator.
 */
class MainActivity : ComponentActivity() {

    private lateinit var location: AndroidLocationProvider
    private val navigator = Navigator()

    private val askLocation =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            // Granted or not, start() is safe to call — it returns immediately
            // without permission, and the screen already says what that means.
            location.start()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        location = AndroidLocationProvider(applicationContext)
        val api = AccumulatedKnowledgeApi(BuildConfig.API_BASE_URL, BuildConfig.API_TOKEN)
        val store = KnowledgeStore(api, AndroidKeyValueStore(applicationContext), lifecycleScope)
        val actions = AndroidActions(applicationContext)

        if (!location.hasPermission()) {
            askLocation.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            )
        }

        onBackPressedDispatcher.addCallback(this) {
            if (!navigator.back()) { isEnabled = false; onBackPressedDispatcher.onBackPressed() }
        }

        setContent { App(store, location, actions, navigator) }
    }

    override fun onDestroy() {
        location.stop()
        super.onDestroy()
    }
}

/** `addCallback` with a lambda body, without pulling in activity-ktx just for it. */
private fun androidx.activity.OnBackPressedDispatcher.addCallback(
    owner: androidx.lifecycle.LifecycleOwner,
    handler: androidx.activity.OnBackPressedCallback.() -> Unit,
) {
    addCallback(owner, object : androidx.activity.OnBackPressedCallback(true) {
        override fun handleOnBackPressed() = handler()
    })
}
