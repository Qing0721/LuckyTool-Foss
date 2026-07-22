package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.camera.CustomCameraOpenGalleryByDefault
import com.fosstool.app.hook.scope.camera.CustomModelWaterMark
import com.fosstool.app.hook.scope.camera.HookCameraConfig
import com.fosstool.app.hook.scope.camera.RemoveCameraFlashLimit
import com.fosstool.app.hook.scope.camera.RemoveFilterModelLimit
import com.fosstool.app.hook.scope.camera.RemoveWatermarkWordLimit
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getAppSet
import com.fosstool.app.utils.getOSVersionCode

object HookCamera : YukiBaseHooker() {
    override fun onHook() {
        val appSet = getAppSet(ModulePrefs, packageName)
        if (appSet[2] == "null") return

        if (SDK >= A13) loadHooker(HookCameraConfig)

        if (SDK >= A13) loadHooker(CustomModelWaterMark)

        val openGallery =
            prefs(ModulePrefs).getString("custom_camera_open_gallery_by_default", "") ?: ""
        if (openGallery.isNotBlank() && getOSVersionCode >= 26) {
            loadHooker(CustomCameraOpenGalleryByDefault)
        }

        if (prefs(ModulePrefs).getBoolean("remove_watermark_word_limit", false)) {
            loadHooker(RemoveWatermarkWordLimit)
        }

        if (prefs(ModulePrefs).getBoolean("remove_camera_flash_limit", false) && getOSVersionCode >= 26) {
            loadHooker(RemoveCameraFlashLimit)
        }

        if (prefs(ModulePrefs).getBoolean("remove_filter_model_limit", false) && getOSVersionCode >= 34) {
            loadHooker(RemoveFilterModelLimit)
        }
    }
}
