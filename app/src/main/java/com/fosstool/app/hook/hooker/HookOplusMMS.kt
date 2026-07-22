package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.oplusmms.HookMMSFeatureOption
import com.fosstool.app.hook.scope.oplusmms.RemoveMmsBottomInputBoxMenu
import com.fosstool.app.hook.scope.oplusmms.RemoveMmsCardMarketingButton
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object HookOplusMMS : YukiBaseHooker() {
    override fun onHook() {
        if (SDK >= A13) loadHooker(HookMMSFeatureOption)
        if (prefs(ModulePrefs).getBoolean("remove_mms_bottom_input_box_menu", false)) {
            loadHooker(RemoveMmsBottomInputBoxMenu)
        }
        if (prefs(ModulePrefs).getBoolean("remove_mms_card_marketing_button", false)) {
            loadHooker(RemoveMmsCardMarketingButton)
        }
    }
}
