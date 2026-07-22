package com.fosstool.app.hook.scope.gallery

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.CharSequenceClass
import com.highcapable.yukihookapi.hook.type.java.IntType

object RemoveGalleryWatermarkWordLimit : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 27) return
        if (getOSVersionCode >= 30) return
        if (!prefs(ModulePrefs).getBoolean("remove_gallery_watermark_word_limit", false)) return

        DexkitUtils.create(appInfo.sourceDir) { bridge ->
            runCatching {
                bridge.findMethod {
                    matcher {
                        name = "filter"
                        returnType = CharSequenceClass.name
                        paramTypes(
                            CharSequenceClass.name,
                            IntType.name,
                            IntType.name,
                            "android.text.Spanned",
                            IntType.name,
                            IntType.name
                        )
                    }
                }.apply {
                    checkDataList("RemoveGalleryWatermarkWordLimit")
                    first().apply {
                        className.toClass().method {
                            name = "filter"
                            returnType = CharSequenceClass
                            param(
                                CharSequenceClass,
                                IntType,
                                IntType,
                                android.text.Spanned::class.java,
                                IntType,
                                IntType
                            )
                        }.hook {
                            before {
                                result = args().first().any()
                            }
                        }
                    }
                }
            }
        }
    }
}
