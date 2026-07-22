package com.fosstool.app.hook.scope.screenshot

import android.graphics.Bitmap
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object EnablePNGSaveFormat : YukiBaseHooker() {

    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(StringClass.name)
                        addForType(Bitmap.CompressFormat::class.java.name)
                    }
                    methods {
                        add { name("values") }
                        add { returnType(StringClass.name) }
                        add { returnType(Bitmap.CompressFormat::class.java.name) }
                    }
                    usingStrings("image/jpeg", "image/png")
                }
            }.apply {
                checkDataList("EnablePNGSaveFormat")
                first().name.toClass().apply {
                    method { returnType = StringClass }.hookAll {
                        after {
                            result = when (result<String>()) {
                                "image/jpeg" -> "image/png"
                                ".jpg" -> ".png"
                                else -> return@after
                            }
                        }
                    }
                    method { returnType = Bitmap.CompressFormat::class.java }.hook {
                        after {
                            result = when (result<Bitmap.CompressFormat>()) {
                                Bitmap.CompressFormat.JPEG -> Bitmap.CompressFormat.PNG
                                else -> return@after
                            }
                        }
                    }
                }
            }
        }
    }
}
