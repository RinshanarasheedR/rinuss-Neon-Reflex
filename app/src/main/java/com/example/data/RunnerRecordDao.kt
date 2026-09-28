package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RunnerRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RunnerRecord): Long

    @Query("SELECT MAX(score) FROM runner_records")
    fun getBestScore(): Flow<Int?>

    @Query("SELECT MAX(distanceMeters) FROM runner_records")
    fun getBestDistance(): Flow<Int?>

    @Query("SELECT SUM(coinsCollected) FROM runner_records")
    fun getTotalCoins(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM runner_records")
    fun getTotalRuns(): Flow<Int>

    @Query("SELECT * FROM runner_records ORDER BY timestamp DESC LIMIT 10")
    fun getRecentRuns(): Flow<List<RunnerRecord>>
}
