package com.example.sensorstreamer.di

import android.content.Context
import androidx.room.Room
import com.example.sensorstreamer.db.StreamingDatabase
import com.example.sensorstreamer.other.Constants.STREAMING_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Singleton //Only allows the creation of 1 item. Further requests will pass the same instance!
    @Provides
    fun provideStreamingDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        StreamingDatabase::class.java,
        STREAMING_DATABASE_NAME
    ).build()

    @Singleton
    @Provides
    fun provideRunDAO(db: StreamingDatabase) = db.getRunDao()


}