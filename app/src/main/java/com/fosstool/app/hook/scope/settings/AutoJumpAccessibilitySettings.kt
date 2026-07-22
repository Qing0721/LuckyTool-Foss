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
                    after {
                        val activity = instance<Activity>() ?: return@after
                        val referrer = activity.referrer ?: return@after
                        val host = referrer.host ?: return@after
                        val intent = activity.intent ?: return@after
                        if (intent.action != "android.settings.ACCESSIBILITY_SETTINGS") return@after

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
            helperClass.getConstructor(Activity::class.java).newInstance(activity)
        }.getOrNull() ?: return
        val map = runCatching {
            helperClass.getMethod("loadAccessibilityInfos").invoke(helper) as? Map<*, *>
        }.getOrNull() ?: return

        var matchedBundle: Bundle? = null
        for ((key, value) in map) {
            val keyStr = key as? String ?: continue
            if (keyStr.startsWith(host, ignoreCase = true)) {
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
