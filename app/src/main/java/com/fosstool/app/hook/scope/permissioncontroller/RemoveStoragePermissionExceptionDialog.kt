package com.fosstool.app.hook.scope.permissioncontroller

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ActivityClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveStoragePermissionExceptionDialog : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    declaredClass {
                        fields {
                            addForType("android.app.Application")
                            addForType("android.os.UserHandle")
                            addForType("android.app.admin.DevicePolicyManager")
                        }
                        usingStrings("GrantPermissionsViewModel")
                    }
                    paramTypes(ActivityClass.name)
                    returnType(UnitType.name)
                    usingStrings(
                        "oplus.intent.extra.PACKAGE_LABEL",
                        "oplus.intent.extra.GROUP_NAME",
                        "android.permission-group.STORAGE"
                    )
                }
            }.checkDataList("RemoveStoragePermissionExceptionDialog").apply {
                val member = firstOrNullSafe() ?: return@apply
                member.className.toClassOrNull(appClassLoader)?.apply {
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
