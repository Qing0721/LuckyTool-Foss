package com.fosstool.app.hook.scope.directui

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveTouchAppRecommendCard : YukiBaseHooker() {
    override fun onHook() {
        "com.coloros.directui.repository.datasource.AppBean".toClass().apply {
            method { name = "toCardUIInfo" }.hook {
                replaceTo(null)
            }
        }
    }
}
