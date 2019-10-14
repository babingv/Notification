package com.oclocksoftware.smarttvnotification.base

/**
 * Created by babin on 12/19/2017.
 */

interface MvpPresenter<V : MvpView> {

    fun onAttachMvpView(mvpView: V)

    fun onDetach()
}

