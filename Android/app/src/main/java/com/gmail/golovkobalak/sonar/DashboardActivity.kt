package com.gmail.golovkobalak.sonar

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Base64
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
import com.gmail.golovkobalak.sonar.config.DatabaseConfig
import com.gmail.golovkobalak.sonar.ui.theme.SonarTheme
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class DashboardActivity : ComponentActivity() {
    val client by lazy { OkHttpClient() }
    var tripData = ""

    var token: String by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GlobalScope.launch {
            tripData = getTripsData();
        }
        setContent {
            SonarTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom) {
                        Text(
                            text = "Your fishing trips",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Entry(this@DashboardActivity)
                    }
                }
            }
        }
    }

    fun handleLogin(email: String, password: String) {
        data class LoginRequest(val username: String, val password: String)

        val loginRequest = LoginRequest(email, password)
        val jsonBody = Gson().toJson(loginRequest)  // Convert to JSON (if needed)

        val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
        val request = Request.Builder().url("${BuildConfig.CLOUD_URL}/auth/login")  // Replace with your API endpoint
            .post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // Handle failure here (e.g., show error message)
                Log.e("Login", "Error logging in: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Handle successful login response (e.g., navigate to another screen)
                    val responseData = response.body?.string() ?: ""
                    Log.d("Login", "Login successful: $responseData")
                    val token = response.headers.size
                    Log.d("Login", "Login successful: $token")
                    // Assuming the cookie name is "session_id" (modify as needed)
                    this@DashboardActivity.token = response.headers["Set-Cookie"].orEmpty()
                    storeCredentials(email, password)
                } else {
                    // Handle unsuccessful login response (e.g., show error message)
                    Log.e("Login", "Login failed: ${response.code}")
                    getUserCred().edit()
                        .clear()
                        .apply()
                    this@DashboardActivity.token = ""
                }
            }

            private fun storeCredentials(email: String, password: String) {
                val encryptedPassword = Base64.encodeToString(password.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                val editor =
                    getUserCred().edit()
                editor.putString("encrypted_password", encryptedPassword)
                editor.putString("email", email)
                editor.apply()
            }

            private fun validateToken(cookieValue: String) {
                val request =
                    Request.Builder().url("${BuildConfig.CLOUD_URL}/auth/user")  // Replace with your API endpoint
                        .get().addHeader("Cookie", cookieValue).build()
                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.d("AUTH", "Login successful: $e")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        Log.d("AUTH", "Login successful: $response")
                    }
                })
            }
        })
    }

    private fun getUserCred(): SharedPreferences {
        return this.baseContext.getSharedPreferences("userCred", MODE_PRIVATE)
    }

    fun getStoredEmail(): String {
        return getUserCred().getString("email", "").orEmpty()
    }

    fun getStoredPassword(): String {
        val password = getUserCred().getString("encrypted_password", "").orEmpty()
        return Base64.decode(password.toByteArray(Charsets.UTF_8), Base64.NO_WRAP).decodeToString()
    }

    fun getTripsData(): String {
        val tripEntityRepoRepo = DatabaseConfig.db.tripEntityRepoRepo()
        val tripEntities = tripEntityRepoRepo.getAll()
        if (!tripEntities.isEmpty()) {
            return tripEntities.map { "${it.sessionId}:${it.date}\n" }
                .reduce { acc, line -> acc + line }
        }
        return "No trips saved";
    }

}

fun handleSync(email: String, password: String) {
    Log.d("test", "$email $password")
}

@Composable
fun Entry(dashboardActivity: DashboardActivity) {
    var email by remember { mutableStateOf(dashboardActivity.getStoredEmail()) }
    var password by remember { mutableStateOf(dashboardActivity.getStoredPassword()) }
    val tripsData by remember { mutableStateOf(dashboardActivity.tripData) }
    if (tripsData.isNotEmpty()) {
        Text(text = tripsData)
    }
    LoginScreen(email = email,
        onEmailChange = { email = it },
        password = password,
        onPasswordChange = { password = it },
        onLogin = { dashboardActivity.handleLogin(email, password) },
        onSync = { handleSync(email, password) })
}


@Composable
fun LoginScreen(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onSync: () -> Unit,

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


