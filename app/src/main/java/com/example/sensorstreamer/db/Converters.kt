package com.example.sensorstreamer.db

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {

    @TypeConverter
    fun fromBitmap(bpm: Bitmap): ByteArray {
        val outputStream = ByteArrayOutputStream()
        bpm.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

        return outputStream.toByteArray()
    }

    @TypeConverter
    fun toBitmap(bytes: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}