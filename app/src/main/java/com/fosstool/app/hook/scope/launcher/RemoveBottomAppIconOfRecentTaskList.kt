package com.fosstool.app.hook.scope.launcher

import android.view.View
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.hasMethod
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object RemoveBottomAppIconOfRecentTaskList : YukiBaseHooker() {
    override fun onHook() {
        val dockView = "com.oplus.quickstep.dock.DockView".toClassOrNull(appClassLoader)
        if (dockView == null) {
            YLog.error("RemoveBottomAppIconOfRecentTaskList: DockView not found")
        } else {
            if (dockView.hasMethod { name = "setVisibilityAlpha"; superClass() }) {
                dockView.method { name = "setVisibilityAlpha"; superClass() }.ignored().hook {
                    after { (instanceOrNull as? View)?.visibility = View.GONE }
                }
            } else {
                dockView.constructor().ignored().hookAll {
                    after { (instanceOrNull as? View)?.visibility = View.GONE }
                }
            }
            if (dockView.hasMethod { name = "hideDockView"; superClass() }) {
                dockView.method { name = "hideDockView"; superClass() }.ignored().hook {
                    before {
                        if (args.isEmpty()) return@before
                        args[0] = true
                    }
                }
            }
        }

        val controller = "com.oplus.quickstep.dock.DockViewController"
            .toClassOrNull(appClassLoader) ?: return
        listOf(
            "onRecentsViewOrientationChange" to false,
            "updateOnTaskDisplayModeChange" to true,
            "updateOnLauncherMultiWindowChange" to true,
        ).forEach { (target, value) ->
            controller.method { name = target }.ignored().hook {
                before {
                    if (args.isEmpty()) return@before
                    args[0] = value
                }
            }
        }
    }
}
