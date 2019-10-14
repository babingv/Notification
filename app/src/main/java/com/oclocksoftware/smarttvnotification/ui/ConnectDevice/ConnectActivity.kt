package com.oclocksoftware.smarttvnotification.ui.ConnectDevice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle

import com.oclocksoftware.smarttvnotification.R
import com.oclocksoftware.smarttvnotification.base.BaseActivity

/**
 * Created by babin on 12/21/2017.
 */

class ConnectActivity : BaseActivity(){

    lateinit var mCoonectFragment:ConnectFragment


    override val contentView: Int
        get() = R.layout.activity_connect

    override val statusbarcolor: Int
        get() = R.color.colorAccent

    override val containerResId: Int
        get() = R.id.fragment_container

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent) {
        super.onViewReady(savedInstanceState, intent)
        mCoonectFragment=ConnectFragment.newInstance()
        switchFragment(mCoonectFragment)
    }

    override fun getmenuView(): Int {
        return 0
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()

    }




}
