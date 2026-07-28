package com.fosstool.app.hook.scope.systemui

import android.content.res.Configuration
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.hook.utils.OplusBuildUtlils
import com.fosstool.app.utils.ModulePrefs
import java.lang.reflect.Field

object SpecialTileGaps : YukiBaseHooker() {
    override fun onHook() {
        hookSpecialTileGaps()
        hookDecreaseHorizontalBrightnessBarTopGap()
    }

    private fun hookSpecialTileGaps() {
        val switchOn = prefs(ModulePrefs).getBoolean(
            "control_center_custom_gaps_for_special_tile_switch",
            false
        ) || prefs(ModulePrefs).getBoolean(
            "control_center_custom_gaps_for_special_tile",
            false
        )
        if (!switchOn) return

        val topGap = prefs(ModulePrefs).getInt("custom_special_tile_top_gap", -1).let {
            if (it >= 0) it
            else prefs(ModulePrefs).getInt("control_center_special_tile_top_gap", 10)
        }
        val bottomGap = prefs(ModulePrefs).getInt("custom_special_tile_bottom_gap", -1).let {
            if (it >= 0) it
            else prefs(ModulePrefs).getInt("control_center_special_tile_bottom_gap", 0)
        }

        val controllers = listOf(
            "com.oplusos.systemui.qs.OplusQSTileMediaContainerController",
            "com.oplus.systemui.qs.OplusQSTileMediaContainerController",
        )
        for (cls in controllers) {
            runCatching {
                cls.toClassOrNull(appClassLoader)
                    ?.method { name = "updateResources" }?.ignored()?.hook {
                        after {
                            val host = instance ?: return@after
                            runCatching {
                                host.javaClass.findField("mTopGap")?.set(host, topGap)
                            }
                            runCatching {
                                host.javaClass.findField("mBottomGap")?.set(host, bottomGap)
                            }
                        }
                    }
            }
        }

        listOf(
            "com.oplus.systemui.qs.OplusQSTileMediaContainer",
            "com.oplusos.systemui.qs.OplusQSTileMediaContainer",
        ).forEach { cls ->
            runCatching {
                cls.toClassOrNull(appClassLoader)
                    ?.method { name = "updateResources" }?.ignored()?.hook {
                        after {
                            val host = instance ?: return@after
                            runCatching {
                                host.javaClass.findField("mTopGap")?.set(host, topGap)
                            }
                            runCatching {
                                host.javaClass.findField("mBottomGap")?.set(host, bottomGap)
                            }
                        }
                    }
            }
        }
    }

    private fun hookDecreaseHorizontalBrightnessBarTopGap() {
        val enabled = prefs(ModulePrefs).getBoolean(
            "decrease_horizontal_brightness_bar_top_gap", false
        )
        if (!enabled) return

        val os = try {
            OplusBuildUtlils().getOSVersionCode ?: 0
        } catch (_: Throwable) {
            0
        }
        if (os < 30) return

        runCatching {
            "com.oplus.systemui.qs.OplusQSBottomImpl"
                .toClassOrNull(appClassLoader)
                ?.method { name = "updateResources" }?.ignored()?.hook {
                    after {
                        val host = instance ?: return@after
                        val pageIndicator = runCatching {
                            host.javaClass.findField("mPageIndicator")?.get(host) as? View
                        }.getOrNull() ?: return@after
                        applyLandscapePageIndicatorBottomMargin(pageIndicator)
                    }
                }
        }
    }

    private fun applyLandscapePageIndicatorBottomMargin(view: View) {
        val orientation = view.resources.configuration.orientation
        if (orientation != Configuration.ORIENTATION_LANDSCAPE) return
        val lp = view.layoutParams as? LinearLayout.LayoutParams ?: return
        val margin6dp = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 6f, view.resources.displayMetrics
        ).toInt()
        if (lp.bottomMargin != margin6dp) {
            lp.bottomMargin = margin6dp
            view.layoutParams = lp
        }
    }

    private fun Class<*>.findField(name: String): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            runCatching { return cls.getDeclaredField(name).also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }
}
