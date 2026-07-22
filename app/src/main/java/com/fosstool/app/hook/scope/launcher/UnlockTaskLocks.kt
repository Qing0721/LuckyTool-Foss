package com.fosstool.app.hook.scope.launcher

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field

object UnlockTaskLocks : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.coloros.quickstep.applock.ColorLockManager",
            "com.oplus.quickstep.applock.OplusLockManager"
        ).toClass().apply {
            constructor { paramCount = 1 }.hook {
                after {
                    field { name = "mLockAppLimit" }.get(instance).set(999)
                }
            }
        }
    }
}
