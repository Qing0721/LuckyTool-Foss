package com.fosstool.app.hook.scope.systemui

import android.content.ContentResolver
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

object RemoveStatusBarDevMode : YukiBaseHooker() {

    private const val FEATURE = "com.android.systemui.send_developer_mode_notification"

    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    methods {
                        add { paramTypes(ContentResolver::class.java.name, null) }
                        add {
                            usingStrings("featurename")
                            returnType("android.database.Cursor")
                        }
                    }
                    usingStrings("content://com.oplus.customize.coreapp.configmanager.configprovider.AppFeatureProvider")
                }
            }.apply {

                checkDataList("AppFeatureProviderUtils")
                val clazz = (firstOrNullSafe()?.name ?: return@apply)
                    .toClassOrNull(appClassLoader) ?: return@apply
                hookFeatureQueries(clazz)
            }
        }
    }

    private fun hookFeatureQueries(clazz: Class<*>) {
        clazz.declaredMethods.forEach { m ->
            val p = m.parameterTypes
            if (p.isEmpty() || !ContentResolver::class.java.isAssignableFrom(p[0])) return@forEach
            if (m.returnType != Boolean::class.javaPrimitiveType &&
                m.returnType != java.lang.Boolean::class.java
            ) return@forEach
            val keyIndex = when {
                p.size == 2 && p[1] == String::class.java -> 1
                p.size == 3 && p[1] == String::class.java &&
                    (p[2] == Boolean::class.javaPrimitiveType ||
                        p[2] == java.lang.Boolean::class.java) -> 1
                p.size == 3 && p[2] == String::class.java -> 2
                else -> return@forEach
            }
            runCatching {
                XposedBridge.hookMethod(m, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if ((param.args.getOrNull(keyIndex) as? String) == FEATURE) {
                            param.result = false
                        }
                    }
                })
            }
        }
    }
}
