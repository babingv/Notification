package com.oclocksoftware.smarttvnotification.common

import android.telephony.TelephonyManager

/**
 * Created by babin on 1/3/2018.
 */

class NotificationBean {
    var title: String? = null
    var notificationType: String? = null
    var secretKey: String? = null
    var content: String? = null
    var packagename: String? = null
    var ticker: String? = null
    var image: String? = null
    var isOngoing: Boolean = false
    var isClearable: Boolean = false
    var callStatus: Int= TelephonyManager.CALL_STATE_OFFHOOK
}
