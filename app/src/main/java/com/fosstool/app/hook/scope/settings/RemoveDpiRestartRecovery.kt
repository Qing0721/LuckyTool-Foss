package com.fosstool.app.hook.scope.settings

import android.content.Context
import android.provider.Settings
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.method
import kotlin.math.max
import kotlin.math.min

object RemoveDpiRestartRecovery : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.settings.widget.preference.OplusDensityPreference".toClass().apply {
            method {
                name = "onPreferenceChange"
                paramCount = 2
            }.hook {
                after {
                    val newValue = args().last().string()
                    val context = instance.current().method {
                        name = "getContext"
                        superClass()
                    }.invoke<Context>() ?: return@after
                    val displayMetrics = context.applicationContext.resources.displayMetrics
                    val min = min(displayMetrics.widthPixels, displayMetrics.heightPixels) *
                            160 / max(newValue.toInt(), 320)
                    val max = max(min, 120)
                    Settings.Secure.putString(
                        context.contentResolver,
                        "display_density_forced",
                        max.toString()
                    )
                    instance.current().method {
                        name = "notifyChanged"
                        superClass()
                    }.call()
                }
            }
        }
    }
}
