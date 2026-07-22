package com.fosstool.app.hook.scope.launcher

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object AllowLockingUnLockingOfExcludedActivity : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.quickstep.applock.OplusLockManager".toClass().apply {
            method { name = "isExcludedFromRecents" }.hook {
                replaceToFalse()
            }
        }
    }
}
