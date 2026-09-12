package com.automatelinux.localKnowledge.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.automatelinux.localKnowledge.data.Located
import com.automatelinux.localKnowledge.data.ageLabel
import com.automatelinux.localKnowledge.ui.components.DistanceLabel
import com.automatelinux.localKnowledge.ui.components.PendingBadge
import com.automatelinux.localKnowledge.ui.components.VerdictBadge

@Composable
fun DetailScreen(
    located: Located,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onConfirm: () -> Unit,
    onDelete: () -> Unit,
    onNavigate: () -> Unit,
) {
    val f = located.finding
    var confirmingDelete by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(f.need) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "חזרה")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "עריכה") }
                    IconButton(onClick = { confirmingDelete = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "מחיקה")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(f.place, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
                if (located.pending) PendingBadge() else VerdictBadge(f.verdict)
            }
            Spacer(Modifier.height(6.dp))
            DistanceLabel(located.metres, located.bearing)

            Spacer(Modifier.height(20.dp))
            // The sentence the whole record exists to carry, set at reading size.
            Text(f.method, style = MaterialTheme.typography.bodyLarge)

            Spacer(Modifier.height(20.dp))
            Text(
                buildString {
                    append("אושר לאחרונה ").append(ageLabel(f.confirmedAt))
                    if (f.confirmedN > 1) append(" · אושר ${f.confirmedN} פעמים")
                    append("\nנרשם ").append(ageLabel(f.foundAt))
                    f.accuracyM?.let { append(" · דיוק מיקום ").append(it).append(" מ׳") }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onNavigate,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("נווט לשם") }

            Spacer(Modifier.height(10.dp))
            // Confirming is how a note becomes knowledge — it is the only thing that
            // tells a reader two years from now whether this is still true.
            OutlinedButton(
                onClick = onConfirm,
                enabled = !located.pending,
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("הייתי כאן — עדיין נכון") }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (confirmingDelete) {
        AlertDialog(
            onDismissRequest = { confirmingDelete = false },
            title = { Text("למחוק את הרישום?") },
            text = { Text("\"${f.place}\" יימחק לגמרי. אין ביטול.") },
            confirmButton = {
                TextButton(onClick = { confirmingDelete = false; onDelete() }) { Text("מחק") }
            },
            dismissButton = {
                TextButton(onClick = { confirmingDelete = false }) { Text("ביטול") }
            },
        )
    }
}
