package com.automatelinux.localKnowledge.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.automatelinux.localKnowledge.data.Fix
import com.automatelinux.localKnowledge.data.Located
import com.automatelinux.localKnowledge.data.ageLabel
import com.automatelinux.localKnowledge.ui.components.DistanceLabel
import com.automatelinux.localKnowledge.ui.components.NeedFilterRow
import com.automatelinux.localKnowledge.ui.components.NoticeStrip
import com.automatelinux.localKnowledge.ui.components.PendingBadge
import com.automatelinux.localKnowledge.ui.components.VerdictBadge

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
                title = { Text("ידע מקומי") },
                actions = {
                    IconButton(onClick = onSync) {
                        Icon(Icons.Default.Refresh, contentDescription = "רענון")
                    }
                },
            )
        },
        floatingActionButton = {
            // The primary act of the app is writing something down, so it is the
            // one control that is always on screen and says what it does in words.
            ExtendedFloatingActionButton(
                onClick = onCapture,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("מה עבד כאן") },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (busy) LinearProgressIndicator(Modifier.fillMaxWidth())

            // States the app names rather than hides. Each one changes what the
            // list below actually means, so saying nothing would be a lie of omission.
            if (!hasLocationPermission)
                NoticeStrip("בלי הרשאת מיקום הרשימה לפי מתי אושר לאחרונה, לא לפי קרבה")
            else if (fix == null)
                NoticeStrip("עוד אין מיקום — הרשימה לפי מתי אושר לאחרונה")
            if (outboxCount > 0)
                NoticeStrip("$outboxCount רישומים עוד לא נשלחו לשרת")

            Spacer(Modifier.height(8.dp))
            NeedFilterRow(needs, selectedNeed, onSelectNeed)
            Spacer(Modifier.height(8.dp))

            if (items.isEmpty()) EmptyState(selectedNeed)
            else LazyColumn(
                contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 96.dp),
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
    OutlinedCard(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    f.place,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Spacer(Modifier.width(8.dp))
                if (located.pending) PendingBadge() else VerdictBadge(f.verdict)
            }
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    f.need,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (located.metres != null) {
                    Text(
                        "  ·  ",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    DistanceLabel(located.metres, located.bearing)
                }
            }
            Spacer(Modifier.height(8.dp))
            // Two lines of the method, because the method is what makes the row
            // worth tapping — the place name alone is what every map already said.
            Text(
                f.method,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(6.dp))
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
private fun EmptyState(selectedNeed: String?) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (selectedNeed == null) "עוד אין כאן כלום" else "אין רישומים ל\"$selectedNeed\"",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "המפה כבר יודעת איפה הדברים. מה שהיא לא יודעת זה " +
                    "שהשער ננעל בלילה ושאפשר לחנות בחוץ וללכת 360 מטר. " +
                    "את זה כותבים כאן, במקום שבו למדת את זה.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
