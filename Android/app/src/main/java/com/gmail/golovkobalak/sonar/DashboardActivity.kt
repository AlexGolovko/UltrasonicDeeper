package com.gmail.golovkobalak.sonar

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.gmail.golovkobalak.sonar.ui.theme.SonarTheme

class DashboardActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            SonarTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                        Text(
                            text = "Your fishing trips",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Entry()
                    }
                }
            }
        }
    }

}

fun handleLogin(email: String, password: String) {
    Log.d("test", "$email $password")
}

fun handleSync(email: String, password: String) {
    Log.d("test", "$email $password")
}

@Composable
fun Entry() {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val tripsData by remember { mutableStateOf(getTripsData()) }
    if (tripsData.isNotEmpty()) {
        Text(text = tripsData)
    }
    LoginScreen(
        email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        onSubmit = { handleLogin(email, password) },
        onSync = { handleSync(email, password) }
    )
}

fun getTripsData(): String {
    return "Trip One : 123456789"
}


@Composable
fun LoginScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onSync: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailField(value = email, onValueChange = onEmailChange)
        Spacer(modifier = Modifier.height(8.dp))
        PasswordField(value = password, onValueChange = onPasswordChange)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = onSubmit) {
                Text("Login")
            }
            OutlinedButton(onClick = onSync) {
                Text("Sync")
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

