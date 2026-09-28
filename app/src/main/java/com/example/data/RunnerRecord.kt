package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "runner_records")
data class RunnerRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val score: Int,
    val distanceMeters: Int,
    val coinsCollected: Int,
    val isNewRecord: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
