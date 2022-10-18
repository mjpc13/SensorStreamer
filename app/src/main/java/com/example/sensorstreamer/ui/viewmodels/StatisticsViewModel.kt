package com.example.sensorstreamer.ui.viewmodels

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sensorstreamer.db.Run
import com.example.sensorstreamer.repositories.MainRepository
import kotlinx.coroutines.launch

//Collect the data from the repository and provide for the other fragments
class StatisticsViewModel @ViewModelInject constructor(
    val mainRepository: MainRepository
): ViewModel() {


}