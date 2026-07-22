package com.fosstool.app.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.fosstool.app.R

class PermissionUtils(val context: Activity) {

    fun checkPermissions() {
        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            context.startActivity(intent.setData(Uri.parse("package:${context.packageName}")))
            context.toast(context.getString(R.string.all_files_access_permission))
            return
        }

        XXPermissions.with(context).apply {
            val permission = context.getString(R.string.get_installed_apps_permission)
            permission(Permission.GET_INSTALLED_APPS)
            request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>, allGranted: Boolean) {

                }

                override fun onDenied(permissions: MutableList<String>, doNotAskAgain: Boolean) {
                    if (doNotAskAgain) {
                        toastDenied(permission)
                        XXPermissions.startPermissionActivity(context, permissions)
                        return
                    } else toastError(permission)
                }
            })
        }
    }

    private fun toastDenied(permission: String) {
        context.toast(context.getString(R.string.permission_denied_toast, permission))
    }

    private fun toastError(permission: String) {
        context.toast(context.getString(R.string.permission_error_toast, permission))
    }
}
