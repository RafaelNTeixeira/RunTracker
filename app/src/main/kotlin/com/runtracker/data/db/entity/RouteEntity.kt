package com.runtracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val distanceKm: Double,
    val waypoints: String,         // "lat1,lng1;lat2,lng2;..."
    val createdAt: Long = System.currentTimeMillis()
)
