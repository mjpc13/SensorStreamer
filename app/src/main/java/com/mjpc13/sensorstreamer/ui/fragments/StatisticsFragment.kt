package com.mjpc13.sensorstreamer.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.mjpc13.sensorstreamer.R
import com.mjpc13.sensorstreamer.other.CustomMarkerView
import com.mjpc13.sensorstreamer.other.TrackingUtility
import com.mjpc13.sensorstreamer.ui.viewmodels.StatisticsViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_statistics.*

@AndroidEntryPoint
class StatisticsFragment: Fragment(R.layout.fragment_statistics) {

    private val viewModel : StatisticsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribeToObserver()
        setupBarChart()
    }

    private fun setupBarChart() {
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }
        barChart.apply {
            description.text = "Avg Speed Over time"
            legend.isEnabled = false
        }
    }

    private fun subscribeToObserver(){
        viewModel.meanTime.observe(viewLifecycleOwner, Observer{
            it?.let{
                val meanTime = TrackingUtility.getFormattedStopWatchTime(it.toLong())
                tvTotalTime.text = meanTime

            }
        })

        viewModel.meanDistance.observe(viewLifecycleOwner, Observer{
            it?.let{
                val meanDistance = it
                tvTotalDistance.text = "${meanDistance}m"
            }
        })
        viewModel.meanAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let{
                val meanAvgSpeed = it
                tvAverageSpeed.text = "${meanAvgSpeed}m/s"
            }
        })
        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let{
                val allAvgSpeeds = it.indices.map{i -> BarEntry(i.toFloat(), it[i].avgSpeed)}
                val bardataSet = BarDataSet(allAvgSpeeds, "Avg Speed Over Time").apply{
                    valueTextColor = Color.WHITE
                    color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                }
                barChart.data = BarData(bardataSet)
                barChart.marker = CustomMarkerView(it/*.reversed()*/, requireContext(), R.layout.marker_view)
                barChart.invalidate()
            }
        })
    }

}