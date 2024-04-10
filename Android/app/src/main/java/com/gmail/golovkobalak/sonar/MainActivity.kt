package com.gmail.golovkobalak.sonar

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.gmail.golovkobalak.sonar.config.DatabaseConfig
import com.gmail.golovkobalak.sonar.service.Runner
import com.gmail.golovkobalak.sonar.ui.theme.SonarTheme

class MainActivity : ComponentActivity() {

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        val SESSION_ID = System.currentTimeMillis().toString()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SonarTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                }
            }
        }
        requestLocationPermission()
        DatabaseConfig.db = Room.databaseBuilder(
            applicationContext,
            DatabaseConfig::class.java,
            "sonar_database"
        ).fallbackToDestructiveMigrationFrom(1).build()
        Runner.start(baseContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        Runner.stop()
    }


    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

}

@Composable
fun MainScreen() {
    val context = LocalContext.current

    Surface(color = Color.White) {
        // Composable content of the main activity
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.padding(32.dp)) {
                Text(
                    text = "Ultrasonic Deeper",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                )
                Text(
                    text = "\n\n\nTurn on sonar\n\nTurn on wifi\n\nConnect to microsonar AP\nwith password = microsonar\n\nClick the button Open Sonar",
                    style = TextStyle(
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                )
            }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = {
                            // Start "DeeperActivity" when the button is clicked
                            val intent = Intent(context, DeeperActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text("Open Sonar")
                    }
                    Button(
                        onClick = {
                            // Start "MapActivity" when the button is clicked
                            val intent = Intent(context, MapActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text("Open Map")
                    }
                    Button(
                        onClick = {
                            val intent = Intent(context, DashboardActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text("Dashboard")
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SonarTheme {
        MainScreen()
    }
}