package com.oclocksoftware.smarttvnotification.di.module

import android.content.Context
import android.content.SharedPreferences
import android.net.nsd.NsdManager
import com.oclocksoftware.smarttvnotification.nsd.NsdHelper

import javax.inject.Singleton

import dagger.Module
import dagger.Provides

/**
 * Created by babin on 12/19/2017.
 */
@Module
class AppPreferenceModule(val context: Context) {

    @Provides
    @Singleton
    fun providesApplicationContext(): Context{
        return context
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(): SharedPreferences {
        return context.getSharedPreferences("PrefName", Context.MODE_PRIVATE)
    }


}