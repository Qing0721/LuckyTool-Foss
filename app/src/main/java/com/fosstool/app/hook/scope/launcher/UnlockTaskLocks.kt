package com.fosstool.app.hook.scope.launcher

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.hasField
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.android.ContextClass

object UnlockTaskLocks : YukiBaseHooker() {
    override fun onHook() {
        val appLockModel = "com.oplus.quickstep.applock.AppLockModel".toClassOrNull(appClassLoader)
        if (appLockModel != null &&
            appLockModel.hasField { name = "noDefaultLockedAppLimit"; superClass() }
        ) {
            listOf("initData", "updateNoDefaultLockAppLimit").forEach { target ->
                appLockModel.method { name = target }.ignored().hook {
                    after {
                        val host = instanceOrNull ?: return@after
                        runCatching {
                            appLockModel.field { name = "noDefaultLockedAppLimit"; superClass() }
                                .ignored().get(host).set(999)
                        }
                    }
                }
            }
        }

        val clazz = VariousClass(
            "com.coloros.quickstep.applock.ColorLockManager",
            "com.oplus.quickstep.applock.OplusLockManager",
        ).toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("UnlockTaskLocks: ColorLockManager / OplusLockManager not found")
            return
        }
        clazz.constructor { param(ContextClass) }.ignored().hook {
            after {
                val host = instanceOrNull ?: return@after
                runCatching {
                    clazz.field { name = "mLockAppLimit"; superClass() }.ignored().get(host).set(999)
                }
            }
        }
    }
}
