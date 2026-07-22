package com.fosstool.app.hook.scope.camera

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.fosstool.app.utils.ModulePrefs

object RemoveFilterModelLimit : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.ocs.camera.ipusdk.processunit.filter.list.SystemUtil".toClass().apply {
            method {
                name = "isMarketNameContainSeriesNum"
                emptyParam()
                returnType = BooleanType
            }.hook {
                replaceToFalse()
            }
        }
    }
}
