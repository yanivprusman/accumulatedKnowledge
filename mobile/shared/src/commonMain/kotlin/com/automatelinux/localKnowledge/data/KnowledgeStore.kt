package com.automatelinux.localKnowledge.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** A finding plus everything that is true about it from where you are standing. */
data class Located(
    val finding: Finding,
    val metres: Double?,
    val bearing: Double?,
    val pending: Boolean,
)

/**
 * Everything the screens read, and the only place that writes.
 *
 * The division of labour with the server is deliberate and narrow:
 *
 *  - **The server is the record.** The list you browse came from it, and a cached
 *    copy is kept only so the app opens useful with no signal — it is never a
 *    second place a finding can be edited.
 *  - **Capture is the one thing that must survive no coverage**, because a tent
 *    spot is learned exactly where there is none, and it cannot be re-learned
 *    later. A capture made offline goes to an outbox that the UI shows as
 *    unsent, and is retried; it is never silently merged.
 *  - **Confirm and delete require a connection** and say so when there isn't one.
 *    They act on a record the server already holds, and they can wait — queueing
 *    them would buy a second source of truth for no benefit.
 */
class KnowledgeStore(
    private val api: LocalKnowledgeApi,
    private val storage: KeyValueStore,
    private val scope: CoroutineScope,
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }
    private val listSerializer = ListSerializer(Finding.serializer())

    private val _synced = MutableStateFlow<List<Finding>>(emptyList())
    private val _outbox = MutableStateFlow<List<Finding>>(emptyList())
    private val _busy = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)
    private val _lastSync = MutableStateFlow<String?>(null)

    val synced: StateFlow<List<Finding>> = _synced.asStateFlow()
    val outbox: StateFlow<List<Finding>> = _outbox.asStateFlow()
    val busy: StateFlow<Boolean> = _busy.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()
    val lastSync: StateFlow<String?> = _lastSync.asStateFlow()

    init {
        _synced.value = read(KEY_CACHE)
        _outbox.value = read(KEY_OUTBOX)
        _lastSync.value = storage.get(KEY_SYNCED_AT)
    }

    /** Every finding worth showing, nearest first when the phone knows where it is.
     *
     *  Unsent captures sort to the top regardless of distance: they are the only
     *  rows that need the user to do something about them. */
    fun located(fix: Fix?, need: String?): List<Located> {
        val here = fix?.let { LatLon(it.lat, it.lon) }
        fun decorate(f: Finding, pending: Boolean) = Located(
            finding = f,
            metres = here?.let { distanceMetres(it, LatLon(f.lat, f.lon)) },
            bearing = here?.let { bearingDegrees(it, LatLon(f.lat, f.lon)) },
            pending = pending,
        )

        val matches = { f: Finding -> need == null || f.need == need }
        val pending = _outbox.value.filter(matches).map { decorate(it, true) }
        val saved = _synced.value.filter(matches).map { decorate(it, false) }

        // With no fix there is no nearest, and pretending otherwise would order the
        // list by something the user cannot see. Most-recently-confirmed instead,
        // and the screen says which ordering it is using.
        val ordered =
            if (here == null) saved.sortedByDescending { it.finding.confirmedAt }
            else saved.sortedBy { it.metres ?: Double.MAX_VALUE }
        return pending + ordered
    }

    /** Every need that actually appears in the records, commonest first — so the
     *  filter row reflects what this person keeps needing, not what was guessed. */
    fun needsInUse(): List<String> =
        (_outbox.value + _synced.value)
            .groupingBy { it.need }.eachCount()
            .entries.sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .map { it.key }

    /** Write a finding. Lands in the outbox first, so the record exists before the
     *  network is consulted and survives the app being killed on the drive home. */
    fun save(finding: Finding) {
        _outbox.value = _outbox.value.filterNot { it.id == finding.id } + finding
        persist()
        scope.launch { flush(); refresh() }
    }

    fun confirm(id: String) {
        val at = nowUtc()
        scope.launch {
            _busy.value = true
            val failure = api.confirm(id, at)
            _busy.value = false
            if (failure != null) { _error.value = failure; return@launch }
            // Reflect it immediately; the next refresh replaces it with the server's word.
            _synced.value = _synced.value.map {
                if (it.id == id) it.copy(confirmedAt = at, confirmedN = it.confirmedN + 1) else it
            }
            persist()
            refresh()
        }
    }

    /** A delete is a real delete. An unsent capture is dropped locally — there is
     *  nothing on the server to ask about. */
    fun delete(id: String) {
        if (_outbox.value.any { it.id == id }) {
            _outbox.value = _outbox.value.filterNot { it.id == id }
            persist()
            return
        }
        scope.launch {
            _busy.value = true
            val failure = api.delete(id)
            _busy.value = false
            if (failure != null) { _error.value = failure; return@launch }
            _synced.value = _synced.value.filterNot { it.id == id }
            persist()
        }
    }

    fun dismissError() { _error.value = null }

    fun sync() { scope.launch { flush(); refresh() } }

    /** Send what the outbox holds, oldest first. A capture only leaves the outbox
     *  when the server has accepted it. */
    private suspend fun flush() {
        if (_outbox.value.isEmpty()) return
        _busy.value = true
        var remaining = _outbox.value
        for (finding in _outbox.value.sortedBy { it.foundAt }) {
            val failure = api.save(finding)
            if (failure != null) { _error.value = failure; break }
            remaining = remaining.filterNot { it.id == finding.id }
            _outbox.value = remaining
            persist()
        }
        _busy.value = false
    }

    private suspend fun refresh() {
        _busy.value = true
        val response = api.list()
        _busy.value = false
        if (!response.ok) { _error.value = response.error ?: "לא הצלחתי לקרוא מהשרת"; return }
        _synced.value = response.findings
        _lastSync.value = nowUtc()
        storage.put(KEY_SYNCED_AT, _lastSync.value!!)
        persist()
    }

    private fun persist() {
        storage.put(KEY_CACHE, json.encodeToString(listSerializer, _synced.value))
        storage.put(KEY_OUTBOX, json.encodeToString(listSerializer, _outbox.value))
    }

    private fun read(key: String): List<Finding> =
        storage.get(key)?.let {
            runCatching { json.decodeFromString(listSerializer, it) }.getOrNull()
        } ?: emptyList()

    private companion object {
        const val KEY_CACHE = "cache.findings"
        const val KEY_OUTBOX = "outbox.findings"
        const val KEY_SYNCED_AT = "cache.syncedAt"
    }
}
