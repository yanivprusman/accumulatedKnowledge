package com.automatelinux.localKnowledge.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.automatelinux.localKnowledge.data.COMMON_NEEDS
import com.automatelinux.localKnowledge.data.Finding
import com.automatelinux.localKnowledge.data.Fix
import com.automatelinux.localKnowledge.data.Verdict
import com.automatelinux.localKnowledge.data.nowUtc
import com.automatelinux.localKnowledge.ui.components.KnowledgeChip
import com.automatelinux.localKnowledge.ui.theme.VerdictColors
import kotlin.math.roundToInt

/**
 * Writing one down.
 *
 * The order of this screen is the order of the thought: what you were after, what
 * actually worked, what you'd call the place. The method box sits second and
 * opens tall, because it is the sentence the whole record exists to carry — the
 * first draft of this screen put twelve chips above it and pushed it off the
 * bottom of the phone, which is exactly backwards.
 *
 * The position is taken when the screen opens and is then FIXED: it is where you
 * were when you learned the thing, and must not drift because you kept typing on
 * the walk back to the car.
 */
@OptIn(ExperimentalMaterial3Api::class)
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

    // What he has actually needed before comes first; the stock list fills in behind.
    val suggestions = remember(knownNeeds) { (knownNeeds + COMMON_NEEDS).distinct() }
    val canSave = need.isNotBlank() && place.isNotBlank() && method.isNotBlank() && capturedFix != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "מה עבד כאן" else "עריכה") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "חזרה")
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())) {

            SectionLabel("מה חיפשת", Modifier.padding(horizontal = 20.dp))
            // One scrolling row, not a wrapping block: twelve chips stacked three
            // deep are a wall, and the answer is usually in the first four.
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 8.dp),
            ) {
                items(suggestions) { s ->
                    KnowledgeChip(label = s, selected = need == s) { need = if (need == s) "" else s }
                }
            }
            Field(
                value = need,
                onValueChange = { need = it },
                label = "או משהו אחר",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            )

            Spacer(Modifier.height(10.dp))
            SectionLabel("מה בדיוק עובד", Modifier.padding(horizontal = 20.dp))
            Text(
                "המפה כבר יודעת איפה זה. כתוב את מה שהיא לא יודעת.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )
            OutlinedTextField(
                value = method,
                onValueChange = { method = it },
                placeholder = {
                    Text(
                        "חנה בחוץ ליד השער ולך 360 מ׳ לחוף. מקלחות פתוחות, מים קרים.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 17.sp),
                minLines = 6,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
            )

            Spacer(Modifier.height(14.dp))
            SectionLabel("איך תקרא למקום", Modifier.padding(horizontal = 20.dp))
            Field(
                value = place,
                onValueChange = { place = it },
                label = "שם שתזהה בעוד שנתיים",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            Spacer(Modifier.height(14.dp))
            SectionLabel("מה יצא מזה", Modifier.padding(horizontal = 20.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                VerdictOption("עבד", VerdictColors.works, verdict == Verdict.WORKS, Modifier.weight(1f)) {
                    verdict = Verdict.WORKS
                }
                VerdictOption("לא שווה", VerdictColors.avoid, verdict == Verdict.AVOID, Modifier.weight(1f)) {
                    verdict = Verdict.AVOID
                }
            }

            Spacer(Modifier.height(18.dp))
            PositionNote(capturedFix, Modifier.padding(horizontal = 20.dp))

            Spacer(Modifier.height(16.dp))
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
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(54.dp),
            ) { Text("שמור", style = MaterialTheme.typography.titleMedium) }

            // Says which field is still empty, rather than leaving a dead button.
            if (!canSave) {
                Text(
                    missingLabel(need, place, method, capturedFix),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 20.dp, end = 20.dp),
                )
            }

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(top = 8.dp),
    )
}

@Composable
private fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth(),
    )
}

/** The verdict reads in its own colour, so the choice is visible without reading. */
@Composable
private fun VerdictOption(
    label: String,
    color: Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        if (selected) color.copy(alpha = 0.18f) else Color.Transparent,
        label = "verdictBg",
    )
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) color else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PositionNote(fix: Fix?, modifier: Modifier = Modifier) {
    Text(
        text = fix?.let {
            val acc = it.accuracyM?.let { a -> " · דיוק ${a.roundToInt()} מ׳" } ?: ""
            "נשמר כאן: ${format6(it.lat)}, ${format6(it.lon)}$acc"
        } ?: "אין עדיין מיקום. רישום בלי מיקום לא יעזור בפעם הבאה — חכה לקליטת GPS.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}

private fun missingLabel(need: String, place: String, method: String, fix: Fix?): String {
    if (fix == null) return "אין מיקום — חכה לקליטת GPS"
    val missing = buildList {
        if (need.isBlank()) add("מה חיפשת")
        if (method.isBlank()) add("מה עובד")
        if (place.isBlank()) add("שם המקום")
    }
    return "חסר: " + missing.joinToString(", ")
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
