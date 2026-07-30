package com.fosstool.app.hook.scope.systemui

import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.View
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

object FluidCloudIconBackgroundTransparency : YukiBaseHooker() {
    override fun onHook() {
        var alpha = prefs(ModulePrefs).getInt("custom_fluid_cloud_icon_background_transparency", -1)
        dataChannel.wait<Int>("custom_fluid_cloud_icon_background_transparency") { alpha = it }

        "com.oplus.systemui.plugins.seedling.capsule.CapsuleViewBg"
            .toClassOrNull(appClassLoader)?.let { c ->
                c.method { name = "onDraw" }.ignored().hook {
                    after {
                        if (alpha < 0) return@after
                        val view = instance as? View ?: return@after
                        val density = view.resources.displayMetrics.density
                        val bg =
                            c.findField("mVolumeBackgroundLayerDrawable")?.get(instance) as? LayerDrawable
                                ?: return@after
                        bg.getDrawable(0)?.let { blur ->
                            if (blur.javaClass.name == "com.android.internal.graphics.drawable.BackgroundBlurDrawable") {
                                runCatching {
                                    XposedHelpers.callMethod(blur, "setBlurRadius", (alpha * density).toInt())
                                }
                            }
                        }
                        (1 until bg.numberOfLayers).forEach { i -> bg.getDrawable(i)?.setAlpha(alpha) }
                        (c.findField("mVolumeBackgroundBlurDrawable")?.get(instance) as? Drawable)
                            ?.setAlpha(alpha)
                    }
                }
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
