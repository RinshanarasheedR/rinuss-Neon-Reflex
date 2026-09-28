package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.example.game.FloatingText
import com.example.game.Particle

@Composable
fun ParticleExplosionView(
    particles: List<Particle>,
    floatingTexts: List<FloatingText>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // Draw all active bursting particles
        particles.forEach { p ->
            val alpha = (p.life / p.maxLife).coerceIn(0f, 1f)
            val currentSize = p.size * alpha

            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = currentSize,
                center = Offset(p.x, p.y)
            )

            // Outer subtle glow
            drawCircle(
                color = p.color.copy(alpha = alpha * 0.4f),
                radius = currentSize * 2.2f,
                center = Offset(p.x, p.y)
            )
        }

        // Draw floating score text with nativeCanvas
        drawContext.canvas.nativeCanvas.apply {
            floatingTexts.forEach { ft ->
                val alphaInt = (ft.alpha.coerceIn(0f, 1f) * 255).toInt()
                if (alphaInt > 0) {
                    val paintGlow = Paint().apply {
                        color = ft.color.toArgb()
                        this.alpha = (alphaInt * 0.6f).toInt()
                        textSize = 42f
                        typeface = Typeface.DEFAULT_BOLD
                        textAlign = Paint.Align.CENTER
                        style = Paint.Style.STROKE
                        strokeWidth = 6f
                        isAntiAlias = true
                    }
                    drawText(ft.text, ft.x, ft.y, paintGlow)

                    val paintFill = Paint().apply {
                        color = android.graphics.Color.WHITE
                        this.alpha = alphaInt
                        textSize = 42f
                        typeface = Typeface.DEFAULT_BOLD
                        textAlign = Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    drawText(ft.text, ft.x, ft.y, paintFill)
                }
            }
        }
    }
}
