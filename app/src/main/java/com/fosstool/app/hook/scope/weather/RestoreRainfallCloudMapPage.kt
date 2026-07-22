package com.fosstool.app.hook.scope.weather

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType

object RestoreRainfallCloudMapPage : YukiBaseHooker() {
    override fun onHook() {
        runCatching {
            "com.oplus.weather.indexoperations.IndexOperationsManager".toClass().apply {
                method {
                    name = "supportIndexOperationsFeature"
                    returnType = BooleanType
                }.hook { replaceToFalse() }
            }
        }
    }
}
