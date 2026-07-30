package com.fosstool.app.hook.scope.market

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.type.java.AtomicBooleanClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveMarketSplashPageAppRecommend : YukiBaseHooker() {
    override fun onHook() {

        val useV4 = "com.heytap.cdo.splash.domain.dto.v4.SplashDtoV4"
            .toClassOrNull(appClassLoader) != null
        val splashDto = if (useV4) {
            "com.heytap.cdo.splash.domain.dto.v4.SplashDtoV4"
        } else {
            "com.heytap.cdo.splash.domain.dto.v2.SplashDto"
        }
        val mediaDto = if (useV4) {
            "com.heytap.cdo.splash.domain.dto.v4.MediaComponentDtoV4"
        } else {
            "com.heytap.cdo.splash.domain.dto.v2.MediaComponentDto"
        }
        val imageDto = if (useV4) {
            "com.heytap.cdo.splash.domain.dto.v4.ImageComponentDtoV4"
        } else {
            "com.heytap.cdo.splash.domain.dto.v2.ImageComponentDto"
        }

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
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
                firstOrNullSafe()?.name?.toClassOrNull(appClassLoader)
                    ?.method {
                        param(BooleanType)
                        returnType = splashDto
                    }
                    ?.ignored()
                    ?.hook { intercept() }
            }
        }
    }
}
