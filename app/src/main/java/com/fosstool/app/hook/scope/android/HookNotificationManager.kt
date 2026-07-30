package com.fosstool.app.hook.scope.android

import android.app.Notification
import android.os.Build
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.hook.scope.wirelesssettings.WlanSla
import com.fosstool.app.utils.ModulePrefs
import dalvik.system.PathClassLoader
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

object HookNotificationManager : YukiBaseHooker() {

    private val HOTSPOT_CHANNEL_HINTS =
        arrayOf("softap", "hotspot", "tether", "wifi", "wlan")

    override fun onHook() {

        val wifiCl = oplusWifiClassLoader(appClassLoader)
        if (wifiCl == null) YLog.error("Hook Oplus Wifi Service is null!")
        WlanSla.wifiClassLoader = wifiCl

        val hotspotPowerConsumption =
            prefs(ModulePrefs).getBoolean("remove_hotspot_power_consumption_notification", false)
        if (!hotspotPowerConsumption) return

        val classNames = arrayOf(
            "com.oplus.server.wifi.hotspot.OplusSoftapStatistics",
            "com.oplus.wifi.hotspot.OplusSoftapStatistics",
        )
        var hooked = false
        for (clsName in classNames) {
            runCatching {
                val cls = if (wifiCl != null) {
                    Class.forName(clsName, false, wifiCl)
                } else {
                    Class.forName(clsName, false, appClassLoader)
                }
                cls.method { name = "startSoftapEnableTimer" }.ignored().hook { intercept() }
                hooked = true
            }
        }
        if (!hooked) {
            YLog.debug("Disable hotspot notify: OplusSoftapStatistics not found, fallback notify id=4")
            runCatching {
                val nm = Class.forName("android.app.NotificationManager", false, appClassLoader)
                for (m in nm.declaredMethods) {
                    if (m.name != "notify" || m.parameterTypes.size != 3) continue
                    if (m.parameterTypes[0] != String::class.java) continue
                    if (m.parameterTypes[1] != Int::class.javaPrimitiveType) continue
                    XposedBridge.hookMethod(m, object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if ((param.args[1] as? Int) != 4) return

                            if (!isHotspotNotification(
                                    param.args[0] as? String,
                                    param.args[2] as? Notification
                                )
                            ) return
                            param.result = null
                        }
                    })
                }
            }
        }
    }

    private fun isHotspotNotification(tag: String?, notification: Notification?): Boolean {
        val channel = runCatching { notification?.channelId }.getOrNull()
        val haystack = (tag.orEmpty() + "|" + channel.orEmpty()).lowercase()
        if (haystack.isBlank() || haystack == "|") return false
        return HOTSPOT_CHANNEL_HINTS.any { haystack.contains(it) }
    }

    fun oplusWifiClassLoader(base: ClassLoader?): ClassLoader? {
        val apex = "/apex/com.android.wifi/javalib/service-wifi.jar"
        val oplus = "/system_ext/framework/oplus-wifi-service.jar"
        runCatching {
            val factory = Class.forName(
                "com.android.internal.os.SystemServerClassLoaderFactory",
                false,
                base
            )
            val getOrCreate = factory.getDeclaredMethod(
                "getOrCreateClassLoader",
                String::class.java,
                ClassLoader::class.java,
                Boolean::class.javaPrimitiveType
            )
            val parent = getOrCreate.invoke(null, apex, null, false) as ClassLoader
            return getOrCreate.invoke(null, oplus, parent, false) as ClassLoader
        }
        runCatching {
            val factory = Class.forName(
                "com.android.internal.os.ClassLoaderFactory",
                false,
                base
            )
            val create = factory.methods.firstOrNull {
                it.name == "createClassLoader" && it.parameterTypes.size >= 7
            } ?: return@runCatching null
            val sdk = Build.VERSION.SDK_INT
            val parent = create.invoke(
                null, apex, null, null, null, sdk, true, null
            ) as ClassLoader
            return create.invoke(
                null, oplus, null, null, parent, sdk, true, null
            ) as ClassLoader
        }
        return runCatching {
            PathClassLoader(oplus, PathClassLoader(apex, base))
        }.getOrNull()
    }
}
