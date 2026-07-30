package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.java.InputStreamClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import de.robv.android.xposed.XposedHelpers

object DisableAccessibilityWarningDialog : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("disable_accessibility_warning_dialog", false)) return
        if (getOSVersionCode < 38) return

        val mgr = "com.android.server.am.FraudBehaviorDetectManager".toClassOrNull(appClassLoader)
        if (mgr == null) {
            YLog.error("DisableAccessibilityWarningDialog: FraudBehaviorDetectManager not found")
            return
        }

        mgr.method {
            name = "updateGlobalCloseConfigToXmlFile"
            param(StringClass, IntType)
        }.ignored().hook {
            after { disableConfig(instance) }
        }

        mgr.method {
            name = "jsonToConfig"
            param(InputStreamClass)
        }.ignored().hook {
            after { disableConfig(instance) }
        }
    }

    private fun disableConfig(host: Any?) {
        if (host == null) return
        runCatching {
            val config = XposedHelpers.getObjectField(host, "mConfig") ?: return
            XposedHelpers.setBooleanField(config, "enabled", false)
        }
    }
}
