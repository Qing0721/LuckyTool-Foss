package com.fosstool.app.hook.scope.pictorial

import android.graphics.Bitmap
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.BitmapClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.HandlerClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.FileClass
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

object RemoveImageSaveWaterMark : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(FileClass.name)
                        addForType(HandlerClass.name)
                        addForType(LongType.name)
                        addForType(BooleanType.name)
                        addForType(StringClass.name)
                    }
                    methods {
                        add { returnType(HandlerClass.name) }
                        add { returnType(BitmapClass.name) }
                        add { returnType(BooleanType.name) }
                        add { paramTypes(ContextClass.name) }
                        add { paramCount(5);returnType(BitmapClass.name) }
                        add { paramTypes("com.heytap.pictorial.core.bean.BasePictorialData") }
                    }
                }
            }.apply {
                checkDataList("RemoveImageSaveWaterMark")
                val cls = (firstOrNullSafe()?.name ?: return@apply).toClassOrNull(appClassLoader) ?: return@apply
                for (m in cls.declaredMethods) {
                    if (m.returnType == Bitmap::class.java && m.parameterCount == 4 &&
                        m.parameterTypes[0] == Boolean::class.javaPrimitiveType &&
                        m.parameterTypes[2] == Bitmap::class.java &&
                        m.parameterTypes[3] == Boolean::class.javaPrimitiveType
                    ) {
                        runCatching {
                            XposedBridge.hookMethod(m, object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    param.result = param.args.getOrNull(2) as? Bitmap ?: return
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}
