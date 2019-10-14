package com.oclocksoftware.smarttvnotification.base

import android.content.Intent
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.CallSuper
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.*
import com.oclocksoftware.smarttvnotification.R
import com.oclocksoftware.smarttvnotification.utils.ResourcesHelper


/**
 * Created by babin on 12/19/2017.
 */

abstract class BaseActivity : AppCompatActivity() {
    protected var toolbarStyle = NO_TOOLBAR
        private set

    protected abstract val contentView: Int

    protected abstract val statusbarcolor: Int

    abstract val containerResId: Int


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ViewDataBinding>(this, contentView)
        onViewReady(savedInstanceState, intent)
        initToolbar()
    }

    @CallSuper
    open fun onViewReady(savedInstanceState: Bundle?, intent: Intent) {
        //To be used by child activities
        setStatusBarcolor(statusbarcolor)
    }


    fun setToolbarState(toolbarState: Int) {
        this.toolbarStyle = toolbarState
    }

    protected fun initToolbar() {
        when (toolbarStyle) {
            NEED_TOOLBAR -> {
                if (supportActionBar != null)
                    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                supportActionBar!!.setHomeButtonEnabled(true)
                supportActionBar!!.setBackgroundDrawable(ColorDrawable(ResourcesHelper
                        .getColor(this, R.color.colorAccent)))
                supportActionBar!!.elevation = 0f
            }

            NO_TOOLBAR -> supportActionBar!!.hide()
            else -> supportActionBar!!.hide()
        }
    }

    fun setToolbarVisibility(visible: Boolean) {
        val h = Handler()
        if (visible) {
            h.post { supportActionBar!!.show() }
        } else {
            h.post { supportActionBar!!.hide() }
        }
    }


    fun setToolBarTitle(title: String) {
        if (supportActionBar != null) supportActionBar!!.title = title
    }

    protected fun setStatusBarcolor(color: Int) {
        val window = this.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, statusbarcolor)
        }
    }

    /**
     * Switches fragments in activity.
     *
     * @param fragment
     * @param fragmentTag
     */
    fun switchFragment(fragment: Fragment, arg: Bundle, fragmentTag: String) {
        switchFragment(fragment, arg, false, fragmentTag)
    }

    /**
     * Switches fragments in activity.
     *
     * @param fragment
     * @param addToBackStack
     */
    @JvmOverloads
    fun switchFragment(fragment: Fragment, arg: Bundle=Bundle(), addToBackStack: Boolean = false) {
        switchFragment(true, arg, fragment, addToBackStack)
    }

    /**
     * Switches fragments in activity.
     *
     * @param fragment
     * @param addToBackStack
     * @param fragmentTag
     */
    fun switchFragment(fragment: Fragment, arg: Bundle, addToBackStack: Boolean, fragmentTag: String) {
        switchFragment(true, arg, fragment, addToBackStack, fragmentTag)
    }

    /**
     * Switches fragments in activity.
     *
     * @param replace
     * @param fragment
     * @param addToBackStack
     * @param fragmentTag
     */
    @JvmOverloads
    fun switchFragment(replace: Boolean, arg: Bundle?, fragment: Fragment, addToBackStack: Boolean, fragmentTag: String = fragment.javaClass.simpleName) {
        val transaction = supportFragmentManager.beginTransaction()

        if (arg != null) {
            fragment.arguments = arg
        }
        transaction.add(containerResId, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(fragmentTag)
        }
        transaction.commitAllowingStateLoss()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == android.R.id.home) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()

            } else {
                onBackPressed()
                finish()
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
            }
        }


        return super.onOptionsItemSelected(item)
    }

    /**
     * Calls onActivityResult inside fragment
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (findViewById<View>(R.id.fragment_container) != null) {
            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as BaseFragment
            fragment.onActivityResult(requestCode, resultCode, data)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (getmenuView() != 0) {
            val inflater = menuInflater
            inflater.inflate(getmenuView(), menu)
            return true
        } else {
            return true
        }
    }


    protected abstract fun getmenuView(): Int

    fun goBack() {
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
    }

    override fun onBackPressed() {

        if (supportFragmentManager.backStackEntryCount > 0) {

            val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as BaseFragment
            supportFragmentManager.popBackStack()


        } else {
            super.onBackPressed()
            finish()
            overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right)
        }
    }

    companion object {

        val NO_TOOLBAR = 0
        val NEED_TOOLBAR = 1
        val ORDER_TOOLBAR = 2
    }
}
/**
 * Switches fragments in activity.
 *
 * @param fragment - next fragment
 */
/**
 * Switches fragments in activity.
 *
 * @param arg
 * @param replace
 * @param fragment
 * @param addToBackStack
 */
