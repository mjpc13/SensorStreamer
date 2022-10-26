package com.example.sensorstreamer.other

import android.graphics.Color

object Constants {

    const val STREAMING_DATABASE_NAME = "streaming_db"

    const val REQUEST_CODE_LOCATION_PERMISSION = 0

    const val ACTION_START_OR_RESUME_SERVICE = "ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE = "ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE = "ACTION_STOP_SERVICE"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"

    const val TIMER_UPDATE_INTERVAL=50L

    const val SHARED_PREFERENCES_NAME= "sharedPref"
    const val KEY_WEBSOCKET= "KEY_WEBSOCKET"
    const val KEY_TOPIC= "KEY_TOPIC"
    const val KEY_FRAME_ID= "KEY_FRAME_ID"
    const val KEY_GPS_MESSAGE_RATE= "KEY_GPS_MESSAGE_RATE"

    const val LOCATION_UPDATE_RATE = 5000L //In milliseconds
    const val FASTEST_LOCATION_INTERVAL = 2000L

    const val MAX_GPS_FREQUENCY = 100L
    const val MIN_GPS_FREQUENCY = 0.0005F

    const val POLYLINE_COLOR = Color.RED
    const val POLYLINE_WIDTH = 8f

    const val MAP_ZOOM = 18f

    const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Tracking"
    const val NOTIFICATION_ID = 1

}