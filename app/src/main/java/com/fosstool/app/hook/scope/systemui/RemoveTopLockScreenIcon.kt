package com.fosstool.app.hook.scope.systemui

import android.view.View
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import java.lang.reflect.Field

object RemoveTopLockScreenIcon : YukiBaseHooker() {
    override fun onHook() {
        "com.android.systemui.statusbar.phone.LockIcon"
            .toClassOrNull(appClassLoader)
            ?.method { name = "updateIconVisibility" }?.ignored()?.hook {
                before {
                    if (args.isNotEmpty()) args[0] = false
                }
            }

        val lockIconView = VariousClass(
            "com.android.keyguard.LockIconView",
            "com.android.keyguard.OplusLockIconView"
        ).toClassOrNull(appClassLoader)
        if (lockIconView == null) {
            YLog.error("RemoveTopLockScreenIcon: LockIconView not found")
            return
        }

        lockIconView.method { name = "updateColorAndBackgroundVisibility" }.ignored().hook {
            after {
                (lockIconView.findFieldByName("mLockIcon")?.get(instance) as? View)
                    ?.visibility = View.GONE
            }
        }

        "com.android.keyguard.LegacyLockIconViewController"
            .toClassOrNull(appClassLoader)?.let { controller ->
                val viewField = controller.findFieldByType(lockIconView)
                controller.method { name { it.contains("updateVisibility") } }.ignored().hook {
                    before {
                        (viewField?.get(instance) as? View)?.visibility = View.GONE
                        resultNull()
                    }
                }
            }
    }

    private fun Class<*>.findFieldByName(name: String): Field? {
        var c: Class<*>? = this
        while (c != null && c != Any::class.java) {
            c.declaredFields.firstOrNull { it.name == name }
                ?.let { return it.apply { isAccessible = true } }
            c = c.superclass
        }
        return null
    }

    private fun Class<*>.findFieldByType(type: Class<*>): Field? {
        var c: Class<*>? = this
        while (c != null && c != Any::class.java) {
            c.declaredFields.firstOrNull { type.isAssignableFrom(it.type) }
                ?.let { return it.apply { isAccessible = true } }
            c = c.superclass
        }
        return null
    }
}
