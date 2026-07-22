package com.fosstool.app.hook.scope.camera

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object RemoveCameraFlashLimit : YukiBaseHooker() {
    override fun onHook() {

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                searchPackages("com.oplus.camera")
                matcher {
                    paramTypes(IntType.name)
                    returnType(UnitType.name)
                    usingNumbers(15, 5, 2)
                }
            }.apply {
                checkDataList("RemoveCameraFlashLimit")
                val member = first()
                member.className.toClass().apply {
                    method {
                        name = member.methodName
                        param(IntType)
                        returnType = UnitType
                    }.hook {
                        before {
                            args(0).set(100)
                        }
                    }
                }
            }
        }
    }
}
