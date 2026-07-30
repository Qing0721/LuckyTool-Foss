package com.fosstool.app.hook.scope.oplusgames

import android.os.Bundle
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.BundleClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe

object RemoveRootCheck : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(BooleanType.name)
                        addForType(IntType.name)
                    }
                    methods {
                        add { paramCount(0);returnType(BundleClass.name) }
                    }
                    usingStrings("COSASDKManager")
                }
            }.apply {
                checkDataList("RemoveRootCheck")
                val cls = (firstOrNullSafe()?.name ?: return@apply).toClassOrNull(appClassLoader) ?: return@apply
                cls.method {
                    emptyParam()
                    returnType = BundleClass
                }.ignored().hook {
                    after {
                        result<Bundle>()?.putInt("isSafe", 0)
                    }
                }
            }
        }
    }
}
