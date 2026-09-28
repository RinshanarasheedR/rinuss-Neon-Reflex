package com.example.runner.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.runner.RunnerState
import com.example.runner.RunnerViewModel
import com.example.ui.components.NeonBackground

@Composable
fun RunnerAppSection(
    onBackToNeonReflex: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RunnerViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val achievements = viewModel.repository.getAchievements()

    BackHandler(enabled = state.state == RunnerState.MENU) {
        onBackToNeonReflex()
    }

    NeonBackground(modifier = modifier) {
        when (state.state) {
            RunnerState.MENU -> {
                RunnerStartScreen(
                    state = state,
                    achievements = achievements,
                    onPlayClick = { viewModel.startRun() },
                    onShowHowToPlay = { show -> viewModel.showHowToPlay(show) },
                    onShowAchievements = { show -> viewModel.showAchievements(show) },
                    onToggleSound = { viewModel.toggleSound() },
                    onBackToArcadeHub = onBackToNeonReflex
                )
            }

            RunnerState.COUNTDOWN,
            RunnerState.PLAYING,
            RunnerState.PAUSED -> {
                RunnerGameScreen(
                    state = state,
                    obstacles = viewModel.obstacles,
                    coins = viewModel.coins,
                    powerUps = viewModel.powerUps,
                    particles = viewModel.particles,
                    floatingTexts = viewModel.floatingTexts,
                    onSwipeLeft = { viewModel.swipeLeft() },
                    onSwipeRight = { viewModel.swipeRight() },
                    onSwipeUp = { viewModel.swipeUp() },
                    onSwipeDown = { viewModel.swipeDown() },
                    onPauseClick = { viewModel.pauseGame() },
                    onResumeClick = { viewModel.resumeGame() },
                    onRestartClick = { viewModel.startRun() },
                    onMenuClick = { viewModel.goToMenu() },
                    onToggleSound = { viewModel.toggleSound() }
                )
            }

            RunnerState.GAME_OVER -> {
                RunnerGameOverScreen(
                    state = state,
                    onPlayAgainClick = { viewModel.startRun() },
                    onMainMenuClick = { viewModel.goToMenu() }
                )
            }
        }
    }
}
