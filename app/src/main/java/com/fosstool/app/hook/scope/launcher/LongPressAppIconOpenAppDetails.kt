package com.fosstool.app.hook.scope.launcher

import android.view.View
import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.openAppDetailIntent

object LongPressAppIconOpenAppDetails : YukiBaseHooker() {
    override fun onHook() {
        "com.android.quickstep.views.OplusTaskViewImpl".toClass().apply {
            method {
                name = "setIcon"
                paramCount(1..2)
            }.hook {
                after {
                    val headerView = method { name = "getHeaderView" }.get(instance).call()
                        ?: return@after
                    val iconView =
                        headerView.current().method { name = "getTaskIcon" }.invoke<View>()
                            ?: return@after
                    val titleView = headerView.current()
                        .field { name = if (SDK >= A13) "titleTv" else "mTitleView" }
                        .cast<TextView>() ?: return@after
                    val task = method {
                        name = "getTask";superClass(true)
                    }.get(instance).call() ?: return@after
                    val key = task.current().field { name = "key" }.any() ?: return@after
                    val packName = key.current().method { name = "getPackageName" }.invoke<String>()
                        ?: return@after
                    val userId = key.current().field { name = "userId" }.int()
                    iconView.setLongClick(packName, userId)
                    titleView.setLongClick(packName, userId)
                }
            }
        }

        "com.oplus.quickstep.dock.DockIconView".toClass().apply {
            method {
                name = "setIcon"
                paramCount = 1
            }.hook {
                after {
                    val task = method { name = "getTask" }.get(instance).call() ?: return@after
                    val key = task.current().field { name = "key" }.any() ?: return@after
                    val packName = key.current().method { name = "getPackageName" }.invoke<String>()
                        ?: return@after
                    val userId = key.current().field { name = "userId" }.int()
                    instance<View>().setLongClick(packName, userId)
                }
            }
        }
    }

    private fun View.setLongClick(packName: String?, userId: Int? = 0) {
        setOnLongClickListener {
            packName?.let { its -> it.context.openAppDetailIntent(its, userId) }
            true
        }
    }
}
