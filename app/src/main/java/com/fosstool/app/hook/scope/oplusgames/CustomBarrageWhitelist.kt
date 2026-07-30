package com.fosstool.app.hook.scope.oplusgames

import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object CustomBarrageWhitelist : YukiBaseHooker() {
    private const val ENABLED = "1"
    private const val TARGET_CLASS = "com.coloros.gamespaceui.module.barrage.GameBarrageUtil"

    override fun onHook() {
        val whitelist = prefs(ModulePrefs)
            .getStringSet("custom_barrage_notification_whitelist_list", emptySet())
            ?.filter { it.isNotBlank() }
            ?.toSet()
            .orEmpty()
        if (whitelist.isEmpty()) return

        val clazz = TARGET_CLASS.toClassOrNull(appClassLoader) ?: return

        clazz.method { name = "initAppState" }.ignored().hook {
            before {
                val map = applicationState(clazz) ?: return@before
                if (map.isEmpty() || map.size != whitelist.size) {
                    whitelist.forEach { pkg -> if (!map.containsKey(pkg)) map[pkg] = ENABLED }
                    runCatching {
                        clazz.method { name = "setGameBarrageApplicationState" }
                            .ignored().get().call(map)
                    }
                }
                result = map
            }
        }

        clazz.method { name = "getGameBarrageAppSwitchMap" }.ignored().hook {
            before {
                val state = applicationState(clazz) ?: return@before
                val map = HashMap<String, Any?>()
                whitelist.forEach { pkg -> map[pkg] = state[pkg] ?: ENABLED }
                result = map
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun applicationState(clazz: Class<*>): HashMap<String, Any?>? = runCatching {
        clazz.method { name = "getGameBarrageApplicationState" }
            .ignored().get().invoke<Any>() as? HashMap<String, Any?>
    }.getOrNull()
}
