package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GameRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val dao = db.gameRecordDao()
    private val prefs: SharedPreferences =
        context.getSharedPreferences("neon_reflex_prefs", Context.MODE_PRIVATE)

    val bestScore: Flow<Int> = dao.getBestScore().map { it ?: prefs.getInt("best_score", 0) }
    val maxCombo: Flow<Int> = dao.getMaxCombo().map { it ?: prefs.getInt("max_combo", 0) }
    val totalGames: Flow<Int> = dao.getTotalGamesCount()
    val totalHits: Flow<Int> = dao.getTotalTargetsHit().map { it ?: 0 }

    fun isSoundEnabled(): Boolean = prefs.getBoolean("sound_enabled", true)
    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun isHapticEnabled(): Boolean = prefs.getBoolean("haptic_enabled", true)
    fun setHapticEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("haptic_enabled", enabled).apply()
    }

    suspend fun saveGameRecord(record: GameRecord): Long {
        val currentBest = prefs.getInt("best_score", 0)
        if (record.score > currentBest) {
            prefs.edit().putInt("best_score", record.score).apply()
        }
        val currentMaxCombo = prefs.getInt("max_combo", 0)
        if (record.highestCombo > currentMaxCombo) {
            prefs.edit().putInt("max_combo", record.highestCombo).apply()
        }
        return dao.insertRecord(record)
    }

    fun getCachedBestScore(): Int = prefs.getInt("best_score", 0)
}
