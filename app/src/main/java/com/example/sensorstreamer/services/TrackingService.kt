package com.example.sensorstreamer.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.GnssStatus
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.sensorstreamer.R
import com.example.sensorstreamer.other.Constants.ACTION_PAUSE_SERVICE
import com.example.sensorstreamer.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.sensorstreamer.other.Constants.ACTION_STOP_SERVICE
import com.example.sensorstreamer.other.Constants.FASTEST_LOCATION_INTERVAL
import com.example.sensorstreamer.other.Constants.LOCATION_UPDATE_RATE
import com.example.sensorstreamer.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.sensorstreamer.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.sensorstreamer.other.Constants.NOTIFICATION_ID
import com.example.sensorstreamer.other.Constants.TIMER_UPDATE_INTERVAL
import com.example.sensorstreamer.other.MessageListener
import com.example.sensorstreamer.other.TrackingUtility
import com.example.sensorstreamer.other.WebSocketManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

typealias PolyLine = MutableList<LatLng>
typealias Polylines = MutableList<PolyLine>

@AndroidEntryPoint
class TrackingService : LifecycleService() {

    var isFirstRun = true
    var serviceKilled = false

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val timeRunInSeconds = MutableLiveData<Long>()

    @Inject
    lateinit var baseNotificationBuilder: NotificationCompat.Builder
    lateinit var curNotificationBuilder: NotificationCompat.Builder

    companion object {
        //Mutable Live Data allows to observe changes in data
        val timeRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
    }

    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }

    override fun onCreate() {
        super.onCreate()
        curNotificationBuilder = baseNotificationBuilder
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        GnssStatus.Builder()

        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }

    private fun killService(){
        serviceKilled = true
        isFirstRun = true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        intent?.let {
            when (it.action){
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun){
                        startForegroundService()
                        isFirstRun = false
                    } else{
                        Timber.d("Resuming Service")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private var isTimerEnable = false
    private var lapTime = 0L // time from the beginning
    private var timeRun = 0L //total time
    private var timeStarted = 0L //Timestamp on when timer started
    private var lastSecondTimestamp = 0L

    //Function to start timer, either by first time or after a stop
    private fun startTimer(){
        addEmptyPolyline() //Fills the Polyline with empty values
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnable = true
        CoroutineScope(Dispatchers.Main).launch{
            while (isTracking.value!!){
                // time difference between now and timeStarted
                lapTime = System.currentTimeMillis() - timeStarted

                timeRunInMillis.postValue(lapTime + timeRun) //post new lapTime in mutable live object
                if(timeRunInMillis.value!! >= lastSecondTimestamp + 1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!! + 1)
                    lastSecondTimestamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnable = false
    }

    private fun updateNotificationTrackingState(isTracking: Boolean){
        val notificationActionText = if(isTracking) "Pause" else "Resume"
        val pendingIntent = if(isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply{
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else{
            val resumeIntent = Intent(this, TrackingService::class.java).apply{
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        curNotificationBuilder.javaClass.getDeclaredField("mActions").apply{
            isAccessible = true
            set(curNotificationBuilder, ArrayList<NotificationCompat.Action>())
        }
        if(!serviceKilled){
            curNotificationBuilder = baseNotificationBuilder
                .addAction(R.drawable.ic_pause_black_24dp, notificationActionText, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, curNotificationBuilder.build())
        }
    }

    private fun updateLocationTracking(isTracking: Boolean){
        if(isTracking){
            if(TrackingUtility.hasLocationPermissions(this)){

                val request = com.google.android.gms.location.LocationRequest().apply {
                    interval = LOCATION_UPDATE_RATE
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
                // Add gnssStatus Builder here!
            }
        } else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback= object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {

            super.onLocationResult(result)
            if(isTracking.value!!){
                result?.locations?.let { locations ->
                    for(location in locations){
                        addPathPoint(location)
                        //NEED TO CALL THE FUNCTION TO PUBLISH MESSAGE TO ROS IN HERE
                        val rosMessage = generateROSMessage(location)
                        WebSocketManager.sendMessage(rosMessage)
                        Timber.d("NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private fun addPathPoint(location: Location?){
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyPolyline() = pathPoints.value?.apply{
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel((channel))
    }
    private fun startForegroundService(){
        startTimer()
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

        //Check if device has android version above android Oreo
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, Observer{
            if (!serviceKilled) {
                val notification = curNotificationBuilder
                    .setContentText(TrackingUtility.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }

    private fun generateROSMessage(location: Location?): String{

        //TODO: Gather these values from gnssStatus object
        val status = -1
        val service = 1

        val gpsMessage = when(location!!.hasAltitude()){
            true -> """
                {"op": "publish",
                "topic": "android/gps",
                "msg": {
                    "status": {
                        "status": ${status},
                        "service": $service
                    },
                    "latitude": ${location.latitude},
                    "longitude": ${location.longitude},
                    "altitude": ${location.altitude},
                    "position_covariance": [1,0,0,0,1,0,0,0,1],
                    "position_covariance_type": 0
                    }
                }
            """.trimIndent()
            else -> """
                {"op": "publish",
                "topic": "/android/gps",
                "msg": {
                    "status": {
                        "status": ${status},
                        "service": $service
                    },
                    "latitude": ${location.latitude},
                    "longitude": ${location.longitude},
                    "position_covariance": [1,0,0,0,1,0,0,0,1],
                    "position_covariance_type": 0
                    }
                }
            """.trimIndent()
        }

        val jsonObject = JSONObject(gpsMessage)
        return jsonObject.toString()
    }

}