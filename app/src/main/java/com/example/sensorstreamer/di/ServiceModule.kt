package com.example.sensorstreamer.di

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.sensorstreamer.R
import com.example.sensorstreamer.other.Constants
import com.example.sensorstreamer.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.sensorstreamer.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class) //Declares how long the dependencies of this module will live
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideFusedLocationProviderClient(
        @ApplicationContext app: Context
    ) = FusedLocationProviderClient(app)

    @ServiceScoped
    @Provides
    fun provideMainActivityPendingIntent(
        @ApplicationContext app: Context
    )= PendingIntent.getActivity(
        app,
        0,
        Intent(app, MainActivity::class.java).also {
            it.action = Constants.ACTION_SHOW_TRACKING_FRAGMENT
        },
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    @ServiceScoped
    @Provides
    fun provideBaseNotificationBuilder(
        @ApplicationContext app: Context,
        pendingIntent: PendingIntent
    ) = NotificationCompat.Builder(app, NOTIFICATION_CHANNEL_ID)
    .setAutoCancel(false)
    .setOngoing(true)
    .setSmallIcon(R.drawable.ic_directions_run_black_24dp)
    .setContentTitle("Sensor Streaming Running")
    .setContentText("00:00:00")
    .setContentIntent(pendingIntent)

}