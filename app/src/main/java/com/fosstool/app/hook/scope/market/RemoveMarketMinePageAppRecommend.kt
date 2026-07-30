package com.fosstool.app.hook.scope.market

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.BundleClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.MapClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveMarketMinePageAppRecommend : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            val viewLayerWrapDto = "com.heytap.cdo.card.domain.dto.ViewLayerWrapDto"

            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(MapClass.name)
                        addForType(StringClass.name)
                        addForType(BooleanType.name)
                        addForType(BundleClass.name)
                        addForType(ContextClass.name)
                        addForType("com.heytap.market.mine.view.MineActionBarView")
                    }
                    methods {
                        add { name = "onCreate" }
                        add { name = "onCreateView" }
                        add { name = "onDestroy" }
                        add { name = "onDestroyView" }
                        add { name = "onConfigurationChanged" }
                        add {
                            paramTypes(viewLayerWrapDto)
                            returnType(MapClass.name)
                        }
                        add {
                            paramTypes(viewLayerWrapDto, BooleanType.name)
                            returnType(UnitType.name)
                        }
                    }
                    usingStrings("MineFragment")
                }
            }.apply {
                checkDataList("RemoveMarketMinePageAppRecommend")
                firstOrNullSafe()?.name?.toClassOrNull(appClassLoader)?.apply {
                    method {
                        param(viewLayerWrapDto, BooleanType)
                        returnType = UnitType
                    }.hook {

                        before {
                            val dto = args().first().any() ?: return@before
                            val cards = dto.current().method { name = "getCards" }.list<Any>()
                            if (cards.isEmpty()) return@before
                            val kept = ArrayList<Any>(cards)
                            kept.removeIf { kept.indexOf(it) != 0 }
                            dto.current().method { name = "setCards" }.call(ArrayList(kept))
                        }
                    }
                }
            }
        }
    }
}
