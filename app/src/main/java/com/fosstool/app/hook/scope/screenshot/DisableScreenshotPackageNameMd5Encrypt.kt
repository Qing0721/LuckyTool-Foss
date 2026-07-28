package com.fosstool.app.hook.scope.screenshot

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.StringClass

object DisableScreenshotPackageNameMd5Encrypt : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            val classes = dexKitBridge.findClass {
                matcher {
                    addMethod { returnType(StringClass.name) }
                    usingStrings("EncryptUtils", "encryptToMd5", "queryEncryptName")
                }
            }.checkDataList("DisableScreenshotPackageNameMd5Encrypt", onlyOne = false)
            if (classes.isEmpty()) return@create

            dexKitBridge.findMethod {
                searchInClass(classes)
                matcher {
                    paramTypes(StringClass.name)
                    returnType(StringClass.name)
                    usingStrings("queryEncryptName")
                }
            }.apply {
                checkDataList("queryEncryptName", onlyOne = false)
                firstOrNullSafe()?.apply {
                    className.toClassOrNull(appClassLoader)
                        ?.method { name = methodName; param(StringClass); returnType = StringClass }
                        ?.ignored()
                        ?.hook {
                            before {

                                val plain = args.getOrNull(0) as? String ?: ""
                                if (plain.isNotBlank()) result = plain
                            }
                        }
                }
            }
        }
    }
}
