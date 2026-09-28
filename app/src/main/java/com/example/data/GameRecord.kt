package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_records")
data class GameRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val score: Int,
    val highestCombo: Int,
    val levelReached: Int,
    val targetsHit: Int,
    val fastHits: Int,
    val dangerHits: Int,
    val avgReactionMs: Long,
    val isNewHighScore: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
