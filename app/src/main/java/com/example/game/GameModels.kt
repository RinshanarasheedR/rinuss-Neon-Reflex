package com.example.game

import androidx.compose.ui.graphics.Color

enum class GameScreenState {
    MENU,
    COUNTDOWN,
    PLAYING,
    PAUSED,
    GAME_OVER
}

enum class TargetType(
    val title: String,
    val basePoints: Int,
    val primaryColor: Color,
    val glowColor: Color,
    val accentColor: Color
) {
    NORMAL(
        title = "Normal",
        basePoints = 10,
        primaryColor = Color(0xFF00F0FF), // Neon Cyan
        glowColor = Color(0x6600F0FF),
        accentColor = Color(0xFFFFFFFF)
    ),
    GOLD(
        title = "Gold Bonus",
        basePoints = 50,
        primaryColor = Color(0xFFFFD700), // Neon Amber Gold
        glowColor = Color(0x88FFD700),
        accentColor = Color(0xFFFFF7C2)
    ),
    COMBO(
        title = "Combo Booster",
        basePoints = 20,
        primaryColor = Color(0xFFFF007F), // Neon Magenta Pink
        glowColor = Color(0x77FF007F),
        accentColor = Color(0xFFFF80BF)
    ),
    DANGER(
        title = "Danger Hazard",
        basePoints = -25,
        primaryColor = Color(0xFFFF2A6D), // Neon Hazard Crimson
        glowColor = Color(0x88FF2A6D),
        accentColor = Color(0xFFFF8597)
    )
}

data class ReflexTarget(
    val id: Long,
    val type: TargetType,
    val x: Float,
    val y: Float,
    val radius: Float,
    val spawnTimeMs: Long,
    val lifespanMs: Long,
    val remainingProgress: Float = 1.0f
)

data class Particle(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var life: Float = 1.0f,
    val maxLife: Float = 1.0f,
    val size: Float = 6f
)

data class FloatingText(
    val id: Long,
    val text: String,
    val color: Color,
    val x: Float,
    var y: Float,
    var alpha: Float = 1.0f,
    var scale: Float = 1.0f
)
