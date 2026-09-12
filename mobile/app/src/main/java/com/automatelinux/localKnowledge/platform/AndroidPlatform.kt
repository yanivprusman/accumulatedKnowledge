package com.automatelinux.localKnowledge.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.automatelinux.localKnowledge.data.KeyValueStore
import com.automatelinux.localKnowledge.data.PlatformActions

/** SharedPreferences, which is exactly the right size for a cache and an outbox. */
class AndroidKeyValueStore(context: Context) : KeyValueStore {
    private val prefs = context.getSharedPreferences("local_knowledge", Context.MODE_PRIVATE)
    override fun get(key: String): String? = prefs.getString(key, null)
    override fun put(key: String, value: String) { prefs.edit().putString(key, value).apply() }
}

class AndroidActions(private val context: Context) : PlatformActions {
    /**
     * A `geo:` URI with a label, so the phone offers Waze or Maps or whatever is
     * installed. Naming one navigator in the code would decide for the person
     * holding the phone — and Waze, which is what gets used here, is not the one
     * Android picks by default.
     */
    override fun navigateTo(lat: Double, lon: Double, label: String) {
        val encoded = Uri.encode(label)
        val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon($encoded)")
        val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        runCatching { context.startActivity(intent) }
    }
}
