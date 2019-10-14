package com.oclocksoftware.smarttvnotification.di.component


import com.oclocksoftware.smarttvnotification.SmartApplication
import com.oclocksoftware.smarttvnotification.di.module.AppPreferenceModule
import com.oclocksoftware.smarttvnotification.ui.QRGenerator.QRFragment
import javax.inject.Singleton

import dagger.Component

/**
 * Created by babin on 12/19/2017.
 */
@Singleton
@Component(modules = arrayOf(AppPreferenceModule::class))
interface AppComponent {
    object Initializer {
        fun init(app: SmartApplication): AppComponent {
            return DaggerAppComponent.builder()
                    .build()
        }
    }

}
