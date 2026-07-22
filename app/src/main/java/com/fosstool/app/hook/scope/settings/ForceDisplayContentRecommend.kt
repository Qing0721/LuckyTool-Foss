package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object ForceDisplayContentRecommend : YukiBaseHooker() {
    override fun onHook() {
        val classes = listOf(
            "com.oplus.settings.feature.othersettings.controller.RecommendController",
            "com.oplus.settings.feature.spfunction.RecommendController",
        )
        for (cn in classes) {
            runCatching {
                cn.toClass().method { name = "getAvailabilityStatus" }.hook {
                    replaceTo(0)
                }
            }
        }
    }
}
