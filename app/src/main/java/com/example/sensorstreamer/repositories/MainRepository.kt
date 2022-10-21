package com.example.sensorstreamer.repositories

import com.example.sensorstreamer.db.Run
import com.example.sensorstreamer.db.RunDAO
import com.example.sensorstreamer.other.MessageListener
import javax.inject.Inject

// Collects the data from the data sources
class MainRepository @Inject constructor(
    val runDAO: RunDAO//,
    //val msgListener: MessageListener
) {

    suspend fun insertRUN(run: Run)= runDAO.insertRun(run)
    suspend fun deleteRUN(run: Run)= runDAO.deleteRun(run)

    fun getsAllRunsSortedByDate() = runDAO.getAllRunsSortedByDate()
    fun getsAllRunsSortedByDistance() = runDAO.getAllRunsSortedByDistance()
    fun getsAllRunsSortedByAvgSpeed() = runDAO.getAllRunsSortedByAvgSpeed()
    fun getsAllRunsSortedByTimeInMillis() = runDAO.getAllRunsSortedByTimeInMillis()

    fun getTotalDistance() = runDAO.getTotalDistance()
    fun getTotalTimeInMillis() = runDAO.getTotalTimeInMillis()
    fun getMeanAvgSpeed() = runDAO.getMeanAvgSpeed()
    fun getTotalMeanDistance() = runDAO.getMeanDistance()
    fun getTotalMeanTimeInMillis() = runDAO.getMeanTimeInMillis()

    //Insert the MessageListener Interface here??
    /*fun onConnectSuccess () = msgListener.onConnectSuccess()
    fun onConnectFailed () = msgListener.onConnectFailed()
    fun onClose () = msgListener.onClose()
    fun onMessage (text: String?) = msgListener.onMessage(text)*/
}