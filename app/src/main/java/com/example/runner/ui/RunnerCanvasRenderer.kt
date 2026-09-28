package com.example.runner.ui

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.runner.EnvironmentTheme
import com.example.runner.ObstacleType
import com.example.runner.PowerUpType
import com.example.runner.RunnerCoin
import com.example.runner.RunnerFloatingText
import com.example.runner.RunnerObstacle
import com.example.runner.RunnerParticle
import com.example.runner.RunnerPowerUpItem
import com.example.runner.RunnerUiState
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun RunnerCanvasRenderer(
    uiState: RunnerUiState,
    obstacles: List<RunnerObstacle>,
    coins: List<RunnerCoin>,
    powerUps: List<RunnerPowerUpItem>,
    particles: List<RunnerParticle>,
    floatingTexts: List<RunnerFloatingText>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        val horizonY = height * 0.35f
        val bottomY = height * 0.96f
        val centerX = width * 0.5f

        val theme = uiState.currentTheme

        // 1. Draw Sky & Cyber Skyline
        drawSkyAndSkyline(width, height, horizonY, theme)

        // 2. Draw 3D Track Base & Moving Perspective Grid
        draw3DTrack(width, horizonY, bottomY, centerX, uiState.distanceMeters, theme)

        // 3. Draw Perspective Objects sorted from Far to Near (Painter's algorithm)
        drawPerspectiveEntities(
            width = width,
            horizonY = horizonY,
            bottomY = bottomY,
            centerX = centerX,
            obstacles = obstacles,
            coins = coins,
            powerUps = powerUps,
            distanceMeters = uiState.distanceMeters
        )

        // 4. Draw Player Character "Volt Dash"
        drawPlayerCharacter(
            width = width,
            horizonY = horizonY,
            bottomY = bottomY,
            centerX = centerX,
            uiState = uiState
        )

        // 5. Draw Particles and Floating Texts
        drawParticlesAndText(particles, floatingTexts, width, height)

        // 6. Speed Warp Lines during Speed Boost
        if (uiState.activeSpeedBoost) {
            drawSpeedWarpLines(width, height, horizonY)
        }
    }
}

private fun DrawScope.drawSkyAndSkyline(
    width: Float,
    height: Float,
    horizonY: Float,
    theme: EnvironmentTheme
) {
    // Sky gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(theme.skyTopColor, theme.skyBottomColor),
            startY = 0f,
            endY = horizonY
        ),
        topLeft = Offset(0f, 0f),
        size = Size(width, horizonY)
    )

    // Distant futuristic skyscraper silhouettes
    val buildingCount = 14
    val buildingWidth = width / (buildingCount - 2)
    val heights = listOf(85f, 130f, 65f, 110f, 145f, 75f, 120f, 95f, 140f, 80f, 125f, 100f, 150f, 90f)

    for (i in 0 until buildingCount) {
        val bx = (i - 1) * buildingWidth
        val bh = heights[i % heights.size]
        val by = horizonY - bh

        drawRect(
            color = theme.buildingColor,
            topLeft = Offset(bx, by),
            size = Size(buildingWidth - 4f, bh)
        )

        // Glowing window grids
        var wy = by + 10f
        while (wy < horizonY - 10f) {
            drawRect(
                color = theme.railGlowColor.copy(alpha = if ((i + wy.toInt()) % 3 == 0) 0.5f else 0.15f),
                topLeft = Offset(bx + 6f, wy),
                size = Size(buildingWidth - 16f, 4f)
            )
            wy += 14f
        }
    }

    // Fictional Neon Billboards in the skyline
    val billboardX = width * 0.72f
    val billboardY = horizonY - 95f
    drawRect(
        color = Color(0x9910162A),
        topLeft = Offset(billboardX, billboardY),
        size = Size(90f, 26f)
    )
    drawRect(
        color = theme.railGlowColor,
        topLeft = Offset(billboardX, billboardY),
        size = Size(90f, 26f),
        style = Stroke(width = 1.5f)
    )
}

private fun DrawScope.draw3DTrack(
    width: Float,
    horizonY: Float,
    bottomY: Float,
    centerX: Float,
    distanceMeters: Int,
    theme: EnvironmentTheme
) {
    val roadWidthTop = width * 0.12f
    val roadWidthBottom = width * 0.92f

    // Road surface polygon
    val roadPath = Path().apply {
        moveTo(centerX - roadWidthTop / 2f, horizonY)
        lineTo(centerX + roadWidthTop / 2f, horizonY)
        lineTo(centerX + roadWidthBottom / 2f, bottomY)
        lineTo(centerX - roadWidthBottom / 2f, bottomY)
        close()
    }

    drawPath(roadPath, color = theme.trackBaseColor)

    // Moving horizontal grid lines (giving high speed motion effect)
    val gridCount = 9
    val speedOffset = (distanceMeters * 3.5f) % 100f
    for (i in 0 until gridCount) {
        val tNorm = ((i.toFloat() / gridCount) + (speedOffset / 100f / gridCount)) % 1f
        // Non-linear perspective distribution
        val depth = tNorm * tNorm
        val y = horizonY + (bottomY - horizonY) * depth
        val currentRoadW = roadWidthTop + (roadWidthBottom - roadWidthTop) * depth

        val alpha = (depth * 0.55f).coerceIn(0.08f, 0.6f)
        drawLine(
            color = theme.gridLineColor.copy(alpha = alpha),
            start = Offset(centerX - currentRoadW / 2f, y),
            end = Offset(centerX + currentRoadW / 2f, y),
            strokeWidth = 1.5f + (depth * 2f)
        )
    }

    // Lane divider stripes (separating 3 lanes)
    for (laneDivider in listOf(-0.33f, 0.33f)) {
        val topX = centerX + (roadWidthTop / 2f) * laneDivider
        val botX = centerX + (roadWidthBottom / 2f) * laneDivider
        drawLine(
            color = theme.railGlowColor.copy(alpha = 0.5f),
            start = Offset(topX, horizonY),
            end = Offset(botX, bottomY),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }

    // Outer Neon Curb Rails
    val leftTop = Offset(centerX - roadWidthTop / 2f, horizonY)
    val leftBot = Offset(centerX - roadWidthBottom / 2f, bottomY)
    val rightTop = Offset(centerX + roadWidthTop / 2f, horizonY)
    val rightBot = Offset(centerX + roadWidthBottom / 2f, bottomY)

    drawLine(theme.railGlowColor, leftTop, leftBot, strokeWidth = 4f)
    drawLine(theme.railGlowColor, rightTop, rightBot, strokeWidth = 4f)

    // Rail neon glow halo
    drawLine(theme.railGlowColor.copy(alpha = 0.35f), leftTop, leftBot, strokeWidth = 10f)
    drawLine(theme.railGlowColor.copy(alpha = 0.35f), rightTop, rightBot, strokeWidth = 10f)
}

private sealed class Renderable(val z: Float) {
    class ObstacleItem(val obs: RunnerObstacle) : Renderable(obs.z)
    class CoinItem(val coin: RunnerCoin) : Renderable(coin.z)
    class PowerUpItem(val item: RunnerPowerUpItem) : Renderable(item.z)
}

private fun DrawScope.drawPerspectiveEntities(
    width: Float,
    horizonY: Float,
    bottomY: Float,
    centerX: Float,
    obstacles: List<RunnerObstacle>,
    coins: List<RunnerCoin>,
    powerUps: List<RunnerPowerUpItem>,
    distanceMeters: Int
) {
    val roadWidthTop = width * 0.12f
    val roadWidthBottom = width * 0.92f

    // Collect all renderables and sort by distance descending (z from 1200 down to 0)
    val allItems = mutableListOf<Renderable>()
    obstacles.filter { it.z in 0f..1200f }.forEach { allItems.add(Renderable.ObstacleItem(it)) }
    coins.filter { !it.collected && it.z in 0f..1200f }.forEach { allItems.add(Renderable.CoinItem(it)) }
    powerUps.filter { !it.collected && it.z in 0f..1200f }.forEach { allItems.add(Renderable.PowerUpItem(it)) }

    allItems.sortByDescending { it.z }

    allItems.forEach { item ->
        val z = item.z
        val t = (z / 1200f).coerceIn(0f, 1f)
        val depth = (1f - t)
        val depthCurve = depth * depth // Quadratic perspective acceleration

        val currentY = horizonY + (bottomY - horizonY) * depthCurve
        val currentRoadW = roadWidthTop + (roadWidthBottom - roadWidthTop) * depthCurve
        val laneSpacing = currentRoadW / 3f
        val scale = 0.18f + (depthCurve * 0.82f)

        when (item) {
            is Renderable.ObstacleItem -> {
                val obs = item.obs
                val posX = centerX + (obs.lane * laneSpacing)
                drawObstacle3D(obs.type, posX, currentY, scale)
            }

            is Renderable.CoinItem -> {
                val coin = item.coin
                val posX = centerX + (coin.lane * laneSpacing)
                drawCoin3D(posX, currentY, scale, distanceMeters)
            }

            is Renderable.PowerUpItem -> {
                val pu = item.item
                val posX = centerX + (pu.lane * laneSpacing)
                drawPowerUp3D(pu.type, posX, currentY, scale)
            }
        }
    }
}

private fun DrawScope.drawObstacle3D(
    type: ObstacleType,
    x: Float,
    y: Float,
    scale: Float
) {
    when (type) {
        ObstacleType.ROAD_BARRIER -> {
            val w = 68f * scale
            val h = 32f * scale
            // Barricade legs
            drawLine(Color(0xFF8899AA), Offset(x - w / 2f + 4f, y), Offset(x - w / 2f + 4f, y - h), strokeWidth = 3f * scale)
            drawLine(Color(0xFF8899AA), Offset(x + w / 2f - 4f, y), Offset(x + w / 2f - 4f, y - h), strokeWidth = 3f * scale)

            // Main barricade board
            drawRect(
                color = type.primaryColor,
                topLeft = Offset(x - w / 2f, y - h),
                size = Size(w, h * 0.65f)
            )
            // Hazard chevrons
            drawRect(
                color = type.accentColor,
                topLeft = Offset(x - w * 0.25f, y - h),
                size = Size(w * 0.18f, h * 0.65f)
            )
            drawRect(
                color = type.accentColor,
                topLeft = Offset(x + w * 0.15f, y - h),
                size = Size(w * 0.18f, h * 0.65f)
            )
            // Glowing neon top rail
            drawLine(
                color = Color.White,
                start = Offset(x - w / 2f, y - h),
                end = Offset(x + w / 2f, y - h),
                strokeWidth = 2.5f * scale
            )
        }

        ObstacleType.OVERHANG_GATE -> {
            val w = 78f * scale
            val archH = 92f * scale
            val clearanceH = 46f * scale // Player can slide under!

            // Tall cyber arch side pillars
            drawLine(Color(0xFF00F0FF), Offset(x - w / 2f, y), Offset(x - w / 2f, y - archH), strokeWidth = 4f * scale)
            drawLine(Color(0xFF00F0FF), Offset(x + w / 2f, y), Offset(x + w / 2f, y - archH), strokeWidth = 4f * scale)

            // Overhead bridge
            drawRect(
                color = Color(0xFF0A1428),
                topLeft = Offset(x - w / 2f, y - archH),
                size = Size(w, archH - clearanceH)
            )
            // Pulsing laser warning beam across
            drawLine(
                color = Color(0xFFFF007F),
                start = Offset(x - w / 2f, y - clearanceH),
                end = Offset(x + w / 2f, y - clearanceH),
                strokeWidth = 3f * scale
            )
            drawCircle(Color.White, radius = 4f * scale, center = Offset(x, y - clearanceH))
        }

        ObstacleType.TRAFFIC_CONE -> {
            val coneW = 34f * scale
            val coneH = 40f * scale

            val conePath = Path().apply {
                moveTo(x, y - coneH)
                lineTo(x + coneW / 2f, y)
                lineTo(x - coneW / 2f, y)
                close()
            }
            drawPath(conePath, color = type.primaryColor)
            // Reflective white stripe
            drawLine(
                color = Color.White,
                start = Offset(x - coneW * 0.28f, y - coneH * 0.45f),
                end = Offset(x + coneW * 0.28f, y - coneH * 0.45f),
                strokeWidth = 4f * scale
            )
        }

        ObstacleType.CONSTRUCTION_BLOCK -> {
            val bw = 62f * scale
            val bh = 36f * scale
            drawRect(
                color = type.primaryColor,
                topLeft = Offset(x - bw / 2f, y - bh),
                size = Size(bw, bh)
            )
            drawRect(
                color = type.accentColor,
                topLeft = Offset(x - bw / 2f, y - bh),
                size = Size(bw, bh),
                style = Stroke(width = 2f * scale)
            )
            drawCircle(Color(0xFFFFD700), radius = 5f * scale, center = Offset(x, y - bh / 2f))
        }

        ObstacleType.CYBER_VEHICLE -> {
            val vw = 64f * scale
            val vh = 44f * scale

            // Cyber patrol car body
            drawRoundRect(
                color = type.primaryColor,
                topLeft = Offset(x - vw / 2f, y - vh),
                size = Size(vw, vh),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f * scale, 8f * scale)
            )
            // Cockpit windshield
            drawRect(
                color = Color(0xFF00F0FF),
                topLeft = Offset(x - vw * 0.35f, y - vh + 6f * scale),
                size = Size(vw * 0.7f, vh * 0.35f)
            )
            // Glowing neon rear brake lights
            drawCircle(Color(0xFFFF2A6D), radius = 4f * scale, center = Offset(x - vw * 0.35f, y - 6f * scale))
            drawCircle(Color(0xFFFF2A6D), radius = 4f * scale, center = Offset(x + vw * 0.35f, y - 6f * scale))
        }
    }
}

private fun DrawScope.drawCoin3D(
    x: Float,
    y: Float,
    scale: Float,
    distanceMeters: Int
) {
    val coinRadius = 14f * scale
    val floatY = y - 18f * scale

    // Horizontal spin simulation using cos
    val spin = cos(distanceMeters * 0.15f + x).toFloat()
    val currentWidth = (coinRadius * spin.coerceIn(-1f, 1f)).coerceAtLeast(2f)

    // Outer gold glow halo
    drawCircle(
        color = Color(0x66FFD700),
        radius = coinRadius * 1.5f,
        center = Offset(x, floatY)
    )

    // Gold coin body
    drawOval(
        color = Color(0xFFFFD700),
        topLeft = Offset(x - currentWidth, floatY - coinRadius),
        size = Size(currentWidth * 2f, coinRadius * 2f)
    )

    // Inner bright glint
    drawOval(
        color = Color(0xFFFFF7C2),
        topLeft = Offset(x - currentWidth * 0.5f, floatY - coinRadius * 0.6f),
        size = Size(currentWidth, coinRadius * 1.2f)
    )
}

private fun DrawScope.drawPowerUp3D(
    type: PowerUpType,
    x: Float,
    y: Float,
    scale: Float
) {
    val radius = 22f * scale
    val floatY = y - 26f * scale

    // Glowing energy sphere
    drawCircle(
        color = type.glowColor,
        radius = radius * 1.4f,
        center = Offset(x, floatY)
    )
    drawCircle(
        color = type.primaryColor,
        radius = radius,
        center = Offset(x, floatY)
    )
    drawCircle(
        color = Color.White,
        radius = radius * 0.85f,
        center = Offset(x, floatY),
        style = Stroke(width = 2f * scale)
    )

    // Draw emoji icon via native canvas
    drawContext.canvas.nativeCanvas.apply {
        val paint = Paint().apply {
            textSize = 28f * scale
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        drawText(type.icon, x, floatY + (9f * scale), paint)
    }
}

private fun DrawScope.drawPlayerCharacter(
    width: Float,
    horizonY: Float,
    bottomY: Float,
    centerX: Float,
    uiState: RunnerUiState
) {
    val roadWidthBottom = width * 0.92f
    val laneSpacing = roadWidthBottom / 3f

    val playerX = centerX + (uiState.laneProgress * laneSpacing)

    // Jump height offset
    val jumpOffset = uiState.jumpHeightNormalized * 115f
    val baseFootY = bottomY - 12f - jumpOffset

    val isSliding = uiState.isSliding
    val heightScale = if (isSliding) 0.52f else 1.0f

    val bodyWidth = 34f
    val bodyHeight = 58f * heightScale

    val characterY = baseFootY - bodyHeight

    // Running leg stride animation
    val stridePhase = (uiState.distanceMeters * 0.45f)
    val legAngle = sin(stridePhase).toFloat() * 18f

    // 1. Shadow underneath player on track
    val shadowW = (bodyWidth * 1.3f) / (1f + (uiState.jumpHeightNormalized * 0.6f))
    drawOval(
        color = Color(0x66000000),
        topLeft = Offset(playerX - shadowW / 2f, bottomY - 8f),
        size = Size(shadowW, 14f)
    )

    // 2. Thruster Flame Trails from Cyber Backpack
    val thrusterFlameLen = if (uiState.isJumping) 34f else 14f
    val flameColor = if (uiState.activeSpeedBoost) Color(0xFF00FF66) else Color(0xFF00F0FF)
    drawLine(
        color = flameColor,
        start = Offset(playerX - 8f, characterY + bodyHeight * 0.5f),
        end = Offset(playerX - 8f, characterY + bodyHeight * 0.5f + thrusterFlameLen),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = flameColor,
        start = Offset(playerX + 8f, characterY + bodyHeight * 0.5f),
        end = Offset(playerX + 8f, characterY + bodyHeight * 0.5f + thrusterFlameLen),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )

    // 3. Legs
    if (isSliding) {
        // Sliding posture: legs extended forward with spark friction
        drawLine(
            color = Color(0xFF141C34),
            start = Offset(playerX, characterY + bodyHeight),
            end = Offset(playerX + 22f, baseFootY),
            strokeWidth = 8f,
            cap = StrokeCap.Round
        )
        // Spark particles at boot sole
        drawCircle(Color(0xFFFFD700), radius = 5f, center = Offset(playerX + 24f, baseFootY))
        drawCircle(Color(0xFFFF007F), radius = 3f, center = Offset(playerX + 28f, baseFootY - 3f))
    } else {
        // Running or jumping legs
        val leftLegFootX = playerX - 9f - (legAngle * 0.4f)
        val rightLegFootX = playerX + 9f + (legAngle * 0.4f)
        val footY = baseFootY

        drawLine(
            color = Color(0xFF141C34),
            start = Offset(playerX - 6f, characterY + bodyHeight * 0.65f),
            end = Offset(leftLegFootX, footY),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color(0xFF141C34),
            start = Offset(playerX + 6f, characterY + bodyHeight * 0.65f),
            end = Offset(rightLegFootX, footY),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )
        // Neon energized boots
        drawCircle(Color(0xFF00F0FF), radius = 4f, center = Offset(leftLegFootX, footY))
        drawCircle(Color(0xFF00F0FF), radius = 4f, center = Offset(rightLegFootX, footY))
    }

    // 4. Torso & Cyber Armor
    drawRoundRect(
        color = Color(0xFF1E284A),
        topLeft = Offset(playerX - bodyWidth / 2f, characterY + 12f * heightScale),
        size = Size(bodyWidth, bodyHeight - 12f * heightScale),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
    )

    // Neon Cyber Core Chest Plate
    drawCircle(
        color = if (uiState.activeSpeedBoost) Color(0xFF00FF66) else Color(0xFF00F0FF),
        radius = 5f * heightScale,
        center = Offset(playerX, characterY + bodyHeight * 0.45f)
    )

    // 5. Head & Visor
    val headRadius = 11f * heightScale
    val headCenterY = characterY + 8f * heightScale
    drawCircle(Color(0xFF10172E), radius = headRadius, center = Offset(playerX, headCenterY))

    // Glowing Cyan Visor Line
    drawLine(
        color = Color(0xFF00F0FF),
        start = Offset(playerX - headRadius * 0.8f, headCenterY),
        end = Offset(playerX + headRadius * 0.8f, headCenterY),
        strokeWidth = 3.5f * heightScale,
        cap = StrokeCap.Round
    )

    // 6. Active Shield Forcefield Bubble
    if (uiState.activeShield) {
        val shieldR = 48f
        drawCircle(
            color = Color(0x3300F0FF),
            radius = shieldR,
            center = Offset(playerX, characterY + bodyHeight / 2f)
        )
        drawCircle(
            color = Color(0xFF00F0FF),
            radius = shieldR,
            center = Offset(playerX, characterY + bodyHeight / 2f),
            style = Stroke(width = 2.5f)
        )
    }

    // 7. Active Magnet Aura
    if (uiState.activeMagnet) {
        drawCircle(
            color = Color(0x22FFD700),
            radius = 54f,
            center = Offset(playerX, characterY + bodyHeight / 2f)
        )
        drawCircle(
            color = Color(0xFFFFD700),
            radius = 54f,
            center = Offset(playerX, characterY + bodyHeight / 2f),
            style = Stroke(width = 1.5f)
        )
    }
}

private fun DrawScope.drawParticlesAndText(
    particles: List<RunnerParticle>,
    floatingTexts: List<RunnerFloatingText>,
    width: Float,
    height: Float
) {
    // Draw active particles
    particles.forEach { p ->
        val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
        drawCircle(
            color = p.color.copy(alpha = alpha),
            radius = p.size * alpha,
            center = Offset(width * 0.5f + p.x, height * 0.78f + p.y)
        )
    }

    // Draw floating popup texts
    drawContext.canvas.nativeCanvas.apply {
        floatingTexts.forEach { ft ->
            val alphaInt = (ft.alpha.coerceIn(0f, 1f) * 255).toInt()
            if (alphaInt > 0) {
                val paint = Paint().apply {
                    color = ft.color.toArgb()
                    this.alpha = alphaInt
                    textSize = 40f
                    typeface = Typeface.DEFAULT_BOLD
                    textAlign = Paint.Align.CENTER
                    isAntiAlias = true
                }
                drawText(ft.text, width * 0.5f + ft.x, height * 0.72f - ft.y, paint)
            }
        }
    }
}

private fun DrawScope.drawSpeedWarpLines(
    width: Float,
    height: Float,
    horizonY: Float
) {
    val warpCount = 12
    for (i in 0 until warpCount) {
        val wx = (i * 73f) % width
        val startY = horizonY + ((i * 47f) % (height - horizonY))
        val len = 65f
        drawLine(
            color = Color(0x8800FF66),
            start = Offset(wx, startY),
            end = Offset(wx + (wx - width / 2f) * 0.2f, startY + len),
            strokeWidth = 2f
        )
    }
}
