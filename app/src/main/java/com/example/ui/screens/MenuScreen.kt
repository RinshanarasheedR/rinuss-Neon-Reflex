package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Vibration
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
import com.example.game.GameUiState
import com.example.ui.components.NeonIconButton
import com.example.ui.components.NeonPrimaryButton
import com.example.ui.components.NeonSecondaryButton
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonDanger
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun MenuScreen(
    state: GameUiState,
    totalGames: Int,
    maxCombo: Int,
    totalHits: Int,
    onPlayClick: () -> Unit,
    onOpenRunRush: () -> Unit,
    onShowHowToPlay: (Boolean) -> Unit,
    onToggleSound: () -> Unit,
    onToggleHaptic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "title_anim")
    val titleGlow by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "titleGlow"
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
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .widthIn(max = 500.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top settings toggles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    NeonIconButton(
                        icon = if (state.isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Toggle Sound",
                        onClick = onToggleSound,
                        tint = if (state.isSoundEnabled) NeonCyan else TextMuted,
                        testTag = "sound_toggle_button"
                    )
                    NeonIconButton(
                        icon = Icons.Default.Vibration,
                        contentDescription = "Toggle Haptics",
                        onClick = onToggleHaptic,
                        tint = if (state.isHapticEnabled) NeonMagenta else TextMuted,
                        testTag = "haptic_toggle_button"
                    )
                }

                NeonIconButton(
                    icon = Icons.Default.HelpOutline,
                    contentDescription = "How To Play",
                    onClick = { onShowHowToPlay(true) },
                    tint = NeonGold,
                    testTag = "how_to_play_icon_button"
                )
            }

            // Hero Brand Title & Emblem
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                // Futuristic Glowing Crosshair Emblem
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .scale(titleGlow)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    NeonCyan.copy(alpha = 0.25f),
                                    NeonMagenta.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                        .border(BorderStroke(2.dp, NeonCyan), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "NEON REFLEX",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    letterSpacing = 4.sp,
                    color = NeonCyan,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Test your reaction speed.",
                    fontSize = 15.sp,
                    color = TextSecondary,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            // Best Score Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.2.dp, NeonGold.copy(alpha = 0.6f)), RoundedCornerShape(20.dp)),
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
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "BEST SCORE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = NeonGold
                        )
                    }

                    Text(
                        text = "${state.bestScore}",
                        fontSize = 44.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Stats grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(label = "GAMES", value = "$totalGames")
                        StatItem(label = "MAX COMBO", value = "${maxOf(maxCombo, state.highestCombo)}x")
                        StatItem(label = "TOTAL HITS", value = "$totalHits")
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
                    text = "PLAY NEON REFLEX",
                    icon = Icons.Default.PlayArrow,
                    onClick = onPlayClick,
                    neonColor = NeonCyan,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "play_game_button"
                )

                // NEW ENDLESS RUNNER MINI-GAME CARD
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.5.dp, NeonGreen.copy(alpha = 0.8f)), RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenRunRush)
                        .testTag("menu_open_run_rush_card"),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard.copy(alpha = 0.95f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(NeonGreen.copy(alpha = 0.2f))
                                .border(BorderStroke(1.5.dp, NeonGreen), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsRun,
                                contentDescription = null,
                                tint = NeonGreen,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "RUN RUSH",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = NeonGreen,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NeonGreen)
                                        .padding(horizontal = 5.dp, vertical = 1.5.dp)
                                ) {
                                    Text(
                                        text = "NEW MINI-GAME",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.Black
                                    )
                                }
                            }
                            Text(
                                text = "3D Endless Runner • Dodge & Collect",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Play Run Rush",
                            tint = NeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                NeonSecondaryButton(
                    text = "HOW TO PLAY",
                    icon = Icons.Default.HelpOutline,
                    onClick = { onShowHowToPlay(true) },
                    neonColor = NeonMagenta,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "how_to_play_button"
                )
            }
        }

        // How To Play Modal Dialog Overlay
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
                        .border(BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.8f)), RoundedCornerShape(24.dp))
                        .clickable(enabled = false) {}, // prevent dismiss when clicking content
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
                                color = NeonCyan,
                                letterSpacing = 2.sp
                            )
                            NeonIconButton(
                                icon = Icons.Default.Close,
                                contentDescription = "Close",
                                onClick = { onShowHowToPlay(false) },
                                tint = TextSecondary,
                                modifier = Modifier.size(36.dp),
                                testTag = "close_how_to_play_button"
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Tap the glowing targets as quickly as possible. Build combos, collect bonus targets, and beat your high score in 30 seconds!",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "TARGET TYPES",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        TargetInfoRow(
                            badge = "🎯 NORMAL",
                            desc = "+10 pts. Tap fast (<450ms) for +5 bonus points!",
                            color = NeonCyan
                        )
                        TargetInfoRow(
                            badge = "⭐ GOLD",
                            desc = "+50 pts! Rare bonus target for massive scoring.",
                            color = NeonGold
                        )
                        TargetInfoRow(
                            badge = "🔥 COMBO",
                            desc = "+20 pts & boosts your combo streak multiplier instantly!",
                            color = NeonMagenta
                        )
                        TargetInfoRow(
                            badge = "💣 DANGER",
                            desc = "AVOID! -25 pts & resets combo! Let it expire safely.",
                            color = NeonDanger
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        NeonPrimaryButton(
                            text = "LET'S PLAY!",
                            onClick = {
                                onShowHowToPlay(false)
                                onPlayClick()
                            },
                            neonColor = NeonCyan,
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "lets_play_dialog_button"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.SemiBold)
        Text(text = value, fontSize = 16.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TargetInfoRow(badge: String, desc: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .background(SurfaceCard.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = badge,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(100.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = desc,
            fontSize = 12.sp,
            color = TextPrimary
        )
    }
}
