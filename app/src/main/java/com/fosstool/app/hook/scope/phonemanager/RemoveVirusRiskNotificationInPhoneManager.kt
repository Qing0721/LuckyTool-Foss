package com.fosstool.app.hook.scope.phonemanager

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.ArrayListClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge

object RemoveVirusRiskNotificationInPhoneManager : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(ContextClass.name)
                        addForType(StringClass.name)
                    }
                    methods {
                        add { paramTypes(ArrayListClass.name) }
                        add { returnType(IntType.name) }
                        add { returnType(StringClass.name) }
                    }
                    usingStrings("VirusScanNotifyListener")
                }
            }.apply {
                checkDataList("RemoveVirusRiskNotificationInPhoneManager")
                val cls = (firstOrNullSafe()?.name ?: return@apply).toClassOrNull(appClassLoader) ?: return@apply
                for (m in cls.declaredMethods) {
                    if (m.parameterCount == 1 &&
                        java.util.ArrayList::class.java.isAssignableFrom(m.parameterTypes[0])
                    ) {
                        runCatching { XposedBridge.hookMethod(m, XC_MethodReplacement.returnConstant(null)) }
                    }
                }
            }
        }
    }
}
