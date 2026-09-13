package com.automatelinux.accumulatedKnowledge.ui.components

import androidx.compose.ui.draw.clip
import androidx.compose.runtime.getValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.automatelinux.accumulatedKnowledge.data.Verdict
import com.automatelinux.accumulatedKnowledge.data.compassPoint
import com.automatelinux.accumulatedKnowledge.data.formatDistance
import com.automatelinux.accumulatedKnowledge.ui.theme.VerdictColors

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

/**
 * The app's own chip.
 *
 * Hand-rolled rather than Material's FilterChip: a selected FilterChip paints
 * itself from `secondaryContainer`, so on a green app it comes out lavender the
 * moment you tap it. This one carries the app's colour in both states.
 */
@Composable
fun KnowledgeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
        label = "chipBg",
    )
    Box(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = if (selected) 0f else 0.55f),
                shape = RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
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
        item { KnowledgeChip("הכל", selected == null) { onSelect(null) } }
        items(needs) { need ->
            KnowledgeChip(need, selected == need) { onSelect(if (selected == need) null else need) }
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
