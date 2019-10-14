

package com.oclocksoftware.smarttvnotification.nsd

import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.UnknownHostException
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

class SocketConnection(private val mUpdateHandler: Handler, private val mSocketInterface: SocketInterface) {
    private val mServer: ServerConnection
    private var mClient: ClientConnection? = null

    private
    var socket: Socket? = null
        @Synchronized set(socket) {
            Log.d(TAG, "setSocket being called.")
            if (socket == null) {
                Log.d(TAG, "Setting a null socket.")
            }
            if (this.socket != null) {
                if (this.socket!!.isConnected) {
                    try {
                        this.socket!!.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
            field = socket
        }
    var localPort = -1

    init {
        mServer = ServerConnection(mUpdateHandler)
    }

    fun tearDown() {
        mServer.tearDown()
        mClient!!.tearDown()
    }

    fun connectToServer(address: InetAddress, port: Int) {
        mClient = ClientConnection(address, port)
    }

    fun sendMessage(msg: String) {
        if (mClient != null) {
            mClient!!.sendMessage(msg)
        }
    }


    @Synchronized
    fun updateMessages(msg: String, local: Boolean) {
        var msg = msg
        Log.e(TAG, "Updating message: " + msg)

        if (local) {
            msg = msg
        } else {
            msg = msg
        }

        val messageBundle = Bundle()
        messageBundle.putString("msg", msg)

        val message = Message()
        message.data = messageBundle
        mUpdateHandler.sendMessage(message)

    }

    private inner class ServerConnection(handler: Handler) {
        internal var mServerSocket: ServerSocket? = null
        internal var mThread: Thread? = null

        init {
            mThread = Thread(ServerThread())
            mThread!!.start()
        }

        fun tearDown() {
            mThread!!.interrupt()
            try {
                mServerSocket!!.close()
            } catch (ioe: IOException) {
                Log.e(TAG, "Error when closing server socket.")
            }

        }

        internal inner class ServerThread : Runnable {

            override fun run() {

                try {
                    // Since discovery will happen via Nsd, we don't need to care which port is
                    // used.  Just grab an available one  and advertise it via Nsd.
                    mServerSocket = ServerSocket(0)
                    localPort = mServerSocket!!.localPort

                    while (!Thread.currentThread().isInterrupted) {
                        Log.d(TAG, "ServerSocket Created, awaiting connection")
                        socket = mServerSocket!!.accept()
                        if (mClient == null) {
                            val port = socket!!.port
                            val address = socket!!.inetAddress
                            connectToServer(address, port)
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Error creating ServerSocket: ", e)
                    e.printStackTrace()
                }

            }
        }
    }

    private inner class ClientConnection(private val mAddress: InetAddress, private val PORT: Int) {

        private val CLIENT_TAG = "ClientConnection"

        private val mSendThread: Thread
        private var mRecThread: Thread? = null

        init {

            Log.d(CLIENT_TAG, "Creating chatClient")

            mSendThread = Thread(SendingThread())
            mSendThread.start()
        }

        internal inner class SendingThread : Runnable {

            var mMessageQueue: BlockingQueue<String>
            private val QUEUE_CAPACITY = 10

            init {
                mMessageQueue = ArrayBlockingQueue(QUEUE_CAPACITY)
            }

            override fun run() {
                try {
                    if (socket == null) {
                        socket = Socket(mAddress, PORT)
                        Log.d(CLIENT_TAG, "Client-side socket initialized.")
                        //sendMessage("initialized")
                        mSocketInterface.onSocketConnected()

                    } else {
                        Log.d(CLIENT_TAG, "Socket already initialized. skipping!")
                    }

                    mRecThread = Thread(ReceivingThread())
                    mRecThread!!.start()

                } catch (e: UnknownHostException) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, UHE", e)
                    mSocketInterface.onSocketConnectionFailed()
                } catch (e: IOException) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, IOE.", e)
                    mSocketInterface.onSocketConnectionFailed()
                }

                while (true) {
                    try {
                        val msg = mMessageQueue.take()
                        sendMessage(msg)
                    } catch (ie: InterruptedException) {
                        Log.d(CLIENT_TAG, "Message sending loop interrupted, exiting")
                    }

                }
            }
        }

        internal inner class ReceivingThread : Runnable {

            override fun run() {

                val input: BufferedReader
                try {
                    input = BufferedReader(InputStreamReader(
                            socket!!.getInputStream()))
                    while (!Thread.currentThread().isInterrupted) {

                        var messageStr: String? = null
                        messageStr = input.readLine()
                        if (messageStr != null) {
                            Log.d(CLIENT_TAG, "Read from the stream: " + messageStr)
                            updateMessages(messageStr, false)
                        } else {
                            Log.d(CLIENT_TAG, "The nulls! The nulls!")
                            mSocketInterface.onSocketDisConnected()

                            break
                        }
                    }
                    input.close()

                } catch (e: IOException) {
                    Log.e(CLIENT_TAG, "Server loop error: ", e)
                }

            }
        }

        fun tearDown() {
            try {
                socket!!.close()
            } catch (ioe: IOException) {
                Log.e(CLIENT_TAG, "Error when closing server socket.")
            }

        }

        fun sendMessage(msg: String) {
            try {
                val socket = socket
                if (socket == null) {
                    Log.d(CLIENT_TAG, "Socket is null, wtf?")
                } else if (socket.getOutputStream() == null) {
                    Log.d(CLIENT_TAG, "Socket output stream is null, wtf?")
                }

                val out = PrintWriter(
                        BufferedWriter(
                                OutputStreamWriter(socket!!.getOutputStream())), true)
                out.println(msg)
                out.flush()
                updateMessages(msg, true)
            } catch (e: UnknownHostException) {
                Log.d(CLIENT_TAG, "Unknown Host", e)
            } catch (e: IOException) {
                Log.d(CLIENT_TAG, "I/O Exception", e)
            } catch (e: Exception) {
                Log.d(CLIENT_TAG, "Error3", e)
            }

            Log.d(CLIENT_TAG, "Client sent message: " + msg)
        }
    }

    companion object {

        private val TAG = "SocketConnection"
    }
    public fun isSocketConnected(): Boolean{
        if(socket==null){
            return false
        }else{
            return socket!!.isConnected()
        }
    }
}
