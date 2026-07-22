package com.fosstool.app.hook.scope.screenshot

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object DisableScreenshotPackageNameMd5Encrypt : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    paramTypes(StringClass.name)
                    returnType(StringClass.name)
                    usingStrings("queryEncryptName")
                }
            }.apply {
                checkDataList("DisableScreenshotPackageNameMd5Encrypt", onlyOne = false)
                forEach { data ->
                    runCatching {
                        data.className.toClass().method {
                            name = data.methodName
                            param(StringClass)
                            returnType = StringClass
                        }.hook {
                            before {
                                val plain = args().first().cast<String>() ?: return@before
                                result = plain
                            }
                        }
                    }
                }
            }
        }
    }
}
