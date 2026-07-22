package com.fosstool.app.hook.scope.systemui

import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.ListClass
import org.json.JSONObject
import java.util.ArrayList
import java.util.Collections

object MusicFluidCloudControl : YukiBaseHooker() {
    private const val KEY_STATIC_VOICE_PRINT_SHOW = "staticVoicePrintShow"
    private const val CLASS_RUS =
        "com.oplus.systemui.media.seedling.rus.OplusMediaRusUpdateManager"
    private const val CLASS_DATA_MODEL =
        "com.oplus.systemui.media.model.OplusMediaDataModelImpl"
    private const val CLASS_MEDIA_CTRL = "com.oplus.media.OplusMediaControlManager"

    override fun onHook() {
        val disableDisplay =
            prefs(ModulePrefs).getBoolean("disable_music_fluid_cloud_display", false)
        val customEnabled =
            prefs(ModulePrefs).getBoolean("custom_music_fluid_cloud_whitelist", false)
        val whitelist = prefs(ModulePrefs).getStringSet(
            "set_custom_music_fluid_cloud_whitelist", emptySet()
        )
        val disableBlacklist =
            prefs(ModulePrefs).getBoolean("disable_music_fluid_cloud_blacklist", false) ||
                prefs(ModulePrefs).getBoolean("disable_media_music_fluid_cloud_blacklist", false)
        val forceRipple =
            prefs(ModulePrefs).getBoolean("force_enable_media_music_fluid_cloud_ripple", false)

        if (!disableDisplay && !customEnabled && !disableBlacklist && !forceRipple) return

        if (disableDisplay || customEnabled) {
            runCatching {
                CLASS_RUS.toClass().method {
                    name = "getRusWhiteList"
                    returnType = ListClass
                }.hook {
                    after {
                        when {
                            disableDisplay -> result = emptyList<Any>()
                            customEnabled -> result = ArrayList(whitelist)
                        }
                    }
                }
            }
        }

        if (disableBlacklist && getOSVersionCode >= 35) {
            runCatching {
                CLASS_DATA_MODEL.toClass().method {
                    name = "setMediaControlBlackList"
                    param(ListClass)
                }.hook {
                    before {
                        runCatching {
                            val mgr = instance.current().field {
                                type = CLASS_MEDIA_CTRL
                            }.any()
                            if (mgr != null) {
                                mgr.current().method {
                                    name = "setMediaControlDenyList"
                                    param(ListClass)
                                }.call(Collections.singletonList(""))
                            }
                        }
                        resultNull()
                    }
                }
            }
        }

        if (forceRipple) hookForceEnableFluidCloudRipple()
    }

    private fun hookForceEnableFluidCloudRipple() {
        runCatching {
            "com.oplus.pantanal.seedling.util.SeedlingTool".toClass().apply {
                method {
                    name { it.isNotEmpty() }
                    modifiers { isStatic }
                    paramCount(1..8)
                }.hookAll {
                    before {
                        val json = runCatching { args(1).any() as? JSONObject }.getOrNull()
                            ?: run {
                                var found: JSONObject? = null
                                for (i in 0 until 6) {
                                    val v = runCatching { args(i).any() }.getOrNull()
                                    if (v is JSONObject) {
                                        found = v
                                        break
                                    }
                                }
                                found
                            } ?: return@before
                        if (json.optBoolean(KEY_STATIC_VOICE_PRINT_SHOW, true)) {
                            runCatching { json.put(KEY_STATIC_VOICE_PRINT_SHOW, false) }
                        }
                    }
                }
            }
        }
    }
}
