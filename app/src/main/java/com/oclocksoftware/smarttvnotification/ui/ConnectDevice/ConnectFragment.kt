package com.oclocksoftware.smarttvnotification.ui.ConnectDevice

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.*
import android.content.Context.ACTIVITY_SERVICE
import android.database.Cursor
import android.databinding.DataBindingUtil
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.ContactsContract
import android.service.notification.StatusBarNotification
import android.support.v4.app.NotificationCompat
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import com.oclocksoftware.smarttvnotification.R

import com.oclocksoftware.smarttvnotification.SmartApplication
import com.oclocksoftware.smarttvnotification.base.BaseFragment
import com.oclocksoftware.smarttvnotification.databinding.FragmentConnectBinding
import com.oclocksoftware.smarttvnotification.di.component.DaggerActivityComponent
import com.oclocksoftware.smarttvnotification.di.module.ActivityModule
import com.oclocksoftware.smarttvnotification.di.module.ActivityPrefrenceModule
import com.oclocksoftware.smarttvnotification.utils.MySharedPreferences

import javax.inject.Inject
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.KeyEvent
import android.widget.RemoteViews
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.integration.android.IntentIntegrator
import com.oclocksoftware.smarttvnotification.BuildConfig
import com.oclocksoftware.smarttvnotification.common.NotificationBean
import com.oclocksoftware.smarttvnotification.common.NotificationType
import com.oclocksoftware.smarttvnotification.nsd.NsdHelper
import com.oclocksoftware.smarttvnotification.nsd.NsdHelper.Companion.TAG
import com.oclocksoftware.smarttvnotification.nsd.SocketConnection
import com.oclocksoftware.smarttvnotification.nsd.SocketInterface
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.lang.reflect.Type


/**
 * Created by babin on 12/21/2017.
 */

class ConnectFragment : BaseFragment(),ConnectContract.View {

    @Inject
    lateinit var mySharedPreferences: MySharedPreferences
    @Inject
    lateinit var presenter: ConnectContract.Presenter<ConnectContract.View>
    lateinit var binding: FragmentConnectBinding
    lateinit var mUpdateHandler: Handler
    lateinit var mConnection: SocketConnection
    lateinit var mSocketInterface: SocketInterface
    lateinit var mNotificationBroadcastReceiver: ConnectFragment.NotificationBroadcastReceiver
    lateinit var mCallBroadcastReceiver: ConnectFragment.CallBroadcastReceiver
    @Inject
    lateinit var mNsdHelper:NsdHelper


    companion object {

        fun newInstance(): ConnectFragment {
            return ConnectFragment()
        }
    }



    override fun initViews(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_connect, container, false)
        initDagger()
        initializeSockectInterface()
        presenter.onAttachMvpView(this)
        nextListener(binding.digit1EditText)
        nextListener(binding.digit2EditText)
        clearListener(binding.digit2EditText)
        nextListener(binding.digit3EditText)
        clearListener(binding.digit3EditText)
        clearListener(binding.digit4EditText)
        presenter.initializeNsd()
        presenter.enableNotificationListener()
        mUpdateHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                val message = msg.data.getString("msg")

                Log.d(TAG, "message:::" + message)
            }
        }

        if(checkServiceRunning()){
            Log.d("::::","::::::::::service is running")
        }else{
            Log.d("::::","::::::::::service is not running")
            val serviceIntent : Intent = Intent()
            serviceIntent.setAction("com.oclocksoftware.smarttvnotification.service.AppNotificationListenerService");
            activity.startService(serviceIntent)
        }

        mConnection = SocketConnection(mUpdateHandler, mSocketInterface)

        presenter.registerDevicetoNetwork(mConnection);
        binding.registerButton.setOnClickListener(View.OnClickListener {
            verifyCode()
        })

        binding.scanButton.setOnClickListener(View.OnClickListener {
            presenter.scanQRCode()
        })

        mNotificationBroadcastReceiver = NotificationBroadcastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.oclocksoftware.smarttvnotification")
        activity.registerReceiver(mNotificationBroadcastReceiver, intentFilter)
        mCallBroadcastReceiver = CallBroadcastReceiver()
        val filter = IntentFilter()
        filter.addAction("android.intent.action.PHONE_STATE")
        activity.registerReceiver(mCallBroadcastReceiver, filter)

        return binding.root
    }
    fun initDagger(){
        DaggerActivityComponent.builder()
                .appComponent(SmartApplication.component)
                .activityModule(ActivityModule(activity))
                .activityPrefrenceModule(ActivityPrefrenceModule(activity))
                .build().inject(this)

    }

    override fun dipslayDeviceList() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    fun nextListener(editText : EditText){
        editText.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int,after: Int) {

            }

            override fun afterTextChanged(s: Editable) {
                if (s.length == 1) {
                    val nextField = editText.focusSearch(View.FOCUS_RIGHT) as EditText
                    nextField.requestFocus()
                }
            }
        })
    }
    fun clearListener(editText : EditText){
        editText.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                if (event.getAction() !== KeyEvent.ACTION_DOWN)
                    return false
                if (keyCode == KeyEvent.KEYCODE_DEL) {
                    //this is for backspace
                    if (editText.text.length === 0) {
                        val nextField = editText.focusSearch(View.FOCUS_LEFT) as EditText
                        nextField.requestFocus()
                    }
                }
                return false
            }
        })
    }
    fun verifyCode(){
        if(binding.digit1EditText.text.length==0||binding.digit2EditText.text.length==0||binding.digit3EditText.text.length==0||binding.digit4EditText.text.length==0){
            Toast.makeText(activity,"Invalid Code..",Toast.LENGTH_SHORT).show()
        }else{
            val sb = StringBuilder()

            presenter.verifyRegisterCode(mConnection,sb.append(binding.digit1EditText.text).append(binding.digit2EditText.text).append(binding.digit3EditText.text).append(binding.digit4EditText.text).toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(activity, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                val scannedContent=result.contents;
                presenter.connectDevice(mConnection,scannedContent)
                Log.d("::::", "scanned result:::" + scannedContent)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun initializeSockectInterface(){
        mSocketInterface=object : SocketInterface {
            override fun onSocketConnected() {
                var nb:NotificationBean = NotificationBean()
                nb.content="Device Connected Sucessfully."
                nb.secretKey=BuildConfig.SECRET_KEY
                nb.notificationType=""+NotificationType.CONNECT
                Log.d(":::::Socket:::",":::Connected")
                val gson : Gson = Gson()
                val turn = object : TypeToken<NotificationBean>() {}.type
                val json = gson.toJson(nb, turn);
                System.out.println(json);
                pushMessage(json)

            }

            override fun onSocketDisConnected() {
                Log.d(":::::Socket:::",":::Disconnected")
                var nb:NotificationBean =NotificationBean()
                nb.content="Device DisConnected."
                nb.secretKey=BuildConfig.SECRET_KEY
                nb.notificationType=""+NotificationType.DISCONNECT
                Log.d(":::::Socket:::",":::Connected")
                val gson : Gson = Gson()
                val turn = object : TypeToken<NotificationBean>() {}.type
                val json = gson.toJson(nb, turn);
                System.out.println(json);
                pushMessage(json)
                if(mConnection!=null){
                    mConnection.tearDown()
                    mConnection = SocketConnection(mUpdateHandler, mSocketInterface)
                }
            }

            override fun onSocketConnectionFailed() {
                Log.d(":::::Socket:::",":::Connection Failed")
                Log.d(":::::Socket:::",":::Disconnected")
                var nb:NotificationBean =NotificationBean()
                nb.content="Device DisConnected."
                nb.secretKey=BuildConfig.SECRET_KEY
                nb.notificationType=""+NotificationType.DISCONNECT
                Log.d(":::::Socket:::",":::Connected")
                val gson : Gson = Gson()
                val turn = object : TypeToken<NotificationBean>() {}.type
                val json = gson.toJson(nb, turn);
                System.out.println(json);
                pushMessage(json)
                if(mConnection!=null){
                    mConnection.tearDown()
                    mConnection = SocketConnection(mUpdateHandler, mSocketInterface)
                }

            }

        }
    }
    inner class NotificationBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val receivedNotificationCode = intent.getIntExtra("Notification Code", -1)
            val receivedNotification = intent.getParcelableExtra<StatusBarNotification>("Notification")
            Log.d(":::::", "received::::" + receivedNotification.packageName)
            presenter.parsePushNotification(receivedNotification)

        }
    }

    inner class CallBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
        val telephony: TelephonyManager = activity.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val customPhoneListener = MyPhoneStateListener()

        telephony.listen(customPhoneListener, PhoneStateListener.LISTEN_CALL_STATE)

        val bundle:Bundle  = intent.getExtras()
        val phone_number = bundle.getString("incoming_number");
        System.out.println("Phone Number : " + phone_number);
        }
    }

    override fun onPause() {

        super.onPause()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {


        Log.d("Fragment:::::::",":::::Destroyed")
        super.onDestroy()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("Fragment:::::::",":::::View Destroyed")

    }

    override fun onDetach() {
        super.onDetach()
        Log.d("Fragment:::::::",":::::Detached")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Fragment:::::::",":::::stop")
    }

    override fun pushMessage(json: String) {
        if(mConnection.isSocketConnected())
            mConnection.sendMessage(json)
    }

    inner class MyPhoneStateListener : PhoneStateListener() {

        override fun onCallStateChanged(state: Int, incomingNumber: String?) {
            println("Icoming Number inside onCallStateChange : " + incomingNumber)
            presenter.parseCallNotification(state,incomingNumber)
        }

    }
    fun disconnectDevice(){
        try {
            activity.unregisterReceiver(mNotificationBroadcastReceiver)
            activity.unregisterReceiver(mCallBroadcastReceiver)
            mNsdHelper.tearDown()
            if(mConnection!=null){
                if(mConnection.isSocketConnected()){
                    var nb:NotificationBean =NotificationBean()
                    nb.content="Device DisConnected."
                    nb.secretKey=BuildConfig.SECRET_KEY
                    nb.notificationType=""+NotificationType.DISCONNECT
                    val gson : Gson = Gson()
                    val turn = object : TypeToken<NotificationBean>() {}.type
                    val json = gson.toJson(nb, turn);
                    System.out.println(json);
                    pushMessage(json)
                }
                mConnection.tearDown()

            }
        } catch (e: Exception) {
        }
    }

    public fun checkServiceRunning():Boolean {
        val manager :ActivityManager = activity.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service : ActivityManager.RunningServiceInfo in manager.getRunningServices(Integer.MAX_VALUE))
                {
                    if ("com.oclocksoftware.smarttvnotification.service.AppNotificationListenerService"
                            .equals(service.service.getClassName()))
                    {
                        return true;
                    }
                }
             return false;
    }

}
