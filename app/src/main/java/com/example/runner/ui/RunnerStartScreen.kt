package com.example.runner.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.runner.RunnerAchievement
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

@Composable
fun RunnerStartScreen(
    state: RunnerUiState,
    achievements: List<RunnerAchievement>,
    onPlayClick: () -> Unit,
    onShowHowToPlay: (Boolean) -> Unit,
    onShowAchievements: (Boolean) -> Unit,
    onToggleSound: () -> Unit,
    onBackToArcadeHub: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "runner_title_pulse")
    val titlePulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titlePulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 14.dp)
                .widthIn(max = 500.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top navigation and audio controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back to Arcade Hub button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard.copy(alpha = 0.85f))
                        .border(BorderStroke(1.2.dp, NeonCyan.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
                        .clickable(onClick = onBackToArcadeHub)
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("runner_back_to_hub_button"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Neon Reflex",
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "NEON REFLEX",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        letterSpacing = 1.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NeonIconButton(
                        icon = if (state.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Toggle Sound",
                        onClick = onToggleSound,
                        tint = if (state.isSoundEnabled) NeonCyan else TextMuted,
                        testTag = "runner_sound_toggle_button"
                    )
                    NeonIconButton(
                        icon = Icons.Default.EmojiEvents,
                        contentDescription = "Achievements",
                        onClick = { onShowAchievements(true) },
                        tint = NeonGold,
                        testTag = "runner_achievements_icon_button"
                    )
                }
            }

            // Title & Visual Identity
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                // Animated Runner Emblem
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .scale(titlePulse)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    NeonGreen.copy(alpha = 0.25f),
                                    NeonCyan.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(BorderStroke(2.dp, NeonGreen), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsRun,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "RUN RUSH",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 3.5.sp,
                    color = NeonGreen,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "“Run. Dodge. Collect. Survive.”",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Stats Card (Best Score, Best Distance, Total Coins)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.2.dp, NeonGreen.copy(alpha = 0.6f)), RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = NeonGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "BEST SCORE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = NeonGold
                        )
                    }

                    Text(
                        text = "${state.bestScore}",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        RunnerStatItem(label = "BEST DISTANCE", value = "${state.bestDistance}m", color = NeonCyan)
                        RunnerStatItem(label = "TOTAL COINS", value = "🪙 ${state.totalCoins}", color = NeonGold)
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                NeonPrimaryButton(
                    text = "PLAY",
                    icon = Icons.Default.PlayArrow,
                    onClick = onPlayClick,
                    neonColor = NeonGreen,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "runner_play_button"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NeonSecondaryButton(
                        text = "HOW TO PLAY",
                        icon = Icons.Default.HelpOutline,
                        onClick = { onShowHowToPlay(true) },
                        neonColor = NeonCyan,
                        modifier = Modifier.weight(1f),
                        testTag = "runner_how_to_play_button"
                    )

                    NeonSecondaryButton(
                        text = "ACHIEVEMENTS",
                        icon = Icons.Default.EmojiEvents,
                        onClick = { onShowAchievements(true) },
                        neonColor = NeonGold,
                        modifier = Modifier.weight(1f),
                        testTag = "runner_achievements_button"
                    )
                }
            }
        }

        // How To Play Modal
        AnimatedVisibility(
            visible = state.showHowToPlay,
            enter = fadeIn() + scaleIn(initialScale = 0.92f, animationSpec = tween(200, easing = FastOutSlowInEasing)),
            exit = fadeOut() + scaleOut(targetScale = 0.92f, animationSpec = tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000))
                    .clickable { onShowHowToPlay(false) }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 440.dp)
                        .border(BorderStroke(1.5.dp, NeonGreen.copy(alpha = 0.8f)), RoundedCornerShape(24.dp))
                        .clickable(enabled = false) {},
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(22.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "HOW TO PLAY",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen,
                                letterSpacing = 2.sp
                            )
                            NeonIconButton(
                                icon = Icons.Default.Close,
                                contentDescription = "Close",
                                onClick = { onShowHowToPlay(false) },
                                tint = TextSecondary,
                                modifier = Modifier.size(36.dp),
                                testTag = "runner_close_how_to_play_button"
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Swipe to switch between 3 lanes, jump over road hazards, and slide under laser gates! Collect coins and power-ups as you race forward.",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "CONTROLS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        ControlRow(action = "👈 SWIPE LEFT / RIGHT", desc = "Switch between the 3 running lanes")
                        ControlRow(action = "👆 SWIPE UP", desc = "Jump over barriers and traffic cones")
                        ControlRow(action = "👇 SWIPE DOWN", desc = "Slide under overhead laser gates")

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "POWER-UPS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        PowerUpRow(icon = "🧲", title = "COIN MAGNET", desc = "Attracts coins from all 3 lanes", color = NeonGold)
                        PowerUpRow(icon = "🛡️", title = "CYBER SHIELD", desc = "Absorbs one collision safely", color = NeonCyan)
                        PowerUpRow(icon = "⚡", title = "2X MULTIPLIER", desc = "Doubles distance & coin score", color = NeonMagenta)
                        PowerUpRow(icon = "🚀", title = "SPEED SURGE", desc = "Invincible hyperspeed sprint", color = NeonGreen)

                        Spacer(modifier = Modifier.height(18.dp))

                        NeonPrimaryButton(
                            text = "START RUNNING",
                            onClick = {
                                onShowHowToPlay(false)
                                onPlayClick()
                            },
                            neonColor = NeonGreen,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "runner_dialog_play_button"
                        )
                    }
                }
            }
        }

        // Achievements Modal
        AnimatedVisibility(
            visible = state.showAchievements,
            enter = fadeIn() + scaleIn(initialScale = 0.92f, animationSpec = tween(200, easing = FastOutSlowInEasing)),
            exit = fadeOut() + scaleOut(targetScale = 0.92f, animationSpec = tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xCC000000))
                    .clickable { onShowAchievements(false) }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 440.dp)
                        .border(BorderStroke(1.5.dp, NeonGold.copy(alpha = 0.8f)), RoundedCornerShape(24.dp))
                        .clickable(enabled = false) {},
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(22.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ACHIEVEMENTS",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGold,
                                letterSpacing = 2.sp
                            )
                            NeonIconButton(
                                icon = Icons.Default.Close,
                                contentDescription = "Close",
                                onClick = { onShowAchievements(false) },
                                tint = TextSecondary,
                                modifier = Modifier.size(36.dp),
                                testTag = "runner_close_achievements_button"
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        achievements.forEach { ach ->
                            AchievementItemRow(ach)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RunnerStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
        Text(text = value, fontSize = 18.sp, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ControlRow(action: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(SurfaceCard.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = action, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan, modifier = Modifier.width(135.dp))
        Text(text = desc, fontSize = 12.sp, color = TextPrimary)
    }
}

@Composable
private fun PowerUpRow(icon: String, title: String, desc: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(SurfaceCard.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "$icon $title", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.width(135.dp))
        Text(text = desc, fontSize = 12.sp, color = TextPrimary)
    }
}

@Composable
private fun AchievementItemRow(achievement: RunnerAchievement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(
                if (achievement.isUnlocked) SurfaceCard else SurfaceCard.copy(alpha = 0.35f),
                RoundedCornerShape(14.dp)
            )
            .border(
                BorderStroke(
                    1.dp,
                    if (achievement.isUnlocked) NeonGold.copy(alpha = 0.8f) else Color(0x33FFFFFF)
                ),
                RoundedCornerShape(14.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = achievement.icon, fontSize = 26.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = achievement.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (achievement.isUnlocked) TextPrimary else TextMuted
            )
            Text(
                text = achievement.description,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
        if (achievement.isUnlocked) {
            Text(
                text = "UNLOCKED ✓",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NeonGreen
            )
        } else {
            Text(
                text = "LOCKED",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextMuted
            )
        }
    }
}
