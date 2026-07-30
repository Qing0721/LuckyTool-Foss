package com.fosstool.app.hook.scope.gallery

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.StringClass

object ReplaceOnePlusModelWatermark : YukiBaseHooker() {
    override fun onHook() {
        val waterMark = prefs(ModulePrefs).getString("custom_model_watermark", "None")

        val watermarkContent =
            "com.oplus.tbluniformeditor.plugins.watermark.data.WatermarkContent"
                .toClassOrNull(appClassLoader) ?: return
        watermarkContent.method { name = "getMake" }.ignored().hook { replaceTo("") }

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    addFieldForType(StringClass.name)
                    addMethod {
                        paramCount(0)
                        returnType(StringClass.name)
                    }
                    usingStrings(
                        "MarketNameInfo",
                        "ro.vendor.oplus.market.name",
                        "ro.vendor.oplus.market.enname"
                    )
                }
            }.apply {
                checkDataList("ReplaceOnePlusModelWatermark MarketNameInfo")
                val clazz = firstOrNullSafe()?.name?.toClassOrNull(appClassLoader) ?: return@apply
                clazz.method {
                    emptyParam()
                    returnType = StringClass
                }.ignored().hookAll {
                    before {

                        if (waterMark.isNotBlank() && waterMark != "None") result = waterMark
                    }
                }
            }
        }
    }
}
