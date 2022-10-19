package com.example.sensorstreamer.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.example.sensorstreamer.R
import com.example.sensorstreamer.other.Constants.KEY_TOPIC
import com.example.sensorstreamer.other.Constants.KEY_WEBSOCKET
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_settings.*
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment: Fragment(R.layout.fragment_settings) {

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
    }

    private fun loadFieldsFromSharedPref(){
        val websocket = sharedPreferences.getString(KEY_WEBSOCKET, "ws://0.0.0.0:9090")
        val topic = sharedPreferences.getString(KEY_TOPIC, "android/")

        etName.setText(websocket)
        etWeight.setText(topic)
    }

    private fun applyChangesToSharedPref(): Boolean {
        val websocketText = etName.text.toString()
        val topicText = etWeight.text.toString()

        if(websocketText.isEmpty() || topicText.isEmpty()){
            return false
        }
        sharedPreferences.edit()
            .putString(KEY_WEBSOCKET, websocketText)
            .putString(KEY_TOPIC, topicText)
            .apply()

        return true

    }
}