package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.camera.CustomCameraOpenGalleryByDefault
import com.fosstool.app.hook.scope.camera.CustomModelWaterMark
import com.fosstool.app.hook.scope.camera.EnableCameraDebugUiOption
import com.fosstool.app.hook.scope.camera.HookCameraConfig
import com.fosstool.app.hook.scope.camera.RemoveCameraFlashLimit
import com.fosstool.app.hook.scope.camera.RemoveFilterModelLimit
import com.fosstool.app.hook.scope.camera.RemoveWatermarkWordLimit
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getOSVersionCode

class HookCamera : YukiBaseHooker() {
    override fun onHook() {
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

        if (prefs(ModulePrefs).getBoolean("enable_camera_debug_ui_option", false) && getOSVersionCode >= 30) {
            loadHooker(EnableCameraDebugUiOption)
        }
    }
}
