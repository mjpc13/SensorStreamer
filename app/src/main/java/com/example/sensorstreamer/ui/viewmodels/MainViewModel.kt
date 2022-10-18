package com.example.sensorstreamer.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensorstreamer.db.Run
import com.example.sensorstreamer.other.SortType
import com.example.sensorstreamer.repositories.MainRepository
import kotlinx.coroutines.launch

//Collect the data from the repository and provide for the other fragments
class MainViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
): ViewModel() {

    private val runsSortedByDate = mainRepository.getsAllRunsSortedByDate()
    private val runsSortedByDistance = mainRepository.getsAllRunsSortedByDistance()
    private val runsSortedByAvgSpeed = mainRepository.getsAllRunsSortedByAvgSpeed()
    private val runsSortedByTimeInMillis = mainRepository.getsAllRunsSortedByTimeInMillis()

    val runs = MediatorLiveData<List<Run>>()

    var sort_type = SortType.DATE

    init {
        runs.addSource(runsSortedByDate) {result ->
            if(sort_type == SortType.DATE){
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByDistance) {result ->
            if(sort_type == SortType.DISTANCE){
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByAvgSpeed) {result ->
            if(sort_type == SortType.AVG_SPEED){
                result?.let { runs.value = it }
            }
        }
        runs.addSource(runsSortedByTimeInMillis) {result ->
            if(sort_type == SortType.RUNNING_TIME){
                result?.let { runs.value = it }
            }
        }
    }

    fun sortRuns(sortType: SortType) = when(sortType) {
        SortType.DATE -> runsSortedByDate.value?.let { runs.value = it }
        SortType.DISTANCE -> runsSortedByDistance.value?.let { runs.value = it }
        SortType.AVG_SPEED -> runsSortedByAvgSpeed.value?.let { runs.value = it }
        SortType.RUNNING_TIME -> runsSortedByTimeInMillis.value?.let { runs.value = it }
    }.also{
        this.sort_type = sortType
    }


    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRUN(run)
    }

}