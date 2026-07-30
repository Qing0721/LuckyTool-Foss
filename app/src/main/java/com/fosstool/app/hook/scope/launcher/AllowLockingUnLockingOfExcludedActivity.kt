package com.fosstool.app.hook.scope.launcher

import android.content.Intent
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.hasMethod
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object AllowLockingUnLockingOfExcludedActivity : YukiBaseHooker() {
    override fun onHook() {
        val clazz = "com.oplus.quickstep.applock.OplusLockManager".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("AllowLockingUnLockingOfExcludedActivity: OplusLockManager not found")
            return
        }
        val hasLockable = clazz.hasMethod { name = "isAppLockable"; superClass() }
        val targetName = if (hasLockable) "isAppLockable" else "isAppSupportLock"
        clazz.method { name = targetName; superClass() }.ignored().hook {
            before {
                val intent = (args.lastOrNull() as? Intent)
                    ?: args.filterIsInstance<Intent>().lastOrNull()
                    ?: return@before
                if (intent.flags and Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS ==
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                ) {
                    intent.removeFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                }
            }
        }
    }
}
