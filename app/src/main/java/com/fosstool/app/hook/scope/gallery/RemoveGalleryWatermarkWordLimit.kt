package com.fosstool.app.hook.scope.gallery

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

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
                        returnType = "java.lang.CharSequence"
                        paramTypes(
                            "java.lang.CharSequence",
                            "int",
                            "int",
                            "android.text.Spanned",
                            "int",
                            "int"
                        )
                        usingNumbers(0, 1, 2)
                        usingStrings("")
                    }
                }.apply {
                    checkDataList("RemoveGalleryWatermarkWordLimit")
                    firstOrNullSafe()?.apply {
                        className.toClassOrNull(appClassLoader)
                            ?.method { name = "filter"; paramCount = 6 }
                            ?.ignored()
                            ?.hook {
                                before {
                                    result = args.getOrNull(0)
                                }
                            }
                    }
                }
            }
        }
    }
}
