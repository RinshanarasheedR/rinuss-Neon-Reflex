package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.FloatingText
import com.example.game.GameScreenState
import com.example.game.GameUiState
import com.example.game.Particle
import com.example.ui.components.NeonIconButton
import com.example.ui.components.NeonPrimaryButton
import com.example.ui.components.NeonSecondaryButton
import com.example.ui.components.NeonTargetView
import com.example.ui.components.ParticleExplosionView
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonDanger
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun GameScreen(
    state: GameUiState,
    particles: List<Particle>,
    floatingTexts: List<FloatingText>,
    onTargetTap: (Long, Float, Float) -> Unit,
    onSetPlayAreaSize: (Float, Float, Float) -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onMenuClick: () -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    // BackHandler to pause or go back
    BackHandler {
        if (state.screenState == GameScreenState.PLAYING) {
            onPauseClick()
        } else if (state.screenState == GameScreenState.PAUSED) {
            onMenuClick()
        }
    }

    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // --- TOP HUD ---
            TopHud(
                score = state.score,
                combo = state.combo,
                level = state.level,
                remainingTimeMs = state.remainingTimeMs,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // --- MAIN PLAY AREA ---
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(BorderStroke(1.2.dp, NeonCyan.copy(alpha = 0.3f)), RoundedCornerShape(20.dp))
                    .background(SurfaceDark.copy(alpha = 0.45f))
                    .graphicsLayer {
                        translationX = state.screenShakeOffsetX
                        translationY = state.screenShakeOffsetY
                    }
                    .testTag("game_play_area")
            ) {
                val widthPx = with(density) { maxWidth.toPx() }
                val heightPx = with(density) { maxHeight.toPx() }

                LaunchedEffect(widthPx, heightPx, density.density) {
                    onSetPlayAreaSize(widthPx, heightPx, density.density)
                }

                // Render current target if playing
                if (state.screenState == GameScreenState.PLAYING && state.currentTarget != null) {
                    NeonTargetView(
                        target = state.currentTarget,
                        onTap = onTargetTap
                    )
                }

                // Bursting particles and floating text
                ParticleExplosionView(
                    particles = particles,
                    floatingTexts = floatingTexts
                )

                // Temporary Level Up Banner
                if (state.levelUpBanner != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        NeonMagenta.copy(alpha = 0.85f),
                                        NeonCyan.copy(alpha = 0.85f)
                                    )
                                )
                            )
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = state.levelUpBanner,
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // --- BOTTOM CONTROLS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NeonIconButton(
                        icon = if (state.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Sound Toggle",
                        onClick = onToggleSound,
                        tint = if (state.isSoundEnabled) NeonCyan else TextMuted,
                        testTag = "game_sound_button"
                    )
                    NeonIconButton(
                        icon = Icons.Default.Vibration,
                        contentDescription = "Haptic Toggle",
                        onClick = onToggleHaptic,
                        tint = if (state.isHapticEnabled) NeonMagenta else TextMuted,
                        testTag = "game_haptic_button"
                    )
                }

                NeonIconButton(
                    icon = Icons.Default.Pause,
                    contentDescription = "Pause Game",
                    onClick = onPauseClick,
                    tint = NeonCyan,
                    testTag = "game_pause_button"
                )
            }
        }

        // --- FULLSCREEN COUNTDOWN OVERLAY ---
        if (state.screenState == GameScreenState.COUNTDOWN) {
            CountdownOverlay(countdownText = state.countdownNumber)
        }

        // --- FULLSCREEN PAUSE OVERLAY ---
        if (state.screenState == GameScreenState.PAUSED) {
            PauseOverlay(
                score = state.score,
                remainingTimeMs = state.remainingTimeMs,
                onResume = onResumeClick,
                onRestart = onRestartClick,
                onMainMenu = onMenuClick
            )
        }
    }
}

@Composable
private fun TopHud(
    score: Int,
    combo: Int,
    level: Int,
    remainingTimeMs: Long,
    modifier: Modifier = Modifier
) {
    val timeSeconds = remainingTimeMs / 1000f
    val timeProgress = (remainingTimeMs / 30000f).coerceIn(0f, 1f)
    val timeColor = when {
        remainingTimeMs < 5000L -> NeonDanger
        remainingTimeMs < 10000L -> NeonGold
        else -> NeonCyan
    }

    val multiplier = when {
        combo >= 15 -> 5
        combo >= 10 -> 4
        combo >= 6 -> 3
        combo >= 3 -> 2
        else -> 1
    }

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard.copy(alpha = 0.85f))
            .border(BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f)), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score
            Column {
                Text(
                    text = "SCORE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$score",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
            }

            // Combo Streak Badge
            if (combo > 1) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    NeonMagenta.copy(alpha = 0.3f),
                                    NeonGold.copy(alpha = 0.3f)
                                )
                            )
                        )
                        .border(BorderStroke(1.2.dp, NeonMagenta), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "x$multiplier COMBO ($combo)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonMagenta
                    )
                }
            } else {
                // Current Level badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(NeonCyan.copy(alpha = 0.15f))
                        .border(BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f)), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "LVL $level",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }

            // Remaining Time
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "TIME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = String.format(Locale.US, "%.1fs", timeSeconds),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = timeColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Neon Glowing Timer Bar
        LinearProgressIndicator(
            progress = { timeProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = timeColor,
            trackColor = SurfaceDark
        )
    }
}

@Composable
private fun CountdownOverlay(countdownText: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "countdown_anim")
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
                .size(160.dp)
                .scale(pulse)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonCyan.copy(alpha = 0.35f),
                            NeonMagenta.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
                .border(BorderStroke(3.dp, NeonCyan), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = countdownText,
                fontSize = if (countdownText == "GO!") 48.sp else 64.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PauseOverlay(
    score: Int,
    remainingTimeMs: Long,
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
                .border(BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.7f)), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "PAUSED",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = NeonCyan,
                    letterSpacing = 3.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "CURRENT SCORE", fontSize = 11.sp, color = TextSecondary)
                        Text(text = "$score", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "TIME LEFT", fontSize = 11.sp, color = TextSecondary)
                        Text(
                            text = String.format(Locale.US, "%.1fs", remainingTimeMs / 1000f),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                NeonPrimaryButton(
                    text = "RESUME",
                    icon = Icons.Default.PlayArrow,
                    onClick = onResume,
                    neonColor = NeonCyan,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "pause_resume_button"
                )

                NeonSecondaryButton(
                    text = "RESTART",
                    icon = Icons.Default.Refresh,
                    onClick = onRestart,
                    neonColor = NeonGold,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "pause_restart_button"
                )

                NeonSecondaryButton(
                    text = "MAIN MENU",
                    onClick = onMainMenu,
                    neonColor = NeonMagenta,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "pause_menu_button"
                )
            }
        }
    }
}
