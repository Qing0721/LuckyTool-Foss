package com.fosstool.app.hook.scope.systemui

import android.view.View
import android.view.ViewGroup
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import com.fosstool.app.utils.getScreenOrientation
import com.fosstool.app.utils.safeOfNull
import android.annotation.SuppressLint
import android.widget.LinearLayout

object FixTileAlignBothSides : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode <= 26) loadHooker(HookTileAlignVertical)
        loadHooker(HookTileAlignHorizontal)
    }

    private object HookTileAlignVertical : YukiBaseHooker() {
        @SuppressLint("DiscouragedApi")
        override fun onHook() {
            "com.android.systemui.qs.QuickStatusBarHeader"
                .toClassOrNull(appClassLoader)?.let { c ->
                    c.method { name = "updateHeadersPadding" }.ignored().hook {
                        after {
                            val header = c.findField("mHeaderQsPanel")?.get(instance) as? LinearLayout
                                ?: return@after
                            val qsHeaderPanelSidePadding = safeOfNull {
                                header.resources.getDimensionPixelSize(
                                    header.resources.getIdentifier(
                                        "qs_header_panel_side_padding", "dimen",
                                        HookTileAlignVertical.packageName
                                    )
                                )
                            } ?: return@after
                            header.setViewPadding(qsHeaderPanelSidePadding)
                        }
                    }
                }
        }

        private fun Class<*>.findField(name: String): java.lang.reflect.Field? {
            var cls: Class<*>? = this
            while (cls != null) {
                runCatching { return cls.getDeclaredField(name).also { it.isAccessible = true } }
                cls = cls.superclass
            }
            return null
        }
    }

    private object HookTileAlignHorizontal : YukiBaseHooker() {
        @SuppressLint("DiscouragedApi")
        override fun onHook() {
            val isCustomTile = prefs(ModulePrefs).getBoolean("control_center_tile_enable", false)
            val columnHorizontal = prefs(ModulePrefs).getInt("tile_columns_horizontal_c13", 4)

            val helperCls = VariousClass(
                "com.oplusos.systemui.qs.helper.QSFragmentHelper",
                "com.oplus.systemui.qs.helper.QSFragmentHelper"
            ).toClassOrNull(appClassLoader)
            if (helperCls == null) {
                YLog.error("FixTileAlignBothSides: QSFragmentHelper not found", tag = "LuckyTool")
                return
            }
            VariousClass(
                "com.android.systemui.qs.QSFragment",
                "com.oplus.systemui.qs.OplusQSFragment",
                "com.oplus.systemui.qs.OplusQSImpl"
            ).toClassOrNull(appClassLoader)?.let { c ->
                c.method { name = "updateQsState" }.ignored().hook {
                    after {
                        val helper = runCatching {
                            helperCls.getDeclaredMethod("getInstance")
                                .also { it.isAccessible = true }.invoke(null)
                        }.getOrNull() ?: return@after
                        val scrollView =
                            helperCls.findField("mQSPanelScrollView")?.get(helper) as? ViewGroup
                                ?: return@after
                        getScreenOrientation(scrollView) {
                            if (it) scrollView.setViewPadding(0)
                            else {
                                val qsBrightnessMirrorSidePadding =
                                    safeOfNull {
                                        scrollView.resources.getDimensionPixelSize(
                                            scrollView.resources.getIdentifier(
                                                "qs_brightness_mirror_side_padding",
                                                "dimen",
                                                HookTileAlignHorizontal.packageName
                                            )
                                        )
                                    } ?: return@getScreenOrientation
                                if (isCustomTile && columnHorizontal > 4) scrollView.setViewPadding(
                                    qsBrightnessMirrorSidePadding
                                )
                            }
                        }
                    }
                }
            }
        }

        private fun Class<*>.findField(name: String): java.lang.reflect.Field? {
            var cls: Class<*>? = this
            while (cls != null) {
                runCatching { return cls.getDeclaredField(name).also { it.isAccessible = true } }
                cls = cls.superclass
            }
            return null
        }
    }

    private fun View.setViewPadding(leftAndRight: Int) {
        setPadding(
            leftAndRight, paddingTop,
            leftAndRight, paddingBottom
        )
    }
}
