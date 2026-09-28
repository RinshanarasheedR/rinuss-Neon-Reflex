package com.example.runner

import androidx.compose.ui.graphics.Color

enum class RunnerState {
    MENU,
    COUNTDOWN,
    PLAYING,
    PAUSED,
    GAME_OVER
}

enum class ObstacleType(
    val title: String,
    val canJumpOver: Boolean,
    val canSlideUnder: Boolean,
    val primaryColor: Color,
    val accentColor: Color
) {
    ROAD_BARRIER(
        title = "Road Barrier",
        canJumpOver = true,
        canSlideUnder = false,
        primaryColor = Color(0xFFFF2A6D), // Neon crimson
        accentColor = Color(0xFFFFE600)  // Hazard yellow
    ),
    OVERHANG_GATE(
        title = "Laser Gate",
        canJumpOver = false,
        canSlideUnder = true,
        primaryColor = Color(0xFF00F0FF), // Neon cyan
        accentColor = Color(0xFFFF007F)  // Neon magenta
    ),
    TRAFFIC_CONE(
        title = "Traffic Cone",
        canJumpOver = true,
        canSlideUnder = false,
        primaryColor = Color(0xFFFF7700), // Vibrant neon orange
        accentColor = Color(0xFFFFFFFF)
    ),
    CONSTRUCTION_BLOCK(
        title = "Concrete Block",
        canJumpOver = false,
        canSlideUnder = false,
        primaryColor = Color(0xFF9D00FF), // Cyber purple
        accentColor = Color(0xFFFFD700)
    ),
    CYBER_VEHICLE(
        title = "Patrol Cruiser",
        canJumpOver = false,
        canSlideUnder = false,
        primaryColor = Color(0xFF3D5AFE), // Electric Blue
        accentColor = Color(0xFFFF2A6D)
    )
}

enum class PowerUpType(
    val title: String,
    val icon: String,
    val durationMs: Long,
    val primaryColor: Color,
    val glowColor: Color
) {
    MAGNET(
        title = "Coin Magnet",
        icon = "🧲",
        durationMs = 9000L,
        primaryColor = Color(0xFFFFD700),
        glowColor = Color(0x66FFD700)
    ),
    SHIELD(
        title = "Cyber Shield",
        icon = "🛡️",
        durationMs = 12000L,
        primaryColor = Color(0xFF00F0FF),
        glowColor = Color(0x6600F0FF)
    ),
    SCORE_BOOSTER(
        title = "2X Multiplier",
        icon = "⚡",
        durationMs = 10000L,
        primaryColor = Color(0xFFFF007F),
        glowColor = Color(0x66FF007F)
    ),
    SPEED_BOOST(
        title = "Speed Surge",
        icon = "🚀",
        durationMs = 7000L,
        primaryColor = Color(0xFF00FF66),
        glowColor = Color(0x6600FF66)
    )
}

enum class EnvironmentTheme(
    val title: String,
    val skyTopColor: Color,
    val skyBottomColor: Color,
    val trackBaseColor: Color,
    val gridLineColor: Color,
    val railGlowColor: Color,
    val buildingColor: Color
) {
    NEON_DISTRICT(
        title = "Neon District",
        skyTopColor = Color(0xFF050814),
        skyBottomColor = Color(0xFF140D2E),
        trackBaseColor = Color(0xFF0A0F24),
        gridLineColor = Color(0x4400F0FF),
        railGlowColor = Color(0xFF00F0FF),
        buildingColor = Color(0xFF161C38)
    ),
    CYBER_HIGHWAY(
        title = "Cyber Highway",
        skyTopColor = Color(0xFF030A1A),
        skyBottomColor = Color(0xFF081C3D),
        trackBaseColor = Color(0xFF081226),
        gridLineColor = Color(0x443D5AFE),
        railGlowColor = Color(0xFF3D5AFE),
        buildingColor = Color(0xFF0E2248)
    ),
    CONSTRUCTION_ZONE(
        title = "Industrial Grid",
        skyTopColor = Color(0xFF140B04),
        skyBottomColor = Color(0xFF2E1705),
        trackBaseColor = Color(0xFF1A1108),
        gridLineColor = Color(0x44FFB300),
        railGlowColor = Color(0xFFFFB300),
        buildingColor = Color(0xFF2E1F12)
    ),
    UNDERGROUND_TUNNEL(
        title = "Quantum Tunnel",
        skyTopColor = Color(0xFF0A0314),
        skyBottomColor = Color(0xFF220A38),
        trackBaseColor = Color(0xFF140824),
        gridLineColor = Color(0x44FF007F),
        railGlowColor = Color(0xFFFF007F),
        buildingColor = Color(0xFF261040)
    )
}

data class RunnerObstacle(
    val id: Long,
    val lane: Int, // -1 (Left), 0 (Center), 1 (Right)
    var z: Float,  // Distance ahead in 3D units (e.g. 0 to 1400)
    val type: ObstacleType,
    val speedZ: Float = 0f
)

data class RunnerCoin(
    val id: Long,
    val lane: Int,
    var z: Float,
    var collected: Boolean = false
)

data class RunnerPowerUpItem(
    val id: Long,
    val lane: Int,
    var z: Float,
    val type: PowerUpType,
    var collected: Boolean = false
)

data class RunnerParticle(
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

data class RunnerFloatingText(
    val id: Long,
    val text: String,
    val color: Color,
    val x: Float,
    var y: Float,
    var alpha: Float = 1.0f,
    var scale: Float = 1.0f
)

data class RunnerAchievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val isUnlocked: Boolean
)
