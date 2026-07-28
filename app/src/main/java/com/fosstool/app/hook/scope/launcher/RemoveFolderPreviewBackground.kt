package com.fosstool.app.hook.scope.launcher

import com.fosstool.app.utils.A13
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object RemoveFolderPreviewBackground : YukiBaseHooker() {
    override fun onHook() {
        val clazz = "com.android.launcher3.folder.OplusPreviewBackground".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("RemoveFolderPreviewBackground: OplusPreviewBackground not found")
            return
        }
        if (getOSVersionCode >= 34) {
            clazz.method { name = "setBackground" }.ignored().hook {
                before {
                    val host = instanceOrNull ?: return@before
                    runCatching {
                        clazz.field { name = "mBgDrawable"; superClass() }.ignored().get(host).set(null)
                    }
                }
            }
            return
        }

        clazz.method { name = "setup" }.ignored().hookAll {
            after {
                val host = instanceOrNull ?: return@after
                runCatching {
                    clazz.field { name = "mBgDrawable"; superClass() }.ignored().get(host).set(null)
                }
            }
        }
        clazz.method { name = "drawBackground" }.ignored().hook { intercept() }

        if (SDK < A13) return
        val animManager = "com.android.launcher3.folder.OplusFolderAnimationManager"
            .toClassOrNull(appClassLoader)
        if (animManager == null) {
            YLog.error("RemoveFolderPreviewBackground: OplusFolderAnimationManager not found")
            return
        }
        animManager.method { name = "getFolderBackgroundAnimator" }.ignored().hook { intercept() }
    }
}
