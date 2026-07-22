package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.pictorial.RemoveImageSaveWaterMark
import com.fosstool.app.hook.scope.pictorial.RemoveVideoSaveWaterMark
import com.fosstool.app.utils.ModulePrefs

object HookPictorial : YukiBaseHooker() {
    override fun onHook() {
        if (prefs(ModulePrefs).getBoolean("remove_image_save_watermark", false)) {
            loadHooker(RemoveImageSaveWaterMark)
        }
        if (prefs(ModulePrefs).getBoolean("remove_video_save_watermark", false)) {
            loadHooker(RemoveVideoSaveWaterMark)
        }
    }
}
