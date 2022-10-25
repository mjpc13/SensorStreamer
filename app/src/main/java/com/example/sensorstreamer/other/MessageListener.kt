package com.example.sensorstreamer.other

interface MessageListener {
    fun onConnectSuccess ()
    fun onConnectFailed ()
    fun onClose ()
    fun onMessage (text: String?)
}
