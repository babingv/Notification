package com.oclocksoftware.smarttvnotification.base


import com.oclocksoftware.smarttvnotification.utils.MySharedPreferences

import javax.inject.Inject

/**
 * Created by babin on 12/26/2017.
 */

open class BasePresenter<V : MvpView> : MvpPresenter<V> {
    lateinit var mvpView: V
        private set
    private var mySharedPreferences: MySharedPreferences? = null

    val isViewAttached: Boolean
        get() = mvpView != null

    @Inject
    constructor(mySharedPreferences: MySharedPreferences) {
        this.mySharedPreferences = mySharedPreferences
    }



    fun checkViewAttached() {
        if (!isViewAttached) throw MvpViewNotAttachedException()
    }

    override fun onAttachMvpView(mvpView: V) {
        this.mvpView = mvpView
    }

    override fun onDetach() {
    }


    class MvpViewNotAttachedException : RuntimeException("Please call Presenter.onAttach(MvpView) before" + " requesting data to the Presenter")

    companion object {

        private val TAG = "BasePresenter"
    }
}
