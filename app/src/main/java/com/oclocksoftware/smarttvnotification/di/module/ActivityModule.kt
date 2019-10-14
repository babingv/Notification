package com.oclocksoftware.smarttvnotification.di.module

import android.app.Activity
import android.content.Context
import android.net.nsd.NsdManager

import com.oclocksoftware.smarttvnotification.di.scope.ActivityScope
import com.oclocksoftware.smarttvnotification.nsd.NsdHelper
import com.oclocksoftware.smarttvnotification.ui.ConnectDevice.ConnectContract
import com.oclocksoftware.smarttvnotification.ui.ConnectDevice.ConnectPresenter
import com.oclocksoftware.smarttvnotification.ui.QRGenerator.QRContract
import com.oclocksoftware.smarttvnotification.ui.QRGenerator.QRPresenter

import dagger.Module
import dagger.Provides
import javax.inject.Singleton

/**
 * Created by babin on 12/21/2017.
 */

@Module
class ActivityModule(val mActivity: Activity) {

    @Provides
    @ActivityScope
    fun providesActivity(): Activity{
        return mActivity
    }
    @Provides
    @ActivityScope
    fun provideConnectPresenter(
            presenter: ConnectPresenter<ConnectContract.View>): ConnectContract.Presenter<ConnectContract.View> {
        return presenter
    }
    @Provides
    @ActivityScope
    fun provideQRPresenter(
            presenter: QRPresenter<QRContract.View>): QRContract.Presenter<QRContract.View> {
        return presenter
    }
    @Provides
    fun providesNsdHelper(): NsdHelper {
        return NsdHelper(mActivity.getSystemService(Context.NSD_SERVICE) as NsdManager)
    }

}