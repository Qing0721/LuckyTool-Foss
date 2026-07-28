package com.fosstool.app.hook.scope.browser

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.ArrayListClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.MapClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType

object RemoveBrowserSearchBarAppPromotion : YukiBaseHooker() {
    private const val APP_HOST = "com.heytap.browser.platform.app.AppHost"

    override fun onHook() {

        if (APP_HOST.toClassOrNull(appClassLoader) == null) return

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            val appClsName = dexKitBridge.findClass {
                matcher {
                    addFieldForType(StringClass.name)
                    methods {
                        add {
                            paramCount = 0
                            returnType = StringClass.name
                        }
                    }
                    usingStrings("res", "initialState", "sugNaturalApp")
                }
            }.checkDataList("RemoveBrowserSearchBarAppPromotion App").firstOrNullSafe()?.name

            val adsClsName = dexKitBridge.findClass {
                matcher {
                    addFieldForType(IntType.name)
                    addFieldForType(StringClass.name)
                    methods {
                        add { name = "getTitle" }
                        add { name = "getCategoryType" }
                        add {
                            paramCount = 0
                            returnType = IntType.name
                        }
                        add {
                            paramCount = 0
                            returnType = StringClass.name
                        }
                    }
                    usingStrings("res", "ad", "sugAd")
                }
            }.checkDataList("RemoveBrowserSearchBarAppPromotion Ads").firstOrNullSafe()?.name

            if (appClsName == null && adsClsName == null) return@create

            val adapter = dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(ListClass.name)
                        addForType(ArrayListClass.name)
                        addForType(MapClass.name)
                        addForType(IntType.name)
                        addForType(APP_HOST)
                    }
                    methods {
                        add {
                            paramCount = 0
                            returnType = ListClass.name
                        }
                        add { paramTypes(IntType.name, IntType.name, IntType.name, IntType.name) }
                        add {
                            paramTypes(ListClass.name)
                            returnType = UnitType.name
                        }
                        add {
                            paramTypes(APP_HOST)
                            returnType = UnitType.name
                        }
                        add { name = "getItemCount" }
                        add { name = "getItemViewType" }
                        add { name = "onAttachedToRecyclerView" }
                        add { name = "onBindViewHolder" }
                        add { name = "onCreateViewHolder" }
                        add { name = "onViewAttachedToWindow" }
                    }
                }
            }.checkDataList("RemoveBrowserSearchBarAppPromotion Adapter")
            if (adapter.isEmpty()) return@create

            dexKitBridge.findMethod {
                searchInClass(adapter)
                matcher {
                    paramTypes(ListClass.name)
                    returnType = UnitType.name
                    usingStrings("linkEdit")
                    addCaller {
                        paramTypes(ListClass.name)
                        returnType = UnitType.name
                        usingStrings("headerData", "linkEdit")
                    }
                }
            }.apply {
                checkDataList("RemoveBrowserSearchBarAppPromotion Method", onlyOne = false)
                firstOrNullSafe()?.apply {
                    className.toClassOrNull(appClassLoader)
                        ?.method { name = methodName; param(ListClass) }
                        ?.ignored()
                        ?.hook {
                            before {
                                @Suppress("UNCHECKED_CAST")
                                val list = args().first().any() as? ArrayList<Any?> ?: return@before
                                list.removeIf {
                                    val clsName = it?.javaClass?.name ?: return@removeIf false
                                    clsName == appClsName || clsName == adsClsName
                                }
                            }
                        }
                }
            }
        }
    }
}
