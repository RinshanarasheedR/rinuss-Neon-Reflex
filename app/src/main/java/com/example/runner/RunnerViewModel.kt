package com.example.runner

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.NeonSoundManager
import com.example.data.RunnerRecord
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
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class RunnerUiState(
    val state: RunnerState = RunnerState.MENU,
    val score: Int = 0,
    val distanceMeters: Int = 0,
    val coinsCollected: Int = 0,
    val bestScore: Int = 0,
    val bestDistance: Int = 0,
    val totalCoins: Int = 0,
    val currentLane: Int = 0, // -1: Left, 0: Center, 1: Right
    val laneProgress: Float = 0f, // Smoothly interpolated position: -1f to 1f
    val isJumping: Boolean = false,
    val jumpHeightNormalized: Float = 0f, // 0f on ground, up to 1f at apex
    val isSliding: Boolean = false,
    val slideProgress: Float = 0f,
    val countdownNumber: String = "3",
    val isNewRecord: Boolean = false,
    val isSoundEnabled: Boolean = true,
    val showHowToPlay: Boolean = false,
    val showAchievements: Boolean = false,
    val currentTheme: EnvironmentTheme = EnvironmentTheme.NEON_DISTRICT,
    val activeShield: Boolean = false,
    val shieldTimeLeftMs: Long = 0L,
    val activeMagnet: Boolean = false,
    val magnetTimeLeftMs: Long = 0L,
    val activeMultiplier: Boolean = false,
    val multiplierTimeLeftMs: Long = 0L,
    val activeSpeedBoost: Boolean = false,
    val speedBoostTimeLeftMs: Long = 0L,
    val screenShake: Float = 0f,
    val speedLevel: Int = 1,
    val bannerNotice: String? = null
)

class RunnerViewModel(application: Application) : AndroidViewModel(application) {

    val repository = RunnerRepository(application)
    val soundManager = NeonSoundManager(application)

    private val _uiState = MutableStateFlow(
        RunnerUiState(
            bestScore = repository.getCachedBestScore(),
            bestDistance = repository.getCachedBestDistance(),
            totalCoins = repository.getCachedTotalCoins(),
            isSoundEnabled = soundManager.isSoundEnabled
        )
    )
    val uiState: StateFlow<RunnerUiState> = _uiState.asStateFlow()

    val bestScoreFlow: StateFlow<Int> = repository.bestScore
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getCachedBestScore())

    val bestDistanceFlow: StateFlow<Int> = repository.bestDistance
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getCachedBestDistance())

    val totalCoinsFlow: StateFlow<Int> = repository.totalCoins
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getCachedTotalCoins())

    val totalRunsFlow: StateFlow<Int> = repository.totalRuns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Game entity collections
    val obstacles = mutableListOf<RunnerObstacle>()
    val coins = mutableListOf<RunnerCoin>()
    val powerUps = mutableListOf<RunnerPowerUpItem>()
    val particles = mutableListOf<RunnerParticle>()
    val floatingTexts = mutableListOf<RunnerFloatingText>()

    private var gameLoopJob: Job? = null
    private var countdownJob: Job? = null
    private var entityIdCounter: Long = 0L

    // Player jump & slide timers
    private var jumpElapsedMs: Long = 0L
    private val jumpDurationMs: Long = 620L

    private var slideElapsedMs: Long = 0L
    private val slideDurationMs: Long = 650L

    private var nextSpawnZ: Float = 400f
    private var distanceAccumulator: Float = 0f

    init {
        viewModelScope.launch {
            repository.bestScore.collect { best ->
                _uiState.update { it.copy(bestScore = best) }
            }
        }
        viewModelScope.launch {
            repository.bestDistance.collect { dist ->
                _uiState.update { it.copy(bestDistance = dist) }
            }
        }
        viewModelScope.launch {
            repository.totalCoins.collect { coins ->
                _uiState.update { it.copy(totalCoins = coins) }
            }
        }
    }

    fun toggleSound() {
        val next = !_uiState.value.isSoundEnabled
        soundManager.isSoundEnabled = next
        _uiState.update { it.copy(isSoundEnabled = next) }
    }

    fun showHowToPlay(show: Boolean) {
        _uiState.update { it.copy(showHowToPlay = show) }
    }

    fun showAchievements(show: Boolean) {
        _uiState.update { it.copy(showAchievements = show) }
    }

    fun startRun() {
        countdownJob?.cancel()
        gameLoopJob?.cancel()

        obstacles.clear()
        coins.clear()
        powerUps.clear()
        particles.clear()
        floatingTexts.clear()

        nextSpawnZ = 400f
        distanceAccumulator = 0f
        jumpElapsedMs = 0L
        slideElapsedMs = 0L

        _uiState.update {
            it.copy(
                state = RunnerState.COUNTDOWN,
                score = 0,
                distanceMeters = 0,
                coinsCollected = 0,
                currentLane = 0,
                laneProgress = 0f,
                isJumping = false,
                jumpHeightNormalized = 0f,
                isSliding = false,
                slideProgress = 0f,
                countdownNumber = "3",
                isNewRecord = false,
                activeShield = false,
                activeMagnet = false,
                activeMultiplier = false,
                activeSpeedBoost = false,
                speedLevel = 1,
                bannerNotice = null
            )
        }

        countdownJob = viewModelScope.launch {
            soundManager.playCountdownTick()
            _uiState.update { it.copy(countdownNumber = "3") }
            delay(750)

            soundManager.playCountdownTick()
            _uiState.update { it.copy(countdownNumber = "2") }
            delay(750)

            soundManager.playCountdownTick()
            _uiState.update { it.copy(countdownNumber = "1") }
            delay(750)

            soundManager.playCountdownGo()
            _uiState.update { it.copy(countdownNumber = "RUN!") }
            delay(450)

            beginRunning()
        }
    }

    private fun beginRunning() {
        _uiState.update { it.copy(state = RunnerState.PLAYING) }

        // Pre-populate track ahead
        while (nextSpawnZ < 1500f) {
            spawnTrackSegment(nextSpawnZ)
            nextSpawnZ += 120f
        }

        startGameLoop()
    }

    fun pauseGame() {
        if (_uiState.value.state == RunnerState.PLAYING) {
            gameLoopJob?.cancel()
            _uiState.update { it.copy(state = RunnerState.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_uiState.value.state == RunnerState.PAUSED) {
            _uiState.update { it.copy(state = RunnerState.PLAYING) }
            startGameLoop()
        }
    }

    fun goToMenu() {
        countdownJob?.cancel()
        gameLoopJob?.cancel()
        _uiState.update { it.copy(state = RunnerState.MENU) }
    }

    fun swipeLeft() {
        if (_uiState.value.state != RunnerState.PLAYING) return
        val current = _uiState.value.currentLane
        if (current > -1) {
            _uiState.update { it.copy(currentLane = current - 1) }
        }
    }

    fun swipeRight() {
        if (_uiState.value.state != RunnerState.PLAYING) return
        val current = _uiState.value.currentLane
        if (current < 1) {
            _uiState.update { it.copy(currentLane = current + 1) }
        }
    }

    fun swipeUp() {
        if (_uiState.value.state != RunnerState.PLAYING) return
        if (!_uiState.value.isJumping) {
            jumpElapsedMs = 0L
            soundManager.playRunnerJump()
            _uiState.update { it.copy(isJumping = true, isSliding = false) }
        }
    }

    fun swipeDown() {
        if (_uiState.value.state != RunnerState.PLAYING) return
        if (!_uiState.value.isSliding) {
            slideElapsedMs = 0L
            soundManager.playRunnerSlide()
            _uiState.update { it.copy(isSliding = true, isJumping = false, jumpHeightNormalized = 0f) }
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        var lastTime = System.currentTimeMillis()

        gameLoopJob = viewModelScope.launch {
            while (isActive && _uiState.value.state == RunnerState.PLAYING) {
                val now = System.currentTimeMillis()
                val deltaMs = (now - lastTime).coerceIn(1L, 100L)
                lastTime = now

                updateGameTick(deltaMs)
                delay(16) // ~60 FPS
            }
        }
    }

    private fun updateGameTick(deltaMs: Long) {
        val dt = deltaMs / 1000f
        val state = _uiState.value

        // Calculate running speed (units per second)
        val baseSpeed = 310f + (state.distanceMeters * 0.18f).coerceAtMost(260f)
        val speedBoostMultiplier = if (state.activeSpeedBoost) 1.55f else 1.0f
        val currentSpeed = baseSpeed * speedBoostMultiplier

        // Update distance meters
        distanceAccumulator += (currentSpeed * dt) / 25f
        val newDistanceMeters = distanceAccumulator.toInt()

        // Progressive speed level
        val newSpeedLevel = when {
            newDistanceMeters >= 1000 -> {
                repository.unlockAchievement("speed_demon")
                4
            }
            newDistanceMeters >= 500 -> 3
            newDistanceMeters >= 200 -> 2
            else -> 1
        }

        // Environment theme progression every 320 meters
        val themes = EnvironmentTheme.values()
        val themeIndex = (newDistanceMeters / 320) % themes.size
        val currentTheme = themes[themeIndex]

        // Smooth lane interpolation
        val targetLaneFloat = state.currentLane.toFloat()
        val currentLaneProgress = state.laneProgress
        val newLaneProgress = currentLaneProgress + (targetLaneFloat - currentLaneProgress) * (14f * dt).coerceAtMost(1f)

        // Jump physics (parabolic arc)
        var newIsJumping = state.isJumping
        var newJumpHeight = 0f
        if (state.isJumping) {
            jumpElapsedMs += deltaMs
            val progress = (jumpElapsedMs.toFloat() / jumpDurationMs).coerceIn(0f, 1f)
            if (progress >= 1f) {
                newIsJumping = false
                newJumpHeight = 0f
            } else {
                newJumpHeight = sin(progress * Math.PI.toFloat())
            }
        }

        // Slide physics
        var newIsSliding = state.isSliding
        var newSlideProgress = 0f
        if (state.isSliding) {
            slideElapsedMs += deltaMs
            val progress = (slideElapsedMs.toFloat() / slideDurationMs).coerceIn(0f, 1f)
            if (progress >= 1f) {
                newIsSliding = false
                newSlideProgress = 0f
            } else {
                newSlideProgress = progress
            }
        }

        // Power-up countdowns
        val newShieldMs = (state.shieldTimeLeftMs - deltaMs).coerceAtLeast(0L)
        val newMagnetMs = (state.magnetTimeLeftMs - deltaMs).coerceAtLeast(0L)
        val newMultiplierMs = (state.multiplierTimeLeftMs - deltaMs).coerceAtLeast(0L)
        val newSpeedBoostMs = (state.speedBoostTimeLeftMs - deltaMs).coerceAtLeast(0L)

        // Score accrual based on distance and multiplier
        val multiplierBonus = if (newMultiplierMs > 0) 2 else 1
        val addedDistanceScore = ((currentSpeed * dt * 0.12f) * multiplierBonus).toInt()
        val newScore = state.score + addedDistanceScore

        // Move entities towards player
        val movementZ = currentSpeed * dt

        // 1. Move & Magnetize Coins
        val coinIterator = coins.iterator()
        var coinsGatheredThisTick = 0
        var scoreFromCoins = 0

        while (coinIterator.hasNext()) {
            val coin = coinIterator.next()
            coin.z -= movementZ

            // If magnet active, attract coin towards player
            if (newMagnetMs > 0 && coin.z in 0f..460f) {
                coin.z -= movementZ * 0.8f
                // Pull lane towards player lane
                val targetLane = state.currentLane
                if (coin.lane != targetLane) {
                    coin.z -= movementZ * 0.4f
                }
            }

            // Coin Collection check (player is around z in [-5, 45])
            if (!coin.collected && coin.z in -10f..48f) {
                val laneDiff = abs(newLaneProgress - coin.lane)
                val inReach = if (newMagnetMs > 0) laneDiff < 1.4f else laneDiff < 0.65f
                if (inReach) {
                    coin.collected = true
                    coinsGatheredThisTick++
                    scoreFromCoins += 25 * multiplierBonus
                    soundManager.playRunnerCoin()
                    spawnParticles(coin.lane.toFloat() * 120f, 30f, androidx.compose.ui.graphics.Color(0xFFFFD700), 10)
                    spawnFloatingText("+1 🪙", androidx.compose.ui.graphics.Color(0xFFFFD700))
                }
            }

            if (coin.z < -60f || coin.collected) {
                coinIterator.remove()
            }
        }

        // 2. Move & Collect Power-ups
        val powerUpIterator = powerUps.iterator()
        var activatedShield = newShieldMs > 0
        var activatedMagnet = newMagnetMs > 0
        var activatedMultiplier = newMultiplierMs > 0
        var activatedSpeedBoost = newSpeedBoostMs > 0

        var shieldTime = newShieldMs
        var magnetTime = newMagnetMs
        var multiplierTime = newMultiplierMs
        var speedBoostTime = newSpeedBoostMs
        var bannerText: String? = state.bannerNotice

        while (powerUpIterator.hasNext()) {
            val item = powerUpIterator.next()
            item.z -= movementZ

            if (!item.collected && item.z in -10f..45f) {
                val laneDiff = abs(newLaneProgress - item.lane)
                if (laneDiff < 0.65f) {
                    item.collected = true
                    soundManager.playRunnerPowerUp()
                    spawnParticles(item.lane.toFloat() * 120f, 40f, item.type.primaryColor, 18)

                    when (item.type) {
                        PowerUpType.SHIELD -> {
                            activatedShield = true
                            shieldTime = item.type.durationMs
                            bannerText = "🛡️ SHIELD ACTIVE!"
                        }
                        PowerUpType.MAGNET -> {
                            activatedMagnet = true
                            magnetTime = item.type.durationMs
                            bannerText = "🧲 MAGNET ACTIVE!"
                        }
                        PowerUpType.SCORE_BOOSTER -> {
                            activatedMultiplier = true
                            multiplierTime = item.type.durationMs
                            bannerText = "⚡ 2X MULTIPLIER!"
                        }
                        PowerUpType.SPEED_BOOST -> {
                            activatedSpeedBoost = true
                            speedBoostTime = item.type.durationMs
                            bannerText = "🚀 SPEED SURGE!"
                        }
                    }
                }
            }

            if (item.z < -60f || item.collected) {
                powerUpIterator.remove()
            }
        }

        // 3. Move Obstacles & Check Collisions
        var collisionOccurred = false
        val obstacleIterator = obstacles.iterator()

        while (obstacleIterator.hasNext()) {
            val obs = obstacleIterator.next()
            obs.z -= (movementZ + (obs.speedZ * dt))

            // Collision check: player is in z in [-10, 40]
            if (obs.z in -12f..38f) {
                val laneDiff = abs(newLaneProgress - obs.lane)
                if (laneDiff < 0.60f) {
                    var safe = false

                    // If jumping over jumpable obstacle
                    if (obs.type.canJumpOver && newJumpHeight > 0.42f) {
                        safe = true
                    }

                    // If sliding under high gate
                    if (obs.type.canSlideUnder && newIsSliding) {
                        safe = true
                    }

                    if (!safe) {
                        // Impact!
                        if (activatedSpeedBoost) {
                            // Speed boost smashes right through obstacles!
                            soundManager.playHitGold()
                            spawnParticles(obs.lane.toFloat() * 120f, 40f, androidx.compose.ui.graphics.Color(0xFF00FF66), 16)
                            obstacleIterator.remove()
                            continue
                        } else if (activatedShield) {
                            // Shield breaks and saves player!
                            soundManager.playHitDanger()
                            activatedShield = false
                            shieldTime = 0L
                            bannerText = "🛡️ SHIELD BROKEN!"
                            spawnParticles(obs.lane.toFloat() * 120f, 40f, androidx.compose.ui.graphics.Color(0xFF00F0FF), 20)
                            obstacleIterator.remove()
                            continue
                        } else {
                            // Fatal crash!
                            collisionOccurred = true
                            break
                        }
                    }
                }
            }

            if (obs.z < -70f) {
                obstacleIterator.remove()
            }
        }

        // Spawn new segments as player advances
        nextSpawnZ -= movementZ
        while (nextSpawnZ < 1400f) {
            spawnTrackSegment(nextSpawnZ)
            nextSpawnZ += 120f
        }

        // Decay screen shake
        var screenShake = state.screenShake
        if (screenShake > 0.1f) {
            screenShake *= 0.85f
        } else {
            screenShake = 0f
        }

        // Update particles and floating text
        updateParticles(dt)
        updateFloatingTexts(dt)

        _uiState.update {
            it.copy(
                score = newScore + scoreFromCoins,
                distanceMeters = newDistanceMeters,
                coinsCollected = it.coinsCollected + coinsGatheredThisTick,
                laneProgress = newLaneProgress,
                isJumping = newIsJumping,
                jumpHeightNormalized = newJumpHeight,
                isSliding = newIsSliding,
                slideProgress = newSlideProgress,
                currentTheme = currentTheme,
                activeShield = activatedShield,
                shieldTimeLeftMs = shieldTime,
                activeMagnet = activatedMagnet,
                magnetTimeLeftMs = magnetTime,
                activeMultiplier = activatedMultiplier,
                multiplierTimeLeftMs = multiplierTime,
                activeSpeedBoost = activatedSpeedBoost,
                speedBoostTimeLeftMs = speedBoostTime,
                speedLevel = newSpeedLevel,
                bannerNotice = bannerText,
                screenShake = screenShake
            )
        }

        if (collisionOccurred) {
            triggerGameOver()
        }
    }

    private fun spawnTrackSegment(atZ: Float) {
        val rand = Random
        val lanes = listOf(-1, 0, 1)

        // Decide if spawning an obstacle
        if (rand.nextFloat() < 0.72f) {
            val obstacleCount = if (_uiState.value.speedLevel >= 3 && rand.nextFloat() < 0.45f) 2 else 1
            val chosenLanes = lanes.shuffled().take(obstacleCount)

            chosenLanes.forEach { lane ->
                val typeRand = rand.nextFloat()
                val type = when {
                    typeRand < 0.32f -> ObstacleType.ROAD_BARRIER
                    typeRand < 0.58f -> ObstacleType.OVERHANG_GATE
                    typeRand < 0.78f -> ObstacleType.TRAFFIC_CONE
                    typeRand < 0.90f -> ObstacleType.CONSTRUCTION_BLOCK
                    else -> ObstacleType.CYBER_VEHICLE
                }

                entityIdCounter++
                obstacles.add(
                    RunnerObstacle(
                        id = entityIdCounter,
                        lane = lane,
                        z = atZ + rand.nextFloat() * 20f,
                        type = type,
                        speedZ = if (type == ObstacleType.CYBER_VEHICLE) 90f else 0f
                    )
                )
            }
        }

        // Decide if spawning a coin or power-up in a free lane
        val occupiedLanes = obstacles.filter { abs(it.z - atZ) < 60f }.map { it.lane }.toSet()
        val availableLanes = lanes.filter { it !in occupiedLanes }

        if (availableLanes.isNotEmpty()) {
            val targetLane = availableLanes.random()

            // 8% chance of power-up
            if (rand.nextFloat() < 0.08f) {
                entityIdCounter++
                val powerType = PowerUpType.values().random()
                powerUps.add(
                    RunnerPowerUpItem(
                        id = entityIdCounter,
                        lane = targetLane,
                        z = atZ + 30f,
                        type = powerType
                    )
                )
            } else if (rand.nextFloat() < 0.65f) {
                // Spawn a line of 3 coins
                for (i in 0 until 3) {
                    entityIdCounter++
                    coins.add(
                        RunnerCoin(
                            id = entityIdCounter,
                            lane = targetLane,
                            z = atZ + (i * 26f)
                        )
                    )
                }
            }
        }
    }

    private fun spawnParticles(x: Float, y: Float, color: androidx.compose.ui.graphics.Color, count: Int) {
        val rand = Random
        for (i in 0 until count) {
            val angle = rand.nextDouble(0.0, Math.PI * 2)
            val speed = rand.nextDouble(40.0, 160.0).toFloat()
            val vx = (cos(angle) * speed).toFloat()
            val vy = (sin(angle) * speed).toFloat()
            val size = rand.nextDouble(4.0, 10.0).toFloat()
            val maxLife = rand.nextDouble(0.3, 0.6).toFloat()

            particles.add(
                RunnerParticle(
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

    private fun spawnFloatingText(text: String, color: androidx.compose.ui.graphics.Color) {
        floatingTexts.add(
            RunnerFloatingText(
                id = System.nanoTime(),
                text = text,
                color = color,
                x = 0f,
                y = 120f,
                alpha = 1.0f,
                scale = 1.0f
            )
        )
    }

    private fun updateParticles(dt: Float) {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= dt
            if (p.life <= 0f) {
                iterator.remove()
            }
        }
    }

    private fun updateFloatingTexts(dt: Float) {
        val iterator = floatingTexts.iterator()
        while (iterator.hasNext()) {
            val ft = iterator.next()
            ft.y -= 70f * dt
            ft.alpha -= 1.2f * dt
            if (ft.alpha <= 0f) {
                iterator.remove()
            }
        }
    }

    private fun triggerGameOver() {
        gameLoopJob?.cancel()

        soundManager.playRunnerCrash()

        val state = _uiState.value
        val isNewBestScore = state.score > state.bestScore
        val isNewBestDist = state.distanceMeters > state.bestDistance
        val isNewRecord = isNewBestScore || isNewBestDist

        soundManager.playGameOver(isNewRecord)

        val newBestScore = maxOf(state.score, state.bestScore)
        val newBestDistance = maxOf(state.distanceMeters, state.bestDistance)

        _uiState.update {
            it.copy(
                state = RunnerState.GAME_OVER,
                isNewRecord = isNewRecord,
                bestScore = newBestScore,
                bestDistance = newBestDistance,
                screenShake = 16f
            )
        }

        // Persist run record asynchronously
        viewModelScope.launch {
            repository.saveRunRecord(
                RunnerRecord(
                    score = state.score,
                    distanceMeters = state.distanceMeters,
                    coinsCollected = state.coinsCollected,
                    isNewRecord = isNewRecord
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
