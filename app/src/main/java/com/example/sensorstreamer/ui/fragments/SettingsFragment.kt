package com.example.sensorstreamer.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.sensorstreamer.R
import com.example.sensorstreamer.other.Constants.KEY_TOPIC
import com.example.sensorstreamer.other.Constants.KEY_WEBSOCKET
import com.example.sensorstreamer.other.MessageListener
import com.example.sensorstreamer.other.WebSocketManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_settings.*
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
            val success = applyChangesToSharedPref()
            if (success){
                Snackbar.make(view, "Saved changes", Snackbar.LENGTH_LONG).show()
            } else{
                Snackbar.make(view, "Please fill all the fields", Snackbar.LENGTH_LONG).show()
            }
        }

        btnConnect.setOnClickListener {
            val websocket = sharedPreferences.getString(KEY_WEBSOCKET, "ws://127.0.0.1:9090")
            val topic = sharedPreferences.getString(KEY_TOPIC, "android/") ?: "android/"
            // I think I need to change the following!

            WebSocketManager.init(websocket.toString(), this)

            thread {
                kotlin.run {
                    WebSocketManager.connect()
                }
            }
            Timer("SettingUp", false).schedule(100L) {
                val topicMessage = "{\"op\": \"advertise\", \"topic\": \"${topic.toString()}gps\", \"type\": \"sensor_msgs/NavSatFix\"}"
                if(WebSocketManager.sendMessage(topicMessage)){
                    Snackbar.make(it, "Connection successful and topic created", Snackbar.LENGTH_LONG).show()
                } else{
                    Snackbar.make(it, "Connection failed, topic was not created", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        btnDisconnect.setOnClickListener {
            val topic = sharedPreferences.getString(KEY_TOPIC, "android/") ?: "android/"

            val topicMessage = "{\"op\": \"unadvertise\", \"topic\": \"${topic.toString()}/gps\"}"
            WebSocketManager.sendMessage(topicMessage)
            WebSocketManager.close()
            Snackbar.make(view, "Connection closed", Snackbar.LENGTH_LONG).show()
        }
    }

    private fun loadFieldsFromSharedPref(){
        val websocket = sharedPreferences.getString(KEY_WEBSOCKET, "ws://127.0.0.1:9090")
        val topic = sharedPreferences.getString(KEY_TOPIC, "android/")

        etWebSocket.setText(websocket)
        etROSTopic.setText(topic)
    }

    private fun applyChangesToSharedPref(): Boolean {
        val websocketText = etWebSocket.text.toString()
        val topicText = etROSTopic.text.toString()

        if(websocketText.isEmpty() || topicText.isEmpty()){
            return false
        }
        sharedPreferences.edit()
            .putString(KEY_WEBSOCKET, websocketText)
            .putString(KEY_TOPIC, topicText)
            .apply()

        return true

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