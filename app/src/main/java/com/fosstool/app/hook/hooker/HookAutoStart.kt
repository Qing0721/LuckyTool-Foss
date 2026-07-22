package com.fosstool.app.hook.hooker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.drake.net.utils.scope
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.BuildConfig
import com.fosstool.app.ui.activity.AliveActivity
import com.fosstool.app.utils.SettingsPrefs
import kotlinx.coroutines.delay

object HookAutoStart : YukiBaseHooker() {
    override fun onHook() {
        var fpsAutoStart = prefs(SettingsPrefs).getBoolean("fps_autostart", false)
        dataChannel.wait<Boolean>("fps_autostart") { fpsAutoStart = it }
        var currentFps = prefs(SettingsPrefs).getInt("current_fps", -1)
        dataChannel.wait<Int>("current_fps") { currentFps = it }

        var tileAutoStart = prefs(SettingsPrefs).getBoolean("tile_auto_start", false)
        dataChannel.wait<Boolean>("tile_auto_start") { tileAutoStart = it }

        var touchSamplingRate = prefs(SettingsPrefs).getBoolean("touch_sampling_rate", false)
        dataChannel.wait<Boolean>("touch_sampling_rate") { touchSamplingRate = it }
        var highBrightness = prefs(SettingsPrefs).getBoolean("high_brightness_mode", false)
        dataChannel.wait<Boolean>("high_brightness_mode") { highBrightness = it }
        var globalDC = prefs(SettingsPrefs).getBoolean("global_dc_mode", false)
        dataChannel.wait<Boolean>("global_dc_mode") { globalDC = it }

        onAppLifecycle {
            registerReceiver(Intent.ACTION_USER_PRESENT) { context, _ ->
                scope {
                    delay(200)
                    val bundle = Bundle().apply {
                        putBoolean("fps_auto", fpsAutoStart)
                        putInt("fps_mode", 2)
                        putInt("fps_cur", currentFps)
                        putBoolean("tileAutoStart", tileAutoStart)
                        putBoolean("touchSamplingRate", touchSamplingRate)
                        putBoolean("highBrightness", highBrightness)
                        putBoolean("globalDC", globalDC)
                    }
                    for (key in bundle.keySet()) {
                        if (bundle.getBoolean(key)) {
                            context.callModule(bundle)
                            break
                        }
                    }
                }
            }
        }
    }

    private fun Context.callModule(bundle: Bundle) {
        Intent(Intent.ACTION_VIEW).apply {
            setClassName(BuildConfig.APPLICATION_ID, AliveActivity::class.java.name)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            putExtras(bundle)
            startActivity(this)
        }
    }
}
