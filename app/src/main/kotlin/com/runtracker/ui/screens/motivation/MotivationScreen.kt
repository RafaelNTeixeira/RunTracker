package com.runtracker.ui.screens.motivation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtracker.ui.theme.*

@Composable
fun MotivationScreen(vm: MotivationViewModel = hiltViewModel()) {
    val quote  by vm.quote.collectAsStateWithLifecycle()
    val tip    by vm.tip.collectAsStateWithLifecycle()
    val streak by vm.streak.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("MOTIVATION", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
        Text("Keep showing up.", color = TextSecondary, style = MaterialTheme.typography.bodySmall)

        // Quote card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(Color(0xFF0D2818), Color(0xFF0D1117))), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                Text(
                    "${quote.text}",
                    style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                    color = TextPrimary,
                    lineHeight = 26.sp
                )
                Spacer(Modifier.height(14.dp))
                Text("— ${quote.author}", color = GreenPrimary, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodySmall)
            }
        }

        OutlinedButton(
            onClick = { vm.nextQuote() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = GreenPrimary),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = Brush.horizontalGradient(listOf(GreenSecondary, GreenPrimary))
            )
        ) {
            Text("🔄  New Quote", fontWeight = FontWeight.SemiBold)
        }

        // Tip card
        Surface(color = DarkSurface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(18.dp)) {
                Surface(
                    color = OrangeAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    Text("💡 TRAINING TIP", color = OrangeAccent, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                }
                Text(tip, style = MaterialTheme.typography.bodyMedium, color = TextPrimary, lineHeight = 22.sp)
            }
        }

        // Streak card
        Surface(color = DarkSurface, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$streak",
                    style = MaterialTheme.typography.displaySmall.copy(fontSize = 48.sp),
                    color = OrangeAccent,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(end = 16.dp)
                )
                Column {
                    Text("Day Streak 🔥", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        when {
                            streak == 0 -> "Log your first run to start a streak!"
                            streak == 1 -> "1 day active — keep it going!"
                            else -> "$streak consecutive days — amazing!"
                        },
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}
