package com.oclocksoftware.smarttvnotification.ui.ConnectDevice

import android.service.notification.StatusBarNotification
import com.oclocksoftware.smarttvnotification.base.MvpPresenter
import com.oclocksoftware.smarttvnotification.base.MvpView
import com.oclocksoftware.smarttvnotification.di.scope.ActivityScope
import com.oclocksoftware.smarttvnotification.nsd.SocketConnection

/**
 * Created by babin on 12/21/2017.
 */

interface ConnectContract {

    interface View : MvpView {

        fun dipslayDeviceList()
        fun pushMessage(json: String)

    }

    @ActivityScope
    interface Presenter<V : View> : MvpPresenter<V> {

        fun verifyRegisterCode(mConnection: SocketConnection,s:String)
        fun scanQRCode()
        fun connectDevice( mConnection: SocketConnection,scannedContent: String)
        fun initializeNsd()
        fun registerDevicetoNetwork(mConnection: SocketConnection)
        fun enableNotificationListener()
        fun parsePushNotification(receivedNotification: StatusBarNotification?)
        fun parseCallNotification(state: Int, incomingNumber: String?)
    }
}
