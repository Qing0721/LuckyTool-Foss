package com.fosstool.app.hook.scope.systemui

import android.graphics.drawable.ShapeDrawable
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.safeOfNan

object TileBackgroundTransparency : YukiBaseHooker() {
    override fun onHook() {
        var alpha = prefs(ModulePrefs).getInt("custom_tile_background_transparency", -1)
        dataChannel.wait<Int>("custom_tile_background_transparency") { alpha = it }

        VariousClass(
            "com.oplusos.systemui.qs.qstileimpl.OplusQSTileBaseView",
            "com.oplus.systemui.qs.qstileimpl.OplusQSTileBaseView",
            "com.oplus.systemui.qs.base.tile.OplusQSTileBaseView",
            "com.oplusos.systemui.qs.qstileimpl.OplusQSHighlightTileView",
            "com.oplus.systemui.qs.qstileimpl.OplusQSHighlightTileView",
            "com.oplus.systemui.qs.base.tile.OplusQSHighlightTileView"
        ).toClass().apply {
            method { name = "generateDrawable" }.hook {
                after {
                    if (alpha < 0) return@after
                    val drawable = result<ShapeDrawable>() ?: return@after
                    val paint = drawable.paint
                    paint.color = paint.color.colorAlphaOf(alpha / 10.0F)
                }
            }
        }
    }

    private fun Int.colorAlphaOf(value: Float) =
        safeOfNan { (255.coerceAtMost(0.coerceAtLeast((value * 255).toInt())) shl 24) + (0x00ffffff and this) }
}
