package com.fosstool.app.hook.scope.oplusgames

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.ArrayListClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.StringClass

object EnableSupportCompetitionMode : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            val classes = dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(ListClass.name)
                    }
                    methods {
                        add {
                            paramCount(0)
                            returnType(ListClass.name)
                        }
                        add {
                            paramCount(0)
                            returnType(BooleanType.name)
                        }
                        add {
                            paramTypes(StringClass.name, ArrayListClass.name)
                        }
                    }
                }
            }.checkDataList("EnableSupportCompetitionMode find CompetitionModeManager")
            if (classes.isEmpty()) return@create

            dexKitBridge.findMethod {
                searchInClass(classes)
                matcher {
                    paramCount(0)
                    returnType(BooleanType.name)
                    usingStrings("isSupportCompetitionMode")
                }
            }.apply {
                checkDataList("EnableSupportCompetitionMode find isSupportCompetitionMode")
                val m = firstOrNullSafe() ?: return@apply
                m.className.toClassOrNull(appClassLoader)
                    ?.method {
                        name = m.methodName
                        emptyParam()
                        returnType = BooleanType
                    }
                    ?.ignored()
                    ?.hook { replaceToTrue() }
            }
        }
    }
}
