package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.hook.utils.OplusBuildUtlils

object ForceDisplayPasswordManagementSetting : YukiBaseHooker() {
    override fun onHook() {
        val osVersionCode = try { OplusBuildUtlils().getOSVersionCode ?: 0 } catch (_: Throwable) { 0 }
        val className = "com.oplus.settings.feature.password.controller.PasswordManagerPreferenceController"
        try {
            className.toClass().apply {
                if (osVersionCode >= 30) {
                    method { name = "isPreferenceNotAvailable" }.hook {
                        replaceToFalse()
                    }
                } else {
                    method { name = "displayPreference" }.hook {
                        after {
                            val prefScreen = args().first().any() ?: return@after
                            val pmPref = invokeFindPreference(prefScreen, "key_password_manager")
                            if (pmPref != null) invokeSetVisible(pmPref, true)
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            YLog.error(
                "ForceDisplayPasswordManagementSetting: $className not found",
                tag = "LuckyTool"
            )
        }
    }

    private fun invokeFindPreference(host: Any, key: CharSequence): Any? {
        return try {
            val method = host.javaClass.methods.find {
                it.name == "findPreference" && it.parameterTypes.size == 1 &&
                    CharSequence::class.java.isAssignableFrom(it.parameterTypes[0])
            } ?: host.javaClass.methods.find {
                it.name == "findPreference" && it.parameterTypes.size == 1
            }
            method?.isAccessible = true
            method?.invoke(host, key)
        } catch (_: Throwable) { null }
    }

    private fun invokeSetVisible(preference: Any, visible: Boolean) {
        try {
            val method = preference.javaClass.methods.find {
                it.name == "setVisible" && it.parameterTypes.size == 1 &&
                    (it.parameterTypes[0] == Boolean::class.javaPrimitiveType ||
                        it.parameterTypes[0] == java.lang.Boolean::class.java)
            }
            method?.isAccessible = true
            method?.invoke(preference, visible)
        } catch (_: Throwable) {}
    }
}
