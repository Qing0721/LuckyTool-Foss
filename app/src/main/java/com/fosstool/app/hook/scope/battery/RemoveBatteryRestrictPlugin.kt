package com.fosstool.app.hook.scope.battery

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object RemoveBatteryRestrictPlugin : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    usingStrings("PluginSupporter", "loadRestrictPlugin")
                }
            }.apply {
                checkDataList("RemoveBatteryRestrictPlugin", onlyOne = false)
                forEach { data ->
                    runCatching {
                        data.className.toClass().method {
                            name = data.methodName
                        }.hook { replaceTo(null) }
                    }
                }
            }
        }
    }
}
