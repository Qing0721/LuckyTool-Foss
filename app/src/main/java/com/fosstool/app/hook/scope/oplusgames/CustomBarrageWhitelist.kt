package com.fosstool.app.hook.scope.oplusgames

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.ModulePrefs

object CustomBarrageWhitelist : YukiBaseHooker() {
    private const val ENABLED = "1"

    override fun onHook() {
        val whitelist = prefs(ModulePrefs)
            .getStringSet("custom_barrage_notification_whitelist_list", emptySet())
            ?.filter { it.isNotBlank() }
            ?.toSet()
            .orEmpty()
        if (whitelist.isEmpty()) return

        val classNames = listOf(
            "com.coloros.gamespaceui.module.barrage.GameBarrageUtil",
            "com.oplus.games.business.barrage.utils.GameBarrageUtil",
            "com.oplus.games.barrage.GameBarrageUtil",
        )

        for (clsName in classNames) {
            runCatching {
                clsName.toClass().apply {
                    method {
                        name { n ->
                            n == "getGameBarrageApplicationState" ||
                                n == "getGameBarrageAppSwitchMap"
                        }
                    }.hookAll {
                        after {
                            val raw = result ?: return@after
                            @Suppress("UNCHECKED_CAST")
                            when (raw) {
                                is MutableMap<*, *> -> {
                                    val map = raw as MutableMap<String, Any?>
                                    for (pkg in whitelist) map[pkg] = ENABLED
                                }
                                is Map<*, *> -> {
                                    val map = HashMap<String, Any?>()
                                    for ((k, v) in raw) {
                                        if (k is String) map[k] = v
                                    }
                                    for (pkg in whitelist) map[pkg] = ENABLED
                                    result = map
                                }
                            }
                        }
                    }
                    method {
                        name = "setGameBarrageApplicationState"
                    }.hookAll {
                        before {
                            @Suppress("UNCHECKED_CAST")
                            val map = runCatching {
                                args().first().cast<MutableMap<String, Any?>>()
                            }.getOrNull() ?: return@before
                            for (pkg in whitelist) map[pkg] = ENABLED
                        }
                    }
                    method {
                        name = "initAppState"
                    }.hookAll {
                        after {
                        }
                    }
                    method {
                        param(StringClass)
                        returnType = BooleanType
                    }.hookAll {
                        after {
                            val pkg = runCatching { args().first().string() }.getOrNull()
                            if (pkg != null && pkg in whitelist) result = true
                        }
                    }
                }
            }
        }
    }
}
