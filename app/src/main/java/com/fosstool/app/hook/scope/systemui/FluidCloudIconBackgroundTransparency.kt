package com.fosstool.app.hook.scope.systemui

import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object FluidCloudIconBackgroundTransparency : YukiBaseHooker() {
    override fun onHook() {
        var alpha = prefs(ModulePrefs).getInt("custom_fluid_cloud_icon_background_transparency", -1)
        dataChannel.wait<Int>("custom_fluid_cloud_icon_background_transparency") { alpha = it }

        "com.oplus.systemui.plugins.seedling.capsule.CapsuleViewBg".toClass().apply {
            method { name = "onDraw" }.hook {
                after {
                    if (alpha < 0) return@after
                    val view = instance<android.view.View>() ?: return@after
                    val density = view.resources.displayMetrics.density
                    val bg = field { name = "mVolumeBackgroundLayerDrawable" }.get(instance).cast<LayerDrawable>()
                        ?: return@after
                    bg.getDrawable(0)?.let { blur ->
                        if (blur.javaClass.name == "com.android.internal.graphics.drawable.BackgroundBlurDrawable") {
                            blur.current().method { name = "setBlurRadius" }.call((alpha * density).toInt())
                        }
                    }
                    (1 until bg.numberOfLayers).forEach { i -> bg.getDrawable(i)?.setAlpha(alpha) }
                    field { name = "mVolumeBackgroundBlurDrawable" }.get(instance).cast<Drawable>()?.setAlpha(alpha)
                }
            }
        }
    }
}
