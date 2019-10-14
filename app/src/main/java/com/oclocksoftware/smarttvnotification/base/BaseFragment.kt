package com.oclocksoftware.smarttvnotification.base

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.oclocksoftware.smarttvnotification.di.component.ActivityComponent


/**
 * Created by babin on 12/19/2017.
 */

abstract class BaseFragment : Fragment(), MvpView {
    // TODO: Rename parameter arguments, choose names that match
    /**
     * Gets root fragment view
     *
     * @return
     */
    lateinit var rootView: View
    lateinit var progressDoalog: ProgressDialog
    private val mActivityComponent: ActivityComponent? = null

    val baseActivity: BaseActivity
        get() = activity as BaseActivity

    override val isNetworkConnected: Boolean
        get() = false

    /**
     * Initialize Data Binding views
     */
    protected abstract fun initViews(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View

    /**
     * Toolbar initialization
     */
    protected fun initToolbar() {}

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = initViews(inflater, container, savedInstanceState)
        initToolbar()
        configureProgressDialogue()
        return rootView
    }


    private fun configureProgressDialogue() {
        progressDoalog = ProgressDialog(activity)
        progressDoalog.setTitle("SMT Traders")
        progressDoalog.setCancelable(false)
    }

    fun showProgressBar(sync: Boolean) {
        progressDoalog.setMessage(if (sync) "Syncing..." else "Clearing Data...")
        progressDoalog.show()
    }

    fun hideProgressBar() {
        progressDoalog.dismiss()
    }


    override fun onDestroy() {
        super.onDestroy()
        progressDoalog.dismiss()
    }

    override fun onPause() {
        super.onPause()
        progressDoalog.dismiss()
    }

    /**
     * Calls onActivityResult inside fragment
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

    override fun openActivityOnTokenExpire() {

    }

    override fun onError(resId: Int) {

    }

    override fun onError(message: String) {

    }

    override fun showMessage(message: String) {

    }

    override fun showMessage(resId: Int) {

    }

    override fun hideKeyboard() {

    }


}
