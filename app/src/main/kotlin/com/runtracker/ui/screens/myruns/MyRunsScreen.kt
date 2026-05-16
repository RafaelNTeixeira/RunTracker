package com.runtracker.ui.screens.myruns

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtracker.data.db.entity.RunEntity
import com.runtracker.ui.screens.dashboard.fmtDate
import com.runtracker.ui.screens.dashboard.fmtDuration
import com.runtracker.ui.screens.dashboard.fmtPaceSec
import com.runtracker.ui.screens.logrun.RunField
import com.runtracker.ui.theme.*

@Composable
fun MyRunsScreen(vm: MyRunsViewModel = hiltViewModel()) {
    val runs by vm.runs.collectAsStateWithLifecycle()
    var editTarget by remember { mutableStateOf<RunEntity?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("MY RUNS", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text("${runs.size} sessions logged", color = TextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 16.dp))

        if (runs.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("👟", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("No runs yet", color = TextSecondary)
                    Text("Head to Log Run to get started!", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(runs, key = { it.id }) { run ->
                    RunCard(
                        run = run,
                        onEdit = { editTarget = run },
                        onDelete = { vm.delete(run) }
                    )
                }
            }
        }
    }

    editTarget?.let { run ->
        EditRunDialog(
            run = run,
            onDismiss = { editTarget = null },
            onSave = { updated -> vm.update(updated); editTarget = null }
        )
    }
}

@Composable
private fun RunCard(run: RunEntity, onEdit: () -> Unit, onDelete: () -> Unit) {
    val pace = if (run.distanceKm > 0) run.durationSeconds / run.distanceKm else 0.0
    Surface(color = DarkSurface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🏃", fontSize = 28.sp, modifier = Modifier.padding(end = 12.dp))
            Column(Modifier.weight(1f)) {
                Text(run.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(fmtDate(run.runDate), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Chip("%.2f km".format(run.distanceKm), GreenPrimary)
                    Chip(fmtDuration(run.durationSeconds), BlueAccent)
                    Chip(fmtPaceSec(pace) + "/km", OrangeAccent)
                }
            }
            Column {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RedAccent, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun Chip(text: String, color: androidx.compose.ui.graphics.Color) {
    Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(20.dp)) {
        Text(text, color = color, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

@Composable
private fun EditRunDialog(run: RunEntity, onDismiss: () -> Unit, onSave: (RunEntity) -> Unit) {
    var title    by remember { mutableStateOf(run.title) }
    var distance by remember { mutableStateOf(run.distanceKm.toString()) }
    var hours    by remember { mutableStateOf((run.durationSeconds / 3600).toString()) }
    var minutes  by remember { mutableStateOf(((run.durationSeconds % 3600) / 60).toString()) }
    var seconds  by remember { mutableStateOf((run.durationSeconds % 60).toString()) }
    var calories by remember { mutableStateOf(run.calories.toString()) }
    var notes    by remember { mutableStateOf(run.notes) }
    var date     by remember { mutableStateOf(run.runDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Edit Run", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RunField("Title", title, { title = it })
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    RunField("Distance (km)", distance, { distance = it }, Modifier.weight(1f), keyboardType = KeyboardType.Decimal)
                    RunField("Date", date, { date = it }, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    RunField("hh", hours,   { hours = it },   Modifier.weight(1f), keyboardType = KeyboardType.Number)
                    Text(":", color = TextSecondary, fontWeight = FontWeight.Bold)
                    RunField("mm", minutes, { minutes = it }, Modifier.weight(1f), keyboardType = KeyboardType.Number)
                    Text(":", color = TextSecondary, fontWeight = FontWeight.Bold)
                    RunField("ss", seconds, { seconds = it }, Modifier.weight(1f), keyboardType = KeyboardType.Number)
                }
                RunField("Calories", calories, { calories = it }, keyboardType = KeyboardType.Number)
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dur = (hours.toIntOrNull() ?: 0) * 3600 + (minutes.toIntOrNull() ?: 0) * 60 + (seconds.toIntOrNull() ?: 0)
                    onSave(run.copy(title = title, distanceKm = distance.toDoubleOrNull() ?: run.distanceKm,
                        durationSeconds = dur, calories = calories.toIntOrNull() ?: run.calories,
                        notes = notes, runDate = date))
                },
                colors = ButtonDefaults.buttonColors(containerColor = GreenSecondary)
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondary) }
        }
    )
}
