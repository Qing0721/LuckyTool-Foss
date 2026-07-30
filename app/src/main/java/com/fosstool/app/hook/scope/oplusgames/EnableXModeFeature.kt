package com.fosstool.app.hook.scope.oplusgames

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.AnyClass

object EnableXModeFeature : YukiBaseHooker() {
    private val classNames = listOf(
        "business.module.perfmode.CoolingBackClipHelper",
        "business.module.perfmode.CoolingBackClipFeature",
    )

    override fun onHook() {
        val clazz = classNames.firstNotNullOfOrNull { it.toClassOrNull(appClassLoader) } ?: return
        clazz.method {
            paramCount = 1
            returnType = AnyClass
        }.ignored().hook {
            after { resultTrue() }
        }
    }
}
