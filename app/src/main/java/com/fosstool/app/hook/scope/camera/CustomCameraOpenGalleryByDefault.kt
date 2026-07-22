package com.fosstool.app.hook.scope.camera

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs

object CustomCameraOpenGalleryByDefault : YukiBaseHooker() {
    override fun onHook() {
        val galleryPkg =
            prefs(ModulePrefs).getString("custom_camera_open_gallery_by_default", "") ?: ""
        if (galleryPkg.isBlank()) return

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    returnType(StringClass.name)
                    paramCount(0)
                    usingStrings("com.oplus.gallery.base")
                }
            }.apply {
                checkDataList("CustomCameraOpenGalleryByDefault")
                val member = first()
                member.className.toClass().apply {
                    method {
                        name = member.methodName
                        emptyParam()
                        returnType = StringClass
                    }.hook {
                        before {
                            result = galleryPkg
                        }
                    }
                }
            }
        }
    }
}
