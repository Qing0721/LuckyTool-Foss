@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.fosstool.app.utils

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.fosstool.app.R

object NotifyUtils {
    private const val POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"

    const val DEFAULT_NOTICE_ID = "default"
    const val DEFAULT_NOTICE_NAME = "默认通知"
    const val DEFAULT_NOTICE_IMPORTANCE = NotificationManager.IMPORTANCE_DEFAULT

    fun getDemoNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, DEFAULT_NOTICE_ID)
            .setSmallIcon(R.drawable.ic_baseline_info_24)
            .setContentTitle("标题")
            .setContentText("内容")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
    }

    fun checkPermission(context: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun sendNotification(context: Context, notifyId: Int, notification: Notification) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notifyId, notification)
    }

    fun clearNotification(context: Context, notifyId: Int) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notifyId)
    }

    fun createChannel(context: Context, channel: NotificationChannel) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun deleteChannel(context: Context, channelId: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.deleteNotificationChannel(channelId)
    }

    fun requestPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkPermission(activity)) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(
                        activity,
                        POST_NOTIFICATIONS
                    )
                ) {
                    enableNotification(activity)
                } else {
                    ActivityCompat.requestPermissions(activity, arrayOf(POST_NOTIFICATIONS), 100)
                }
            }
        } else {
            val enabled = NotificationManagerCompat.from(activity).areNotificationsEnabled()
            if (!enabled) {
                enableNotification(activity)
            }
        }
    }

    private fun enableNotification(context: Context) {
        try {
            val intent = Intent()
            intent.action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            intent.putExtra(Settings.EXTRA_CHANNEL_ID, context.applicationInfo.uid)
            intent.putExtra("app_package", context.packageName)
            intent.putExtra("app_uid", context.applicationInfo.uid)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = Uri.fromParts("package", context.packageName, null)
            context.startActivity(intent)
        }
    }
}
