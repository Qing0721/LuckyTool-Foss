package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveControlCenterUserSwitcher : YukiBaseHooker() {
    override fun onHook() {
        "com.oplusos.systemui.qs.OplusQSFooterImpl"
            .toClassOrNull(appClassLoader)
            ?.method { name = "showUserSwitcher"; paramCount = 0 }?.ignored()?.hook { replaceToFalse() }
    }
}
