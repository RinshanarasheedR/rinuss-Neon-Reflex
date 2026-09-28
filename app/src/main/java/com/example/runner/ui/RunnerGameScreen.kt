package com.example.runner.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runner.RunnerCoin
import com.example.runner.RunnerFloatingText
import com.example.runner.RunnerObstacle
import com.example.runner.RunnerParticle
import com.example.runner.RunnerPowerUpItem
import com.example.runner.RunnerState
import com.example.runner.RunnerUiState
import com.example.ui.components.NeonIconButton
import com.example.ui.components.NeonPrimaryButton
import com.example.ui.components.NeonSecondaryButton
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.abs

@Composable
fun RunnerGameScreen(
    state: RunnerUiState,
    obstacles: List<RunnerObstacle>,
    coins: List<RunnerCoin>,
    powerUps: List<RunnerPowerUpItem>,
    particles: List<RunnerParticle>,
    floatingTexts: List<RunnerFloatingText>,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onMenuClick: () -> Unit,
    onToggleSound: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        if (state.state == RunnerState.PLAYING) {
            onPauseClick()
        } else if (state.state == RunnerState.PAUSED) {
            onMenuClick()
        }
    }

    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDragEnd = {
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDragCancel = {
                        totalDragX = 0f
                        totalDragY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        totalDragX += dragAmount.x
                        totalDragY += dragAmount.y

                        val threshold = 36f
                        if (abs(totalDragX) > abs(totalDragY)) {
                            if (totalDragX < -threshold) {
                                onSwipeLeft()
                                totalDragX = 0f
                                totalDragY = 0f
                            } else if (totalDragX > threshold) {
                                onSwipeRight()
                                totalDragX = 0f
                                totalDragY = 0f
                            }
                        } else {
                            if (totalDragY < -threshold) {
                                onSwipeUp()
                                totalDragX = 0f
                                totalDragY = 0f
                            } else if (totalDragY > threshold) {
                                onSwipeDown()
                                totalDragX = 0f
                                totalDragY = 0f
                            }
                        }
                    }
                )
            }
    ) {
        // --- 3D CANVAS GAMEPLAY AREA ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = if (state.screenShake > 0f) (kotlin.random.Random.nextFloat() * 12f - 6f) else 0f
                    translationY = if (state.screenShake > 0f) (kotlin.random.Random.nextFloat() * 12f - 6f) else 0f
                }
        ) {
            RunnerCanvasRenderer(
                uiState = state,
                obstacles = obstacles,
                coins = coins,
                powerUps = powerUps,
                particles = particles,
                floatingTexts = floatingTexts
            )
        }

        // --- OVERLAY HUD AND CONTROLS ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP HUD
            RunnerTopHud(
                state = state,
                onPauseClick = onPauseClick,
                onToggleSound = onToggleSound
            )

            // Temporary Notification Banner (e.g. Shield active, 2x multiplier)
            if (state.bannerNotice != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    NeonGreen.copy(alpha = 0.85f),
                                    NeonCyan.copy(alpha = 0.85f)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = state.bannerNotice,
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }

            // BOTTOM QUICK ACTION CONTROLS (for fast tapping & accessibility)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left & Right Lane Switch Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RunnerQuickButton(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Lane Left",
                        onClick = onSwipeLeft,
                        tint = NeonCyan,
                        testTag = "runner_left_button"
                    )
                    RunnerQuickButton(
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Lane Right",
                        onClick = onSwipeRight,
                        tint = NeonCyan,
                        testTag = "runner_right_button"
                    )
                }

                // Jump & Slide Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RunnerQuickButton(
                        icon = Icons.Default.ArrowDownward,
                        contentDescription = "Slide Down",
                        onClick = onSwipeDown,
                        tint = NeonMagenta,
                        testTag = "runner_slide_button"
                    )
                    RunnerQuickButton(
                        icon = Icons.Default.ArrowUpward,
                        contentDescription = "Jump Up",
                        onClick = onSwipeUp,
                        tint = NeonGreen,
                        testTag = "runner_jump_button"
                    )
                }
            }
        }

        // FULLSCREEN COUNTDOWN OVERLAY
        if (state.state == RunnerState.COUNTDOWN) {
            RunnerCountdownOverlay(countdownText = state.countdownNumber)
        }

        // FULLSCREEN PAUSE OVERLAY
        if (state.state == RunnerState.PAUSED) {
            RunnerPauseOverlay(
                score = state.score,
                distance = state.distanceMeters,
                coins = state.coinsCollected,
                onResume = onResumeClick,
                onRestart = onRestartClick,
                onMainMenu = onMenuClick
            )
        }
    }
}

@Composable
private fun RunnerTopHud(
    state: RunnerUiState,
    onPauseClick: () -> Unit,
    onToggleSound: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(SurfaceDark.copy(alpha = 0.88f))
            .border(BorderStroke(1.2.dp, NeonGreen.copy(alpha = 0.4f)), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score
            Column {
                Text(text = "SCORE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Text(text = "${state.score}", fontSize = 22.sp, fontWeight = FontWeight.Black, color = TextPrimary)
            }

            // Distance
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "DISTANCE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Text(text = "${state.distanceMeters}m", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = NeonCyan)
            }

            // Coins
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "COINS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                Text(text = "🪙 ${state.coinsCollected}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = NeonGold)
            }

            // Audio & Pause controls
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                NeonIconButton(
                    icon = if (state.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = "Sound",
                    onClick = onToggleSound,
                    tint = if (state.isSoundEnabled) NeonCyan else TextMuted,
                    modifier = Modifier.size(38.dp),
                    testTag = "hud_sound_button"
                )
                NeonIconButton(
                    icon = Icons.Default.Pause,
                    contentDescription = "Pause",
                    onClick = onPauseClick,
                    tint = NeonGreen,
                    modifier = Modifier.size(38.dp),
                    testTag = "hud_pause_button"
                )
            }
        }

        // Active Power-ups Bar
        val hasActivePowerUps = state.activeShield || state.activeMagnet || state.activeMultiplier || state.activeSpeedBoost
        if (hasActivePowerUps) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.activeShield) {
                    PowerUpBadge(icon = "🛡️", label = "${state.shieldTimeLeftMs / 1000}s", color = NeonCyan)
                }
                if (state.activeMagnet) {
                    PowerUpBadge(icon = "🧲", label = "${state.magnetTimeLeftMs / 1000}s", color = NeonGold)
                }
                if (state.activeMultiplier) {
                    PowerUpBadge(icon = "⚡ 2X", label = "${state.multiplierTimeLeftMs / 1000}s", color = NeonMagenta)
                }
                if (state.activeSpeedBoost) {
                    PowerUpBadge(icon = "🚀 SPEED", label = "${state.speedBoostTimeLeftMs / 1000}s", color = NeonGreen)
                }
            }
        }
    }
}

@Composable
private fun PowerUpBadge(icon: String, label: String, color: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.2f))
            .border(BorderStroke(1.dp, color), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$icon $label", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun RunnerQuickButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    tint: Color,
    testTag: String
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(SurfaceDark.copy(alpha = 0.82f))
            .border(BorderStroke(1.5.dp, tint.copy(alpha = 0.7f)), CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun RunnerCountdownOverlay(countdownText: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "runner_countdown_anim")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xDD000000)),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(170.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonGreen.copy(alpha = 0.35f),
                            NeonCyan.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
                .border(BorderStroke(3.dp, NeonGreen), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = countdownText,
                fontSize = if (countdownText == "RUN!") 46.sp else 64.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun RunnerPauseOverlay(
    score: Int,
    distance: Int,
    coins: Int,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xCC000000))
            .clickable(enabled = false) {},
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp)
                .padding(24.dp)
                .border(BorderStroke(1.5.dp, NeonGreen.copy(alpha = 0.7f)), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "RUN PAUSED",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonGreen,
                    letterSpacing = 2.5.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "SCORE", fontSize = 11.sp, color = TextSecondary)
                        Text(text = "$score", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "DISTANCE", fontSize = 11.sp, color = TextSecondary)
                        Text(text = "${distance}m", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "COINS", fontSize = 11.sp, color = TextSecondary)
                        Text(text = "🪙 $coins", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NeonGold)
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                NeonPrimaryButton(
                    text = "RESUME",
                    icon = Icons.Default.PlayArrow,
                    onClick = onResume,
                    neonColor = NeonGreen,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "runner_pause_resume_button"
                )

                NeonSecondaryButton(
                    text = "RESTART",
                    icon = Icons.Default.Refresh,
                    onClick = onRestart,
                    neonColor = NeonGold,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "runner_pause_restart_button"
                )

                NeonSecondaryButton(
                    text = "MAIN MENU",
                    onClick = onMainMenu,
                    neonColor = NeonCyan,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "runner_pause_menu_button"
                )
            }
        }
    }
}
