package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

class NeonSoundManager(private val context: Context) {

    private val audioScope = CoroutineScope(Dispatchers.Default)
    private val sampleRate = 22050

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var isSoundEnabled: Boolean = true
    var isHapticEnabled: Boolean = true

    // Pre-generated sound byte arrays
    private val hitNormalSound: ByteArray by lazy {
        generateChirp(sampleRate, 800f, 1200f, 0.08f, decay = true)
    }

    private val hitFastSound: ByteArray by lazy {
        generateArpeggio(sampleRate, floatArrayOf(880f, 1320f), 0.12f)
    }

    private val hitGoldSound: ByteArray by lazy {
        generateArpeggio(sampleRate, floatArrayOf(1046f, 1318f, 1568f, 2093f), 0.22f)
    }

    private val comboBoostSound: ByteArray by lazy {
        generateArpeggio(sampleRate, floatArrayOf(659f, 880f, 1174f), 0.16f)
    }

    private val dangerSound: ByteArray by lazy {
        generateBuzz(sampleRate, 140f, 0.25f)
    }

    private val countdownTickSound: ByteArray by lazy {
        generateSineTone(sampleRate, 520f, 0.07f)
    }

    private val countdownGoSound: ByteArray by lazy {
        generateArpeggio(sampleRate, floatArrayOf(784f, 1046f, 1318f), 0.35f)
    }

    private val levelUpSound: ByteArray by lazy {
        generateArpeggio(sampleRate, floatArrayOf(523f, 659f, 784f, 1046f), 0.4f)
    }

    private val gameOverSound: ByteArray by lazy {
        generateChirp(sampleRate, 600f, 180f, 0.45f, decay = true)
    }

    private val highScoreSound: ByteArray by lazy {
        generateArpeggio(sampleRate, floatArrayOf(659f, 880f, 1046f, 1318f, 1760f), 0.6f)
    }

    private val runnerJumpSound: ByteArray by lazy {
        generateChirp(sampleRate, 380f, 840f, 0.14f, decay = true)
    }

    private val runnerSlideSound: ByteArray by lazy {
        generateChirp(sampleRate, 620f, 240f, 0.18f, decay = true)
    }

    private val runnerCoinSound: ByteArray by lazy {
        generateArpeggio(sampleRate, floatArrayOf(1318f, 1760f), 0.09f)
    }

    private val runnerPowerUpSound: ByteArray by lazy {
        generateArpeggio(sampleRate, floatArrayOf(523f, 659f, 784f, 1046f, 1318f), 0.35f)
    }

    private val runnerCrashSound: ByteArray by lazy {
        generateBuzz(sampleRate, 110f, 0.32f)
    }

    fun playHitNormal() {
        if (!isSoundEnabled) return
        playSound(hitNormalSound)
        vibrate(30)
    }

    fun playHitFast() {
        if (!isSoundEnabled) return
        playSound(hitFastSound)
        vibrate(45)
    }

    fun playHitGold() {
        if (!isSoundEnabled) return
        playSound(hitGoldSound)
        vibrate(60)
    }

    fun playHitCombo() {
        if (!isSoundEnabled) return
        playSound(comboBoostSound)
        vibrate(50)
    }

    fun playHitDanger() {
        if (!isSoundEnabled) return
        playSound(dangerSound)
        vibrate(100)
    }

    fun playCountdownTick() {
        if (!isSoundEnabled) return
        playSound(countdownTickSound)
        vibrate(20)
    }

    fun playCountdownGo() {
        if (!isSoundEnabled) return
        playSound(countdownGoSound)
        vibrate(60)
    }

    fun playLevelUp() {
        if (!isSoundEnabled) return
        playSound(levelUpSound)
        vibrate(80)
    }

    fun playGameOver(isNewHighScore: Boolean) {
        if (!isSoundEnabled) return
        if (isNewHighScore) {
            playSound(highScoreSound)
            vibrate(120)
        } else {
            playSound(gameOverSound)
            vibrate(80)
        }
    }

    fun playRunnerJump() {
        if (!isSoundEnabled) return
        playSound(runnerJumpSound)
        vibrate(35)
    }

    fun playRunnerSlide() {
        if (!isSoundEnabled) return
        playSound(runnerSlideSound)
        vibrate(30)
    }

    fun playRunnerCoin() {
        if (!isSoundEnabled) return
        playSound(runnerCoinSound)
        vibrate(15)
    }

    fun playRunnerPowerUp() {
        if (!isSoundEnabled) return
        playSound(runnerPowerUpSound)
        vibrate(80)
    }

    fun playRunnerCrash() {
        if (!isSoundEnabled) return
        playSound(runnerCrashSound)
        vibrate(140)
    }

    private fun playSound(audioData: ByteArray) {
        audioScope.launch {
            try {
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(audioData.size)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(audioData, 0, audioData.size)
                track.play()
                // Release after playback duration
                val durationMs = (audioData.size.toDouble() / (sampleRate * 2) * 1000).toLong() + 50
                kotlinx.coroutines.delay(durationMs)
                track.stop()
                track.release()
            } catch (_: Exception) {
                // Audio might not be available or interrupted; fail gracefully
            }
        }
    }

    private fun vibrate(durationMs: Long) {
        if (!isHapticEnabled) return
        try {
            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    it.vibrate(
                        VibrationEffect.createOneShot(
                            durationMs.coerceAtLeast(10),
                            VibrationEffect.DEFAULT_AMPLITUDE
                        )
                    )
                } else {
                    @Suppress("DEPRECATION")
                    it.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {
            // Ignore if vibration fails
        }
    }

    companion object {
        private fun generateSineTone(sampleRate: Int, freq: Float, durationSec: Float): ByteArray {
            val numSamples = (sampleRate * durationSec).toInt()
            val output = ByteArray(numSamples * 2)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val decay = 1.0 - (i.toDouble() / numSamples)
                val sampleVal = (sin(2.0 * Math.PI * freq * t) * decay * 28000.0).toInt().coerceIn(-32767, 32767)
                output[i * 2] = (sampleVal and 0xFF).toByte()
                output[i * 2 + 1] = ((sampleVal shr 8) and 0xFF).toByte()
            }
            return output
        }

        private fun generateChirp(sampleRate: Int, startFreq: Float, endFreq: Float, durationSec: Float, decay: Boolean): ByteArray {
            val numSamples = (sampleRate * durationSec).toInt()
            val output = ByteArray(numSamples * 2)
            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                phase += 2.0 * Math.PI * currentFreq / sampleRate
                val envelope = if (decay) (1.0 - progress * progress) else 1.0
                val sampleVal = (sin(phase) * envelope * 28000.0).toInt().coerceIn(-32767, 32767)
                output[i * 2] = (sampleVal and 0xFF).toByte()
                output[i * 2 + 1] = ((sampleVal shr 8) and 0xFF).toByte()
            }
            return output
        }

        private fun generateArpeggio(sampleRate: Int, freqs: FloatArray, totalDuration: Float): ByteArray {
            val noteDuration = totalDuration / freqs.size
            val numSamplesPerNote = (sampleRate * noteDuration).toInt()
            val totalSamples = numSamplesPerNote * freqs.size
            val output = ByteArray(totalSamples * 2)

            for (noteIdx in freqs.indices) {
                val freq = freqs[noteIdx]
                val offset = noteIdx * numSamplesPerNote
                for (i in 0 until numSamplesPerNote) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / numSamplesPerNote
                    val decay = (1.0 - progress * 0.8)
                    val sampleVal = (sin(2.0 * Math.PI * freq * t) * decay * 26000.0).toInt().coerceIn(-32767, 32767)
                    val outIdx = (offset + i) * 2
                    output[outIdx] = (sampleVal and 0xFF).toByte()
                    output[outIdx + 1] = ((sampleVal shr 8) and 0xFF).toByte()
                }
            }
            return output
        }

        private fun generateBuzz(sampleRate: Int, freq: Float, durationSec: Float): ByteArray {
            val numSamples = (sampleRate * durationSec).toInt()
            val output = ByteArray(numSamples * 2)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val progress = i.toDouble() / numSamples
                // Sawtooth + square wave for crunchy warning feel
                val phase = (freq * t) % 1.0
                val raw = if (phase < 0.5) 1.0 else -1.0
                val envelope = (1.0 - progress) * 24000.0
                val sampleVal = (raw * envelope).toInt().coerceIn(-32767, 32767)
                output[i * 2] = (sampleVal and 0xFF).toByte()
                output[i * 2 + 1] = ((sampleVal shr 8) and 0xFF).toByte()
            }
            return output
        }
    }
}
