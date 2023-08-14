package com.gmail.golovkobalak.sonar

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gmail.golovkobalak.sonar.service.heavyLogicSimulation
import com.gmail.golovkobalak.sonar.ui.theme.SonarTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SonarTheme {
                MapScreen()
            }
        }
    }
}

@Composable
fun MapScreen() {
    val context = LocalContext.current
    val view = LocalView.current
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }

    Surface(modifier = Modifier.fillMaxSize()) {
        // Create a Box to stack the WebView and the button on top of each other
        Box(modifier = Modifier.fillMaxSize()) {
            // WebView in the background
            val url = "https://www.openstreetmap.org/"
            WebViewScreen(url)

            // Progress bar
            if (isLoading) {
                // Progress bar (LinearProgressIndicator)
                LinearProgressIndicator(
                    progress = progress, // Replace 0.5f with the actual progress value (0.0 to 1.0)
                    color = Color.Green, // Customize the color of the progress bar
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomEnd) // Align the progress bar to the top-center
                )
            }

            // "Download" button on top layer
            Button(
                onClick = {
                    // Implement your button's click action here
                    isLoading = true // Show the progress bar when the button is clicked
                    scope.launch(Dispatchers.Default) {
                        heavyLogicSimulation(progressCallback = { progressValue ->
                            // Update the progress value
                            progress = progressValue
                        })

                        // Once the heavy logic is completed, update the isLoading state
                        withContext(Dispatchers.Main) {
                            isLoading = false
                            Toast.makeText(context, "Button Clicked", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomEnd) // Align the button to the bottom-end (top layer)
            ) {
                Text("Download detailed map", fontSize = 18.sp)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapActivityPreview() {
    SonarTheme {
        MapScreen()
    }
}