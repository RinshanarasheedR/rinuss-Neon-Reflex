package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.NeonSoundManager
import com.example.data.GameRecord
import com.example.data.GameRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class GameUiState(
    val screenState: GameScreenState = GameScreenState.MENU,
    val score: Int = 0,
    val bestScore: Int = 0,
    val combo: Int = 0,
    val highestCombo: Int = 0,
    val level: Int = 1,
    val remainingTimeMs: Long = 30000L,
    val currentTarget: ReflexTarget? = null,
    val countdownNumber: String = "3",
    val isNewHighScore: Boolean = false,
    val targetsHit: Int = 0,
    val fastHits: Int = 0,
    val dangerHits: Int = 0,
    val missedTargets: Int = 0,
    val avgReactionMs: Long = 0L,
    val screenShakeOffsetX: Float = 0f,
    val screenShakeOffsetY: Float = 0f,
    val isSoundEnabled: Boolean = true,
    val isHapticEnabled: Boolean = true,
    val showHowToPlay: Boolean = false,
    val levelUpBanner: String? = null
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application)
    val soundManager = NeonSoundManager(application)

    private val _uiState = MutableStateFlow(
        GameUiState(
            bestScore = repository.getCachedBestScore(),
            isSoundEnabled = repository.isSoundEnabled(),
            isHapticEnabled = repository.isHapticEnabled()
        )
    )
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    val totalGamesPlayed: StateFlow<Int> = repository.totalGames
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allTimeMaxCombo: StateFlow<Int> = repository.maxCombo
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allTimeTotalHits: StateFlow<Int> = repository.totalHits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Particle and floating text collections for Compose Canvas
    val particles = mutableListOf<Particle>()
    val floatingTexts = mutableListOf<FloatingText>()

    private var gameLoopJob: Job? = null
    private var countdownJob: Job? = null
    private var playAreaWidthPx: Float = 800f
    private var playAreaHeightPx: Float = 1200f
    private var densityRatio: Float = 2.5f

    private var targetCounter: Long = 0L
    private val reactionTimes = mutableListOf<Long>()
    private var lastFrameTime = System.currentTimeMillis()

    init {
        soundManager.isSoundEnabled = repository.isSoundEnabled()
        soundManager.isHapticEnabled = repository.isHapticEnabled()

        viewModelScope.launch {
            repository.bestScore.collect { best ->
                _uiState.update { it.copy(bestScore = best) }
            }
        }
    }

    fun setPlayAreaSize(width: Float, height: Float, density: Float) {
        if (width > 50 && height > 50) {
            playAreaWidthPx = width
            playAreaHeightPx = height
            densityRatio = density
        }
    }

    fun toggleSound() {
        val next = !_uiState.value.isSoundEnabled
        soundManager.isSoundEnabled = next
        repository.setSoundEnabled(next)
        _uiState.update { it.copy(isSoundEnabled = next) }
    }

    fun toggleHaptic() {
        val next = !_uiState.value.isHapticEnabled
        soundManager.isHapticEnabled = next
        repository.setHapticEnabled(next)
        _uiState.update { it.copy(isHapticEnabled = next) }
    }

    fun showHowToPlay(show: Boolean) {
        _uiState.update { it.copy(showHowToPlay = show) }
    }

    fun startGame() {
        countdownJob?.cancel()
        gameLoopJob?.cancel()

        reactionTimes.clear()
        particles.clear()
        floatingTexts.clear()

        _uiState.update {
            it.copy(
                screenState = GameScreenState.COUNTDOWN,
                score = 0,
                combo = 0,
                highestCombo = 0,
                level = 1,
                remainingTimeMs = 30000L,
                currentTarget = null,
                countdownNumber = "3",
                isNewHighScore = false,
                targetsHit = 0,
                fastHits = 0,
                dangerHits = 0,
                missedTargets = 0,
                avgReactionMs = 0L,
                levelUpBanner = null
            )
        }

        countdownJob = viewModelScope.launch {
            soundManager.playCountdownTick()
            _uiState.update { it.copy(countdownNumber = "3") }
            delay(800)

            soundManager.playCountdownTick()
            _uiState.update { it.copy(countdownNumber = "2") }
            delay(800)

            soundManager.playCountdownTick()
            _uiState.update { it.copy(countdownNumber = "1") }
            delay(800)

            soundManager.playCountdownGo()
            _uiState.update { it.copy(countdownNumber = "GO!") }
            delay(500)

            beginPlaying()
        }
    }

    private fun beginPlaying() {
        _uiState.update {
            it.copy(
                screenState = GameScreenState.PLAYING,
                remainingTimeMs = 30000L
            )
        }
        spawnNewTarget()
        startGameLoop()
    }

    fun pauseGame() {
        if (_uiState.value.screenState == GameScreenState.PLAYING) {
            gameLoopJob?.cancel()
            _uiState.update { it.copy(screenState = GameScreenState.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_uiState.value.screenState == GameScreenState.PAUSED) {
            _uiState.update { it.copy(screenState = GameScreenState.PLAYING) }
            startGameLoop()
        }
    }

    fun goToMenu() {
        countdownJob?.cancel()
        gameLoopJob?.cancel()
        _uiState.update {
            it.copy(
                screenState = GameScreenState.MENU,
                currentTarget = null
            )
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        lastFrameTime = System.currentTimeMillis()

        gameLoopJob = viewModelScope.launch {
            while (isActive && _uiState.value.screenState == GameScreenState.PLAYING) {
                val now = System.currentTimeMillis()
                val delta = (now - lastFrameTime).coerceIn(1L, 100L)
                lastFrameTime = now

                updateGameTick(delta, now)
                delay(16) // Target ~60 FPS
            }
        }
    }

    private fun updateGameTick(deltaMs: Long, now: Long) {
        val currentRemaining = (_uiState.value.remainingTimeMs - deltaMs).coerceAtLeast(0L)

        // Update target progress or handle timeout
        val current = _uiState.value.currentTarget
        var nextTarget = current

        if (current != null) {
            val elapsed = now - current.spawnTimeMs
            val remainingRatio = (1.0f - (elapsed.toFloat() / current.lifespanMs)).coerceIn(0f, 1f)

            if (elapsed >= current.lifespanMs) {
                // Target timed out!
                handleTargetTimeout(current)
                nextTarget = createRandomTarget(now)
            } else {
                nextTarget = current.copy(remainingProgress = remainingRatio)
            }
        } else {
            nextTarget = createRandomTarget(now)
        }

        // Update visual particles
        updateParticles(deltaMs)
        updateFloatingTexts(deltaMs)

        // Decay screen shake
        var shakeX = _uiState.value.screenShakeOffsetX
        var shakeY = _uiState.value.screenShakeOffsetY
        if (shakeX != 0f || shakeY != 0f) {
            shakeX *= 0.85f
            shakeY *= 0.85f
            if (kotlin.math.abs(shakeX) < 0.5f) shakeX = 0f
            if (kotlin.math.abs(shakeY) < 0.5f) shakeY = 0f
        }

        _uiState.update {
            it.copy(
                remainingTimeMs = currentRemaining,
                currentTarget = nextTarget,
                screenShakeOffsetX = shakeX,
                screenShakeOffsetY = shakeY
            )
        }

        if (currentRemaining <= 0L) {
            triggerGameOver()
        }
    }

    private fun handleTargetTimeout(target: ReflexTarget) {
        if (target.type == TargetType.DANGER) {
            // Player successfully avoided danger! Small bonus feedback
            spawnFloatingText("+5 AVOIDED!", target.type.accentColor, target.x, target.y)
            _uiState.update { it.copy(score = it.score + 5) }
        } else {
            // Missed normal/gold/combo target: combo drops by 2 or resets
            _uiState.update {
                val newCombo = (it.combo - 2).coerceAtLeast(0)
                it.copy(
                    combo = newCombo,
                    missedTargets = it.missedTargets + 1
                )
            }
        }
    }

    fun onTargetTapped(targetId: Long, tapX: Float, tapY: Float) {
        if (_uiState.value.screenState != GameScreenState.PLAYING) return
        val current = _uiState.value.currentTarget ?: return
        if (current.id != targetId) return

        val now = System.currentTimeMillis()
        val reactionTime = now - current.spawnTimeMs
        reactionTimes.add(reactionTime)

        val target = current
        val state = _uiState.value

        when (target.type) {
            TargetType.NORMAL -> {
                val isFast = reactionTime < 450
                val multiplier = getComboMultiplier(state.combo)
                val base = target.type.basePoints * multiplier
                val fastBonus = if (isFast) 5 * multiplier else 0
                val totalAdded = base + fastBonus

                val newCombo = state.combo + 1
                val highestCombo = maxOf(state.highestCombo, newCombo)
                val newScore = state.score + totalAdded

                if (isFast) {
                    soundManager.playHitFast()
                    spawnFloatingText("FAST! +$totalAdded", target.type.primaryColor, target.x, target.y)
                } else {
                    soundManager.playHitNormal()
                    val comboText = if (multiplier > 1) "x$multiplier +$totalAdded" else "+$totalAdded"
                    spawnFloatingText(comboText, target.type.primaryColor, target.x, target.y)
                }

                spawnExplosion(target.x, target.y, target.type.primaryColor, 18)

                _uiState.update {
                    it.copy(
                        score = newScore,
                        combo = newCombo,
                        highestCombo = highestCombo,
                        targetsHit = it.targetsHit + 1,
                        fastHits = if (isFast) it.fastHits + 1 else it.fastHits
                    )
                }
            }

            TargetType.GOLD -> {
                val multiplier = getComboMultiplier(state.combo)
                val totalAdded = target.type.basePoints * multiplier
                val newCombo = state.combo + 1
                val highestCombo = maxOf(state.highestCombo, newCombo)
                val newScore = state.score + totalAdded

                soundManager.playHitGold()
                spawnFloatingText("⭐ GOLD! +$totalAdded", target.type.primaryColor, target.x, target.y)
                spawnExplosion(target.x, target.y, target.type.primaryColor, 28)

                _uiState.update {
                    it.copy(
                        score = newScore,
                        combo = newCombo,
                        highestCombo = highestCombo,
                        targetsHit = it.targetsHit + 1
                    )
                }
            }

            TargetType.COMBO -> {
                // Instantly boosts combo by +3 streak
                val newCombo = state.combo + 3
                val multiplier = getComboMultiplier(newCombo)
                val totalAdded = target.type.basePoints * multiplier
                val highestCombo = maxOf(state.highestCombo, newCombo)
                val newScore = state.score + totalAdded

                soundManager.playHitCombo()
                spawnFloatingText("🔥 COMBO BOOST! x$multiplier", target.type.primaryColor, target.x, target.y)
                spawnExplosion(target.x, target.y, target.type.primaryColor, 24)

                _uiState.update {
                    it.copy(
                        score = newScore,
                        combo = newCombo,
                        highestCombo = highestCombo,
                        targetsHit = it.targetsHit + 1
                    )
                }
            }

            TargetType.DANGER -> {
                // Penalty: -25 points, resets combo, screen shake!
                soundManager.playHitDanger()
                val newScore = (state.score + target.type.basePoints).coerceAtLeast(0)
                spawnFloatingText("💣 DANGER! -25", target.type.primaryColor, target.x, target.y)
                spawnExplosion(target.x, target.y, target.type.primaryColor, 22)

                _uiState.update {
                    it.copy(
                        score = newScore,
                        combo = 0,
                        dangerHits = it.dangerHits + 1,
                        screenShakeOffsetX = (Random.nextFloat() * 24f - 12f),
                        screenShakeOffsetY = (Random.nextFloat() * 24f - 12f)
                    )
                }
            }
        }

        checkLevelProgression()
        spawnNewTarget()
    }

    private fun getComboMultiplier(combo: Int): Int {
        return when {
            combo >= 15 -> 5
            combo >= 10 -> 4
            combo >= 6 -> 3
            combo >= 3 -> 2
            else -> 1
        }
    }

    private fun checkLevelProgression() {
        val score = _uiState.value.score
        val currentLevel = _uiState.value.level
        val newLevel = when {
            score >= 650 -> 4
            score >= 350 -> 3
            score >= 150 -> 2
            else -> 1
        }

        if (newLevel > currentLevel) {
            soundManager.playLevelUp()
            _uiState.update {
                it.copy(
                    level = newLevel,
                    levelUpBanner = "LEVEL $newLevel!"
                )
            }
            viewModelScope.launch {
                delay(1500)
                _uiState.update { it.copy(levelUpBanner = null) }
            }
        }
    }

    private fun spawnNewTarget() {
        val now = System.currentTimeMillis()
        val target = createRandomTarget(now)
        _uiState.update { it.copy(currentTarget = target) }
    }

    private fun createRandomTarget(now: Long): ReflexTarget {
        val level = _uiState.value.level
        targetCounter++

        // Progressive radius in dp converted to px
        val radiusDp = when (level) {
            1 -> 42f
            2 -> 37f
            3 -> 33f
            else -> 29f
        }
        val radiusPx = radiusDp * densityRatio

        // Lifespan: targets become faster as level increases
        val lifespanMs = when (level) {
            1 -> 2000L
            2 -> 1600L
            3 -> 1250L
            else -> 980L
        }

        // Probability of target types based on level
        val rand = Random.nextFloat()
        val type = when (level) {
            1 -> {
                if (rand < 0.12f) TargetType.GOLD else TargetType.NORMAL
            }
            2 -> {
                when {
                    rand < 0.18f -> TargetType.GOLD
                    rand < 0.28f -> TargetType.COMBO
                    else -> TargetType.NORMAL
                }
            }
            3 -> {
                when {
                    rand < 0.18f -> TargetType.GOLD
                    rand < 0.32f -> TargetType.COMBO
                    rand < 0.44f -> TargetType.DANGER
                    else -> TargetType.NORMAL
                }
            }
            else -> {
                when {
                    rand < 0.20f -> TargetType.GOLD
                    rand < 0.36f -> TargetType.COMBO
                    rand < 0.52f -> TargetType.DANGER
                    else -> TargetType.NORMAL
                }
            }
        }

        // Safe bounds calculation: ensure circle is COMPLETELY inside play area
        val minX = radiusPx + 16f
        val maxX = (playAreaWidthPx - radiusPx - 16f).coerceAtLeast(minX + 1f)
        val minY = radiusPx + 16f
        val maxY = (playAreaHeightPx - radiusPx - 16f).coerceAtLeast(minY + 1f)

        val posX = Random.nextFloat() * (maxX - minX) + minX
        val posY = Random.nextFloat() * (maxY - minY) + minY

        return ReflexTarget(
            id = targetCounter,
            type = type,
            x = posX,
            y = posY,
            radius = radiusPx,
            spawnTimeMs = now,
            lifespanMs = lifespanMs,
            remainingProgress = 1.0f
        )
    }

    private fun spawnExplosion(x: Float, y: Float, color: androidx.compose.ui.graphics.Color, count: Int) {
        val rand = Random
        for (i in 0 until count) {
            val angle = rand.nextDouble(0.0, Math.PI * 2)
            val speed = rand.nextDouble(4.0, 14.0).toFloat()
            val vx = (cos(angle) * speed).toFloat()
            val vy = (sin(angle) * speed).toFloat()
            val size = rand.nextDouble(4.0, 10.0).toFloat()
            val maxLife = rand.nextDouble(0.35, 0.65).toFloat()

            particles.add(
                Particle(
                    id = System.nanoTime() + i,
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    color = color,
                    life = maxLife,
                    maxLife = maxLife,
                    size = size
                )
            )
        }
    }

    private fun spawnFloatingText(text: String, color: androidx.compose.ui.graphics.Color, x: Float, y: Float) {
        floatingTexts.add(
            FloatingText(
                id = System.nanoTime(),
                text = text,
                color = color,
                x = x,
                y = y
            )
        )
    }

    private fun updateParticles(deltaMs: Long) {
        val dt = deltaMs / 1000f
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx
            p.y += p.vy
            p.vx *= 0.94f // Air friction
            p.vy *= 0.94f
            p.life -= dt
            if (p.life <= 0f) {
                iterator.remove()
            }
        }
    }

    private fun updateFloatingTexts(deltaMs: Long) {
        val dt = deltaMs / 1000f
        val iterator = floatingTexts.iterator()
        while (iterator.hasNext()) {
            val ft = iterator.next()
            ft.y -= 50f * dt // Float upward
            ft.alpha -= 1.1f * dt // Fade out
            if (ft.alpha <= 0f) {
                iterator.remove()
            }
        }
    }

    private fun triggerGameOver() {
        gameLoopJob?.cancel()

        val state = _uiState.value
        val isNewBest = state.score > state.bestScore
        val newBestScore = if (isNewBest) state.score else state.bestScore

        val avgReaction = if (reactionTimes.isNotEmpty()) {
            reactionTimes.average().toLong()
        } else {
            0L
        }

        soundManager.playGameOver(isNewBest)

        _uiState.update {
            it.copy(
                screenState = GameScreenState.GAME_OVER,
                currentTarget = null,
                isNewHighScore = isNewBest,
                bestScore = newBestScore,
                avgReactionMs = avgReaction
            )
        }

        // Persist to Room Database asynchronously
        viewModelScope.launch {
            repository.saveGameRecord(
                GameRecord(
                    score = state.score,
                    highestCombo = state.highestCombo,
                    levelReached = state.level,
                    targetsHit = state.targetsHit,
                    fastHits = state.fastHits,
                    dangerHits = state.dangerHits,
                    avgReactionMs = avgReaction,
                    isNewHighScore = isNewBest
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        gameLoopJob?.cancel()
    }
}
