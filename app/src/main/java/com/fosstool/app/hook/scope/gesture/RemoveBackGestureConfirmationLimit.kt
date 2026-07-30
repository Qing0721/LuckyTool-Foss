package com.fosstool.app.hook.scope.gesture

import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import de.robv.android.xposed.XposedHelpers

object RemoveBackGestureConfirmationLimit : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_back_gesture_confirmation_limit", false)) return
        if (getOSVersionCode !in 35..36) return
        val clazz = "com.oplus.systemui.navigationbar.gesture.sidegesture.SideGestureDetector"
            .toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("RemoveBackGestureConfirmationLimit: SideGestureDetector not found", tag = "LuckyTool")
            return
        }

        clazz.method {
            name = "shouldRespondToGesture"
            emptyParam()
            returnType = BooleanType
        }.ignored().hook {
            before {
                runCatching {
                    XposedHelpers.setBooleanField(instance, "mIsExitMisTouchPreventionFlag", true)
                }
            }
        }

        clazz.method {
            name = "shouldInjectToGestureMode"
            emptyParam()
            returnType = BooleanType
        }.ignored().hook {
            before {
                runCatching {
                    XposedHelpers.setBooleanField(instance, "mIsFirstGestureInGameMode", false)
                }
            }
        }
    }
}
