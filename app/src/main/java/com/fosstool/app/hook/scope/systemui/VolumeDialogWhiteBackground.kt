package com.fosstool.app.hook.scope.systemui

import android.annotation.SuppressLint
import android.graphics.drawable.LayerDrawable
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.DialogInterfaceClass
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.dp

object VolumeDialogWhiteBackground : YukiBaseHooker() {
    @SuppressLint("DiscouragedApi")
    override fun onHook() {
        var customAlpha =
            prefs(ModulePrefs).getInt("custom_volume_dialog_background_transparency", -1)
        dataChannel.wait<Int>("custom_volume_dialog_background_transparency") {
            customAlpha = it
        }
        VariousClass(
            "com.oplusos.systemui.volume.VolumeDialogImplEx",
            "com.oplus.systemui.volume.OplusVolumeDialogImpl"
        ).toClass().apply {
            method { name = "isSurrealQualityOn" }.hook {
                after {
                    if (customAlpha < 0) return@after
                    resultFalse()
                }
            }
            method { param(DialogInterfaceClass) }.hook {
                before {
                    if (customAlpha < 0) return@before
                    val value = customAlpha * 25
                    field { name = "mVerticalRowsLayerDrawable" }.get(instance)
                        .cast<LayerDrawable>()?.apply {
                            getDrawable(0)?.setBlurRadius(value.dp)
                            getDrawable(1)?.alpha = value
                        }
                    field { name = "mVolumeMoreLayerDrawable" }.get(instance).cast<LayerDrawable>()
                        ?.apply {
                            getDrawable(0)?.setBlurRadius(value.dp)
                            getDrawable(1)?.alpha = value
                        }
                    field { name = "mVolumeCaptionLayerDrawable" }.get(instance)
                        .cast<LayerDrawable>()
                        ?.apply {
                            getDrawable(0)?.setBlurRadius(value.dp)
                            getDrawable(1)?.alpha = value
                        }
                }
            }
        }
    }

    private fun Any.setBlurRadius(blurRadius: Int) {
        current().method {
            name = "setBlurRadius"
            paramCount = 1
        }.call(blurRadius)
    }
}
