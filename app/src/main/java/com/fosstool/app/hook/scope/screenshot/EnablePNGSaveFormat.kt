package com.fosstool.app.hook.scope.screenshot

import android.graphics.Bitmap
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

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
                val cls = (firstOrNullSafe()?.name ?: return@apply).toClassOrNull(appClassLoader) ?: return@apply
                for (m in cls.declaredMethods) {
                    when (m.returnType) {
                        String::class.java -> runCatching {
                            XposedBridge.hookMethod(m, object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    param.result = when (param.result as? String) {
                                        "image/jpeg" -> "image/png"
                                        ".jpg" -> ".png"
                                        else -> return
                                    }
                                }
                            })
                        }
                        Bitmap.CompressFormat::class.java -> runCatching {
                            XposedBridge.hookMethod(m, object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    param.result = when (param.result as? Bitmap.CompressFormat) {
                                        Bitmap.CompressFormat.JPEG -> Bitmap.CompressFormat.PNG
                                        else -> return
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}
