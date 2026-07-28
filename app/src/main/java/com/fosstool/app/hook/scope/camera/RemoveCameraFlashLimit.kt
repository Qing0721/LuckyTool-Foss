package com.fosstool.app.hook.scope.camera

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe

object RemoveCameraFlashLimit : YukiBaseHooker() {
    override fun onHook() {

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            val classes = dexKitBridge.findClass {
                matcher {
                    className("com.oplus.camera.CameraManager")
                }
            }.apply {
                checkDataList("RemoveCameraFlashLimit Clazz")
            }

            if (classes.isEmpty()) return@create

            dexKitBridge.findMethod {
                searchInClass(classes)
                matcher {
                    paramTypes(IntType.name)
                    returnType(UnitType.name)
                    usingNumbers(15, 5, 2)
                }
            }.apply {
                checkDataList("RemoveCameraFlashLimit Method")
                val member = firstOrNullSafe() ?: return@apply
                member.className.toClassOrNull(appClassLoader)?.apply {
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
