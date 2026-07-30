package com.fosstool.app.hook.scope.systemui

import android.app.PendingIntent
import android.content.Intent
import android.os.Handler
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.hook.utils.sysui.DependencyUtils
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

object LongPressTileOpenThePage : YukiBaseHooker() {
    override fun onHook() {
        if (SDK < A13) return
        val isRestore = prefs(ModulePrefs).getBoolean("restore_some_tile_long_press_event", false)

        "com.android.systemui.qs.tileimpl.QSTileImpl"
            .toClassOrNull(appClassLoader)?.let { c ->
                c.method { name = "longClick"; paramCount = 1 }.ignored().hook {
                    before {
                        if (!isRestore) return@before
                        val mState = c.findField("mState")?.get(instance) ?: return@before
                        val dualTarget = runCatching {
                            XposedHelpers.getBooleanField(mState, "dualTarget")
                        }.getOrNull() ?: return@before
                        if (dualTarget) {
                            (c.findField("mClickHandler")?.get(instance) as? Handler)
                                ?.sendEmptyMessage(4)
                            result = null
                        }
                    }
                }
            }
        VariousClass(
            "com.oplusos.systemui.qs.tiles.OplusCellularTile",
            "com.oplus.systemui.qs.tiles.OplusCellularTile"
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "getLongClickIntent" }.ignored().hook {
                before {
                    if (!isRestore) return@before
                    val getState = runCatching {
                        var cls: Class<*>? = c
                        var m: java.lang.reflect.Method? = null
                        while (cls != null && m == null) {
                            m = runCatching { cls.getDeclaredMethod("getState") }.getOrNull()
                            cls = cls.superclass
                        }
                        m?.apply { isAccessible = true }?.invoke(instance)
                    }.getOrNull() ?: return@before
                    val state = runCatching {
                        XposedHelpers.getIntField(getState, "state")
                    }.getOrNull() ?: return@before
                    result = if (state == 0) Intent("android.settings.WIRELESS_SETTINGS")
                    else runCatching {
                        var cls: Class<*>? = c
                        var m: java.lang.reflect.Method? = null
                        while (cls != null && m == null) {
                            m = runCatching { cls.getDeclaredMethod("getCellularSettingIntent") }.getOrNull()
                            cls = cls.superclass
                        }
                        m?.apply { isAccessible = true }?.invoke(instance)
                    }.getOrNull()
                }
            }
        }
    }

    @Suppress("SameParameterValue", "unused")
    private fun openIntent(intent: PendingIntent) {
        val activityStarterCls = "com.android.systemui.plugins.ActivityStarter"
            .toClassOrNull(appClassLoader) ?: return
        val activityStarter = DependencyUtils(appClassLoader).get(activityStarterCls)
        runCatching {
            XposedHelpers.callMethod(
                activityStarter,
                "postStartActivityDismissingKeyguard",
                intent
            )
        }
    }

    @Suppress("SameParameterValue", "unused")
    private fun openIntent(intent: Intent, int: Int) {
        val activityStarterCls = "com.android.systemui.plugins.ActivityStarter"
            .toClassOrNull(appClassLoader) ?: return
        val activityStarter = DependencyUtils(appClassLoader).get(activityStarterCls)
        runCatching {
            XposedHelpers.callMethod(
                activityStarter,
                "postStartActivityDismissingKeyguard",
                intent,
                int
            )
        }
    }

    private fun Class<*>.findField(name: String): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            runCatching { return cls.getDeclaredField(name).also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }
}
