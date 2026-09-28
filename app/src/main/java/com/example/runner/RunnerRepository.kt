package com.example.runner

import android.content.Context
import android.content.SharedPreferences
import com.example.data.AppDatabase
import com.example.data.RunnerRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RunnerRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val dao = db.runnerRecordDao()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("run_rush_prefs", Context.MODE_PRIVATE)

    val bestScore: Flow<Int> = dao.getBestScore().map { it ?: prefs.getInt("best_score", 0) }
    val bestDistance: Flow<Int> = dao.getBestDistance().map { it ?: prefs.getInt("best_distance", 0) }
    val totalCoins: Flow<Int> = dao.getTotalCoins().map { it ?: prefs.getInt("total_coins", 0) }
    val totalRuns: Flow<Int> = dao.getTotalRuns()

    fun getCachedBestScore(): Int = prefs.getInt("best_score", 0)
    fun getCachedBestDistance(): Int = prefs.getInt("best_distance", 0)
    fun getCachedTotalCoins(): Int = prefs.getInt("total_coins", 0)

    fun isAchievementUnlocked(id: String): Boolean = prefs.getBoolean("ach_$id", false)

    fun unlockAchievement(id: String) {
        prefs.edit().putBoolean("ach_$id", true).apply()
    }

    fun getAchievements(): List<RunnerAchievement> {
        return listOf(
            RunnerAchievement(
                id = "first_run",
                title = "First Run",
                description = "Complete your first run in Run Rush",
                icon = "🏃",
                isUnlocked = isAchievementUnlocked("first_run")
            ),
            RunnerAchievement(
                id = "coin_collector",
                title = "Coin Collector",
                description = "Collect 100 total coins across runs",
                icon = "🪙",
                isUnlocked = isAchievementUnlocked("coin_collector")
            ),
            RunnerAchievement(
                id = "distance_1000m",
                title = "1,000 Meter Run",
                description = "Reach 1,000 meters in a single run",
                icon = "🔥",
                isUnlocked = isAchievementUnlocked("distance_1000m")
            ),
            RunnerAchievement(
                id = "speed_demon",
                title = "Speed Demon",
                description = "Reach maximum velocity level in a run",
                icon = "⚡",
                isUnlocked = isAchievementUnlocked("speed_demon")
            ),
            RunnerAchievement(
                id = "high_scorer",
                title = "High Scorer",
                description = "Score over 5,000 points in one run",
                icon = "🏆",
                isUnlocked = isAchievementUnlocked("high_scorer")
            )
        )
    }

    suspend fun saveRunRecord(record: RunnerRecord): Long {
        val currentBestScore = prefs.getInt("best_score", 0)
        if (record.score > currentBestScore) {
            prefs.edit().putInt("best_score", record.score).apply()
        }

        val currentBestDistance = prefs.getInt("best_distance", 0)
        if (record.distanceMeters > currentBestDistance) {
            prefs.edit().putInt("best_distance", record.distanceMeters).apply()
        }

        val currentCoins = prefs.getInt("total_coins", 0)
        val newTotalCoins = currentCoins + record.coinsCollected
        prefs.edit().putInt("total_coins", newTotalCoins).apply()

        // Check achievements
        unlockAchievement("first_run")
        if (newTotalCoins >= 100) unlockAchievement("coin_collector")
        if (record.distanceMeters >= 1000) unlockAchievement("distance_1000m")
        if (record.score >= 5000) unlockAchievement("high_scorer")

        return dao.insertRecord(record)
    }
}
