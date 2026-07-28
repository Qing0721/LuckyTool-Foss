package com.fosstool.app.hook.scope.launcher

import android.util.Pair
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object LauncherLayoutRowColume : YukiBaseHooker() {
    override fun onHook() {
        val maxRows = prefs(ModulePrefs).getInt("launcher_layout_max_rows", 6)
        val maxColumns = prefs(ModulePrefs).getInt("launcher_layout_max_columns", 4)

        val uiConfig = "com.android.launcher.UiConfig".toClassOrNull(appClassLoader)
        if (uiConfig == null) {
            YLog.error("LauncherLayoutRowColume: UiConfig not found")
        } else {
            uiConfig.method { name = "isSupportLayout" }.ignored().hook { replaceToTrue() }
        }

        if (getOSVersionCode >= 37) {
            if (uiConfig == null) return
            uiConfig.method { name = "getSupportLayout" }.ignored().hook {
                before {
                    val list = ArrayList<Pair<Int, Pair<Int, Int>>>()
                    for (column in 4..maxColumns) {
                        for (row in 6..maxRows) {
                            list.add(Pair(column, Pair(row, row + 1)))
                        }
                    }
                    result = list
                }
            }
            return
        }

        val adapter = "com.android.launcher.togglebar.adapter.ToggleBarLayoutAdapter"
            .toClassOrNull(appClassLoader)
        if (adapter == null) {
            YLog.error("LauncherLayoutRowColume: ToggleBarLayoutAdapter not found")
            return
        }
        adapter.method { name = "initToggleBarLayoutConfigs" }.ignored().hook {
            before {
                runCatching {
                    adapter.field { name = "MIN_MAX_COLUMN"; superClass() }
                        .ignored().get().cast<IntArray>()?.set(1, maxColumns)
                }
                runCatching {
                    adapter.field { name = "MIN_MAX_ROW"; superClass() }
                        .ignored().get().cast<IntArray>()?.set(1, maxRows)
                }
            }
        }
    }
}
