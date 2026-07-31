package com.fosstool.app.hook.scope.otherapp

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.BooleanType

object HookGpsJoyStick : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("gps_joystick_unlock_pro", false)) return

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {

                    returnType(BooleanType.name)
                    paramCount(0)
                    usingStrings("AD_FREE", "PRO")
                    usingNumbers(3600000, 25200000)
                }
            }.apply {
                checkDataList("GpsJoyStick unlockPro")
                val member = firstOrNullSafe() ?: return@apply
                member.className.toClassOrNull(appClassLoader)
                    ?.method {
                        name = member.methodName
                        emptyParam()
                    }
                    ?.ignored()
                    ?.hook {

                        replaceToTrue()
                    }
            }

            dexKitBridge.findMethod {
                matcher {
                    returnType("int")
                    paramCount(0)
                    usingStrings("AD_FREE", "PRO")
                    usingNumbers(3600000, 25200000)
                }
            }.apply {
                val member = firstOrNullSafe() ?: return@apply
                member.className.toClassOrNull(appClassLoader)
                    ?.method {
                        name = member.methodName
                        emptyParam()
                    }
                    ?.ignored()
                    ?.hook { replaceTo(2) }
            }
        }
    }
}
