package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.oplusmms.RemoveMmsBottomInputBoxMenu
import com.fosstool.app.hook.scope.oplusmms.RemoveMmsCardMarketingButton
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode
import com.fosstool.app.utils.ModulePrefs

object HookOplusMMS : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(SystemPropertiesOverrideEngineHooker(mode = Mode.RM0_Q))

        if (prefs(ModulePrefs).getBoolean("remove_mms_bottom_input_box_menu", false)) {
            loadHooker(RemoveMmsBottomInputBoxMenu)
        }
        if (prefs(ModulePrefs).getBoolean("remove_mms_card_marketing_button", false)) {
            loadHooker(RemoveMmsCardMarketingButton)
        }
    }
}
