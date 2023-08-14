package com.gmail.golovkobalak.sonar

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.gmail.golovkobalak.sonar.ui.theme.SonarTheme
import com.gmail.golovkobalak.sonar.util.CustomChromClient

class SonarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent {
            SonarTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    SonarScreen()
                }
            }
        }
    }
}

@Composable
fun SonarScreen() {
    val url = "https://www.openstreetmap.org/"
    WebViewScreen(url)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen(url: String) {
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    Surface(modifier = Modifier) {
        // WebView
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    // Set a WebViewClient to handle page navigation inside the WebView
                    webViewClient = WebViewClient()
                    webChromeClient = CustomChromClient(this.context)
                    settings.javaScriptEnabled = true // <-- This line

                    // Load the web page you want to display
                    loadUrl(url)
                }
            },
            update = { webView ->
                // WebView should be updated here if needed
                // For example, you can use webView.loadUrl("https://www.newurl.com")
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SonarActivityPreview() {
    SonarTheme {
        SonarScreen()
    }
}