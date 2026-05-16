package com.runtracker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.runtracker.data.db.dao.RouteDao
import com.runtracker.data.db.dao.RunDao
import com.runtracker.data.db.dao.UserDao
import com.runtracker.data.db.entity.RouteEntity
import com.runtracker.data.db.entity.RunEntity
import com.runtracker.data.db.entity.UserEntity

@Database(
    entities = [UserEntity::class, RunEntity::class, RouteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class RunTrackerDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun runDao(): RunDao
    abstract fun routeDao(): RouteDao
}
