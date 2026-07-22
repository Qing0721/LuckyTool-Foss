package com.fosstool.app.hook.scope.packageinstaller

import android.view.View
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.method

object RemoveInstallAds : YukiBaseHooker() {
    override fun onHook() {
        var ins: Any? = null
        "com.android.packageinstaller.oplus.InstallAppProgress".toClass().apply {
            method { name = "initView" }.hook {
                after { ins = instance;ins?.removeViews() }
            }
        }
        "com.android.packageinstaller.oplus.InstallAppProgress$1".toClass().apply {
            method { name = "handleMessage" }.hook {
                after { ins?.removeViews() }
            }
        }
    }

    private fun Any.removeViews() {
        current {
            field { name = "mSuggestLayoutAScrollView" }.cast<View>()?.isVisible = false
            field { name = "mSuggestLayoutB" }.cast<View>()?.isVisible = false
        }
    }
}
