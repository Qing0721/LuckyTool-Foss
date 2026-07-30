package com.fosstool.app.hook.scope.systemui

import android.view.View
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import java.lang.reflect.Field

object RemoveLockScreenClockComponent : YukiBaseHooker() {
    override fun onHook() {
        "com.android.keyguard.KeyguardClockSwitch"
            .toClassOrNull(appClassLoader)?.let { c ->
                c.method { name = "onFinishInflate" }.ignored().hook {
                    after {
                        (instance as? View)?.isVisible = false
                    }
                }
                c.method { name = "setVisibility" }.ignored().hook {
                    before {
                        if (args.isNotEmpty()) args[0] = View.GONE
                    }
                }
            }
    }
}
