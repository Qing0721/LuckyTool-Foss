package com.fosstool.app.hook.scope.oplusgames

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import org.luckypray.dexkit.query.enums.StringMatchType

object RemoveStartupAnimation : YukiBaseHooker() {
    private const val TARGET_CLASS = "business.secondarypanel.view.GameOptimizedNewView"

    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            val classes = dexKitBridge.findClass {
                matcher {
                    className(TARGET_CLASS, StringMatchType.Equals)
                }
            }.checkDataList("RemoveStartupAnimation find GameOptimizedNewView")
            if (classes.isEmpty()) return@create

            dexKitBridge.findMethod {
                searchInClass(classes)
                matcher {
                    paramCount(0)
                    usingStrings("startAnimationIn")
                }
            }.apply {
                checkDataList("RemoveStartupAnimation find startAnimationIn")
                val m = firstOrNullSafe() ?: return@apply
                m.className.toClassOrNull(appClassLoader)
                    ?.method { name = m.methodName; emptyParam() }
                    ?.ignored()
                    ?.hook { intercept() }
            }
        }
    }
}
