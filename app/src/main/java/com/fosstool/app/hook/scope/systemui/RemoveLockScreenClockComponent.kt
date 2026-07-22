package com.fosstool.app.hook.scope.systemui

import android.view.View
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveLockScreenClockComponent : YukiBaseHooker() {
    override fun onHook() {
        "com.android.keyguard.KeyguardClockSwitch".toClass().apply {
            method { name = "onFinishInflate" }.hookAll {
                after { instance<View>().isVisible = false }
            }
            method { name = "setVisibility" }.hook {
                before { args().first().set(View.GONE) }
            }
        }
    }
}
