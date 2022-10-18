package com.example.sensorstreamer.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.sensorstreamer.R
import com.example.sensorstreamer.db.Run
import com.example.sensorstreamer.other.Constants.ACTION_PAUSE_SERVICE
import com.example.sensorstreamer.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.sensorstreamer.other.Constants.ACTION_STOP_SERVICE
import com.example.sensorstreamer.other.Constants.MAP_ZOOM
import com.example.sensorstreamer.other.Constants.POLYLINE_COLOR
import com.example.sensorstreamer.other.Constants.POLYLINE_WIDTH
import com.example.sensorstreamer.other.TrackingUtility
import com.example.sensorstreamer.services.PolyLine
import com.example.sensorstreamer.services.Polylines
import com.example.sensorstreamer.services.TrackingService
import com.example.sensorstreamer.ui.viewmodels.MainViewModel
import com.example.sensorstreamer.ui.viewmodels.StatisticsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking) {

    private val viewModel: MainViewModel by viewModels()

    private var map: GoogleMap? = null

    private var isTracking = false
    private var pathPoints = mutableListOf<PolyLine>()

    private var currentTimeMillis = 0L

    private var menu: Menu? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnFinishRun.setOnClickListener{
            zoomToSeeWholeTrack()
            endRunAndSaveToDB()
        }

        mapView.onCreate(savedInstanceState)
        btnToggleRun.setOnClickListener{
            toggleRun()
        }

        mapView.getMapAsync {
            map = it
            addAllPolylines()
        }
        subscribeToService()
    }

    private fun subscribeToService(){
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer{
            updateTracking(it)
        })

        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer{
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })

        TrackingService.timeRunInMillis.observe(viewLifecycleOwner, Observer{
            currentTimeMillis = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeMillis,true)
            tvTimer.text = formattedTime
        })
    }

    private fun toggleRun(){
        if(isTracking){
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else{
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog(){
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure you want to spot streaming?")
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes"){ _,_ ->
                stopRun()
            }
            .setNegativeButton("No") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    private fun stopRun(){
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun updateTracking(isTracking :Boolean){
        this.isTracking = isTracking
        if(!isTracking){
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else{
            btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser(){
        if(pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()){
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun zoomToSeeWholeTrack(){
        val bounds = LatLngBounds.Builder()

        for (polyline in pathPoints){
            for (pos in polyline){
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
            bounds.build(),
            mapView.width,
            mapView.height,
                (mapView.height* 0.05f).toInt()))
    }

    private fun endRunAndSaveToDB(){
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for (polyline in pathPoints){
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            var avgSpeed = round(distanceInMeters/(currentTimeMillis/1000f)*10)/10f //in meter/sec
            val dateTimestamp = Calendar.getInstance().timeInMillis
            val run = Run(bmp, dateTimestamp, avgSpeed, distanceInMeters, currentTimeMillis)
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG
            ).show()
            stopRun()
        }
    }

    //Draw every Polyline on the map. This needs to be here cause if the user rotates the screen every polyline is lost and needs to be re-draw
    private fun addAllPolylines(){
        for (polyline in pathPoints){
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline(){
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1){
            val preLastLatLong = pathPoints.last()[pathPoints.last().size-2]
            val lastLatLong = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLong)
                .add(lastLatLong)
            map?.addPolyline(polylineOptions)
        }
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        mapView?.onResume()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    /*The following function might crash the app
    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDestroy()
    }*/

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
}