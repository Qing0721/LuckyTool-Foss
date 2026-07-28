package com.fosstool.app.hook.scope.settings

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.log.YLog

object AutoJumpAccessibilitySettings : YukiBaseHooker() {
    override fun onHook() {
        val className = "com.android.settings.SettingsActivity"
        try {
            className.toClass().apply {
                method { name = "onCreate"; paramCount = 1 }.hook {
                    before {
                        val activity = instance<Activity>() ?: return@before
                        val referrer = activity.referrer ?: return@before
                        val host = referrer.host ?: return@before
                        val intent = activity.intent ?: return@before
                        if (intent.action != "android.settings.ACCESSIBILITY_SETTINGS") return@before

                        jumpToAccessibilityService(activity, host)
                    }
                }
            }
        } catch (e: Throwable) {
            YLog.error("AutoJumpAccessibilitySettings: $className not found", tag = "LuckyTool")
        }
    }

    private fun jumpToAccessibilityService(activity: Activity, host: String) {
        val helperClass = try {
            "com.oplus.settings.feature.accessibility.controller.AccessibilityDataHelper".toClass()
        } catch (_: Throwable) {
            return
        }

        val helper = runCatching {
            val ctor2 = helperClass.declaredConstructors.firstOrNull {
                it.parameterCount == 2 && it.parameterTypes[0].isAssignableFrom(activity.javaClass) &&
                    !it.parameterTypes[1].isPrimitive
            }
            if (ctor2 != null) {
                ctor2.isAccessible = true
                return@runCatching ctor2.newInstance(activity, null)
            }
            val ctor1 = helperClass.declaredConstructors.firstOrNull {
                it.parameterCount == 1 && it.parameterTypes[0].isAssignableFrom(activity.javaClass)
            } ?: return@runCatching null
            ctor1.isAccessible = true
            ctor1.newInstance(activity)
        }.getOrNull() ?: return
        val map = runCatching {
            var c: Class<*>? = helperClass
            var m: java.lang.reflect.Method? = null
            while (c != null && m == null) {
                m = c.declaredMethods.firstOrNull {
                    it.name == "loadAccessibilityInfos" && it.parameterCount == 0
                }
                c = c.superclass
            }
            m?.isAccessible = true
            m?.invoke(helper) as? Map<*, *>
        }.getOrNull() ?: return

        var matchedBundle: Bundle? = null
        for ((key, value) in map) {
            val keyStr = key as? String ?: continue

            if (keyStr.startsWith(host)) {
                matchedBundle = value as? Bundle
                break
            }
        }
        val bundle = matchedBundle ?: return

        val targetIntent = Intent("android.intent.action.MAIN").apply {
            component = ComponentName(
                "com.android.settings",
                "com.android.settings.SubSettings"
            )
            putExtra(
                ":settings:show_fragment",
                "com.oplus.settings.feature.accessibility.OplusToggleAccessibilityServicePreferenceFragment"
            )
            putExtra(":settings:show_fragment_args", bundle)
        }
        runCatching { activity.startActivity(targetIntent) }
    }
}
