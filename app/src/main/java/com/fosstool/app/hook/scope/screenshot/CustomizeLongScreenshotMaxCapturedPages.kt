package com.fosstool.app.hook.scope.screenshot

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge

object CustomizeLongScreenshotMaxCapturedPages : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fieldCount(0)
                    methods {
                        add { returnType(IntType.name) }
                        add { returnType(BooleanType.name) }
                        add {
                            paramTypes(IntType.name, IntType.name)
                            returnType(IntType.name)
                        }
                    }
                    usingStrings("StitchLimitUtils")
                }
            }.apply {
                checkDataList("CustomizeLongScreenshotMaxCapturedPages")
                val cls = (firstOrNullSafe()?.name ?: return@apply).toClassOrNull(appClassLoader) ?: return@apply
                for (m in cls.declaredMethods) {
                    when {
                        m.parameterCount == 2 &&
                            m.parameterTypes[1] == Int::class.javaPrimitiveType &&
                            m.returnType == Boolean::class.javaPrimitiveType ->
                            runCatching { XposedBridge.hookMethod(m, XC_MethodReplacement.returnConstant(false)) }
                        m.parameterCount == 3 &&
                            m.parameterTypes[1] == Int::class.javaPrimitiveType &&
                            m.parameterTypes[2] == Int::class.javaPrimitiveType &&
                            m.returnType == Int::class.javaPrimitiveType ->
                            runCatching { XposedBridge.hookMethod(m, XC_MethodReplacement.returnConstant(-1)) }
                    }
                }
            }
        }
    }
}
