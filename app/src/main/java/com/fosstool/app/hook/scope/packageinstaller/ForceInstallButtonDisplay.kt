package com.fosstool.app.hook.scope.packageinstaller

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs
import java.security.SecureRandom

object ForceInstallButtonDisplay : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("fix_install_button_display_exception", false)) return

        val targets = listOf(
            "com.android.packageinstaller.oplus.view.ConfusedButton",
            "com.android.packageinstaller.oplus.view.ConfusedTextView",
        )
        for (clsName in targets) {
            runCatching {
                clsName.toClass().apply {
                    method { name = "getAccessibilityViewId" }.hookAll {
                        before { markCts(instance) }
                    }
                    runCatching {
                        method { name = "getText" }.hookAll {
                            before { markCts(instance) }
                        }
                    }
                }
            }
        }
    }

    private fun markCts(instance: Any?) {
        if (instance == null) return
        runCatching {
            instance.current().method {
                name = "setCts"
                param(Boolean::class.javaPrimitiveType!!)
            }.call(true)
        }
        runCatching {
            val fields = instance.javaClass.declaredFields
            for (f in fields) {
                if (f.type == SecureRandom::class.java ||
                    f.type.name.contains("Random", ignoreCase = true)
                ) {
                    f.isAccessible = true
                    f.set(instance, SecureRandom())
                    break
                }
            }
        }
    }
}
