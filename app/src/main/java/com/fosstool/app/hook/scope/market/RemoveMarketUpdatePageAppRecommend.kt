package com.fosstool.app.hook.scope.market

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.defined.VagueType
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.FloatType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.highcapable.yukihookapi.hook.type.java.MapClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveMarketUpdatePageAppRecommend : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            dexKitBridge.findClass {
                matcher {
                    fields { add { name = "mDataUtil" } }
                    methods {
                        add {
                            name = "processData"
                            returnType = ListClass.name
                        }
                    }
                }
            }.apply {
                checkDataList("RemoveMarketUpdateDownloadPageAppRecommend")
                firstOrNullSafe()?.name?.toClassOrNull(appClassLoader)
                    ?.method { name = "processData" }
                    ?.ignored()
                    ?.hook { after { (result as? ArrayList<*>)?.clear() } }
            }

            val cardDto = "com.heytap.cdo.card.domain.dto.CardDto"
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType("android.view.ViewGroup")
                        addForType("android.widget.TextView")
                        addForType("com.nearme.imageloader.ImageLoader")
                    }
                    methods {
                        add {
                            paramTypes(ContextClass.name, IntType.name)
                            returnType = UnitType.name
                        }
                        add {
                            paramTypes(ContextClass.name, StringClass.name, IntType.name, IntType.name)
                            returnType = UnitType.name
                        }
                        add {
                            paramTypes("android.view.View", BooleanType.name)
                            returnType = UnitType.name
                        }
                        add {
                            paramCount = 0
                            returnType = "android.view.View"
                        }
                        add {
                            paramTypes("android.view.LayoutInflater")
                            returnType = "android.view.View"
                        }
                    }
                }
            }.apply {
                checkDataList("RemoveMarketUpdatePageAppRecommend APPUpdateItemHolder")
                firstOrNullSafe()?.name?.toClassOrNull(appClassLoader)
                    ?.method {
                        param(cardDto, StringClass, VagueType, MapClass, BooleanType, LongType)
                        returnType = UnitType
                    }
                    ?.ignored()
                    ?.hook { intercept() }
            }

            val fragment = "com.heytap.cdo.client.ui.upgrademgrv2.AppUpdateFragmentV2"
                .toClassOrNull(appClassLoader) ?: return@create

            val scope = dexKitBridge.findClass {
                matcher { className(fragment.name) }
            }.checkDataList("RemoveMarketUpdatePageAppRecommend AppUpdateFragmentV2")
            if (scope.isEmpty()) return@create

            dexKitBridge.findMethod {
                searchInClass(scope)
                matcher {
                    paramTypes(ListClass.name)
                    addInvoke {
                        paramTypes(ContextClass.name, FloatType.name)
                        returnType(IntType.name)
                    }
                    usingNumbers(114.0F)
                }
            }.apply {
                checkDataList("RemoveMarketUpdatePageAppRecommend addDataAndNotifyChanged")
                firstOrNullSafe()?.apply {
                    fragment.method { name = methodName; param(ListClass) }.hook {
                        before { (args().first().any() as? MutableList<*>)?.clear() }
                    }
                }
            }

            dexKitBridge.findMethod {
                searchInClass(scope)
                matcher {
                    paramTypes(BooleanType.name)
                    usingNumbers(0, 300L)
                    addUsingField { type("android.animation.ValueAnimator") }
                    addInvoke { paramCount(0) }
                    usingStrings("mRecommendUpdateContainer", "mNormalUpdateContainer")
                }
            }.apply {
                checkDataList("RemoveMarketUpdatePageAppRecommend AutoScrollWhenUpdateAll")
                firstOrNullSafe()?.apply {
                    fragment.method { name = methodName; param(BooleanType) }.hook { intercept() }
                }
            }
        }
    }
}
