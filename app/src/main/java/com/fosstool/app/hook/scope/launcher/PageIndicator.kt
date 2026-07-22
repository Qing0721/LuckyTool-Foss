package com.fosstool.app.hook.scope.launcher

import android.view.View
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.CanvasClass
import com.highcapable.yukihookapi.hook.type.android.MotionEventClass
import com.fosstool.app.utils.ModulePrefs

object PageIndicator : YukiBaseHooker() {
    override fun onHook() {
        val removeDesktop = prefs(ModulePrefs).getBoolean("remove_pagination_component", false)
        val removeFolder =
            prefs(ModulePrefs).getBoolean("remove_folder_pagination_component", false)
        val disableSliding =
            prefs(ModulePrefs).getBoolean("disable_pagination_component_sliding", false)

        "com.android.launcher.pageindicators.OplusPageIndicator".toClass().apply {
            method {
                name = "onDraw"
                param(CanvasClass)
            }.hook {
                before {
                    val view = instance<View>()
                    val parent = view.parent as? View ?: return@before
                    val resName = try {
                        parent.resources.getResourceEntryName(parent.id)
                    } catch (_: Throwable) {
                        null
                    } ?: return@before
                    when (resName) {
                        "drag_layer" -> if (removeDesktop) {
                            view.visibility = View.GONE
                            resultNull()
                        }
                        "folder_content_root" -> if (removeFolder) {
                            view.visibility = View.GONE
                            resultNull()
                        }
                    }
                }
            }
        }

        runCatching {
            "com.android.launcher.pageindicators.PageIndicatorTouchHelper".toClass().apply {
                method {
                    name = "onActionMove"
                    param(MotionEventClass)
                }.hook {
                    if (disableSliding) intercept()
                }
            }
        }

        runCatching {
            "com.android.launcher3.folder.big.BigFolderIcon".toClass().apply {
                method { name = "onScrollPageStart" }.hookAll {
                    after {
                        if (!disableSliding) return@after
                        runCatching {
                            field { name = "indicator" }.get(instance).cast<View>()
                        }.getOrNull()?.let { it.visibility = View.GONE }
                    }
                }
            }
        }
    }
}
