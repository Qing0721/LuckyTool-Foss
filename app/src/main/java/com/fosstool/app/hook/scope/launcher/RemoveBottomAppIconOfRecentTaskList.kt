package com.fosstool.app.hook.scope.launcher

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveBottomAppIconOfRecentTaskList : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.quickstep.dock.DockView".toClass().apply {
            method { name = "setVisibilityAlpha" }.hook {
                after { instance<ViewGroup>().isVisible = false }
            }
        }
    }
}
