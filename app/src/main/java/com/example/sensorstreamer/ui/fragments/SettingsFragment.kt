package com.example.sensorstreamer.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.sensorstreamer.R
import com.example.sensorstreamer.other.Constants.KEY_FRAME_ID
import com.example.sensorstreamer.other.Constants.KEY_GPS_MESSAGE_RATE
import com.example.sensorstreamer.other.Constants.KEY_TOPIC
import com.example.sensorstreamer.other.Constants.KEY_WEBSOCKET
import com.example.sensorstreamer.other.Constants.MAX_GPS_FREQUENCY
import com.example.sensorstreamer.other.Constants.MIN_GPS_FREQUENCY
import com.example.sensorstreamer.other.MessageListener
import com.example.sensorstreamer.other.WebSocketManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_settings.*
import java.lang.Float.parseFloat
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.thread
import kotlin.concurrent.schedule

@AndroidEntryPoint
class SettingsFragment: Fragment(R.layout.fragment_settings), MessageListener{

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadFieldsFromSharedPref()

        btnApplyChanges.setOnClickListener{

            when(applyChangesToSharedPref()){
                "empty" -> Snackbar.make(view, "Please fill all the fields", Snackbar.LENGTH_LONG).show()
                "notnumber" -> Snackbar.make(view, "The publish rate (Hz) must be a number", Snackbar.LENGTH_LONG).show()
                "wronginterval" -> Snackbar.make(view, "The publish rate needs to be greater than 0 and lower than $MAX_GPS_FREQUENCY !", Snackbar.LENGTH_LONG).show()
                else -> Snackbar.make(view, "Saved changes", Snackbar.LENGTH_LONG).show()
            }

        }

        btnConnect.setOnClickListener {
            val websocket = sharedPreferences.getString(KEY_WEBSOCKET, "ws://127.0.0.1:9090")
            val topic = sharedPreferences.getString(KEY_TOPIC, "android") ?: "android"
            // I think I need to change the following!

            WebSocketManager.init(websocket.toString(), this)

            thread {
                kotlin.run {
                    WebSocketManager.connect()
                }
            }
            Timer("SettingUp", false).schedule(500L) {
                val topicMessage = "{\"op\": \"advertise\", \"topic\": \"${topic}/gps/assisted\", \"type\": \"sensor_msgs/NavSatFix\"}"
                if(WebSocketManager.sendMessage(topicMessage)){
                    Snackbar.make(it, "Connection successful and topic created", Snackbar.LENGTH_LONG).show()
                } else{
                    Snackbar.make(it, "Connection failed, topic was not created", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        btnDisconnect.setOnClickListener {
            val topic = sharedPreferences.getString(KEY_TOPIC, "android") ?: "android"

            val topicMessage = "{\"op\": \"unadvertise\", \"topic\": \"${topic}/gps/assisted\"}"
            WebSocketManager.sendMessage(topicMessage)
            WebSocketManager.close()
            Snackbar.make(view, "Connection closed", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun loadFieldsFromSharedPref(){
        val websocket = sharedPreferences.getString(KEY_WEBSOCKET, "ws://127.0.0.1:9090")
        val topic = sharedPreferences.getString(KEY_TOPIC, "android")
        val frameId = sharedPreferences.getString(KEY_FRAME_ID, "smartphone_frame")
        val publishRate = sharedPreferences.getFloat(KEY_GPS_MESSAGE_RATE, 0.2F)

        etWebSocket.setText(websocket)
        etROSTopic.setText(topic)
        etFrameID.setText(frameId)
        etPublishRate.setText(publishRate.toString())
    }

    private fun applyChangesToSharedPref(): String? {
        val websocketText = etWebSocket.text.toString()
        val topicText = etROSTopic.text.toString()
        val frameIdText = etFrameID.text.toString()
        val publishRate = etPublishRate.text.toString()

        val publishRateFloat: Float

        if(websocketText.isEmpty() || topicText.isEmpty() || frameIdText.isEmpty() || publishRate.isEmpty()){
            return "empty" // returns that one or more fields are empty
        }

        try {
            publishRateFloat = parseFloat(publishRate)
        } catch (e: NumberFormatException) {
            return "notnumber" //returns if the publish rate is not a number
        }

        //Can only publish at positive rate and a maximum rate of
        if(publishRateFloat > MAX_GPS_FREQUENCY || publishRateFloat < MIN_GPS_FREQUENCY){
            return "wronginterval" //returns if the publish rate is in the wrong interval
        }

        sharedPreferences.edit()
            .putString(KEY_WEBSOCKET, websocketText)
            .putString(KEY_TOPIC, topicText)
            .putString(KEY_FRAME_ID, frameIdText)
            .putFloat(KEY_GPS_MESSAGE_RATE, publishRateFloat)
            .apply()
        return null
    }

    override fun onConnectSuccess() {
        Log.i("SettingsFragment", "OnConnectSuccess")
    }

    override fun onConnectFailed() {
        Log.i("SettingsFragment", "OnConnectFailed")
    }

    override fun onClose() {
        Log.i("SettingsFragment", "OnClose")
    }

    override fun onMessage(text: String?) {
        Log.i("SettingsFragment", "Received Message: $text \n")
    }

}