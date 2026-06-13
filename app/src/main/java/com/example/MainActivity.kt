package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.ScreenState

class MainActivity : ComponentActivity() {
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val screenState by viewModel.screenState.collectAsState()

                    AnimatedContent(
                        targetState = screenState,
                        transitionSpec = {
                            fadeIn() togetherWith fadeOut()
                        },
                        label = "screen_navigation"
                    ) { target ->
                        when (target) {
                            ScreenState.MainMenu -> {
                                MainMenuScreen(viewModel = viewModel)
                            }
                            ScreenState.HeroSelection -> {
                                HeroSelectionScreen(viewModel = viewModel)
                            }
                            ScreenState.LevelSelection -> {
                                LevelSelectionScreen(viewModel = viewModel)
                            }
                            ScreenState.Gameplay -> {
                                GameplayScreen(viewModel = viewModel)
                            }
                            ScreenState.LevelComplete -> {
                                LevelCompleteScreen(viewModel = viewModel)
                            }
                            ScreenState.GameOver -> {
                                GameOverScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}
