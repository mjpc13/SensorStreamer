package com.mjpc13.sensorstreamer.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Run::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class StreamingDatabase : RoomDatabase() {

    abstract fun getRunDao(): RunDAO
}