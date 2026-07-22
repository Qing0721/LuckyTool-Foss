package com.fosstool.app.hook.scope.market

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object RemoveMarketMinePageAppRecommend : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { bridge ->
            runCatching {
                bridge.findMethod {
                    searchPackages("com.heytap.market.mine", "com.heytap.cdo.client")
                    matcher {
                        declaredClass {
                            usingStrings("MineFragment", "MineActionBarView")
                        }
                        paramCount = 2
                        returnType(UnitType.name)
                    }
                }.apply {
                    if (isNotEmpty()) {
                        forEach {
                            it.className.toClass().method {
                                name = it.methodName
                                paramCount = 2
                            }.hookAll {
                                before {
                                    result = null
                                }
                            }
                        }
                        return@create
                    }
                }
            }
            runCatching {
                bridge.findClass {
                    matcher {
                        usingStrings("MineActionBarView")
                    }
                }.apply {
                    checkDataList("RemoveMarketMinePageAppRecommend", false)
                    forEach { classData ->
                        classData.name.toClass().apply {
                            method {
                                paramCount = 2
                                returnType = UnitType
                            }.hookAll {
                                before {
                                    val p0 = args().first().any()
                                    val typeName = p0?.javaClass?.name.orEmpty()
                                    if (typeName.contains("ViewLayerWrapDto") ||
                                        typeName.contains("ViewLayer")
                                    ) {
                                        result = null
                                    }
                                }
                            }
                            method {
                                paramCount = 2
                                returnType = UnitType
                            }.hookAll {
                                before {
                                    if (args().last().any() is Boolean) {
                                        args().last().set(false)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            runCatching {
                bridge.findMethod {
                    searchPackages("com.heytap.market.mine")
                    matcher {
                        addParamType("java.util.List")
                        returnType(UnitType.name)
                    }
                }.forEach {
                    it.className.toClass().method {
                        name = it.methodName
                        paramCount(1..3)
                    }.hookAll {
                        before {
                            for (i in 0 until 4) {
                                if (runCatching { args(i).any() }.getOrNull() is List<*>) {
                                    args(i).set(ArrayList<Any>())
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
