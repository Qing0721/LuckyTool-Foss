package com.fosstool.app.hook.scope.systemui

import android.view.View
import android.view.ViewGroup
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getScreenOrientation

object ControlCenterTiles : YukiBaseHooker() {
    var callback: ((key: String, value: String) -> Unit)? = null

    override fun onHook() {
        val isEnable = prefs(ModulePrefs).getBoolean("control_center_tile_enable", false)
        if (!isEnable) return
        if (SDK >= A13) loadHooker(ControlCenterTilesLayout)
        else loadHooker(ControlCenterTilesLayoutC12)
    }

    object ControlCenterTilesLayout : YukiBaseHooker() {
        override fun onHook() {
            val columnUnexpandedVerticalC13 =
                prefs(ModulePrefs).getInt("tile_unexpanded_columns_vertical_c13", 5)
            val rowExpandedVerticalC13 =
                prefs(ModulePrefs).getInt("tile_expanded_rows_vertical_c13", 3)
            val columnExpandedVerticalC13 =
                prefs(ModulePrefs).getInt("tile_expanded_columns_vertical_c13", 4)
            val columnHorizontal = prefs(ModulePrefs).getInt("tile_columns_horizontal_c13", 4)
            var mediaMode = prefs(ModulePrefs).getString("set_media_player_display_mode", "0")
            var autoExpandTile = SDK >= A14 && prefs(ModulePrefs).getBoolean(
                "auto_expand_tile_rows_horizontal", false
            )
            dataChannel.wait<Boolean>("auto_expand_tile_rows_horizontal") { autoExpandTile = it }

            callback = { k: String, v: String ->
                when (k) {
                    "set_media_player_display_mode" -> mediaMode = v
                }
            }

            "com.android.systemui.qs.QuickQSPanel".toClass().apply {
                method { name = "getNumQuickTiles" }.hook {
                    replaceTo(columnUnexpandedVerticalC13)
                }
            }

            "com.android.systemui.qs.TileLayout".toClass().apply {
                method { name = "updateMaxRows" }.hook {
                    before {
                        getScreenOrientation(instance<ViewGroup>()) {
                            val mRows = field { name = "mRows" }.get(instance).int()
                            val newRows = if (it) {
                                rowExpandedVerticalC13
                            } else {
                                if (autoExpandTile) {
                                    when (mediaMode) {
                                        "2" -> 2
                                        "3" -> {
                                            if (MediaPlayerPanel.getMediaData() == null) 2
                                            else return@getScreenOrientation
                                        }

                                        else -> return@getScreenOrientation
                                    }
                                } else return@getScreenOrientation
                            }
                            field { name = "mRows" }.get(instance).set(newRows)
                            result = mRows != newRows
                        }
                    }
                }
                method { name = "updateColumns" }.hook {
                    before {
                        instance<ViewGroup>().apply {
                            getScreenOrientation(this) {
                                val mColumns = field { name = "mColumns" }.get(instance).int()
                                val newColumns = if (it) columnExpandedVerticalC13
                                else columnHorizontal
                                field { name = "mColumns" }.get(instance).set(newColumns)
                                result = mColumns != newColumns
                            }
                        }
                    }
                }
            }
        }
    }

    object ControlCenterTilesLayoutC12 : YukiBaseHooker() {
        override fun onHook() {
            val columnUnexpandedVertical =
                prefs(ModulePrefs).getInt("tile_unexpanded_columns_vertical", 6)
            val columnUnexpandedHorizontal =
                prefs(ModulePrefs).getInt("tile_unexpanded_columns_horizontal", 6)
            val columnExpandedVertical =
                prefs(ModulePrefs).getInt("tile_expanded_columns_vertical", 4)
            val columnExpandedHorizontal =
                prefs(ModulePrefs).getInt("tile_expanded_columns_horizontal", 6)

            "com.android.systemui.qs.QuickQSPanel".toClass().apply {
                method { name = "getNumQuickTiles" }.hook {
                    before {
                        getScreenOrientation(instance<View>()) {
                            result = if (it) columnUnexpandedVertical
                            else columnUnexpandedHorizontal
                        }
                    }
                }
            }

            "com.android.systemui.qs.TileLayout".toClass().apply {
                method { name = "updateColumns" }.hook {
                    before {
                        instance<ViewGroup>().apply {
                            getScreenOrientation(this) {
                                val mColumns = field { name = "mColumns" }.get(instance).int()
                                val newColumns = if (it) columnExpandedVertical
                                else columnExpandedHorizontal
                                field { name = "mColumns" }.get(instance).set(newColumns)
                                result = mColumns != newColumns
                            }
                        }
                    }
                }
            }
        }
    }
}
