package com.fosstool.app.hook.scope.oplusgames

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object EnableSupportCompetitionMode : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    usingStrings("isSupportCompetitionMode")
                    returnType(BooleanType.name)
                    paramCount(0)
                }
            }.apply {
                checkDataList("EnableSupportCompetitionMode find isSupportCompetitionMode")
                val m = first()
                m.className.toClass().method {
                    name = m.methodName
                    emptyParam()
                    returnType = BooleanType
                }.hook {
                    replaceToTrue()
                }
            }
        }
    }
}
