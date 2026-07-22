package com.fosstool.app.hook.scope.quicksearchbox

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.StringClass

object RemoveSearchBoxUninstalledAppSuggestions : YukiBaseHooker() {
    override fun onHook() {
        val configNode: Map<String, Any> = mapOf("new_suggest_app_card" to false)
        runCatching {
            DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
                dexKitBridge.findMethod {
                    matcher {
                        paramTypes(StringClass.name, StringClass.name)
                        returnType = StringClass.name
                        usingStrings("getString")
                    }
                }.apply {
                    checkDataList("RemoveSearchBoxUninstalledAppSuggestions.getString")
                    firstOrNull()?.apply {
                        className.toClass().method {
                            name = methodName
                            param(StringClass, StringClass)
                            returnType = StringClass
                        }.hook {
                            before {
                                val key = args().first().string()
                                configNode[key]?.let { if (it is Boolean) result = it.toString() }
                            }
                        }
                    }
                }
                dexKitBridge.findMethod {
                    matcher {
                        paramTypes(StringClass.name, BooleanType.name)
                        returnType = BooleanType.name
                        usingStrings("getBoolean")
                    }
                }.apply {
                    checkDataList("RemoveSearchBoxUninstalledAppSuggestions.getBoolean")
                    firstOrNull()?.apply {
                        className.toClass().method {
                            name = methodName
                            param(StringClass, BooleanType)
                            returnType = BooleanType
                        }.hook {
                            before {
                                val key = args().first().string()
                                configNode[key]?.let { if (it is Boolean) result = it }
                            }
                        }
                    }
                }
            }
        }
    }
}
