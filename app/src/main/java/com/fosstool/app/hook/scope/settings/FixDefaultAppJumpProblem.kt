package com.fosstool.app.hook.scope.settings

import android.content.Context
import android.content.Intent
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object FixDefaultAppJumpProblem : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.settings.feature.appmanager.controller.DefaultAppManagerPreferenceController"
            .toClassOrNull(appClassLoader)
            ?.method { name = "handlePreferenceTreeClick" }
            ?.ignored()
            ?.hook {
                before {
                    val preference = args.getOrNull(0) ?: return@before
                    val key = invokeGetKey(preference)
                    val context = invokeGetContext(preference)
                    if (key == "default_apps_manager" && context != null) {
                        runCatching {
                            context.startActivity(
                                Intent("action.oplusos.safecenter.DefaultAppListActivity"),
                            )
                        }
                    }
                }
            }
    }

    private fun invokeGetKey(preference: Any): String? {
        return try {
            val method = preference.javaClass.methods.find {
                it.name == "getKey" && it.parameterTypes.isEmpty()
            }
            method?.isAccessible = true
            method?.invoke(preference) as? String
        } catch (_: Throwable) {
            null
        }
    }

    private fun invokeGetContext(preference: Any): Context? {
        return try {
            val method = preference.javaClass.methods.find {
                it.name == "getContext" && it.parameterTypes.isEmpty()
            }
            method?.isAccessible = true
            method?.invoke(preference) as? Context
        } catch (_: Throwable) {
            null
        }
    }
}
