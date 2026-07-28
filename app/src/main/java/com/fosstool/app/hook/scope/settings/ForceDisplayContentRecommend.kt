package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object ForceDisplayContentRecommend : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplus.settings.feature.othersettings.controller.RecommendController",
            "com.oplus.settings.feature.spfunction.RecommendController",
        ).toClassOrNull(appClassLoader)
            ?.method { name = "getAvailabilityStatus" }
            ?.ignored()
            ?.hook { replaceTo(0) }
    }
}
