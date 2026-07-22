package com.fosstool.app.hook.scope.oplusgames

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveStartupAnimation : YukiBaseHooker() {
    private const val TARGET_CLASS = "business.secondarypanel.view.GameOptimizedNewView"

    override fun onHook() {
        var hooked = false
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    usingStrings("startAnimationIn")
                    paramCount(0)
                }
            }.apply {
                val targets = filter {
                    it.className == TARGET_CLASS || it.className.endsWith(".GameOptimizedNewView")
                }.ifEmpty { this }
                if (targets.isNotEmpty()) {
                    checkDataList("RemoveStartupAnimation find startAnimationIn", onlyOne = false)
                    val m = targets.first()
                    m.className.toClass().method {
                        name = m.methodName
                        emptyParam()
                    }.hook { intercept() }
                    hooked = true
                }
            }
        }
        if (hooked) return
        runCatching {
            TARGET_CLASS.toClass().apply {
                method {
                    name { it.contains("startAnimation", ignoreCase = true) }
                    emptyParam()
                }.hookAll { intercept() }
            }
        }
    }
}
