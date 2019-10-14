package com.oclocksoftware.smarttvnotification.nsd

/**
 * Created by babin on 1/3/2018.
 */

interface SocketInterface{
    fun onSocketConnected()
    fun onSocketDisConnected()
    fun onSocketConnectionFailed()
}
