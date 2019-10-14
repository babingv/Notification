package com.oclocksoftware.smarttvnotification

import android.app.Application

import com.oclocksoftware.smarttvnotification.di.component.AppComponent

/**
 * Created by babin on 12/21/2017.
 */

class SmartApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        buildComponentAndInject()
    }

    companion object {
        lateinit var instance: SmartApplication
        var component: AppComponent? = null

        fun buildComponentAndInject() {
            component = AppComponent.Initializer.init(instance)
        }
    }
}
