package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object MediaVolumeLevel : YukiBaseHooker() {
    override fun onHook() {
        val mediaVolumeLevel = prefs(ModulePrefs).getInt("media_volume_level", 0)
        val minVolumeZero = prefs(ModulePrefs).getBoolean("minimum_volume_level_can_be_zero", false)
        if (mediaVolumeLevel == 0 && !minVolumeZero) return

        val cls = "com.android.server.audio.AudioServiceExtImpl".toClassOrNull(appClassLoader) ?: return
        cls.method { name = "resetSystemVolume" }.ignored().hook {
            after {
                if (mediaVolumeLevel != 0) {
                    val maxFieldName = if (SDK >= A13) "mMaxStreamVolume" else "MAX_STREAM_VOLUME"
                    @Suppress("UNCHECKED_CAST")
                    val maxArray = runCatching {
                        cls.field { name = maxFieldName }.ignored().get(instance).any() as? IntArray
                    }.getOrNull()
                    maxArray?.set(3, mediaVolumeLevel)
                }

                if (minVolumeZero) {
                    val minFieldName = if (SDK >= A13) "mMinStreamVolume" else "MIN_STREAM_VOLUME"
                    @Suppress("UNCHECKED_CAST")
                    val minArray = runCatching {
                        cls.field { name = minFieldName }.ignored().get(instance).any() as? IntArray
                    }.getOrNull()
                    minArray?.forEachIndexed { index, i ->
                        if (i > 0) minArray[index] = 0
                    }
                }
            }
        }
    }
}
