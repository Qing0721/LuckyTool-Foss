package com.fosstool.app.ui.service

import android.content.Intent
import com.fosstool.app.IAdbDebugController
import com.fosstool.app.hook.utils.SystemPropertiesUtils
import com.fosstool.app.utils.LogUtils
import com.fosstool.app.utils.ShellUtils
import com.topjohnwu.superuser.ipc.RootService
import java.net.Inet4Address
import java.net.NetworkInterface

class AdbDebugControllerService : RootService() {
    val tag = "AdbDebugControllerService"

    override fun onBind(intent: Intent) = object : IAdbDebugController.Stub() {
        override fun getAdbPort(): Int {
            val port = SystemPropertiesUtils(null).get("service.adb.tcp.port")
            return if (port.isNullOrBlank()) 0 else port.toInt()
        }

        override fun setAdbPort(port: Int) {
            SystemPropertiesUtils(null).set(
                "service.adb.tcp.port",
                (if (port == 0) "" else port).toString()
            )
        }

        override fun getWifiIP(): String {
            return getIpAddress("wlan0") ?: "IP"
        }

        override fun restartAdb() {
            val commands = arrayOf("stop adbd", "killall -9 adbd 2>/dev/null", "start adbd")
            ShellUtils.execCommand(commands, true)
        }

        fun getIpAddress(networkName: String): String? {
            try {
                val networkInterfaces = NetworkInterface.getNetworkInterfaces()
                while (networkInterfaces.hasMoreElements()) {
                    val element = networkInterfaces.nextElement()
                    if (!networkName.equals(element.name, true)) continue
                    val addresses = element.inetAddresses
                    while (addresses.hasMoreElements()) {
                        val address = addresses.nextElement()
                        if (address is Inet4Address && !address.isLoopbackAddress()) {
                            return address.getHostAddress()
                        }
                    }
                }
            } catch (e: Exception) {
                LogUtils.e(tag, "getIpAddress", "$e")
                return null
            }
            return null
        }
    }
}
