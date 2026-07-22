package com.fosstool.app.hook.scope.oplusgames

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.AnyClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType

object EnableXModeFeature : YukiBaseHooker() {
    override fun onHook() {
        "business.module.perfmode.CoolingBackClipHelper".toClass().apply {
            method { emptyParam();returnType = BooleanType }.hookAll {
                replaceToTrue()
            }
            method {
                paramCount = 1
                returnType = AnyClass
            }.hook {
                after { resultTrue() }
            }
        }
    }
}
