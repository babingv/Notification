package com.oclocksoftware.smarttvnotification.di.component


import com.oclocksoftware.smarttvnotification.di.module.*
import com.oclocksoftware.smarttvnotification.di.scope.ActivityScope
import com.oclocksoftware.smarttvnotification.ui.ConnectDevice.ConnectFragment
import com.oclocksoftware.smarttvnotification.ui.QRGenerator.QRFragment

import dagger.Component

/**
 * Created by babin on 12/19/2017.
 */
@ActivityScope
@Component(dependencies = arrayOf(AppComponent::class), modules = arrayOf(ActivityPrefrenceModule::class,ActivityModule::class))
interface ActivityComponent{
    fun inject(mConnectFragment: ConnectFragment)
    fun inject(mQRFragment: QRFragment)
}


