package com.fosstool.app.hook.scope.permissioncontroller

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ActivityClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object RemoveStoragePermissionExceptionDialog : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    declaredClass {
                        superClass("android.app.Activity")
                        usingStrings(
                            "oplus.intent.extra.PACKAGE_LABEL",
                            "oplus.intent.extra.GROUP_NAME",
                            "android.permission-group.STORAGE"
                        )
                    }
                    paramTypes(ActivityClass.name)
                    returnType(UnitType.name)
                }
            }.checkDataList("RemoveStoragePermissionExceptionDialog").apply {
                val member = first()
                member.className.toClass().apply {
                    method {
                        name = member.methodName
                        param(ActivityClass)
                        returnType = UnitType
                    }.hook {
                        intercept()
                    }
                }
            }
        }
    }
}
