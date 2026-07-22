package com.fosstool.app.hook.scope.oplusgames

import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.UnitType

object RemoveToolRecommendationCard : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_tool_recommendation_card", false)) return
        runCatching {
            "business.module.toolsrecommend.ToolsRecommendCardLayout".toClass().apply {
                method {
                    param(ListClass)
                    returnType = UnitType
                }.hookAll {
                    intercept()
                }
            }
        }
    }
}
