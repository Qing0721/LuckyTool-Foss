package com.fosstool.app.hook.scope.systemui

import android.view.View
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.getOSVersionCode
import java.lang.reflect.Field

object DisableDuplicateFloatingWindow : YukiBaseHooker() {
    override fun onHook() {
        "com.android.systemui.clipboardoverlay.ClipboardOverlayController"
            .toClassOrNull(appClassLoader)?.let { c ->
                c.method { name = "showSinglePreview"; superClass() }.ignored().hook {
                    after {
                        (args.getOrNull(0) as? View)?.isVisible = false
                        (c.findField("mView")?.get(instance) as? View)?.isVisible = false
                    }
                }
            }

        if (getOSVersionCode < 30) return
        "com.android.systemui.clipboardoverlay.ClipboardOverlayView"
            .toClassOrNull(appClassLoader)?.let { c ->
                c.method { name = "showSinglePreview"; superClass() }.ignored().hook {
                    after {
                        (args.getOrNull(0) as? View)?.isVisible = false
                        (instance as? View)?.isVisible = false
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
