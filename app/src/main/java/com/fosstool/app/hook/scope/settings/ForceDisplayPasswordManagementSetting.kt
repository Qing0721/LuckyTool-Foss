package com.fosstool.app.hook.scope.settings

import com.fosstool.app.hook.utils.OplusBuildUtlils
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object ForceDisplayPasswordManagementSetting : YukiBaseHooker() {
    override fun onHook() {
        val osVersionCode = try {
            OplusBuildUtlils().getOSVersionCode ?: 0
        } catch (_: Throwable) {
            0
        }
        val clazz = "com.oplus.settings.feature.password.controller.PasswordManagerPreferenceController"
            .toClassOrNull(appClassLoader) ?: return
        if (osVersionCode >= 30) {
            clazz.method { name = "isPreferenceNotAvailable" }.ignored().hook { replaceToFalse() }
        } else {
            clazz.method { name = "displayPreference" }.ignored().hook {
                after {
                    val prefScreen = args.getOrNull(0) ?: return@after
                    val pmPref = invokeFindPreference(prefScreen, "key_password_manager")
                    if (pmPref != null) invokeSetVisible(pmPref, true)
                }
            }

            clazz.method { name = "updateState" }.ignored().hook {
                after {
                    val preference = args.getOrNull(0) ?: return@after
                    invokeSetVisible(preference, true)
                }
            }
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
        } catch (_: Throwable) {
            null
        }
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
        } catch (_: Throwable) {
        }
    }
}
