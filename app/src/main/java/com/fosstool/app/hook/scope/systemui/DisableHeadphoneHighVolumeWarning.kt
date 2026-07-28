package com.fosstool.app.hook.scope.systemui

import android.content.Context
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

object DisableHeadphoneHighVolumeWarning : YukiBaseHooker() {
    override fun onHook() {

        VariousClass(
            "com.oplusos.systemui.volume.VolumeDialogImplEx",
            "com.oplus.systemui.volume.OplusVolumeDialogImpl",
            "com.android.systemui.volume.VolumeDialogImpl"
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "init" }.ignored().hook {
                after {
                    val mContext = c.findField("mContext")?.get(instance) as? Context
                        ?: return@after
                    val audioManager = mContext.getSystemService(Context.AUDIO_SERVICE)
                        ?: return@after
                    runCatching {
                        XposedHelpers.callMethod(audioManager, "disableSafeMediaVolume")
                    }
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
