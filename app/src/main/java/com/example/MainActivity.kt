package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.GameScreenState
import com.example.game.GameViewModel
import com.example.runner.ui.RunnerAppSection
import com.example.ui.components.NeonBackground
import com.example.ui.screens.GameOverScreen
import com.example.ui.screens.GameScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.NeonReflexTheme

enum class ActiveArcadeGame {
    NEON_REFLEX,
    RUN_RUSH
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NeonReflexTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BackgroundDark
                ) {
                    ArcadeHubContainer()
                }
            }
        }
    }
}

@Composable
fun ArcadeHubContainer() {
    var activeGame by rememberSaveable { mutableStateOf(ActiveArcadeGame.NEON_REFLEX) }

    when (activeGame) {
        ActiveArcadeGame.NEON_REFLEX -> {
            NeonReflexApp(
                onOpenRunRush = { activeGame = ActiveArcadeGame.RUN_RUSH }
            )
        }

        ActiveArcadeGame.RUN_RUSH -> {
            RunnerAppSection(
                onBackToNeonReflex = { activeGame = ActiveArcadeGame.NEON_REFLEX }
            )
        }
    }
}

@Composable
fun NeonReflexApp(
    onOpenRunRush: () -> Unit = {},
    viewModel: GameViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val totalGames by viewModel.totalGamesPlayed.collectAsStateWithLifecycle()
    val maxCombo by viewModel.allTimeMaxCombo.collectAsStateWithLifecycle()
    val totalHits by viewModel.allTimeTotalHits.collectAsStateWithLifecycle()

    NeonBackground {
        when (state.screenState) {
            GameScreenState.MENU -> {
                MenuScreen(
                    state = state,
                    totalGames = totalGames,
                    maxCombo = maxCombo,
                    totalHits = totalHits,
                    onPlayClick = { viewModel.startGame() },
                    onOpenRunRush = onOpenRunRush,
                    onShowHowToPlay = { show -> viewModel.showHowToPlay(show) },
                    onToggleSound = { viewModel.toggleSound() },
                    onToggleHaptic = { viewModel.toggleHaptic() }
                )
            }

            GameScreenState.COUNTDOWN,
            GameScreenState.PLAYING,
            GameScreenState.PAUSED -> {
                GameScreen(
                    state = state,
                    particles = viewModel.particles,
                    floatingTexts = viewModel.floatingTexts,
                    onTargetTap = { id, x, y -> viewModel.onTargetTapped(id, x, y) },
                    onSetPlayAreaSize = { width, height, density ->
                        viewModel.setPlayAreaSize(width, height, density)
                    },
                    onPauseClick = { viewModel.pauseGame() },
                    onResumeClick = { viewModel.resumeGame() },
                    onRestartClick = { viewModel.startGame() },
                    onMenuClick = { viewModel.goToMenu() },
                    onToggleSound = { viewModel.toggleSound() },
                    onToggleHaptic = { viewModel.toggleHaptic() }
                )
            }

            GameScreenState.GAME_OVER -> {
                GameOverScreen(
                    state = state,
                    onPlayAgainClick = { viewModel.startGame() },
                    onMainMenuClick = { viewModel.goToMenu() }
                )
            }
        }
    }
}
