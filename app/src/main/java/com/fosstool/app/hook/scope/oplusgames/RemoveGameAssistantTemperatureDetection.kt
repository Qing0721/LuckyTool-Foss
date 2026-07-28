package com.fosstool.app.hook.scope.oplusgames

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.IntType

object RemoveGameAssistantTemperatureDetection : YukiBaseHooker() {
    override fun onHook() {
        "business.module.perfmode.CoolingBubbleTipsHelper".toClassOrNull(appClassLoader)?.apply {
            field { type = IntType }.all().forEach { if (it.int() > 0) it.set(100) }
        }
    }
}
