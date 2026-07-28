package com.fosstool.app.hook.scope.camera

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveFilterModelLimit : YukiBaseHooker() {
    override fun onHook() {

        "com.oplus.ocs.camera.ipusdk.processunit.filter.list.SystemUtil".toClassOrNull(appClassLoader)
            ?.method { name = "isMarketNameContainSeriesNum" }
            ?.ignored()
            ?.hook { replaceToTrue() }
    }
}
