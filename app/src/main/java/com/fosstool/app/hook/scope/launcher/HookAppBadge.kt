package com.fosstool.app.hook.scope.launcher

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object HookAppBadge : YukiBaseHooker() {
    override fun onHook() {
        if (SDK >= A14) loadHooker(AppBadge) else loadHooker(AppBadgeC13)
    }

    object AppBadge : YukiBaseHooker() {
        override fun onHook() {
            val isShortcut = prefs(ModulePrefs).getBoolean("remove_app_shortcut_badge", false)
            val isWork = prefs(ModulePrefs).getBoolean("remove_app_work_badge", false)
            val isClone = prefs(ModulePrefs).getBoolean("remove_app_clone_badge", false)

            "com.android.launcher3.icons.BitmapInfo".toClass().apply {
                method { name = "applyFlags";paramCount = 3 }.hook {
                    before {
                        val drawableCreationFlags = args().last().int()
                        val badgeInfo = field { name = "badgeInfo" }.get(instance).any()
                        val flag = field { name = "flags" }.get(instance).int()
                        if ((drawableCreationFlags and 2) == 0) {
                            if (badgeInfo != null) {
                                if (isShortcut) resultNull()
                            } else if ((flag and 2) != 0) {
                            } else if ((flag and 4) != 0) {
                                if (isClone) resultNull()
                            } else if ((flag and 1) != 0) {
                                if (isWork) resultNull()
                            } else if ((flag and 4) != 0) {
                                if (isClone) resultNull()
                            }
                        }
                    }
                }
            }
        }
    }

    object AppBadgeC13 : YukiBaseHooker() {
        override fun onHook() {
            val isShortcut = prefs(ModulePrefs).getBoolean("remove_app_shortcut_badge", false)
            val isWork = prefs(ModulePrefs).getBoolean("remove_app_work_badge", false)
            val isClone = prefs(ModulePrefs).getBoolean("remove_app_clone_badge", false)

            "com.android.launcher3.icons.BitmapInfo".toClass().apply {
                method { name = "applyFlags";paramCount = 3 }.hook {
                    before {
                        val drawableCreationFlags = args().last().int()
                        val badgeInfo = field { name = "badgeInfo" }.get(instance).any()
                        val flag = field { name = "flags" }.get(instance).int()
                        if ((drawableCreationFlags and 2) == 0) {
                            if (badgeInfo != null) {
                                if (isShortcut) resultNull()
                            } else if ((flag and 2) != 0) {
                            } else if ((flag and 1) != 0) {
                                if (isWork) resultNull()
                            } else if ((flag and 4) != 0) {
                                if (isClone) resultNull()
                            }
                        }
                    }
                }
            }
        }
    }
}
