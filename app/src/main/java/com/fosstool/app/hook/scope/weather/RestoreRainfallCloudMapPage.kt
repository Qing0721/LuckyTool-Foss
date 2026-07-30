package com.fosstool.app.hook.scope.weather

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RestoreRainfallCloudMapPage : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.weather.indexoperations.IndexOperationsManager".toClassOrNull(appClassLoader)
            ?.method { name = "supportIndexOperationsFeature" }
            ?.ignored()
            ?.hook { replaceToFalse() }
    }
}
