package com.fosstool.app.ui.service

import android.content.Intent
import com.fosstool.app.IGlobalFuncController
import com.fosstool.app.hook.utils.SystemPropertiesUtils
import com.fosstool.app.utils.formatSpace
import com.fosstool.app.utils.replaceSpace
import com.topjohnwu.superuser.ipc.RootService
import java.io.File

class GlobalFuncControllerService : RootService() {
    override fun onBind(intent: Intent) = object : IGlobalFuncController.Stub() {
        override fun getProp(key: String, def: String): String {
            return SystemPropertiesUtils(null).get(key, def) ?: def
        }

        override fun getFileText(dir: String): String {
            val file = File(dir)
            return if (file.exists() && file.isFile) return file.readText()
            else ""
        }

        override fun getOtaVersion(): String {
            return getProp("ro.build.version.ota", "null")
        }

        override fun getMarketName(): String {
            return getProp("ro.vendor.oplus.market.name", "null")

        }

        override fun getLcdInfo(): String {
            val text = getFileText("/proc/devinfo/lcd")
            return if (text.isBlank()) "null" else {
                val list = text.lines().toMutableList()
                list.forEachIndexed { index, s ->
                    val value = s.substringAfterLast(":").replace("\t", "").uppercase()
                    list[index] = value
                }
                "${list[0]} ${list[1]}"
            }
        }

        override fun getFlashInfo(): String {
            val text = getFileText("/sys/class/block/sda/device/inquiry")
            return if (text.isBlank()) "null" else formatSpace(text)
        }

        override fun getPcbInfo(): String {
            val gms = getProp("gsm.serial", "")
            val vendor = getProp("vendor.gsm.serial", "null")
            return (gms + vendor).replaceSpace

        }

        override fun getSnInfo(): String {
            return getProp("ro.serialno", "null").uppercase()
        }
    }
}
