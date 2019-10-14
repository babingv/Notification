package com.oclocksoftware.smarttvnotification.base

import android.support.annotation.StringRes

/**
 * Created by babin on 12/19/2017.
 */

interface MvpView {

    val isNetworkConnected: Boolean

    fun showLoading()

    fun hideLoading()

    fun openActivityOnTokenExpire()

    fun onError(@StringRes resId: Int)

    fun onError(message: String)

    fun showMessage(message: String)

    fun showMessage(@StringRes resId: Int)

    fun hideKeyboard()

}
