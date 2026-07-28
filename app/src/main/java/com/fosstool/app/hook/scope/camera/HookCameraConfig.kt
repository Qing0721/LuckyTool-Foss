package com.fosstool.app.hook.scope.camera

import android.util.ArrayMap
import android.util.ArraySet
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs

object HookCameraConfig : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(HookCameraConfig)
    }

    private object HookCameraConfig : YukiBaseHooker() {
        override fun onHook() {
            val is10bit = prefs(ModulePrefs).getBoolean("enable_10_bit_image_support", false)
            val isHasselblad =
                prefs(ModulePrefs).getBoolean("enable_hasselblad_watermark_style", false)

            val frameWatermark =
                prefs(ModulePrefs).getBoolean("enable_frame_watermark_style", false)
            val nightZoom30x =
                prefs(ModulePrefs).getBoolean("enable_camera_night_zoom_30x", false)
            val rouletteZoom =
                prefs(ModulePrefs).getBoolean("enable_video_capture_roulette_zoom", false)
            val aiMasterWatermark =
                prefs(ModulePrefs).getBoolean("enable_ai_master_watermark", false)
            val removeFlashLimit =
                prefs(ModulePrefs).getBoolean("remove_camera_flash_limit", false)
            val universalFilters =
                prefs(ModulePrefs).getStringSet("camera_universal_filter_settings", ArraySet())
                    ?: emptySet()
            val masterFilter = "master_filter" in universalFilters
            val jiangwenFilter = "jiangwen_filter" in universalFilters
            val grandTourFilter = "grand_tour_filter" in universalFilters
            val os15ZhiGanFilter = "os15_zhi_gan_filter" in universalFilters
            val jzkFilter = "jzk_filter" in universalFilters
            val vignetteGrainFilter = "vignette_grain_filter" in universalFilters
            val desertFilter = "desert_filter" in universalFilters
            val tolFilter = "tol_filter" in universalFilters

            val portraitFilters =
                prefs(ModulePrefs).getStringSet("camera_portrait_filter_settings", ArraySet())
                    ?: emptySet()
            val portraitRetention = "retention" in portraitFilters
            val portraitBokehFlare = "bokeh_flare_portrait" in portraitFilters

            val videoFilters =
                prefs(ModulePrefs).getStringSet("camera_video_filter_settings", ArraySet())
                    ?: emptySet()
            val videoColorExtraction = "color_extraction" in videoFilters
            val videoRetention = "retention" in videoFilters
            val videoBokehFlare = "bokeh_flare_portrait" in videoFilters

            val arrayMap = ArrayMap<String, Any>()
            if (is10bit) {
                arrayMap["com.oplus.10bits.heic.encode.support"] = true
                arrayMap["com.oplus.feature.video.10bit.support"] = true
            }

            if (frameWatermark) {
                arrayMap["com.oplus.camera.support.frame.watermark"] = true
            }
            if (aiMasterWatermark) arrayMap["com.oplus.camera.support.ai.master.watermark"] = true
            if (isHasselblad) {
                arrayMap["com.oplus.camera.support.frame.watermark"] = false
                arrayMap["com.oplus.hasselblad.watermark.support.default"] = true
                arrayMap["com.oplus.camera.support.custom.hasselblad.watermark"] = true
                arrayMap["com.oplus.hasselblad.watermark.guide.support"] = true
                arrayMap["com.oplus.use.hasselblad.style.support"] = true
            }
            if (nightZoom30x) {
                arrayMap["com.oplus.night.mode.max.zoom.support"] = true
                arrayMap["com.oplus.night.zoom.max.value.default"] = 30
            }
            if (rouletteZoom) {
                arrayMap["com.oplus.video.inertial.zoom.support"] = false
            }
            if (removeFlashLimit) arrayMap["com.oplus.feature.temperature.protection.support"] = false
            if (masterFilter) {
                arrayMap["com.oplus.photo.master.filter.type.list"] =
                    "Radiance.cube.rgb.bin,Serenity.cube.rgb.bin,Emerald.cube.rgb.bin"
                arrayMap["com.oplus.portrait.master.filter.type.list"] =
                    "Radiance.cube.rgb.bin,Serenity.cube.rgb.bin,Emerald.cube.rgb.bin"
            }
            if (jiangwenFilter) {
                arrayMap["com.oplus.director.filter.support"] = true
                arrayMap["com.oplus.director.filter.rus"] = true
                arrayMap["com.oplus.director.filter.upgrade.support"] = true
            }
            if (grandTourFilter) arrayMap["com.oplus.support.grand.tour.filter"] = true
            if (os15ZhiGanFilter) arrayMap["com.oplus.feature.os15.new.filter.support"] = true
            if (jzkFilter) arrayMap["com.oplus.support.jzk.movie.filter"] = true
            if (vignetteGrainFilter) arrayMap["com.oplus.vignette.grain.filter.type.support"] = true
            if (desertFilter) arrayMap["com.oplus.desert.filter.type.support"] = true
            if (tolFilter) arrayMap["com.oplus.tol.style.filter.support"] = true
            if (portraitRetention) {
                arrayMap["com.oplus.feature.portrait.retention.support"] = true
                arrayMap["com.oplus.feature.portrait.front.retention.support"] = true
                arrayMap["com.oplus.feature.portrait.back.retention.support"] = true
            }
            if (portraitBokehFlare) {
                arrayMap["com.oplus.feature.portrait.neon.support"] = true
                arrayMap["com.oplus.feature.portrait.neon.front.support"] = true
            }
            if (videoColorExtraction) arrayMap["com.oplus.video.color_extraction.support"] = true
            if (videoRetention) arrayMap["com.oplus.video.retention.support"] = true
            if (videoBokehFlare) {
                arrayMap["com.oplus.video.neon.support"] = true
                arrayMap["com.oplus.video.only.blur.support"] = true
            }

            listOf(
                "com.oplus.ocs.camera.appinterface.adapter.CameraAdapterUtils",
                "com.oplus.ocs.camera.consumer.apsAdapter.adapter.ApsUtils"
            ).forEach { target ->
                target.toClassOrNull(appClassLoader)?.method {
                    name = "getVendorTagConfig"
                    paramCount = 1
                }?.ignored()?.hook {
                    after {
                        val key = args().first().string()
                        if (key.isBlank()) return@after
                        val override = arrayMap[key] ?: return@after

                        result = when (override) {
                            is Boolean -> if (override) "1" else "0"
                            is Int -> override.toString()
                            else -> override
                        }
                    }
                }
            }
        }
    }
}
