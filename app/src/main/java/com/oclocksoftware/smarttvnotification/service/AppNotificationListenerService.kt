package com.oclocksoftware.smarttvnotification.service

import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.content.Context.ACTIVITY_SERVICE
import android.app.ActivityManager
import android.content.Context


/**
 * Created by babin on 1/3/2018.
 */

class AppNotificationListenerService : NotificationListenerService() {

    /*
        These are the package names of the apps. for which we want to
        listen the notifications
     */
    private object ApplicationPackageNames {
        val FACEBOOK_PACK_NAME = "com.facebook.katana"
        val FACEBOOK_MESSENGER_PACK_NAME = "com.facebook.orca"
        val WHATSAPP_PACK_NAME = "com.whatsapp"
        val INSTAGRAM_PACK_NAME = "com.instagram.android"
        val FAITHSOCIAL_PACK_NAME = "com.oclockapps.faithsocial"
    }

    /*
        These are the return codes we use in the method which intercepts
        the notifications, to decide whether we should do something or not
     */
    object InterceptedNotificationCode {
        val FACEBOOK_CODE = 1
        val WHATSAPP_CODE = 2
        val INSTAGRAM_CODE = 3
        val FAITHSOCIAL_CODE = 5
        val OTHER_NOTIFICATIONS_CODE = 4 // We ignore all notification with code == 4
    }

    override fun onBind(intent: Intent): IBinder? {
        return super.onBind(intent)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val notificationCode = matchNotificationCode(sbn)
        Log.d("sbn::::",":::"+sbn.notification)
        Log.d("sbn::::",":::"+sbn.notification)
        Log.d("sbn::::",":::"+sbn)
        Log.d("sbn::::",":::"+sbn.notification.category)
        Log.d("sbn::::",":::"+sbn.notification.tickerText)

            val intent = Intent("com.oclocksoftware.smarttvnotification")
            intent.putExtra("Notification Code", notificationCode)
            intent.putExtra("Notification", sbn)
            sendBroadcast(intent)

    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val notificationCode = matchNotificationCode(sbn)
        Log.d("sbn::::",":::"+sbn.notification)
        Log.d("sbn::::",":::"+sbn)
        Log.d("sbn::::",":::"+sbn.notification.category)
        Log.d("sbn::::",":::"+sbn.notification.tickerText)

        if (notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {

            val activeNotifications = this.activeNotifications

            if (activeNotifications != null && activeNotifications.size > 0) {
                for (i in activeNotifications.indices) {
                    if (notificationCode == matchNotificationCode(activeNotifications[i])) {
                        val intent = Intent("com.oclocksoftware.smarttvnotification")
                        intent.putExtra("Notification Code", notificationCode)
                        sendBroadcast(intent)
                        break
                    }
                }
            }
        }
    }

    private fun matchNotificationCode(sbn: StatusBarNotification): Int {
        val packageName = sbn.packageName

        return if (packageName == ApplicationPackageNames.FACEBOOK_PACK_NAME || packageName == ApplicationPackageNames.FACEBOOK_MESSENGER_PACK_NAME) {
            InterceptedNotificationCode.FACEBOOK_CODE
        } else if (packageName == ApplicationPackageNames.INSTAGRAM_PACK_NAME) {
            InterceptedNotificationCode.INSTAGRAM_CODE
        } else if (packageName == ApplicationPackageNames.WHATSAPP_PACK_NAME) {
            InterceptedNotificationCode.WHATSAPP_CODE
        } else if (packageName == ApplicationPackageNames.FAITHSOCIAL_PACK_NAME) {
            InterceptedNotificationCode.FAITHSOCIAL_CODE
        } else {
            InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE
        }
    }


}

