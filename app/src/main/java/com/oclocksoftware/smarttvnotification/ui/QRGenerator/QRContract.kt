package com.oclocksoftware.smarttvnotification.ui.QRGenerator

import android.graphics.Bitmap
import com.oclocksoftware.smarttvnotification.base.MvpPresenter
import com.oclocksoftware.smarttvnotification.base.MvpView
import com.oclocksoftware.smarttvnotification.di.scope.ActivityScope
import com.oclocksoftware.smarttvnotification.nsd.SocketConnection

/**
 * Created by babin on 12/22/2017.
 */

open interface QRContract{
    interface View : MvpView {

        fun dipslayQRCode(bitmap: Bitmap)
        fun dipslayRandomNumber(randomNumber: String)
        fun displayNotification(image: Bitmap?, title: String, content: String)

    }

    @ActivityScope
    interface Presenter<V : View> : MvpPresenter<V> {

        fun generateQRCode(smallestDimension: Int, mServiceName: String)
        fun registerDevicetoNSD(mConnection: SocketConnection, mServiceName: String)

    }
}
