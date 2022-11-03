package com.mjpc13.sensorstreamer.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import com.mjpc13.sensorstreamer.repositories.MainRepository

//Collect the data from the repository and provide for the other fragments
class StatisticsViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
): ViewModel() {

    val meanTime = mainRepository.getTotalMeanTimeInMillis()
    val meanDistance = mainRepository.getTotalMeanDistance()
    val meanAvgSpeed = mainRepository.getMeanAvgSpeed()

    val runsSortedByDate = mainRepository.getsAllRunsSortedByDate()
}