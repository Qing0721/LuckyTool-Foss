package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.hook.scope.aod.AodRandomTextAndTypeface
import com.fosstool.app.utils.ModulePrefs

object HookAod : YukiBaseHooker() {
    override fun onHook() {
        if (prefs(ModulePrefs).getBoolean("force_enable_screen_off_music_support", false)) {
            "com.oplus.aod.proxy.AodSettingsValueProxy".toClass().apply {
                method { name = "getAodSceneMusicSupport" }.hook().replaceToTrue()
                method { name = "getAodSceneMusicSwitchEnable" }.hook().replaceTo(1)
            }
        }
        loadHooker(AodRandomTextAndTypeface)
    }
}
