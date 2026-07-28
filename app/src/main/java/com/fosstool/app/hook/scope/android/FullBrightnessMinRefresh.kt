package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object FullBrightnessMinRefresh : YukiBaseHooker() {
    override fun onHook() {
        val ltpoMode = prefs(ModulePrefs).getString("set_ltpo_refresh_rate_mode", "0") ?: "0"
        if (ltpoMode != "1") return

        val enableForce1Hz =
            prefs(ModulePrefs).getBoolean("enable_full_brightness_min_refresh_1", false) ||
                prefs(ModulePrefs).getBoolean(
                    "enable_full_brightness_refresh_rate_minimum_one",
                    false
                )

        val backLightBeanCls = "com.oplus.vrr.bean.BackLightBean".toClassOrNull(appClassLoader)

        if (enableForce1Hz) {
            "com.oplus.vrr.OPlusFeatureManager".toClassOrNull(appClassLoader)?.let { mgr ->

                val methods = mgr.declaredMethods.filter { m ->
                    m.name.startsWith("on") &&
                        m.parameterTypes.size == 1 &&
                        m.parameterTypes.any {
                            it == backLightBeanCls || it.name.contains("BackLightBean")
                        }
                }
                methods.forEach { m ->
                    XposedBridge.hookMethod(m, object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam) {
                            val bean = param.args.firstOrNull { arg ->
                                arg != null && (
                                    (backLightBeanCls != null && backLightBeanCls.isInstance(arg)) ||
                                        arg.javaClass.name.contains("BackLightBean")
                                    )
                            } ?: return
                            forceNitsToMinFps(bean)
                        }
                    })
                }
            }
        }

        "com.oplus.vrr.OPlusOnlineConfigManager".toClassOrNull(appClassLoader)?.let { cfg ->
            cfg.method { name = "createGameEvent" }.ignored().hook {
                after {
                    val host = instance
                    disableBackLightBean(host, "mBackLightBean")
                    disableBackLightBean(host, "mPwmBackLightBean")
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun forceNitsToMinFps(bean: Any) {
        runCatching {
            val map = softFieldOrGet(bean, "mNitsToMinFPS") as? HashMap<*, *> ?: return
            for ((key, value) in map.entries.toList()) {
                val list = value as? ArrayList<*> ?: continue
                for (inner in list) {
                    val innerMap = inner as? HashMap<Any?, Any?> ?: continue
                    for (k in innerMap.keys.toList()) {
                        if (k is Float || k is Number) {
                            innerMap[k] = 1.0f
                        }
                    }
                }
                (map as HashMap<Any?, Any?>)[key] = list
            }
        }
    }

    private fun disableBackLightBean(host: Any, fieldName: String) {
        runCatching {
            val bean = softFieldOrGet(host, fieldName) ?: return
            XposedHelpers.setBooleanField(bean, "mEnable", false)
            (softFieldOrGet(bean, "mNitsToMinFPS") as? HashMap<*, *>)?.clear()
        }
    }

    private fun softFieldOrGet(obj: Any, fieldName: String): Any? {
        return runCatching {
            obj.javaClass.field { name = fieldName }.ignored().get(obj).any()
        }.getOrNull() ?: runCatching { XposedHelpers.getObjectField(obj, fieldName) }.getOrNull()
    }
}
