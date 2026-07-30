package com.fosstool.app.hook.scope.packageinstaller

import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import de.robv.android.xposed.XposedHelpers
import java.security.SecureRandom

object ForceInstallButtonDisplay : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("fix_install_button_display_exception", false)) return

        val targets = listOf(
            "com.android.packageinstaller.oplus.view.ConfusedButton",
            "com.android.packageinstaller.oplus.view.ConfusedTextView",
        )
        for (clsName in targets) {
            val clazz = clsName.toClassOrNull(appClassLoader) ?: continue
            clazz.method { name = "getAccessibilityViewId" }.ignored().hook {
                before { markCts(instance) }
            }
            clazz.method { name = "getText" }.ignored().hook {
                before { markCts(instance) }
            }
        }
    }

    private fun markCts(instance: Any?) {
        if (instance == null) return
        runCatching {
            XposedHelpers.callMethod(instance, "setCts", true)
        }
        runCatching {
            for (f in instance.javaClass.declaredFields) {
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
