package com.fosstool.app.hook.scope.camera

import android.net.Uri
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs

object CustomCameraOpenGalleryByDefault : YukiBaseHooker() {
    override fun onHook() {
        val galleryPkg =
            prefs(ModulePrefs).getString("custom_camera_open_gallery_by_default", "") ?: ""
        if (galleryPkg.isBlank()) return

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            val classes = dexKitBridge.findClass {
                matcher {
                    addFieldForType(Uri::class.java.name)
                    addFieldForType(BooleanType.name)
                    addMethod {
                        paramCount(0)
                        returnType(StringClass.name)
                    }
                    usingStrings("content://com.color.provider.removableapp", "removableapp")
                }
            }.checkDataList("CustomCameraOpenGalleryByDefault Clazz")

            if (classes.isEmpty()) return@create

            dexKitBridge.findMethod {
                searchInClass(classes)
                matcher {
                    returnType(StringClass.name)
                    paramCount(0)
                    usingStrings("com.oplus.gallery.base")
                }
            }.apply {
                checkDataList("CustomCameraOpenGalleryByDefault Method")
                val member = firstOrNullSafe() ?: return@apply
                member.className.toClassOrNull(appClassLoader)?.apply {
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
