package com.runtracker.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runtracker.ui.theme.*

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var isLogin by remember { mutableStateOf(true) }

    var username by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state) {
        if (state is AuthViewModel.State.Success) viewModel.resetState()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFF0D2818), DarkBg), radius = 1200f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🏃", fontSize = 52.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "RUNTRACKER",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 4.sp
                ),
                color = GreenPrimary
            )
            Text(
                "Log, plan & conquer your runs",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 32.dp)
            )

            // Tab switcher
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurface2,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(4.dp)) {
                    listOf("Sign In", "Register").forEachIndexed { i, label ->
                        val selected = (i == 0) == isLogin
                        Button(
                            onClick = { isLogin = i == 0; viewModel.resetState() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(9.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) DarkSurface else Color.Transparent,
                                contentColor   = if (selected) TextPrimary else TextSecondary
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) { Text(label, fontWeight = FontWeight.SemiBold) }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Error banner
            if (state is AuthViewModel.State.Error) {
                Surface(
                    color = Color(0x26F85149),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        (state as AuthViewModel.State.Error).msg,
                        color = RedAccent,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            AuthField("Username", username, onValueChange = { username = it })
            if (!isLogin) {
                Spacer(Modifier.height(12.dp))
                AuthField("Email", email, onValueChange = { email = it }, keyboardType = KeyboardType.Email)
            }
            Spacer(Modifier.height(12.dp))
            AuthField(
                "Password", password,
                onValueChange = { password = it },
                keyboardType = KeyboardType.Password,
                isPassword = true
            )
            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    if (isLogin) viewModel.login(username, password)
                    else viewModel.register(username, email, password)
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenSecondary),
                enabled = state !is AuthViewModel.State.Loading
            ) {
                if (state is AuthViewModel.State.Loading)
                    CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else
                    Text(if (isLogin) "Sign In" else "Create Account", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = GreenPrimary,
                unfocusedBorderColor = DarkBorder,
                focusedContainerColor   = DarkSurface2,
                unfocusedContainerColor = DarkSurface2,
                focusedTextColor   = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = GreenPrimary
            ),
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}
