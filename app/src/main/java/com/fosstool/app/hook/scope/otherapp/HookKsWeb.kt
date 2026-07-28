package com.fosstool.app.hook.scope.otherapp

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.SharedPreferencesClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

object HookKsWeb : YukiBaseHooker() {
    override fun onHook() {
        val isPro = prefs(ModulePrefs).getBoolean("ksweb_remove_check_license", false)
        if (!isPro) return
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(IntType.name)
                        addForType(BooleanType.name)
                        addForType(SharedPreferencesClass.name)
                    }
                    methods {
                        add { paramCount(0);returnType(IntType.name) }
                        add { paramCount(0);returnType(BooleanType.name) }
                        add { paramTypes(IntType.name);returnType(UnitType.name) }
                        add { paramTypes(ContextClass.name);returnType(UnitType.name) }
                    }
                    usingStrings(
                        "EXTEND TO PRO VERSION",
                        "CHECK SERIAL KEY",
                        "KSWEB PRO",
                        "KSWEB STANDARD"
                    )
                }
            }.apply {
                checkDataList("HookKsWeb")
                val cls = (firstOrNullSafe()?.name ?: return@apply).toClassOrNull(appClassLoader) ?: return@apply
                for (m in cls.declaredMethods) {
                    if (m.parameterCount == 0 && m.returnType == Boolean::class.javaPrimitiveType) {
                        runCatching {
                            XposedBridge.hookMethod(m, object : XC_MethodHook() {
                                override fun beforeHookedMethod(param: MethodHookParam) {

                                    cls.declaredFields.firstOrNull {
                                        it.type == Boolean::class.javaPrimitiveType ||
                                            it.type == Boolean::class.java
                                    }?.let { f ->
                                        runCatching {
                                            f.isAccessible = true
                                            f.set(param.thisObject, true)
                                        }
                                    }
                                    cls.declaredFields.firstOrNull {
                                        it.type == Int::class.javaPrimitiveType ||
                                            it.type == Int::class.java
                                    }?.let { f ->
                                        runCatching {
                                            f.isAccessible = true
                                            f.set(param.thisObject, 2)
                                        }
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
