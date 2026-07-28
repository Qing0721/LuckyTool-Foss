package com.fosstool.app.hook.scope.systemui

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import org.json.JSONObject
import java.lang.reflect.Field
import java.util.Collections

object MusicFluidCloudControl : YukiBaseHooker() {
    private const val KEY_STATIC_VOICE_PRINT_SHOW = "staticVoicePrintShow"
    private const val CLASS_RUS =
        "com.oplus.systemui.media.controls.pipeline.MediaActionPrioritySelectorImpl"
    private const val CLASS_DATA_MODEL =
        "com.oplus.systemui.media.controls.pipeline.OplusMediaDataManagerExImpl"
    private const val CLASS_MEDIA_CTRL = "com.oplus.media.OplusMediaControlManager"
    private const val CUSTOM_LYRIC_PATH = "/sdcard/Musics/"

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
                CLASS_RUS.toClassOrNull(appClassLoader)
                    ?.method { name = "getLyricEntrance" }?.ignored()?.hook {
                        after {
                            when {
                                disableDisplay -> result = 0
                                customEnabled -> {
                                    val pkg = args.firstOrNull() as? String ?: ""
                                    if (pkg.isNotEmpty() && !whitelist.contains(pkg)) {
                                        result = 0
                                    }
                                }
                            }
                        }
                    }
            }
        }

        runCatching {
            CLASS_DATA_MODEL.toClassOrNull(appClassLoader)?.let { cls ->
                cls.findField("lyricSearchPath")?.let { field ->
                    cls.method { name = "loadLyricInBg" }.ignored().hook {
                        before {
                            runCatching {
                                field.isAccessible = true
                                field.set(instance, CUSTOM_LYRIC_PATH)
                            }
                        }
                    }
                }
            }
        }

        if (disableBlacklist && getOSVersionCode >= 35) {
            runCatching {
                CLASS_DATA_MODEL.toClassOrNull(appClassLoader)
                    ?.method { name = "loadLyricInBg" }?.ignored()?.hook {
                        before {
                            runCatching {
                                val mgrField = instance.javaClass.declaredFields.firstOrNull {
                                    it.type.name == CLASS_MEDIA_CTRL
                                }
                                mgrField?.isAccessible = true
                                val mgr = mgrField?.get(instance)
                                if (mgr != null) {
                                    XposedHelpers.callMethod(
                                        mgr,
                                        "setMediaControlDenyList",
                                        Collections.singletonList("")
                                    )
                                }
                            }
                            result = null
                        }
                    }
            }
        }

        if (forceRipple) hookForceEnableFluidCloudRipple()
    }

    private fun hookForceEnableFluidCloudRipple() {
        runCatching {
            "com.oplus.pantanal.seedling.util.SeedlingTool"
                .toClassOrNull(appClassLoader)?.declaredMethods
                ?.filter { m ->
                    java.lang.reflect.Modifier.isStatic(m.modifiers) &&
                        m.name.isNotEmpty() &&
                        m.parameterCount in 1..8
                }?.forEach { m ->
                    runCatching {
                        XposedBridge.hookMethod(m, object : XC_MethodHook() {
                            override fun beforeHookedMethod(param: MethodHookParam) {
                                val json = param.args.filterIsInstance<JSONObject>().firstOrNull()
                                    ?: return
                                if (json.optBoolean(KEY_STATIC_VOICE_PRINT_SHOW, true)) {
                                    runCatching { json.put(KEY_STATIC_VOICE_PRINT_SHOW, false) }
                                }
                            }
                        })
                    }
                }
        }
    }

    private fun Class<*>.findField(name: String): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            runCatching { return cls.getDeclaredField(name).also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }
}
