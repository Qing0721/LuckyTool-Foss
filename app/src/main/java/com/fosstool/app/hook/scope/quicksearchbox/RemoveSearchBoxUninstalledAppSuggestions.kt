package com.fosstool.app.hook.scope.quicksearchbox

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.StringClass

object RemoveSearchBoxUninstalledAppSuggestions : YukiBaseHooker() {
    override fun onHook() {

        val configNode: Map<String, Any> = mapOf("new_suggest_app_card" to false)
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            val classes = dexKitBridge.findClass {
                matcher { className("com.heytap.quicksearchbox.common.manager.MMKVManager") }
            }.checkDataList("HookMMKV find clazz", onlyOne = false)
            if (classes.isEmpty()) return@create

            dexKitBridge.findMethod {
                searchInClass(classes)
                matcher {
                    paramTypes(StringClass.name, StringClass.name)
                    returnType(StringClass.name)
                    usingStrings("getString")
                }
            }.apply {
                checkDataList("HookMMKV find getString", onlyOne = false)
                firstOrNullSafe()?.apply {
                    className.toClassOrNull(appClassLoader)
                        ?.method {
                            name = methodName
                            param(StringClass, StringClass)
                            returnType = StringClass
                        }
                        ?.ignored()
                        ?.hook {
                            before {
                                val key = args.getOrNull(0) as? String ?: return@before
                                if (key.isBlank()) return@before
                                when (val value = configNode[key]) {
                                    is Boolean -> result = value.toString()
                                    is String, is Int -> result = value
                                    else -> {}
                                }
                            }
                        }
                }
            }

            dexKitBridge.findMethod {
                searchInClass(classes)
                matcher {
                    paramTypes(StringClass.name, BooleanType.name)
                    returnType(BooleanType.name)
                    usingStrings("getBoolean")
                }
            }.apply {
                checkDataList("HookMMKV find getBoolean", onlyOne = false)
                firstOrNullSafe()?.apply {
                    className.toClassOrNull(appClassLoader)
                        ?.method {
                            name = methodName
                            param(StringClass, BooleanType)
                            returnType = BooleanType
                        }
                        ?.ignored()
                        ?.hook {
                            before {
                                val key = args.getOrNull(0) as? String ?: return@before
                                if (key.isBlank()) return@before
                                when (val value = configNode[key]) {
                                    "1", "true" -> resultTrue()
                                    "0", "false" -> resultFalse()
                                    is Boolean -> result = value
                                    else -> {}
                                }
                            }
                        }
                }
            }
        }
    }
}
