package com.oclocksoftware.smarttvnotification.ui.QRGenerator

import android.content.Intent
import android.os.Bundle
import com.oclocksoftware.smarttvnotification.R

import com.oclocksoftware.smarttvnotification.base.BaseActivity

/**
 * Created by babin on 12/22/2017.
 */

class QRActivity : BaseActivity() {

    override val contentView: Int
        get() = R.layout.activity_connect

    override val statusbarcolor: Int
        get() = R.color.colorAccent

    override val containerResId: Int
        get() = R.id.fragment_container

    override fun onViewReady(savedInstanceState: Bundle?, intent: Intent) {
        super.onViewReady(savedInstanceState, intent)
        switchFragment(QRFragment.newInstance())
    }

    override fun getmenuView(): Int {
        return 0
    }


}
