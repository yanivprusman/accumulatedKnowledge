package com.automatelinux.localKnowledge.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.automatelinux.localKnowledge.data.COMMON_NEEDS
import com.automatelinux.localKnowledge.data.Finding
import com.automatelinux.localKnowledge.data.Fix
import com.automatelinux.localKnowledge.data.Verdict
import com.automatelinux.localKnowledge.data.nowUtc
import kotlin.math.roundToInt

/**
 * Writing one down.
 *
 * The position is taken when the screen opens and is then FIXED — it is where you
 * were when you learned the thing, and it must not drift because you kept typing
 * while walking back to the car.
 */
@Composable
fun CaptureScreen(
    existing: Finding?,
    fix: Fix?,
    knownNeeds: List<String>,
    onSave: (Finding) -> Unit,
    onBack: () -> Unit,
) {
    val capturedFix = remember { existing?.let { Fix(it.lat, it.lon, it.accuracyM?.toFloat()) } ?: fix }

    var need by rememberSaveable { mutableStateOf(existing?.need ?: "") }
    var place by rememberSaveable { mutableStateOf(existing?.place ?: "") }
    var method by rememberSaveable { mutableStateOf(existing?.method ?: "") }
    var verdict by rememberSaveable { mutableStateOf(existing?.verdict ?: Verdict.WORKS) }

    val suggestions = remember(knownNeeds) { (knownNeeds + COMMON_NEEDS).distinct().take(14) }
    val complete = need.isNotBlank() && place.isNotBlank() && method.isNotBlank()
    val canSave = complete && capturedFix != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "מה עבד כאן" else "עריכה") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "חזרה")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            Text("מה חיפשת", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                suggestions.forEach { s ->
                    FilterChip(
                        selected = need == s,
                        onClick = { need = if (need == s) "" else s },
                        label = { Text(s) },
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            // Free text, always. The next thing he needs is one nobody listed.
            OutlinedTextField(
                value = need,
                onValueChange = { need = it },
                label = { Text("או כתוב משהו אחר") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = place,
                onValueChange = { place = it },
                label = { Text("איך תקרא למקום") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            Text("מה יצא מזה", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = verdict == Verdict.WORKS,
                    onClick = { verdict = Verdict.WORKS },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) { Text("עבד") }
                SegmentedButton(
                    selected = verdict == Verdict.AVOID,
                    onClick = { verdict = Verdict.AVOID },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) { Text("לא שווה") }
            }

            Spacer(Modifier.height(16.dp))
            // The hero field. Tall from the start, because a one-line box asks for
            // a label and this asks for the sentence that makes the record useful.
            OutlinedTextField(
                value = method,
                onValueChange = { method = it },
                label = { Text("מה בדיוק עובד") },
                placeholder = { Text("חנה בחוץ ליד השער ולך 360 מ׳ לחוף. מקלחות פתוחות, מים קרים.") },
                minLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))
            Text(
                text = capturedFix?.let {
                    val acc = it.accuracyM?.let { a -> " · דיוק ${a.roundToInt()} מ׳" } ?: ""
                    "נשמר כאן: ${format6(it.lat)}, ${format6(it.lon)}$acc"
                } ?: "אין עדיין מיקום. רישום בלי מיקום לא יעזור בפעם הבאה — חכה לקליטת GPS.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Start,
            )

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val f = capturedFix ?: return@Button
                    val now = nowUtc()
                    onSave(
                        Finding(
                            id = existing?.id ?: newId(),
                            need = need.trim(),
                            place = place.trim(),
                            lat = f.lat,
                            lon = f.lon,
                            accuracyM = f.accuracyM?.roundToInt(),
                            verdict = verdict,
                            method = method.trim(),
                            foundAt = existing?.foundAt ?: now,
                            confirmedAt = existing?.confirmedAt ?: now,
                            confirmedN = existing?.confirmedN ?: 1,
                            updatedAt = now,
                        ),
                    )
                },
                enabled = canSave,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("שמור") }

            Spacer(Modifier.height(32.dp))
        }
    }
}

private fun format6(v: Double): String {
    val scaled = (v * 1_000_000).roundToInt()
    val whole = scaled / 1_000_000
    val frac = (if (scaled < 0) -scaled else scaled) % 1_000_000
    return "$whole.${frac.toString().padStart(6, '0')}"
}

/** Ids are made on the device so a finding exists before any network does. */
private fun newId(): String {
    val chars = "0123456789abcdefghijklmnopqrstuvwxyz"
    val stamp = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    val tail = (1..8).map { chars.random() }.joinToString("")
    return "$stamp-$tail"
}
