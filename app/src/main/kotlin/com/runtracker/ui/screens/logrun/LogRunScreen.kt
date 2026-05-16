package com.runtracker.ui.screens.logrun

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtracker.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun LogRunScreen(
    onSaved: () -> Unit,
    vm: LogRunViewModel = hiltViewModel()
) {
    val saved by vm.saved.collectAsStateWithLifecycle()

    var title    by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var hours    by remember { mutableStateOf("0") }
    var minutes  by remember { mutableStateOf("") }
    var seconds  by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var notes    by remember { mutableStateOf("") }
    var date     by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }
    var error    by remember { mutableStateOf("") }

    LaunchedEffect(saved) {
        if (saved) { vm.resetSaved(); onSaved() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("LOG A RUN", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text("Record your latest session", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        if (error.isNotBlank()) {
            Surface(color = androidx.compose.ui.graphics.Color(0x26F85149), shape = RoundedCornerShape(8.dp)) {
                Text(error, color = RedAccent, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
            }
        }

        RunField("Run Title", title, onValueChange = { title = it }, placeholder = "Morning 5K, Evening jog…")

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            RunField("Distance (km)", distance, onValueChange = { distance = it },
                modifier = Modifier.weight(1f), keyboardType = KeyboardType.Decimal, placeholder = "5.0")
            RunField("Date (YYYY-MM-DD)", date, onValueChange = { date = it }, modifier = Modifier.weight(1f), placeholder = "2024-06-01")
        }

        Text("DURATION".uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            RunField("hh", hours,   onValueChange = { hours = it },   modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number, placeholder = "0")
            Text(":", color = TextSecondary, fontWeight = FontWeight.Bold)
            RunField("mm", minutes, onValueChange = { minutes = it }, modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number, placeholder = "30")
            Text(":", color = TextSecondary, fontWeight = FontWeight.Bold)
            RunField("ss", seconds, onValueChange = { seconds = it }, modifier = Modifier.weight(1f), keyboardType = KeyboardType.Number, placeholder = "00")
        }

        RunField("Calories (kcal)", calories, onValueChange = { calories = it }, keyboardType = KeyboardType.Number, placeholder = "300")
        RunField("Notes", notes, onValueChange = { notes = it }, placeholder = "How did it feel?", singleLine = false, minLines = 3)

        Button(
            onClick = {
                error = ""
                val dist = distance.toDoubleOrNull()
                val dur  = (hours.toIntOrNull() ?: 0) * 3600 + (minutes.toIntOrNull() ?: 0) * 60 + (seconds.toIntOrNull() ?: 0)
                when {
                    title.isBlank()    -> error = "Please enter a title"
                    dist == null || dist <= 0 -> error = "Please enter a valid distance"
                    dur == 0           -> error = "Please enter a duration"
                    date.isBlank()     -> error = "Please enter a date"
                    else -> vm.saveRun(title.trim(), dist, dur, calories.toIntOrNull() ?: 0, notes.trim(), date.trim())
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GreenSecondary)
        ) {
            Text("💾  Save Run", fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
fun RunField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column(modifier = modifier) {
        Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.padding(bottom = 4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            placeholder = { Text(placeholder, color = TextSecondary.copy(alpha = 0.5f)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary, unfocusedBorderColor = DarkBorder,
                focusedContainerColor = DarkSurface2, unfocusedContainerColor = DarkSurface2,
                focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, cursorColor = GreenPrimary
            ),
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}
