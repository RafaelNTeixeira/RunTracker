package com.runtracker.data.db.dao

import androidx.room.*
import com.runtracker.data.db.entity.RouteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes ORDER BY createdAt DESC")
    fun getAll(): Flow<List<RouteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: RouteEntity)

    @Delete
    suspend fun delete(route: RouteEntity)
}
