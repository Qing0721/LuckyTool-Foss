package com.fosstool.app.hook.scope.launcher

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.SDK

object RemoveFolderPreviewBackground : YukiBaseHooker() {
    override fun onHook() {
        "com.android.launcher3.folder.OplusPreviewBackground".toClass().apply {
            method { name = "setup" }.hookAll {
                after { field { name = "mBgDrawable" }.get(instance).setNull() }
            }
            method { name = "drawBackground" }.hook {
                intercept()
            }
        }
        if (SDK < A13) return
        "com.android.launcher3.folder.OplusFolderAnimationManager".toClass().apply {
            method { name = "getFolderBackgroundAnimator" }.hook {
                intercept()
            }

        }
    }
}
