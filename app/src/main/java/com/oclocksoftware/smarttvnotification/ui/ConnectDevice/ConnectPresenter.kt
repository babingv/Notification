package com.oclocksoftware.smarttvnotification.ui.ConnectDevice

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.ContactsContract
import android.provider.Settings
import android.service.notification.StatusBarNotification
import android.support.v4.content.ContextCompat.startActivity
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import com.oclocksoftware.smarttvnotification.base.BasePresenter
import com.oclocksoftware.smarttvnotification.utils.MySharedPreferences

import javax.inject.Inject
import com.google.zxing.integration.android.IntentIntegrator
import com.oclocksoftware.smarttvnotification.BuildConfig
import com.oclocksoftware.smarttvnotification.R
import com.oclocksoftware.smarttvnotification.common.NotificationBean
import com.oclocksoftware.smarttvnotification.common.NotificationType
import com.oclocksoftware.smarttvnotification.nsd.NsdHelper
import com.oclocksoftware.smarttvnotification.nsd.SocketConnection
import java.io.ByteArrayOutputStream
import java.util.*


/**
 * Created by babin on 12/21/2017.
 */

class ConnectPresenter<V : ConnectContract.View> @Inject
constructor(mySharedPreferences: MySharedPreferences) : BasePresenter<V>(mySharedPreferences), ConnectContract.Presenter<V> {

    @Inject
    lateinit var mActivity: Activity
    @Inject
    lateinit var nsdHelper : NsdHelper
    lateinit internal var mDiscoveryListener: NsdManager.DiscoveryListener
    protected var mServiceName=""
    protected var scannedPin=""
    protected var scannedMac=""
    private val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
    private val ACTION_NOTIFICATION_LISTENER_SETTINGS = "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"
    override fun scanQRCode() {
        IntentIntegrator(mActivity).initiateScan()
    }
    override fun verifyRegisterCode(mConnection: SocketConnection,s: String) {

        initializeDiscoveryListener(mConnection,s,"")

        nsdHelper.discoverServices()

    }


    override fun connectDevice(mConnection: SocketConnection, scannedContent: String) {
        if(scannedContent.contains(mActivity.resources.getString(R.string.service_name))){
            val array = scannedContent.split("_".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            if (array != null && array.size > 0) {
                for (i in array.indices) {
                    if (i == 0)
                        Log.d("::::", "Service:::: " + array[0])
                    else if (i == 1){
                        Log.d("::::", "PIN:::: " + array[1])
                        scannedPin=array[1]
                    }

                    else if (i == 2){
                        Log.d("::::", "MAC:::: " + array[2])
                        scannedMac=array[2]
                    }

                }
            }
        }

        initializeDiscoveryListener(mConnection,scannedPin,scannedMac)

        nsdHelper.discoverServices()
    }
    override fun initializeNsd() {
        nsdHelper.initializeNsd();

    }
    override fun registerDevicetoNetwork(mConnection: SocketConnection) {
        mServiceName=mActivity.resources.getString(R.string.service_name)
        val random = Random()
        val id = String.format("%04d", random.nextInt(10000))
        mServiceName = mServiceName + "_" + id
        val wifiManager = mActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wInfo = wifiManager.connectionInfo
        val macAddress = wInfo.macAddress
        mServiceName = mServiceName + "_" + macAddress
        nsdHelper.registerService(mConnection.localPort,mServiceName)
    }

    fun initializeDiscoveryListener(mConnection : SocketConnection,scannedPin: String, scannedMac: String) {

        mDiscoveryListener = object : NsdManager.DiscoveryListener {

            override fun onDiscoveryStarted(regType: String) {
                Log.d(NsdHelper.TAG, "Service discovery started")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Log.d(NsdHelper.TAG, "Service discovery success" + service)
                if (service.serviceType != NsdHelper.SERVICE_TYPE) {
                    Log.d(NsdHelper.TAG, "Unknown Service Type: " + service.serviceType)
                } else if (service.serviceName == mServiceName) {
                    Log.d(NsdHelper.TAG, "Same machine: " + mServiceName)
                } else if (service.serviceName.contains(mActivity.resources.getString(R.string.service_name))) {
                    //mNsdManager.resolveService(service, mResolveListener)
                    val array = service.getServiceName().split("_".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
                    if (array != null && array.size > 0) {
                        for (i in array.indices) {
                            if (i == 0)
                                Log.d("::::", "Service:::: " + array[0])
                            else if (i == 1){
                                Log.d("::::", "PIN:::: " + array[1])
                                Log.d("::::", "scannedPin:::: " +scannedPin)
                                if(scannedPin.equals(array[1],true)){
                                    Log.d("::::", "PIN Matched:::: " )
                                    nsdHelper.mNsdManager.resolveService(service, object : NsdManager.ResolveListener {
                                        override fun onResolveFailed(nsdServiceInfo: NsdServiceInfo, i: Int) {
                                            Log.d("::::::", "resolved:::fail")
                                        }

                                        override fun onServiceResolved(nsdServiceInfo: NsdServiceInfo) {
                                            Log.d(":::::", "resolved:::sucess")
                                            if(!mConnection.isSocketConnected())
                                                mConnection.connectToServer(nsdServiceInfo.host,nsdServiceInfo.port)
                                            else{
                                                mConnection.tearDown()
                                                mConnection.connectToServer(nsdServiceInfo.host,nsdServiceInfo.port)
                                            }

                                        }
                                    })
                                }
                            }

                            else if (i == 2)
                                Log.d("::::", "MAC:::: " + array[2])
                        }
                    }
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.e(NsdHelper.TAG, "service lost" + service)
                //nsdHelper.discoveryServiceLost(this)
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.i(NsdHelper.TAG, "Discovery stopped: " + serviceType)
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(NsdHelper.TAG, "Discovery failed: Error code:" + errorCode)
               // nsdHelper.stopServiceDiscovery(this)
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(NsdHelper.TAG, "Discovery failed: Error code:" + errorCode)
                //nsdHelper.stopServiceDiscovery(this)
            }
        }
        nsdHelper.initializeDiscoveryListener(mDiscoveryListener)
    }

    override fun enableNotificationListener() {
        if (!isNotificationServiceEnabled()) {
            val alertDialogBuilder = AlertDialog.Builder(mActivity)
            alertDialogBuilder.setTitle(R.string.app_name)
            alertDialogBuilder.setMessage(R.string.notification_listener_service_explanation)
            alertDialogBuilder.setPositiveButton(R.string.yes,
                    DialogInterface.OnClickListener { dialog, id -> startActivity(mActivity,Intent(ACTION_NOTIFICATION_LISTENER_SETTINGS),null) })
            alertDialogBuilder.setNegativeButton(R.string.no,
                    DialogInterface.OnClickListener { dialog, id ->
                        mActivity.finish()
                    })
            alertDialogBuilder.create().show()
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = mActivity.getPackageName()
        val flat = Settings.Secure.getString(mActivity.getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS)
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    override fun parsePushNotification(receivedNotification: StatusBarNotification?) {
        val packagename = receivedNotification!!.packageName
        val notification = receivedNotification.notification.toString()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val extras = receivedNotification.notification.extras

            var title = "";
            if(extras.containsKey("android.title")){
                title=extras.getString("android.title")
            }
            var text = "";
            if(extras.containsKey("android.text")){
                text = extras.getCharSequence("android.text")!!.toString()
            }


            val pack = receivedNotification!!.packageName
            val ticker = receivedNotification.notification.tickerText
            val ongoing = receivedNotification.isOngoing
            val clearable = receivedNotification.isClearable

            val nb = NotificationBean()
            try {
                val icon = mActivity.getPackageManager().getApplicationIcon(packagename)

                val photo = (icon as BitmapDrawable).bitmap
                val bao = ByteArrayOutputStream()
                photo.compress(Bitmap.CompressFormat.JPEG, 90, bao)
                val ba = bao.toByteArray()
                val ba1 = Base64.encodeToString(ba, Base64.DEFAULT)

                nb.secretKey= BuildConfig.SECRET_KEY
                nb.notificationType=""+ NotificationType.PUSH
                nb.title=title
                nb.content=text
                nb.packagename=packagename
                if (ticker!=null)
                    nb.ticker=ticker.toString()
                nb.isClearable=clearable
                nb.isOngoing=ongoing
                nb.image=ba1

                val gson : Gson = Gson()
                val turn = object : TypeToken<NotificationBean>() {}.type
                val json = gson.toJson(nb, turn);
                System.out.println(json);
                mvpView.pushMessage(json)

            } catch (e: Exception) {
                e.printStackTrace()
            }


            Log.d("title:::::", "received::::" + title!!)
            Log.d("text:::::", "received::::" + text)
            Log.d("pack:::::", "received::::" + pack)
            Log.d("ticker:::::", "received::::" + ticker)
            Log.d("ongoing:::::", "received::::" + ongoing)
            Log.d("clearable:::::", "received::::" + clearable)
        }
    }

    override fun parseCallNotification(state: Int, incomingNumber: String?) {
        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                println("PHONE RINGING.........TAKE IT.........")
                getContactDetails(incomingNumber.toString(),state)
            }
            else ->{
                println("CALL_STATE_OFFHOOK...........")
                val nb:NotificationBean = NotificationBean()
                nb.notificationType=""+NotificationType.CLOSE
                nb.secretKey=BuildConfig.SECRET_KEY
                nb.callStatus=state
                val gson : Gson = Gson()
                val turn = object : TypeToken<NotificationBean>() {}.type
                val json = gson.toJson(nb, turn);
                System.out.println(json);
                mvpView.pushMessage(json)
            }

            /*TelephonyManager.CALL_STATE_OFFHOOK -> {

            }*/
        }


    }

    open fun getContactDetails(phoneNumber: String, state: Int): String{
        var contactID=""
        val nb:NotificationBean = NotificationBean()
        nb.notificationType=""+NotificationType.CALL
        nb.secretKey=BuildConfig.SECRET_KEY
        nb.callStatus=state
        val cr: ContentResolver = mActivity.getContentResolver();
        val uri : Uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID)
        val cursor: Cursor = cr.query(uri, projection, null, null, null);
        try {
            if (cursor != null) {
                if(cursor.moveToFirst()) {
                    val contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    nb.title=contactName+" calling...."
                    nb.content=phoneNumber
                    contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup._ID));
                    println("Name::::: " + contactName+":::contactImage"+contactID)
                }else{
                    nb.title=phoneNumber
                    nb.content=" calling...."
                    println("Name::::: notfound" )
                }

            }else{
                nb.title=phoneNumber
                nb.content=" calling...."
                println("Name::::: notfound" )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if(cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
            openPhoto(contactID,nb);
        }
        return contactID

    }

    public fun openPhoto(contactCode: String, nb: NotificationBean)  {
        if(!TextUtils.isEmpty(contactCode)){
            val contactId=contactCode.toLong()
            val contactUri: Uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            val photoUri : Uri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
            val cursor : Cursor = mActivity.getContentResolver().query(photoUri, arrayOf(ContactsContract.Contacts.Photo.PHOTO), null, null, null);
            if (cursor == null) {
                println("photo::::: notfound" )
            }else{
                try {
                    println("photo::::: found"+cursor.count )
                    if (cursor.moveToFirst()) {
                        val data : ByteArray = cursor.getBlob(0);
                        if (data != null) {
                            val ba1 = Base64.encodeToString(data, Base64.DEFAULT)
                            nb.image=ba1
                        }
                    }
                } finally {
                    cursor.close();
                }
            }

        }
        val gson : Gson = Gson()
        val turn = object : TypeToken<NotificationBean>() {}.type
        val json = gson.toJson(nb, turn);
        System.out.println(json);
        mvpView.pushMessage(json)

    }

}
