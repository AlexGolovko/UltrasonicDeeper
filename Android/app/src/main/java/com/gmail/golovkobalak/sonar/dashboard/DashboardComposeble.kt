package com.gmail.golovkobalak.sonar.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.gmail.golovkobalak.sonar.DashboardActivity


@Composable
fun Entry(dashboardActivity: DashboardActivity) {
    var email by remember { mutableStateOf(dashboardActivity.getStoredEmail()) }
    var password by remember { mutableStateOf(dashboardActivity.getStoredPassword()) }
    val tripsData by remember { mutableStateOf(dashboardActivity.tripData) }
    if (tripsData.isNotEmpty()) {
        val trips = tripsData.map { "${it.sessionId}:${it.date}\n" }
            .reduce { acc, line -> acc + line } ?: "No trips saved"
        Text(text = trips)
    }
    LoginScreen(
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        onLogin = { dashboardActivity.handleLogin(email, password) })
}


@Composable
fun LoginScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailField(value = email, onValueChange = onEmailChange)
        Spacer(modifier = Modifier.height(8.dp))
        PasswordField(value = password, onValueChange = onPasswordChange)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onLogin) {
                Text("Login and Push trips to Cloud")
            }
        }
    }
}

@Composable
fun EmailField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Email") },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun PasswordField(value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Password") },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = PasswordVisualTransformation(),
    )
}


