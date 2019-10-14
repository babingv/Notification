package com.oclocksoftware.smarttvnotification.ui.QRGenerator

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.wifi.WifiManager
import android.util.Log
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.oclocksoftware.smarttvnotification.R
import com.oclocksoftware.smarttvnotification.base.BasePresenter
import com.oclocksoftware.smarttvnotification.nsd.NsdHelper
import com.oclocksoftware.smarttvnotification.nsd.SocketConnection
import com.oclocksoftware.smarttvnotification.utils.MySharedPreferences
import java.util.*
import javax.inject.Inject

/**
 * Created by babin on 12/22/2017.
 */

class QRPresenter <V : QRContract.View >@Inject
constructor(mySharedPreferences: MySharedPreferences) : BasePresenter<V>(mySharedPreferences), QRContract.Presenter<V> {

    @Inject
    lateinit var mActivity:Activity
    @Inject
    lateinit var nsdHelper : NsdHelper
    protected var mServiceName=""

    override fun generateQRCode(smallestDimension: Int, mServiceName: String) {

        val charset = "UTF-8" // or "ISO-8859-1"
        val hintMap = HashMap<EncodeHintType, ErrorCorrectionLevel>()
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L)
        createQRCode(mServiceName,charset, hintMap, smallestDimension, smallestDimension)
    }
    override fun registerDevicetoNSD(mConnection: SocketConnection, mServiceName: String) {
        nsdHelper.initializeNsd()
        nsdHelper.registerService(mConnection.localPort,mServiceName)

    }


    fun createQRCode(mServiceName: String,charset: String, hintMap: HashMap<EncodeHintType, ErrorCorrectionLevel>, qrCodeheight: Int, qrCodewidth: Int) {
        try {
            //generating qr code in bitmatrix type
            val matrix = MultiFormatWriter().encode(String(mServiceName.toByteArray(charset(charset)), charset(charset)), BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap)
            //converting bitmatrix to bitmap
            val width = matrix.width
            val height = matrix.height
            val pixels = IntArray(width * height)
            // All are 0, or black, by default
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (matrix.get(x, y)) Color.BLACK else Color.WHITE
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            //setting bitmap to image view
            mvpView.dipslayQRCode(bitmap)
        } catch (er: Exception) {
            Log.e("QrGenerate", er.message)
        }

    }






}
