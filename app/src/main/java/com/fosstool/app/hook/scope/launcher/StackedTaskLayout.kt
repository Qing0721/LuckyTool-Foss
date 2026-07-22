package com.fosstool.app.hook.scope.launcher

import android.view.View
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.dp

object StackedTaskLayout : YukiBaseHooker() {

    override fun onHook() {

        val isEnable = prefs(ModulePrefs).getBoolean("enable_stacked_task_layout", false)
        val level = prefs(ModulePrefs).getInt("set_task_stacking_level", 7)
        val isFix = prefs(ModulePrefs).getBoolean("fix_current_task_to_the_top", false)
        if (!isEnable) return

        "com.android.launcher3.DeviceProfile".toClass().apply {
            constructor { paramCount(8..11) }.hook {
                after {
                    if (SDK >= A13) field {
                        name = "overviewPageSpacing"
                    }.get(instance).set(-(level * 10).dp)
                }
            }
        }
        var oldView: View? = null
        "com.android.quickstep.views.RecentsView".toClass().apply {
            if (SDK < A13) {
                method {
                    name = "setPageSpacing"
                    superClass()
                }.hook {
                    before { args().first().set(-(level * 10).dp) }
                }
            }
            method { name = "notifyPageSwitchListener" }.hook {
                before {
                    if (!isFix) return@before
                    val count = method { name = "getTaskViewCount" }.get(instance)
                        .invoke<Int>()
                    if (count == null || count == 0) return@before
                    val view = method { name = "getCurrentPageTaskView" }.get(instance)
                        .invoke<View>() ?: return@before
                    view.z = 999f
                    if (oldView != view) {
                        oldView?.z = 0f
                        oldView = view
                    }
                }
            }
            method { name = "resetTaskVisuals" }.hook {
                after {
                    if (!isFix) return@after
                    val count = method { name = "getTaskViewCount" }.get(instance)
                        .invoke<Int>()
                    if (count == null || count == 0) return@after
                    val view = method { name = "getCurrentPageTaskView" }.get(instance)
                        .invoke<View>() ?: return@after
                    view.z = 999f
                    if (oldView != view) {
                        oldView?.z = 0f
                        oldView = view
                    }
                }
            }
        }
    }
}
