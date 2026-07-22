package com.fosstool.app.hook.scope.android

import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.method

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

        val backLightBeanCls = runCatching {
            "com.oplus.vrr.bean.BackLightBean".toClass()
        }.getOrNull()

        if (enableForce1Hz) {
            runCatching {
                "com.oplus.vrr.OPlusFeatureManager".toClass().apply {
                    if (backLightBeanCls != null) {
                        method {
                            name { it.contains("on", ignoreCase = true) }
                            param { params -> params.any { it == backLightBeanCls } }
                        }.hookAll {
                            before {
                                val bean = (0 until 4).mapNotNull { i ->
                                    runCatching { args(i).any() }.getOrNull()
                                }.firstOrNull { it != null && backLightBeanCls.isInstance(it) }
                                    ?: return@before
                                forceNitsToMinFps(bean)
                            }
                        }
                    } else {
                        method {
                            name { it.contains("on", ignoreCase = true) }
                            paramCount(1..3)
                        }.hookAll {
                            before {
                                val bean = (0 until 4).mapNotNull { i ->
                                    runCatching { args(i).any() }.getOrNull()
                                }.firstOrNull {
                                    it != null && it.javaClass.name.contains("BackLightBean")
                                } ?: return@before
                                forceNitsToMinFps(bean)
                            }
                        }
                    }
                }
            }
        }

        runCatching {
            "com.oplus.vrr.OPlusOnlineConfigManager".toClass().apply {
                method {
                    name = "createGameEvent"
                }.hookAll {
                    after {
                        val host = instance ?: return@after
                        disableBackLightBean(host, "mBackLightBean")
                        disableBackLightBean(host, "mPwmBackLightBean")
                    }
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun forceNitsToMinFps(bean: Any) {
        runCatching {
            val map = bean.current().field { name = "mNitsToMinFPS" }.cast<HashMap<*, *>>()
                ?: return
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
            val bean = host.current().field { name = fieldName }.any() ?: return
            bean.current().field { name = "mEnable" }.set(false)
            bean.current().field { name = "mNitsToMinFPS" }.cast<HashMap<*, *>>()?.clear()
        }
    }
}
