package com.fosstool.app.hook.scope.launcher

import android.util.DisplayMetrics
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

object FolderLayoutRowColume : YukiBaseHooker() {
    override fun onHook() {
        val columns = prefs(ModulePrefs).getInt("set_icon_columns_in_folder", 3)
        val rows = prefs(ModulePrefs).getInt("set_folder_icon_rows", -1).let {
            if (it >= 0) it else prefs(ModulePrefs).getInt("set_icon_rows_in_folder", 4)
        }
        val syncPreview =
            prefs(ModulePrefs).getBoolean("sync_folder_columns_to_preview", false) ||
                prefs(ModulePrefs).getBoolean("sync_folder_icon_column_number_preview", false)

        val odp = "com.android.launcher3.OplusDeviceProfile".toClassOrNull(appClassLoader)
        if (odp == null) {
            YLog.error("FolderLayoutRowColume: OplusDeviceProfile not found")
        } else {
            odp.method { name = "updateOplusFolderCellSize"; paramCount = 2 }
                .ignored()
                .hook {
                    after {
                        val inv = odp.findFieldCompat("inv")?.get(instance)
                        val folderDisplayOption = inv?.let {
                            runCatching {
                                var c: Class<*>? = it.javaClass
                                var f: Any? = null
                                while (c != null && f == null) {
                                    f = runCatching {
                                        c!!.getDeclaredField("folderDisplayOption")
                                            .apply { isAccessible = true }.get(it)
                                    }.getOrNull()
                                    c = c.superclass
                                }
                                f
                            }.getOrNull()
                        }
                        val folderPageMarginLRDp = folderDisplayOption?.let {
                            runCatching {
                                it.javaClass.getDeclaredField("folderPageMarginLRDp")
                                    .apply { isAccessible = true }.get(it) as? Float
                            }.getOrNull()
                        }
                        val metrics = odp.findFieldCompat("mInfo")?.get(instance)?.let { info ->
                            runCatching {
                                info.javaClass.getDeclaredField("metrics")
                                    .apply { isAccessible = true }.get(info) as? DisplayMetrics
                            }.getOrNull()
                        }
                        val lrMargin = pxFromDp(folderPageMarginLRDp, metrics) ?: 0
                        val f = (args.getOrNull(0) as? Float) ?: 1f
                        val availableWidthPx = odp.findFieldCompat("availableWidthPx")
                            ?.get(instance) as? Int ?: 0
                        odp.findFieldCompat("folderCellWidthPx")
                            ?.set(instance, (((availableWidthPx - (lrMargin * 2)) / columns) * f).toInt())
                    }
                }
        }

        val idp = "com.android.launcher3.InvariantDeviceProfile".toClassOrNull(appClassLoader)
        if (idp == null) {
            YLog.error("FolderLayoutRowColume: InvariantDeviceProfile not found")
        } else {
            idp.method { name = "initGrid" }.ignored().hookAll {
                after { applyFolderFields(idp, instanceOrNull, rows, columns, syncPreview, false) }
            }
        }

        val gridOption = "com.android.launcher3.InvariantDeviceProfile\$GridOption"
            .toClassOrNull(appClassLoader)
        if (gridOption == null) {
            YLog.error("FolderLayoutRowColume: InvariantDeviceProfile\$GridOption not found")
        } else {
            gridOption.constructor { paramCount(2..3) }.ignored().hookAll {
                after { applyFolderFields(gridOption, instanceOrNull, rows, columns, syncPreview, true) }
            }
        }

        val oidp = "com.android.launcher3.OplusInvariantDeviceProfile".toClassOrNull(appClassLoader)
        if (oidp == null) {
            YLog.error("FolderLayoutRowColume: OplusInvariantDeviceProfile not found")
        } else {
            oidp.method { name { it.startsWith("injectInitGrid") } }.ignored().hookAll {
                after { applyFolderFields(oidp, instanceOrNull, rows, columns, syncPreview, true) }
            }
        }

        val folderInfo = "com.android.launcher3.model.data.FolderInfo".toClassOrNull(appClassLoader)
        if (folderInfo == null) {
            YLog.error("FolderLayoutRowColume: FolderInfo not found")
        } else {
            listOf("getPreviewRow" to rows, "getPreviewColumn" to columns).forEach { (target, value) ->
                folderInfo.method { name = target; superClass() }.ignored().hook {
                    before {
                        if (!syncPreview) return@before
                        val host = instanceOrNull ?: return@before
                        val spanX = runCatching {
                            folderInfo.field { name = "spanX"; superClass() }.ignored().get(host).any() as? Int
                        }.getOrNull()
                        val spanY = runCatching {
                            folderInfo.field { name = "spanY"; superClass() }.ignored().get(host).any() as? Int
                        }.getOrNull()
                        if (spanX == 1 && spanY == 1) result = value
                    }
                }
            }
        }

        if (SDK < A13) return
        val organizer = "com.android.launcher3.folder.big.BigFolderGridOrganizer"
            .toClassOrNull(appClassLoader)
        if (organizer == null) {
            YLog.error("FolderLayoutRowColume: BigFolderGridOrganizer not found")
            return
        }
        organizer.method { name = "calculateGridSize" }
            .ignored()
            .hook {
                after {
                    organizer.findFieldCompat("mCountX")?.set(instance, if (syncPreview) columns else 3)
                    organizer.findFieldCompat("mCountY")?.set(instance, if (syncPreview) rows else 3)
                }
            }
    }

    private fun applyFolderFields(
        clazz: Class<*>,
        host: Any?,
        rows: Int,
        columns: Int,
        syncPreview: Boolean,
        withPreview: Boolean,
    ) {
        if (host == null) return
        runCatching {
            clazz.field { name = "numFolderRows"; superClass() }.ignored().get(host).set(rows)
        }
        runCatching {
            clazz.field { name = "numFolderColumns"; superClass() }.ignored().get(host).set(columns)
        }
        if (withPreview && syncPreview && columns > 3) {
            runCatching {
                clazz.field { name = "numFolderPreview"; superClass() }.ignored().get(host).set(columns)
            }
        }
    }

    private fun pxFromDp(float: Float?, displayMetrics: DisplayMetrics?): Int? {
        if (float == null || displayMetrics == null) return null
        return runCatching {
            XposedHelpers.callStaticMethod(
                "com.android.launcher3.ResourceUtils".toClassOrNull(appClassLoader),
                "pxFromDp",
                float,
                displayMetrics,
            ) as? Int
        }.getOrNull()
    }

    private fun Class<*>.findFieldCompat(name: String): Field? {
        var c: Class<*>? = this
        while (c != null && c != Any::class.java) {
            c.declaredFields.firstOrNull { it.name == name }?.let { return it.apply { isAccessible = true } }
            c = c.superclass
        }
        return null
    }
}
