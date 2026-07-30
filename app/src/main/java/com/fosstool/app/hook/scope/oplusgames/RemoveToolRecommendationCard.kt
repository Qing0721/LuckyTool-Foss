package com.fosstool.app.hook.scope.oplusgames

import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveToolRecommendationCard : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_tool_recommendation_card", false)) return
        runCatching {
            "business.module.toolsrecommend.ToolsRecommendCardLayout".toClassOrNull(appClassLoader)?.apply {
                method {
                    param(ListClass)
                    returnType = UnitType
                }.ignored().hook {
                    before { args().first().set(ArrayList<Any>()) }
                }
            }
        }
    }
}
