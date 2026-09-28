package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.game.ReflexTarget
import com.example.game.TargetType
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NeonTargetView(
    target: ReflexTarget,
    onTap: (targetId: Long, x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val diameterDp = with(density) { (target.radius * 2f).toDp() }

    // Spawn bounce animation
    val scaleAnim = remember(target.id) { Animatable(0.3f) }
    LaunchedEffect(target.id) {
        scaleAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
        )
    }

    // Continuous subtle pulsing
    val infiniteTransition = rememberInfiniteTransition(label = "target_pulse_${target.id}")
    val pulseRing by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseRing"
    )

    // Center offset: target.x and target.y are centers in px
    val leftDp = with(density) { (target.x - target.radius).toDp() }
    val topDp = with(density) { (target.y - target.radius).toDp() }

    Box(
        modifier = modifier
            .offset(x = leftDp, y = topDp)
            .size(diameterDp)
            .testTag("game_target_${target.type.name.lowercase()}")
            .pointerInput(target.id) {
                detectTapGestures(
                    onPress = { offset ->
                        // Instant down detection for maximum reflex responsiveness!
                        onTap(target.id, target.x, target.y)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.size(diameterDp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val currentRadius = (size.width / 2f) * scaleAnim.value
            val type = target.type

            // Outer glow halo
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        type.glowColor,
                        type.glowColor.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = currentRadius * pulseRing
                ),
                radius = currentRadius * pulseRing,
                center = center
            )

            // Inner dark backing
            drawCircle(
                color = Color(0xDD0D1224),
                radius = currentRadius * 0.95f,
                center = center
            )

            // Timeout countdown perimeter arc
            val sweepAngle = target.remainingProgress * 360f
            drawArc(
                color = type.primaryColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - currentRadius * 0.92f, center.y - currentRadius * 0.92f),
                size = Size(currentRadius * 1.84f, currentRadius * 1.84f),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // Outer subtle border track
            drawCircle(
                color = type.primaryColor.copy(alpha = 0.25f),
                radius = currentRadius * 0.92f,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // Inner concentric reticle
            drawCircle(
                color = type.primaryColor.copy(alpha = 0.7f),
                radius = currentRadius * 0.62f,
                center = center,
                style = Stroke(width = 2f)
            )

            // Crosshair tick marks
            val tickLen = currentRadius * 0.20f
            val rInner = currentRadius * 0.45f
            val rOuter = rInner + tickLen
            // 4 ticks
            val angles = listOf(0.0, Math.PI / 2, Math.PI, 3 * Math.PI / 2)
            angles.forEach { angle ->
                val cosA = cos(angle).toFloat()
                val sinA = sin(angle).toFloat()
                drawLine(
                    color = type.primaryColor,
                    start = Offset(center.x + rInner * cosA, center.y + rInner * sinA),
                    end = Offset(center.x + rOuter * cosA, center.y + rOuter * sinA),
                    strokeWidth = 2.2f,
                    cap = StrokeCap.Round
                )
            }

            // Target Type Specific Iconography in Center
            when (type) {
                TargetType.NORMAL -> {
                    // Center bullseye dot and glowing diamond
                    drawCircle(
                        color = Color.White,
                        radius = currentRadius * 0.22f,
                        center = center
                    )
                    drawCircle(
                        color = type.primaryColor,
                        radius = currentRadius * 0.12f,
                        center = center
                    )
                }

                TargetType.GOLD -> {
                    // Golden 5-point Star
                    val starPath = Path()
                    val outerR = currentRadius * 0.38f
                    val innerR = outerR * 0.45f
                    for (i in 0 until 10) {
                        val r = if (i % 2 == 0) outerR else innerR
                        val a = (i * Math.PI / 5 - Math.PI / 2)
                        val px = (center.x + r * cos(a)).toFloat()
                        val py = (center.y + r * sin(a)).toFloat()
                        if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
                    }
                    starPath.close()
                    drawPath(starPath, color = Color(0xFFFFD700))
                    drawPath(starPath, color = Color.White, style = Stroke(width = 1.5f))
                }

                TargetType.COMBO -> {
                    // Neon Flame glyph
                    val flamePath = Path()
                    val r = currentRadius * 0.36f
                    flamePath.moveTo(center.x, center.y - r * 1.1f)
                    flamePath.cubicTo(
                        center.x + r * 0.8f, center.y - r * 0.4f,
                        center.x + r * 0.9f, center.y + r * 0.7f,
                        center.x, center.y + r * 1.0f
                    )
                    flamePath.cubicTo(
                        center.x - r * 0.9f, center.y + r * 0.7f,
                        center.x - r * 0.8f, center.y - r * 0.4f,
                        center.x, center.y - r * 1.1f
                    )
                    flamePath.close()
                    drawPath(flamePath, color = Color(0xFFFF007F))
                    drawCircle(color = Color(0xFFFFE600), radius = r * 0.35f, center = Offset(center.x, center.y + r * 0.3f))
                }

                TargetType.DANGER -> {
                    // Hazard Skull / Danger Cross (X)
                    val arm = currentRadius * 0.32f
                    drawLine(
                        color = Color.White,
                        start = Offset(center.x - arm, center.y - arm),
                        end = Offset(center.x + arm, center.y + arm),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color.White,
                        start = Offset(center.x + arm, center.y - arm),
                        end = Offset(center.x - arm, center.y + arm),
                        strokeWidth = 4f,
                        cap = StrokeCap.Round
                    )
                    // Warning center dot
                    drawCircle(
                        color = Color(0xFFFF2A6D),
                        radius = currentRadius * 0.15f,
                        center = center
                    )
                }
            }
        }
    }
}
