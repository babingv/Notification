package com.oclocksoftware.smarttvnotification.di.module

import android.content.Context
import android.content.SharedPreferences


import com.oclocksoftware.smarttvnotification.di.scope.ActivityScope

import dagger.Module
import dagger.Provides

/**
 * Created by babin on 12/21/2017.
 */

@Module
class ActivityPrefrenceModule(val context: Context) {

    @Provides
    @ActivityScope
    fun provideSharedPreferences(): SharedPreferences {
        return context.getSharedPreferences("PrefName", Context.MODE_PRIVATE)
    }

}
