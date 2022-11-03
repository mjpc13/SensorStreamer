package com.mjpc13.sensorstreamer.db

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "recording_table")
data class Run (
    var img: Bitmap? = null,
    var timestamp: Long = 0L,
    var avgSpeed: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L
    ){
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}