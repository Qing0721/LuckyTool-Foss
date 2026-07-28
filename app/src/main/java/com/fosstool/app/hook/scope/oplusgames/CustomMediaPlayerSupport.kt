package com.fosstool.app.hook.scope.oplusgames

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.replaceSpace
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object CustomMediaPlayerSupport : YukiBaseHooker() {
    override fun onHook() {
        val customList = loadCustomList()
        if (customList.isEmpty()) return

        val classNames = listOf(
            "business.module.media.MediaSessionHelper",
            "com.oplus.games.musicplayer.main.MediaSessionHelper",
        )
        for (cn in classNames) {
            runCatching {
                cn.toClassOrNull(appClassLoader)?.apply {
                    method {
                        emptyParam()
                        returnType = ListClass
                    }.hook {
                        after {
                            val list = result<List<String>>() ?: return@after
                            result = list.toMutableList().apply { addAll(customList) }
                        }
                    }
                }
            }
        }
    }

    private fun loadCustomList(): List<String> {
        val set = prefs(ModulePrefs).getStringSet("custom_media_player_support_list", emptySet())
            ?.filter { it.isNotBlank() }
            .orEmpty()
        if (set.isNotEmpty()) return set.toList()

        val legacy = prefs(ModulePrefs).getString("custom_media_player_support", "None")
        if (legacy.isBlank() || legacy == "None") return emptyList()
        val normalized = legacy.replaceSpace
        return if (normalized.contains("\n")) {
            normalized.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        } else {
            listOf(legacy.trim()).filter { it.isNotBlank() }
        }
    }
}
