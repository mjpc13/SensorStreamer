package com.example.sensorstreamer.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface RunDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: Run)

    @Delete
    suspend fun deleteRun(run: Run)

    @Query( value = "SELECT * FROM recording_table ORDER BY timestamp DESC")
    fun getAllRunsSortedByDate(): LiveData<List<Run>>

    @Query( value = "SELECT * FROM recording_table ORDER BY avgSpeed DESC")
    fun getAllRunsSortedByAvgSpeed(): LiveData<List<Run>>

    @Query( value = "SELECT * FROM recording_table ORDER BY timeInMillis DESC")
    fun getAllRunsSortedByTimeInMillis(): LiveData<List<Run>>

    @Query( value = "SELECT * FROM recording_table ORDER BY distanceInMeters DESC")
    fun getAllRunsSortedByDistance(): LiveData<List<Run>>

    @Query( value = "SELECT SUM(timeInMillis) FROM recording_table")
    fun getTotalTimeInMillis(): LiveData<Long>

    @Query( value = "SELECT SUM(distanceInMeters) FROM recording_table")
    fun getTotalDistance(): LiveData<Int>

    @Query( value = "SELECT AVG(timeInMillis) FROM recording_table")
    fun getMeanTimeInMillis(): LiveData<Float>

    @Query( value = "SELECT AVG(distanceInMeters) FROM recording_table")
    fun getMeanDistance(): LiveData<Float>

    @Query( value = "SELECT AVG(avgSpeed) FROM recording_table")
    fun getMeanAvgSpeed(): LiveData<Float>

}