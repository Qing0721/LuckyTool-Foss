package com.fosstool.app.hook.scope.market

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.AtomicBooleanClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object RemoveMarketSplashPageAppRecommend : YukiBaseHooker() {
    override fun onHook() {
        val splashDto = "com.heytap.cdo.splash.domain.dto.v2.SplashDto"
        val mediaDto = "com.heytap.cdo.splash.domain.dto.v2.MediaComponentDto"
        val imageDto = "com.heytap.cdo.splash.domain.dto.v2.ImageComponentDto"

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                searchPackages("com.nearme.splash.net")
                matcher {
                    fields {
                        addForType(IntType.name)
                        addForType(LongType.name)
                        addForType(BooleanType.name)
                        addForType(AtomicBooleanClass.name)
                    }
                    methods {
                        add { paramTypes(StringClass.name);returnType(BooleanType.name) }
                        add { paramTypes(BooleanType.name);returnType(splashDto) }
                        add {
                            paramTypes(BooleanType.name, IntType.name, splashDto)
                            returnType(UnitType.name)
                        }
                        add { paramTypes(splashDto, BooleanType.name, mediaDto) }
                        add { paramTypes(splashDto, BooleanType.name, imageDto) }
                    }
                    usingStrings("getSplashData")
                }
            }.apply {
                checkDataList("RemoveMarketSplashPageAppRecommend")
                val member = first()
                member.name.toClass().apply {
                    method { param(BooleanType);returnType(splashDto) }.hook {
                        replaceTo(null)
                    }
                }
            }
        }
    }
}
