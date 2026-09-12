package com.automatelinux.localKnowledge.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.automatelinux.localKnowledge.data.Fix
import com.automatelinux.localKnowledge.data.Located
import com.automatelinux.localKnowledge.data.Verdict
import com.automatelinux.localKnowledge.data.ageLabel
import com.automatelinux.localKnowledge.ui.components.DistanceLabel
import com.automatelinux.localKnowledge.ui.components.NeedFilterRow
import com.automatelinux.localKnowledge.ui.components.NoticeStrip
import com.automatelinux.localKnowledge.ui.components.PendingBadge
import com.automatelinux.localKnowledge.ui.theme.VerdictColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    items: List<Located>,
    needs: List<String>,
    selectedNeed: String?,
    fix: Fix?,
    hasLocationPermission: Boolean,
    busy: Boolean,
    outboxCount: Int,
    onSelectNeed: (String?) -> Unit,
    onOpen: (String) -> Unit,
    onCapture: () -> Unit,
    onSync: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ידע מקומי", fontWeight = FontWeight.SemiBold) },
                actions = {
                    IconButton(onClick = onSync) {
                        Icon(Icons.Default.Refresh, contentDescription = "רענון")
                    }
                },
            )
        },
        floatingActionButton = {
            // The primary act of the app is writing something down, so it is the one
            // control always on screen and it says what it does in words.
            ExtendedFloatingActionButton(
                onClick = onCapture,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("מה עבד כאן", fontWeight = FontWeight.SemiBold) },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())

            // States the app names rather than hides. Each changes what the list
            // below actually means, so silence here would be a lie of omission.
            if (!hasLocationPermission)
                NoticeStrip("בלי הרשאת מיקום הרשימה לפי מתי אושר לאחרונה, לא לפי קרבה")
            else if (fix == null)
                NoticeStrip("עוד אין מיקום — הרשימה לפי מתי אושר לאחרונה")
            if (outboxCount > 0)
                NoticeStrip("$outboxCount רישומים עוד לא נשלחו לשרת")

            Spacer(Modifier.height(10.dp))
            NeedFilterRow(needs, selectedNeed, onSelectNeed)
            Spacer(Modifier.height(4.dp))

            if (items.isEmpty()) EmptyState(selectedNeed)
            else LazyColumn(
                contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items, key = { it.finding.id }) { FindingCard(it) { onOpen(it.finding.id) } }
            }
        }
    }
}

@Composable
private fun FindingCard(located: Located, onClick: () -> Unit) {
    val f = located.finding
    val accent =
        if (located.pending) MaterialTheme.colorScheme.secondary
        else if (f.verdict == Verdict.WORKS) VerdictColors.works
        else VerdictColors.avoid

    Row(
        Modifier
            .fillMaxWidth()
            // Without an intrinsic height the Row wraps its content and the rail's
            // fillMaxHeight resolves to nothing — the accent silently disappears.
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
    ) {
        // A colour rail rather than a badge: the whole list scans as works / avoid
        // / unsent without reading a single word.
        Box(Modifier.width(5.dp).fillMaxHeight().background(accent))

        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    f.place,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                if (located.pending) PendingBadge()
                else if (f.verdict == Verdict.AVOID) {
                    // Only the surprising verdict is labelled. Tagging every row
                    // "works" trains the eye to skip the word entirely.
                    Text(
                        "לא שווה",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = VerdictColors.avoid,
                    )
                }
            }

            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                NeedPill(f.need)
                if (located.metres != null) {
                    Spacer(Modifier.width(8.dp))
                    DistanceLabel(located.metres, located.bearing)
                }
            }

            Spacer(Modifier.height(8.dp))
            // Two lines of the method: it is what makes the row worth tapping. The
            // place name alone is what every map already told him.
            Text(
                f.method,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(8.dp))
            Text(
                buildString {
                    append("אושר ").append(ageLabel(f.confirmedAt))
                    if (f.confirmedN > 1) append(" · ${f.confirmedN} פעמים")
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun NeedPill(need: String) {
    Box(
        Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    ) {
        Text(
            need,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun EmptyState(selectedNeed: String?) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (selectedNeed == null) "עוד אין כאן כלום" else "אין רישומים ל\"$selectedNeed\"",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "המפה כבר יודעת איפה הדברים. מה שהיא לא יודעת זה שהשער ננעל " +
                    "בלילה ושאפשר לחנות בחוץ וללכת 360 מטר. את זה כותבים כאן, " +
                    "במקום שבו למדת את זה.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
