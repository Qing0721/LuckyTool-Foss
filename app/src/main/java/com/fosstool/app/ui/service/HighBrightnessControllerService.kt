package com.fosstool.app.ui.service

import android.content.Intent
import com.fosstool.app.IHighBrightnessController
import com.fosstool.app.utils.replaceSpace
import com.topjohnwu.superuser.ipc.RootService
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class HighBrightnessControllerService : RootService() {
    companion object {
        private const val fileDir = "/sys/kernel/oplus_display/hbm"
        val file = File(fileDir)
    }

    override fun onBind(intent: Intent) = object : IHighBrightnessController.Stub() {
        override fun checkHighBrightnessMode(): Boolean {
            return try {
                file.exists()
            } catch (_: Throwable) {
                false
            }
        }

        override fun getHighBrightnessMode(): Boolean {
            return try {
                when (BufferedReader(FileReader(file)).readLine()?.replaceSpace?.substring(0, 1)
                    ?.toIntOrNull()) {
                    0 -> false
                    1 -> true
                    else -> false
                }
            } catch (_: Throwable) {
                false
            }
        }

        override fun setHighBrightnessMode(status: Boolean) {
            try {
                if (file.exists()) file.writeText(if (status) "1" else "0")
            } catch (_: Throwable) {

            }
        }
    }
}
