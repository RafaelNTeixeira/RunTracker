package com.runtracker.ui.screens.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtracker.data.db.entity.RunEntity
import com.runtracker.ui.screens.auth.AuthViewModel
import com.runtracker.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun DashboardScreen(
    authVm: AuthViewModel,
    vm: DashboardViewModel = hiltViewModel()
) {
    val username    by vm.username.collectAsStateWithLifecycle()
    val recentRuns  by vm.recentRuns.collectAsStateWithLifecycle()
    val allRuns     by vm.allRuns.collectAsStateWithLifecycle()
    val totalRuns   by vm.totalRuns.collectAsStateWithLifecycle()
    val totalDist   by vm.totalDistKm.collectAsStateWithLifecycle()
    val totalCal    by vm.totalCal.collectAsStateWithLifecycle()
    val bestPaceSec by vm.bestPaceSec.collectAsStateWithLifecycle()

    val hour = LocalDate.now().let { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greeting = when {
            hour < 12 -> "Good morning"
            hour < 18 -> "Good afternoon"
            else -> "Good evening"
        }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("$greeting,", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                    Text(username ?: "Runner", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                }
                TextButton(onClick = { authVm.logout() }) {
                    Text("Sign out", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Stat cards 2×2
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Total Runs",     "$totalRuns",                            "sessions",    GreenPrimary,  Modifier.weight(1f))
                    StatCard("Distance",       "%.1f".format(totalDist ?: 0.0),         "kilometres",  BlueAccent,    Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatCard("Best Pace",      bestPaceSec?.let { fmtPaceSec(it) } ?: "—", "min / km", RedAccent,   Modifier.weight(1f))
                    StatCard("Calories",       "${totalCal ?: 0}",                      "kcal",        OrangeAccent,  Modifier.weight(1f))
                }
            }
        }

        // Weekly chart
        item {
            Surface(color = DarkSurface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("WEEKLY KM", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                    Spacer(Modifier.height(12.dp))
                    WeeklyChart(allRuns)
                }
            }
        }

        // Recent runs
        item {
            Text("RECENT RUNS", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        if (recentRuns.isEmpty()) {
            item {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No runs yet — tap Log Run to start!", color = TextSecondary, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            items(recentRuns) { run ->
                MiniRunCard(run)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, unit: String, color: Color, modifier: Modifier = Modifier) {
    Surface(color = DarkSurface, shape = RoundedCornerShape(12.dp), modifier = modifier) {
        Column(Modifier.padding(14.dp)) {
            Box(Modifier.fillMaxWidth().height(2.dp).background(color, RoundedCornerShape(1.dp)))
            Spacer(Modifier.height(10.dp))
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            Text(value, style = MaterialTheme.typography.displaySmall.copy(fontSize = 28.sp), color = color, fontWeight = FontWeight.ExtraBold)
            Text(unit, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
    }
}

@Composable
private fun WeeklyChart(runs: List<RunEntity>) {
    val today = LocalDate.now()
    val days = (6 downTo 0).map { today.minusDays(it.toLong()) }
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val data = days.map { day ->
        val label = day.format(DateTimeFormatter.ofPattern("EEE")).take(3)
        val dist = runs.filter { it.runDate == day.format(fmt) }.sumOf { it.distanceKm }.toFloat()
        label to dist
    }
    val maxVal = data.maxOfOrNull { it.second }?.takeIf { it > 0 } ?: 1f

    Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
        val barW = size.width / (data.size * 1.6f)
        val gap  = (size.width - barW * data.size) / (data.size + 1)
        data.forEachIndexed { i, (_, v) ->
            val barH = (v / maxVal) * size.height * 0.85f
            val x = gap + i * (barW + gap)
            val y = size.height - barH
            if (barH > 0f) {
                drawRoundRect(
                    color = GreenPrimary,
                    topLeft = Offset(x, y),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(6f)
                )
            } else {
                drawRoundRect(
                    color = DarkSurface2,
                    topLeft = Offset(x, size.height - 4f),
                    size = Size(barW, 4f),
                    cornerRadius = CornerRadius(2f)
                )
            }
        }
    }

    // Day labels
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
        data.forEach { (label, _) ->
            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun MiniRunCard(run: RunEntity) {
    Surface(color = DarkSurface, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🏃", fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
            Column(Modifier.weight(1f)) {
                Text(run.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(fmtDate(run.runDate), style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("%.2f km".format(run.distanceKm), color = GreenPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(fmtDuration(run.durationSeconds), color = TextSecondary, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

fun fmtDuration(sec: Int): String {
    val h = sec / 3600; val m = (sec % 3600) / 60; val s = sec % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

fun fmtPaceSec(secPerKm: Double): String {
    val m = (secPerKm / 60).toInt(); val s = (secPerKm % 60).toInt()
    return "%d:%02d".format(m, s)
}

fun fmtDate(d: String): String = try {
    val (y, mo, day) = d.split("-").map { it.toInt() }
    val mon = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")[mo - 1]
    "$day $mon $y"
} catch (e: Exception) { d }
