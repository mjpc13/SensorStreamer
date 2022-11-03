package com.mjpc13.sensorstreamer.other

interface MessageListener {
    fun onConnectSuccess ()
    fun onConnectFailed ()
    fun onClose ()
    fun onMessage (text: String?)
}
