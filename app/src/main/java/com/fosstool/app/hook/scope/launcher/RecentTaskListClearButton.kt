package com.fosstool.app.hook.scope.launcher

import android.widget.Button
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method

object RecentTaskListClearButton : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.quickstep.views.OplusClearAllPanelView".toClass().apply {
            method { name = "inflateIfNeeded" }.hook {
                after {
                    field { name = "mClearAllBtn" }.get(instance).cast<Button>()?.isVisible = false
                }
            }
        }
    }
}
