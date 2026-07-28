package com.fosstool.app.hook.scope.launcher

import android.view.View
import android.widget.Button
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.hasMethod
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object RecentTaskListClearButton : YukiBaseHooker() {
    override fun onHook() {
        val clazz = "com.oplus.quickstep.views.OplusClearAllPanelView".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("RecentTaskListClearButton: OplusClearAllPanelView not found")
            return
        }
        val hasInflateIfNeeded = clazz.hasMethod { name = "inflateIfNeeded"; superClass() }
        clazz.method {
            name = if (hasInflateIfNeeded) "inflateIfNeeded" else "onFinishInflate"
            superClass()
        }.ignored().hook {
            after {
                val host = instanceOrNull ?: return@after
                runCatching {
                    clazz.field { name = "mClearAllBtn"; superClass() }
                        .ignored().get(host).cast<Button>()
                }.getOrNull()?.visibility = View.GONE
            }
        }
    }
}
