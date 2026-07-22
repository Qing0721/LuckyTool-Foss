package com.fosstool.app.hook.scope.android

import android.os.Build
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.utils.ModulePrefs
import dalvik.system.PathClassLoader
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object HookNotificationManager : YukiBaseHooker() {
    override fun onHook() {
        val hotspotPowerConsumption =
            prefs(ModulePrefs).getBoolean("remove_hotspot_power_consumption_notification", false)
        if (!hotspotPowerConsumption) return

        val wifiCl = loadOplusWifiClassLoader()
        val classNames = arrayOf(
            "com.oplus.server.wifi.hotspot.OplusSoftapStatistics",
            "com.oplus.wifi.hotspot.OplusSoftapStatistics",
        )
        var hooked = false
        for (name in classNames) {
            runCatching {
                val cls = if (wifiCl != null) {
                    Class.forName(name, false, wifiCl)
                } else {
                    Class.forName(name, false, appClassLoader)
                }
                XposedBridge.hookAllMethods(
                    cls,
                    "startSoftapEnableTimer",
                    XC_MethodReplacement.DO_NOTHING
                )
                hooked = true
            }
        }
        if (!hooked) {
            YLog.debug("Disable hotspot notify: OplusSoftapStatistics not found, fallback notify id=4")
            runCatching {
                XposedHelpers.findAndHookMethod(
                    "android.app.NotificationManager",
                    appClassLoader,
                    "notify",
                    String::class.java,
                    Int::class.javaPrimitiveType,
                    android.app.Notification::class.java,
                    object : de.robv.android.xposed.XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            if ((param.args[1] as? Int) == 4) param.result = null
                        }
                    }
                )
            }
        }
    }

    private fun loadOplusWifiClassLoader(): ClassLoader? {
        val apex = "/apex/com.android.wifi/javalib/service-wifi.jar"
        val oplus = "/system_ext/framework/oplus-wifi-service.jar"
        runCatching {
            val factory = Class.forName(
                "com.android.internal.os.SystemServerClassLoaderFactory",
                false,
                appClassLoader
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
                appClassLoader
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
            PathClassLoader(oplus, PathClassLoader(apex, appClassLoader))
        }.getOrNull()
    }
}
