package com.fosstool.app.hook.scope.otherapp

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs

object AdmUnlockMoreThreads : YukiBaseHooker() {
    override fun onHook() {
        val raw = prefs(ModulePrefs).getString("adm_unlock_more_threads", "0") ?: "0"
        val threads = raw.toIntOrNull() ?: 0
        if (threads <= 0) return
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    returnType = IntType.name
                    usingStrings("max_threads", "thread", "download")
                }
            }.apply {
                checkDataList("AdmUnlockMoreThreads", onlyOne = false)
                forEach { data ->
                    runCatching {
                        data.className.toClass().method {
                            name = data.methodName
                            returnType = IntType
                        }.hook {
                            after {
                                val cur = result<Int>() ?: return@after
                                if (cur in 1..64) result = threads
                            }
                        }
                    }
                }
            }
        }
        runCatching {
            "com.dv.get.Config".toClass().apply {
                method { name = "getMaxThreads"; returnType = IntType }.hook {
                    replaceTo(threads)
                }
                method { name = "getThreads"; returnType = IntType }.hook {
                    replaceTo(threads)
                }
            }
        }
    }
}
