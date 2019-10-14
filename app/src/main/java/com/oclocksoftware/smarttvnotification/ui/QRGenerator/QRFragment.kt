package com.oclocksoftware.smarttvnotification.ui.QRGenerator

import android.content.Context
import android.databinding.DataBindingUtil
import com.oclocksoftware.smarttvnotification.R
import com.oclocksoftware.smarttvnotification.SmartApplication

import com.oclocksoftware.smarttvnotification.base.BaseFragment
import com.oclocksoftware.smarttvnotification.databinding.FragmentQrgeneratorBinding
import com.oclocksoftware.smarttvnotification.di.module.ActivityModule
import com.oclocksoftware.smarttvnotification.utils.MySharedPreferences
import javax.inject.Inject
import android.content.Context.WINDOW_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import com.oclocksoftware.smarttvnotification.di.module.ActivityPrefrenceModule
import android.net.wifi.WifiManager
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.oclocksoftware.smarttvnotification.di.component.DaggerActivityComponent
import com.oclocksoftware.smarttvnotification.nsd.NsdHelper
import com.oclocksoftware.smarttvnotification.nsd.SocketConnection
import com.oclocksoftware.smarttvnotification.nsd.SocketInterface
import java.util.*
import android.os.*
import android.util.Base64
import com.google.gson.Gson
import com.oclocksoftware.smarttvnotification.BuildConfig
import com.oclocksoftware.smarttvnotification.common.NotificationBean
import com.oclocksoftware.smarttvnotification.common.NotificationType
import android.content.Intent
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.text.TextUtils
import android.widget.LinearLayout
import com.oclocksoftware.smarttvnotification.utils.ResourcesHelper


/**
 * Created by babin on 12/22/2017.
 */

class QRFragment : BaseFragment(),QRContract.View {

    @Inject
    lateinit var mySharedPreferences: MySharedPreferences
    @Inject
    lateinit var presenter: QRContract.Presenter<QRContract.View>
    internal lateinit var binding: FragmentQrgeneratorBinding
    lateinit var bmp: Bitmap
    lateinit var mUpdateHandler: Handler
    lateinit var mServiceName: String
    lateinit var mConnection: SocketConnection
    lateinit var mSocketInterface: SocketInterface
    @Inject
    lateinit var mNsdHelper:NsdHelper
    lateinit var toast:Toast


    companion object {

        fun newInstance(): QRFragment {
            return QRFragment()
        }
    }

    override fun initViews(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_qrgenerator, container, false)
        initializeSockectInterface()
        val gson = Gson()
        mUpdateHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                val message = msg.data.getString("msg")

                Log.d(NsdHelper.TAG, "message:::" + message)
                if(!message.equals("initialized",true)){
                    val nb=gson.fromJson(message,NotificationBean::class.java)
                    Log.d(NsdHelper.TAG, "message:::" +nb.content)
                    try {
                        if(toast!=null)
                            toast.cancel()
                    } catch (e: Exception) {
                    }
                    if(nb.secretKey.equals(BuildConfig.SECRET_KEY,true)){
                        if(nb.notificationType.equals(""+NotificationType.PUSH,true)){
                            displayAppConnectNotification(nb)
                        }else if(nb.notificationType.equals(""+NotificationType.CALL,true)){
                            displayAppConnectNotification(nb)
                        }else if(nb.notificationType.equals(""+NotificationType.CLOSE,true)){
                            if(toast!=null)
                                toast.cancel()
                        }else if(nb.notificationType.equals(""+NotificationType.CONNECT,true)){
                            displayAppConnectNotification(nb)
                            val startMain = Intent(Intent.ACTION_MAIN)
                            startMain.addCategory(Intent.CATEGORY_HOME)
                            startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(startMain)
                        }else if(nb.notificationType.equals(""+NotificationType.DISCONNECT,true)){
                            activity.finish()
                        }

                    }


                }

            }
        }
        mConnection = SocketConnection(mUpdateHandler,mSocketInterface)
        initDagger()
        generateServiceName()
        presenter.onAttachMvpView(this)
        val sysmanager = activity.getSystemService(WINDOW_SERVICE) as WindowManager
        val display = sysmanager.defaultDisplay
        val point = Point()
        display.getSize(point)
        var width = point.x
        var height = point.y
        width=width/2;
        height=height/2

        val smallestDimension = if (width < height) width else height
        presenter.generateQRCode(smallestDimension,mServiceName);

        presenter.registerDevicetoNSD(mConnection,mServiceName);

        return binding.root
    }

    fun initDagger(){
        DaggerActivityComponent.builder()
                .appComponent(SmartApplication.component)
                .activityModule(ActivityModule(activity))
                .activityPrefrenceModule(ActivityPrefrenceModule(activity))
                .build().inject(this)
    }

    override fun dipslayQRCode(bitmap: Bitmap) {
        binding.imageViewQr.setImageBitmap(bitmap)
    }

    override fun dipslayRandomNumber(randomNumber: String) {
        binding.randomText.setText(randomNumber)
    }

    fun generateServiceName(){
        val random = Random()
        val id = String.format("%04d", random.nextInt(10000))
        dipslayRandomNumber(id);
        mServiceName = activity.resources.getString(R.string.service_name) + "_" + id
        val wifiManager = activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wInfo = wifiManager.connectionInfo
        val macAddress = wInfo.macAddress
        mServiceName = mServiceName + "_" + macAddress
    }

    override fun displayNotification(image: Bitmap?, title: String, content: String) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.notification, activity.findViewById<ViewGroup>(R.id.toast_layout))

        layout.findViewById<TextView>(R.id.title).text = title
        layout.findViewById<TextView>(R.id.text).text = content
        layout.findViewById<ImageView>(R.id.icon).setImageBitmap(image)
        toast = Toast(activity)
        toast.setGravity(Gravity.BOTTOM or Gravity.LEFT, 16, 16)

        //toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }
    fun displayAppConnectNotification(notification: NotificationBean) {

        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.notification, activity.findViewById<ViewGroup>(R.id.toast_layout))
        if(notification.title.isNullOrBlank()){
            layout.findViewById<TextView>(R.id.title).visibility=View.GONE
        }else{
            layout.findViewById<TextView>(R.id.title).text = notification.title
        }
        if(notification.content.isNullOrBlank()){
            layout.findViewById<TextView>(R.id.text).visibility=View.GONE
        }else{
            layout.findViewById<TextView>(R.id.text).text = notification.content
        }

        if(!notification.image.isNullOrBlank()){
            val encodeByte = Base64.decode(notification.image, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
            val p: Palette = Palette.from(bitmap).generate()
           // val vibrant = p.vibrantSwatch
           // if(vibrant!=null)
            val color: Int=p.getMutedColor(ContextCompat.getColor(activity,R.color.QR_bg))
            layout.findViewById<LinearLayout>(R.id.toast_layout).setBackgroundColor(ResourcesHelper.getColorWithAplha(color,0.95f))
            layout.findViewById<ImageView>(R.id.icon).setImageBitmap(bitmap)

        }else{
            layout.findViewById<ImageView>(R.id.icon).visibility=View.GONE
        }

        toast = Toast(activity)
        toast.setGravity(Gravity.BOTTOM or Gravity.LEFT, 16, 16)

        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();


        /*val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.notification, activity.findViewById<ViewGroup>(R.id.toast_layout))

        layout.findViewById<TextView>(R.id.title).text = "dgfshgfhg"
        layout.findViewById<TextView>(R.id.text).text = "fghdgdhfh"
        //layout.findViewById<ImageView>(R.id.icon).setImageBitmap(image)
        val toast = Toast(activity)
        toast.setGravity(Gravity.BOTTOM or Gravity.LEFT, 16, 16)
        var timer : Int
        timer=5000000

        toast.setDuration(timer)
        toast.setView(layout);

        object : CountDownTimer(300000, 1000) {
            override fun onFinish() {
                toast.show()
            }

            override fun onTick(millisUntilFinished: Long) {
                toast.show()
            }
        }.start()*/

    }
    fun initializeSockectInterface(){
        mSocketInterface=object : SocketInterface{
            override fun onSocketConnected() {
                activity.runOnUiThread(Runnable {
                    displayNotification(null,"","Device Connected Sucessfully");
                })

            }

            override fun onSocketDisConnected() {
                displayNotification(null,"","Device Disconnected");
                activity.finish()
            }

            override fun onSocketConnectionFailed() {
                displayNotification(null,"","Unable to connect Device");
            }

        }
    }
    override fun onPause() {

        super.onPause()
    }

    override fun onResume() {
        super.onResume()

    }

    override fun onDestroy() {
        super.onDestroy()
        mNsdHelper.tearDown()
        if(mConnection!=null){
            mConnection.tearDown()
        }
    }

}
