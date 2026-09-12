package com.automatelinux.localKnowledge.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.automatelinux.localKnowledge.data.Verdict
import com.automatelinux.localKnowledge.data.compassPoint
import com.automatelinux.localKnowledge.data.formatDistance
import com.automatelinux.localKnowledge.ui.theme.VerdictColors

/** WORKS / AVOID, said in a word and a colour rather than a tick and a cross —
 *  the two are read at a glance in a list and must not be mistaken for each other. */
@Composable
fun VerdictBadge(verdict: Verdict, modifier: Modifier = Modifier) {
    val color = if (verdict == Verdict.WORKS) VerdictColors.works else VerdictColors.avoid
    Box(
        modifier
            .background(color.copy(alpha = 0.14f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            text = if (verdict == Verdict.WORKS) "עובד" else "לא שווה",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

/**
 * "360 מ׳ · צפון-מערב", or nothing at all.
 *
 * The direction is NAMED, never drawn as an arrow: an arrow means something only
 * against the way the phone is being held, and this app does not read the
 * compass. A north-up arrow would look authoritative and point wrong.
 */
@Composable
fun DistanceLabel(metres: Double?, bearing: Double?, modifier: Modifier = Modifier) {
    if (metres == null) return
    val text = buildString {
        append(formatDistance(metres))
        if (bearing != null) { append(" · "); append(compassPoint(bearing)) }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

/** The needs actually on record, as a filter. "הכל" clears it. */
@Composable
fun NeedFilterRow(
    needs: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (needs.isEmpty()) return
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text("הכל") },
            )
        }
        items(needs) { need ->
            FilterChip(
                selected = selected == need,
                onClick = { onSelect(if (selected == need) null else need) },
                label = { Text(need) },
            )
        }
    }
}

/** A capture that has not reached the server yet. Said plainly, because the user
 *  is the one who has to get back into coverage for it. */
@Composable
fun PendingBadge(modifier: Modifier = Modifier) {
    Box(
        modifier
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            "לא נשלח",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

/** A quiet full-width strip for "no fix yet" / "showing the cached list" — states
 *  the UI must name rather than hide behind a spinner. */
@Composable
fun NoticeStrip(text: String, tone: Color = MaterialTheme.colorScheme.surfaceVariant) {
    Row(
        Modifier
            .background(tone)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

internal val CardBorder: BorderStroke
    @Composable get() = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
