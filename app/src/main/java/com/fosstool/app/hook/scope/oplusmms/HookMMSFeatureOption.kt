package com.fosstool.app.hook.scope.oplusmms

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs

object HookMMSFeatureOption : YukiBaseHooker() {
    override fun onHook() {
        val removeVerifyCode =
            prefs(ModulePrefs).getBoolean("remove_verification_code_floating_window", false)

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(BooleanType.name)
                    }
                    methods {
                        add { paramTypes(ContextClass.name) }
                        add { paramTypes(StringClass.name) }
                        add { paramTypes(StringClass.name, BooleanType.name) }
                        add { paramTypes(ContextClass.name, StringClass.name) }
                        add { paramTypes(ContextClass.name, StringClass.name, StringClass.name) }
                        add { paramTypes(ContextClass.name);returnType(UnitType.name) }
                        add { paramTypes(ContextClass.name);returnType(BooleanType.name) }
                        add { paramCount(0);returnType(BooleanType.name) }
                    }
                    usingStrings("FeatureOption")
                }
            }.apply {
                checkDataList("HookMMSFeatureOption")
                val member = first()
                member.name.toClass().apply {
                    method {
                        param(ContextClass, StringClass, StringClass)
                        returnType = BooleanType
                    }.hook {
                        before {
                            val key = args(1).cast<String>()
                            val key2 = args(2).cast<String>()
                            if (key == null) {
                                when (key2) {
                                    "oplus.software.inputmethod.verify_code_enable" -> if (removeVerifyCode) resultFalse()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
