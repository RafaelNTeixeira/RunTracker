package com.runtracker.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "runs")
data class RunEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val distanceKm: Double,
    val durationSeconds: Int,
    val calories: Int,
    val notes: String,
    val runDate: String,           // "YYYY-MM-DD"
    val createdAt: Long = System.currentTimeMillis()
)
