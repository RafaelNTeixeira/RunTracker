package com.runtracker.data.db.dao

import androidx.room.*
import com.runtracker.data.db.entity.RunEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RunDao {
    @Query("SELECT * FROM runs ORDER BY runDate DESC")
    fun getAll(): Flow<List<RunEntity>>

    @Query("SELECT * FROM runs ORDER BY runDate DESC LIMIT 5")
    fun getRecent(): Flow<List<RunEntity>>

    @Query("SELECT COUNT(*) FROM runs")
    fun countAll(): Flow<Int>

    @Query("SELECT SUM(distanceKm) FROM runs")
    fun sumDistance(): Flow<Double?>

    @Query("SELECT SUM(durationSeconds) FROM runs")
    fun sumDuration(): Flow<Long?>

    @Query("SELECT SUM(calories) FROM runs")
    fun sumCalories(): Flow<Long?>

    @Query("SELECT MIN(durationSeconds * 1.0 / distanceKm) FROM runs WHERE distanceKm > 0")
    fun bestPaceSecondsPerKm(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(run: RunEntity)

    @Update
    suspend fun update(run: RunEntity)

    @Delete
    suspend fun delete(run: RunEntity)
}
