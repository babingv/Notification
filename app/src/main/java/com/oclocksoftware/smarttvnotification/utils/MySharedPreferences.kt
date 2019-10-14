package com.oclocksoftware.smarttvnotification.utils

import android.content.SharedPreferences

import javax.inject.Inject

/**
 * Created by babin on 12/19/2017.
 */

class MySharedPreferences {
    @Inject
    constructor(mSharedPreferences: SharedPreferences) {

        fun putData(key: String, data: Int) {
            mSharedPreferences.edit().putInt(key, data).apply()
        }

        fun getData(key: String): Int {
            return mSharedPreferences.getInt(key, 0)
        }
    }
}

