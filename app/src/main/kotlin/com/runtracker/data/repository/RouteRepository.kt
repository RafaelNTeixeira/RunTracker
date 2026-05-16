package com.runtracker.data.repository

import com.runtracker.data.db.dao.RouteDao
import com.runtracker.data.db.entity.RouteEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteRepository @Inject constructor(private val dao: RouteDao) {
    fun getAll(): Flow<List<RouteEntity>>    = dao.getAll()
    suspend fun add(route: RouteEntity)      = dao.insert(route)
    suspend fun delete(route: RouteEntity)   = dao.delete(route)
}
