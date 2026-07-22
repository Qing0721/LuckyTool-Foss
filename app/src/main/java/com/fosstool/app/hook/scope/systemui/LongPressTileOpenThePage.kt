package com.fosstool.app.hook.scope.systemui

import android.app.PendingIntent
import android.content.Intent
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.IntentClass
import com.highcapable.yukihookapi.hook.type.android.PendingIntentClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.fosstool.app.hook.utils.sysui.DependencyUtils
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object LongPressTileOpenThePage : YukiBaseHooker() {
    override fun onHook() {
        if (SDK < A13) return
        val isRestore = prefs(ModulePrefs).getBoolean("restore_some_tile_long_press_event", false)

        "com.android.systemui.qs.tileimpl.QSTileImpl".toClass().apply {
            method { name = "longClick";paramCount = 1 }.hook {
                before {
                    if (!isRestore) return@before
                    val mState = field { name = "mState" }.get(instance).any() ?: return@before
                    val dualTarget = mState.current().field {
                        name = "dualTarget";superClass()
                    }.boolean()
                    if (dualTarget) {
                        field { name = "mClickHandler" }.get(instance).cast<android.os.Handler>()
                            ?.sendEmptyMessage(4)
                        resultNull()
                    }
                }
            }
        }
        VariousClass(
            "com.oplusos.systemui.qs.tiles.OplusCellularTile",
            "com.oplus.systemui.qs.tiles.OplusCellularTile"
        ).toClass().apply {
            method { name = "getLongClickIntent" }.hook {
                before {
                    if (!isRestore) return@before
                    val getState = method {
                        name = "getState";superClass()
                    }.get(instance).invoke<Any>() ?: return@before
                    val state = getState.current().field {
                        name = "state";superClass()
                    }.cast<Int>() ?: return@before
                    result = if (state == 0) Intent("android.settings.WIRELESS_SETTINGS")
                    else method {
                        name = "getCellularSettingIntent";superClass()
                    }.get(instance).invoke<Intent>()
                }
            }
        }
    }

    @Suppress("SameParameterValue", "unused")
    private fun openIntent(intent: PendingIntent) {
        val activityStarterCls = "com.android.systemui.plugins.ActivityStarter".toClass()
        val activityStarter = DependencyUtils(appClassLoader).get(activityStarterCls)
        activityStarter?.current()?.method {
            name = "postStartActivityDismissingKeyguard"
            param(PendingIntentClass)
        }?.call(intent)
    }

    @Suppress("SameParameterValue", "unused")
    private fun openIntent(intent: Intent, int: Int) {
        val activityStarterCls = "com.android.systemui.plugins.ActivityStarter".toClass()
        val activityStarter = DependencyUtils(appClassLoader).get(activityStarterCls)
        activityStarter?.current()?.method {
            name = "postStartActivityDismissingKeyguard"
            param(IntentClass, IntType)
        }?.call(intent, int)
    }
}
