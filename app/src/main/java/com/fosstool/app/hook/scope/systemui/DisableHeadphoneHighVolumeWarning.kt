package com.fosstool.app.hook.scope.systemui

import android.content.Context
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method

object DisableHeadphoneHighVolumeWarning : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.volume.VolumeDialogImplEx",
            "com.oplus.systemui.volume.OplusVolumeDialogImpl",
            "com.android.systemui.volume.VolumeDialogImpl"
        ).toClass().apply {
            method { name = "showSafetyWarningH" }.hook {
                before {
                    val mContext = field { name = "mContext" }.get(instance).cast<Context>()
                        ?: return@before
                    val audioManager =
                        mContext.getSystemService(Context.AUDIO_SERVICE) ?: return@before
                    audioManager.current().method { name = "disableSafeMediaVolume" }.call()
                    resultNull()
                }
            }
        }
    }
}
