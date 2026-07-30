package com.fosstool.app.hook.scope.launcher

import android.graphics.Canvas
import android.view.MotionEvent
import android.view.View
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.hasMethod
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object PageIndicator : YukiBaseHooker() {
    override fun onHook() {
        val removeDesktop = prefs(ModulePrefs).getBoolean("remove_pagination_component", false)
        val removeFolder =
            prefs(ModulePrefs).getBoolean("remove_folder_pagination_component", false)
        val disableSliding =
            prefs(ModulePrefs).getBoolean("disable_pagination_component_sliding", false)

        val indicator = "com.android.launcher.pageindicators.OplusPageIndicator"
            .toClassOrNull(appClassLoader)
        if (indicator == null) {
            YLog.error("PageIndicator: OplusPageIndicator not found")
        } else {
            val hasDispatchDraw = indicator.hasMethod {
                name = "dispatchDraw"; param(Canvas::class.java); superClass()
            }
            indicator.method {
                name = if (hasDispatchDraw) "dispatchDraw" else "onDraw"
                param(Canvas::class.java)
                superClass()
            }.ignored().hook {
                before {
                    val view = instance as? View ?: return@before
                    val parent = view.parent as? View ?: return@before
                    val resName = try {
                        parent.resources.getResourceEntryName(parent.id)
                    } catch (_: Throwable) {
                        null
                    } ?: return@before
                    when (resName) {
                        "drag_layer" -> if (removeDesktop) {
                            view.visibility = View.GONE
                            result = null
                        }
                        "folder_content_root" -> if (removeFolder) {
                            view.visibility = View.GONE
                            result = null
                        }
                    }
                }
            }
        }

        if (SDK < A13) return

        val touchHelper = "com.android.launcher.pageindicators.PageIndicatorTouchHelper"
            .toClassOrNull(appClassLoader)
        if (touchHelper == null) {
            YLog.error("PageIndicator: PageIndicatorTouchHelper not found")
        } else if (disableSliding) {
            touchHelper.method { name = "onActionMove"; param(MotionEvent::class.java) }
                .ignored().hook { intercept() }
        }

        val bigFolderIcon = "com.android.launcher3.folder.big.BigFolderIcon"
            .toClassOrNull(appClassLoader) ?: return
        listOf("onScrollPageStart", "exposureForWorkspace").forEach { target ->
            bigFolderIcon.method { name = target }.ignored().hook {
                after {
                    if (!removeFolder) return@after
                    val host = instanceOrNull ?: return@after
                    runCatching {
                        bigFolderIcon.field { name = "indicator"; superClass() }
                            .ignored().get(host).cast<View>()
                    }.getOrNull()?.visibility = View.GONE
                }
            }
        }
    }
}
