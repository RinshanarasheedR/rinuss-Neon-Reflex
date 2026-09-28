package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: GameRecord): Long

    @Query("SELECT MAX(score) FROM game_records")
    fun getBestScore(): Flow<Int?>

    @Query("SELECT MAX(highestCombo) FROM game_records")
    fun getMaxCombo(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM game_records")
    fun getTotalGamesCount(): Flow<Int>

    @Query("SELECT SUM(targetsHit) FROM game_records")
    fun getTotalTargetsHit(): Flow<Int?>

    @Query("SELECT * FROM game_records ORDER BY timestamp DESC LIMIT 10")
    fun getRecentGames(): Flow<List<GameRecord>>
}
