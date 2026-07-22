package com.fosstool.app.hook.scope.launcher

import android.util.DisplayMetrics
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.DisplayMetricsClass
import com.highcapable.yukihookapi.hook.type.java.FloatType
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object FolderLayoutRowColume : YukiBaseHooker() {
    override fun onHook() {
        val columns = prefs(ModulePrefs).getInt("set_icon_columns_in_folder", 3)
        val rows = prefs(ModulePrefs).getInt("set_folder_icon_rows", -1).let {
            if (it >= 0) it else prefs(ModulePrefs).getInt("set_icon_rows_in_folder", 3)
        }
        val syncPreview =
            prefs(ModulePrefs).getBoolean("sync_folder_columns_to_preview", false) ||
                prefs(ModulePrefs).getBoolean("sync_folder_icon_column_number_preview", false)
        "com.android.launcher3.OplusDeviceProfile".toClass().apply {
            method {
                name = "updateOplusFolderCellSize"
                paramCount = 2
            }.hook {
                after {
                    val folderPageMarginLRDp = field {
                        name = "inv"
                        superClass()
                    }.get(instance).any()?.current()?.field {
                        name = "folderDisplayOption"
                    }?.any()?.current()?.field {
                        name = "folderPageMarginLRDp"
                    }?.float()
                    val metrics = field {
                        name = "mInfo"
                        superClass()
                    }.get(instance).any()?.current()?.field {
                        name = "metrics"
                    }?.cast<DisplayMetrics>()
                    val lrMargin = pxFromDp(folderPageMarginLRDp, metrics) ?: 0
                    val f = args().first().float()
                    val availableWidthPx = field {
                        name = "availableWidthPx"
                        superClass()
                    }.get(instance).int()
                    field {
                        name = "folderCellWidthPx"
                        superClass()
                    }.get(instance)
                        .set((((availableWidthPx - (lrMargin * 2)) / columns) * f).toInt())
                }
            }
        }
        "com.android.launcher3.InvariantDeviceProfile".toClass().apply {
            method {
                name = "initGrid"
                paramCount(3..4)
            }.hook {
                after {
                    field { name = "numFolderColumns" }.get(instance).set(columns)
                    field { name = "numFolderRows" }.get(instance).set(rows)
                }
            }
        }
        if (SDK < A13) return
        "com.android.launcher3.folder.big.BigFolderGridOrganizer".toClass().apply {
            method { name = "calculateGridSize" }.hook {
                after {
                    field {
                        name = "mCountX"
                        superClass()
                    }.get(instance).set(if (syncPreview) columns else 3)
                    field {
                        name = "mCountY"
                        superClass()
                    }.get(instance).set(if (syncPreview) rows else 3)
                }
            }
        }
    }

    private fun pxFromDp(float: Float?, displayMetrics: DisplayMetrics?): Int? {
        return "com.android.launcher3.ResourceUtils".toClass().method {
            name = "pxFromDp"
            param(FloatType, DisplayMetricsClass)
        }.get().invoke<Int>(float, displayMetrics)
    }
}
