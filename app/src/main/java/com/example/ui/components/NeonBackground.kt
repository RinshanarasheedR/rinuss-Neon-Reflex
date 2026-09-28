package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BackgroundDarkSecondary
import kotlin.random.Random

private data class AmbientDot(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val speed: Float,
    val color: Color
)

@Composable
fun NeonBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_anim")
    val gridScroll by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gridScroll"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    val ambientDots = remember {
        val rand = Random(42)
        val colors = listOf(
            Color(0x3300F0FF),
            Color(0x33FF007F),
            Color(0x339D00FF),
            Color(0x223D5AFE)
        )
        List(24) {
            AmbientDot(
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat(),
                radius = rand.nextFloat() * 2.5f + 1.5f,
                speed = rand.nextFloat() * 0.15f + 0.05f,
                color = colors[rand.nextInt(colors.size)]
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundDark,
                        BackgroundDarkSecondary,
                        Color(0xFF060913)
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val gridSize = 50f

            // Cyber grid horizontal lines with animation
            val startY = (gridScroll % gridSize)
            var y = startY
            while (y < height) {
                val alpha = (0.04f + (y / height) * 0.06f)
                drawLine(
                    color = Color(0xFF00F0FF).copy(alpha = alpha),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.0f
                )
                y += gridSize
            }

            // Cyber grid vertical lines
            var x = 0f
            while (x < width) {
                drawLine(
                    color = Color(0xFF00F0FF).copy(alpha = 0.04f),
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 1.0f
                )
                x += gridSize
            }

            // Subtle center radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF9D00FF).copy(alpha = pulseGlow * 0.25f),
                        Color(0xFF00F0FF).copy(alpha = pulseGlow * 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(width * 0.5f, height * 0.45f),
                    radius = width * 0.7f
                ),
                radius = width * 0.7f,
                center = Offset(width * 0.5f, height * 0.45f)
            )

            // Ambient floating dust dots
            ambientDots.forEach { dot ->
                val currentY = ((dot.yRatio - (gridScroll * 0.005f * dot.speed)) % 1f + 1f) % 1f * height
                val currentX = dot.xRatio * width
                drawCircle(
                    color = dot.color,
                    radius = dot.radius,
                    center = Offset(currentX, currentY)
                )
            }
        }

        content()
    }
}
