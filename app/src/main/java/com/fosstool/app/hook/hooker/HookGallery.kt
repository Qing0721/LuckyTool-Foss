package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.camera.HookCameraConfig
import com.fosstool.app.hook.scope.gallery.ReplaceOnePlusModelWatermark
import com.fosstool.app.hook.scope.gallery.HookConfigAbility
import com.fosstool.app.hook.scope.gallery.HookFunctionManager
import com.fosstool.app.hook.scope.gallery.HookSystemStorage
import com.fosstool.app.hook.scope.gallery.RemoveAigcEliminationLimit
import com.fosstool.app.hook.scope.gallery.RemoveGalleryWatermarkWordLimit
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getOSVersionCode

object HookGallery : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 27) return

        if (SDK >= A13) loadHooker(HookCameraConfig)

        loadHooker(HookSystemStorage)
        loadHooker(HookConfigAbility)

        if (getOSVersionCode < 34) loadHooker(HookFunctionManager)
        loadHooker(RemoveAigcEliminationLimit)
        loadHooker(RemoveGalleryWatermarkWordLimit)

        if (prefs(ModulePrefs).getBoolean("replace_oneplus_model_watermark", false) &&
            getOSVersionCode >= 34
        ) {
            loadHooker(ReplaceOnePlusModelWatermark)
        }
    }
}
