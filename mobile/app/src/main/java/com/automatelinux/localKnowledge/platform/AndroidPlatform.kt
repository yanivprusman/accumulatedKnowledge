package com.automatelinux.localKnowledge.platform

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.automatelinux.localKnowledge.data.KeyValueStore
import com.automatelinux.localKnowledge.data.PlatformActions
import com.automatelinux.localKnowledge.data.internationalDigits

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
        start(Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lon?q=$lat,$lon($encoded)")))
    }

    /** ACTION_DIAL rather than ACTION_CALL: it needs no permission, and the green
     *  button stays the person's to press. */
    override fun call(phone: String) {
        start(Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + Uri.encode(phone))))
    }

    /** `wa.me` is WhatsApp's own link and an App Link, so it opens the chat in the
     *  app — or offers WhatsApp and WhatsApp Business when both are installed. */
    override fun openWhatsApp(phone: String) {
        val digits = internationalDigits(phone) ?: return
        start(Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$digits")))
    }

    private fun start(intent: Intent) {
        runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }
}
