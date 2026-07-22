package com.fosstool.app.hook.scope.systemui

import android.view.View
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object RemoveControlCenterEditMoreButton : YukiBaseHooker() {
    override fun onHook() {
        val removeEdit = prefs(ModulePrefs).getBoolean("remove_control_center_edit_button", false)
        val removeMore = prefs(ModulePrefs).getBoolean("remove_control_center_more_button", false)
        if (!removeEdit && !removeMore) return

        "com.oplus.systemui.plugins.qs.bottom.OplusQSBottomViewController".toClass().apply {
            method { name = "init" }.hook {
                after {
                    if (removeEdit) field { name = "editBtn" }.get(instance).cast<View>()?.visibility =
                        View.GONE
                    if (removeMore) field { name = "moreBtn" }.get(instance).cast<View>()?.visibility =
                        View.GONE
                }
            }
        }
    }
}
