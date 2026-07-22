package com.fosstool.app.ui.service

import android.content.Intent
import com.fosstool.app.IGlobalDCController
import com.fosstool.app.utils.replaceSpace
import com.topjohnwu.superuser.ipc.RootService
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class GlobalDCControllerService : RootService() {
    companion object {
        private const val oppoFileDir = "/sys/kernel/oppo_display/dimlayer_hbm"
        private const val oplusFileDir = "/sys/kernel/oplus_display/dimlayer_hbm"
        private val oppoFile = File(oppoFileDir)
        private val oplusFile = File(oplusFileDir)
    }

    override fun onBind(intent: Intent) = object : IGlobalDCController.Stub() {

        override fun checkGlobalDCMode(): Boolean {
            return try {
                oppoFile.exists() || oplusFile.exists()
            } catch (_: Throwable) {
                false
            }
        }

        override fun getGlobalDCMode(): Boolean {
            return try {
                val file = (if (oppoFile.exists()) oppoFile
                else if (oplusFile.exists()) oplusFile
                else null) ?: return false
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

        override fun setGlobalDCMode(status: Boolean) {
            try {
                val file = (if (oppoFile.exists()) oppoFile
                else if (oplusFile.exists()) oplusFile
                else null) ?: return
                file.writeText(if (status) "1" else "0")
            } catch (_: Throwable) {

            }
        }
    }
}
