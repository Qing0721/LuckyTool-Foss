package com.fosstool.app.hook.scope.battery

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.StringClass

object RemoveBatteryRestrictPlugin : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            val supporter = dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(ContextClass.name)
                        addForType(StringClass.name)
                    }
                    methods {
                        add { paramTypes(StringClass.name, "android.os.Bundle") }
                        add { paramTypes(StringClass.name, "android.content.Intent") }
                    }
                    usingStrings("loadRestrictPlugin", "loadConfigPlugin", "onPluginConnected")
                }
            }.checkDataList("PluginSupporter")
            if (supporter.isEmpty()) return@create

            dexKitBridge.findMethod {
                searchInClass(supporter)
                matcher {
                    usingStrings("loadRestrictPlugin")
                }
            }.apply {
                checkDataList("loadRestrictPlugin", onlyOne = false)
                forEach { data ->
                    data.className.toClassOrNull(appClassLoader)
                        ?.method {
                            name = data.methodName
                            paramCount = data.paramTypeNames.size
                        }
                        ?.ignored()
                        ?.hook { intercept() }
                }
            }
        }
    }
}
