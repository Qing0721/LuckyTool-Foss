package com.fosstool.app.hook.scope.otherapp

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import org.luckypray.dexkit.query.enums.StringMatchType

object AdmUnlockMoreThreads : YukiBaseHooker() {
    override fun onHook() {
        val raw = prefs(ModulePrefs).getString("adm_unlock_more_threads", "0") ?: "0"
        val threads = raw.toIntOrNull() ?: 0
        if (threads <= 0) return

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields { addForType(IntType.name) }
                    methods {
                        add {
                            name("call", StringMatchType.Contains)
                            paramCount = 0
                            returnType(IntType.name)
                            usingNumbers(15)
                        }
                        add {
                            name("call", StringMatchType.Contains)
                            paramCount = 0
                            returnType(BooleanType.name)
                        }
                    }
                }
            }.apply {
                checkDataList("AdmUnlockMoreThreads")
                val data = firstOrNullSafe() ?: return@apply
                data.name.toClassOrNull(appClassLoader)
                    ?.method {
                        name { it.contains("call") }
                        emptyParam()
                        returnType = IntType
                    }
                    ?.ignored()
                    ?.hook {
                        after {
                            val cur = result as? Int ?: return@after
                            if (cur == 15) result = threads - 1
                        }
                    }
            }
        }
    }
}
