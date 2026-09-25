package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.HxMainScreen
import com.example.ui.screens.HxSplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.HxViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        var showSplash by rememberSaveable { mutableStateOf(true) }

        Surface(modifier = Modifier.fillMaxSize()) {
          val hxViewModel: HxViewModel = viewModel()

          Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
              visible = !showSplash,
              enter = fadeIn(animationSpec = tween(600)),
              exit = fadeOut(animationSpec = tween(300))
            ) {
              HxMainScreen(
                viewModel = hxViewModel,
                onShowSplashScreen = { showSplash = true }
              )
            }

            AnimatedVisibility(
              visible = showSplash,
              enter = fadeIn(animationSpec = tween(300)),
              exit = fadeOut(animationSpec = tween(600))
            ) {
              HxSplashScreen(
                onSplashFinished = { showSplash = false }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("HX") }
}

