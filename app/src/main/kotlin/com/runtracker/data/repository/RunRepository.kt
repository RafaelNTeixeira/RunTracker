package com.runtracker.data.repository

import com.runtracker.data.db.dao.RunDao
import com.runtracker.data.db.entity.RunEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RunRepository @Inject constructor(private val dao: RunDao) {
    fun getAll(): Flow<List<RunEntity>>    = dao.getAll()
    fun getRecent(): Flow<List<RunEntity>> = dao.getRecent()
    fun countAll(): Flow<Int>             = dao.countAll()
    fun sumDistance(): Flow<Double?>      = dao.sumDistance()
    fun sumDuration(): Flow<Long?>        = dao.sumDuration()
    fun sumCalories(): Flow<Long?>        = dao.sumCalories()
    fun bestPace(): Flow<Double?>         = dao.bestPaceSecondsPerKm()

    suspend fun add(run: RunEntity)    = dao.insert(run)
    suspend fun update(run: RunEntity) = dao.update(run)
    suspend fun delete(run: RunEntity) = dao.delete(run)
}
