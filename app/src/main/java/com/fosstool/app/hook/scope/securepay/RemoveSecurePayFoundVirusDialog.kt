package com.fosstool.app.hook.scope.securepay

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.CheckBoxClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.DialogInterfaceClass
import com.highcapable.yukihookapi.hook.type.android.ViewClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge

object RemoveSecurePayFoundVirusDialog : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(BooleanType.name)
                        addForType(CheckBoxClass.name)
                    }
                    methods {
                        add { paramCount(0);returnType(UnitType.name) }
                        add { paramCount(4..8);returnType(UnitType.name) }
                        add { paramCount(0);returnType(BooleanType.name) }
                        add { paramTypes(ViewClass.name);returnType(UnitType.name) }
                        add {
                            paramTypes(
                                ContextClass.name,
                                StringClass.name,
                                IntType.name,
                                DialogInterfaceClass.name,
                                IntType.name
                            )
                            returnType(UnitType.name)
                        }
                    }
                }
            }.apply {
                checkDataList("RemoveSecurePayFoundVirusDialog")
                val cls = (firstOrNullSafe()?.name ?: return@apply).toClassOrNull(appClassLoader) ?: return@apply
                for (m in cls.declaredMethods) {
                    if (m.returnType != Void.TYPE) continue
                    if (m.parameterCount == 2 && m.parameterTypes[1] == String::class.java) {
                        runCatching { XposedBridge.hookMethod(m, XC_MethodReplacement.returnConstant(null)) }
                    } else if (m.parameterCount == 0) {
                        runCatching { XposedBridge.hookMethod(m, XC_MethodReplacement.returnConstant(null)) }
                    }
                }
            }
        }
    }
}
