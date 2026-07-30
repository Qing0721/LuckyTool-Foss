package com.fosstool.app.hook.scope.launcher

import android.graphics.drawable.Drawable
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object HookAppBadge : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode >= 30) loadHooker(AppBadge) else loadHooker(AppBadgeC13)
    }

    private fun hookApplyFlags(
        isShortcut: Boolean,
        isWork: Boolean,
        isClone: Boolean,
        newStyle: Boolean,
    ) {
        val clazz = "com.android.launcher3.icons.BitmapInfo".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("HookAppBadge: BitmapInfo not found")
            return
        }
        clazz.method {
            name = "applyFlags"
            if (!newStyle) paramCount = 3
        }.ignored().hook {
            before {
                val host = instanceOrNull ?: return@before
                val drawableCreationFlags = (
                    if (newStyle) args.firstOrNull { it is Int } else args.lastOrNull()
                    ) as? Int ?: return@before
                val badgeInfo = runCatching {
                    clazz.field { name = "badgeInfo"; superClass() }.ignored().get(host).any()
                }.getOrNull()
                val flag = runCatching {
                    clazz.field { name = "flags"; superClass() }.ignored().get(host).any() as? Int
                }.getOrNull() ?: return@before
                if ((drawableCreationFlags and 2) != 0) return@before
                if (newStyle) {
                    if (badgeInfo != null && isShortcut) result = null
                    if ((flag and 4) != 0) {
                        if (isClone) result = null
                    } else if ((flag and 1) != 0) {
                        if (isWork) result = null
                    }
                } else {
                    if (badgeInfo != null) {
                        if (isShortcut) result = null
                    } else if ((flag and 2) == 0) {
                        if ((flag and 1) != 0) {
                            if (isWork) result = null
                        } else if ((flag and 4) != 0) {
                            if (isClone) result = null
                        }
                    }
                }
            }
        }
    }

    private fun hookCloneAppDrawable(isClone: Boolean) {
        val cacheUtils = "com.android.common.util.CacheUtils".toClassOrNull(appClassLoader)
        if (cacheUtils == null) {
            YLog.error("HookAppBadge: CacheUtils not found")
            return
        }
        cacheUtils.method {
            name = "getCloneAppDrawable"
            returnType = Drawable::class.java
        }.ignored().hook {
            after { if (isClone) result = null }
        }
    }

    object AppBadge : YukiBaseHooker() {
        override fun onHook() {
            val isClone = prefs(ModulePrefs).getBoolean("remove_app_clone_badge", false)
            hookApplyFlags(
                isShortcut = prefs(ModulePrefs).getBoolean("remove_app_shortcut_badge", false),
                isWork = prefs(ModulePrefs).getBoolean("remove_app_work_badge", false),
                isClone = isClone,
                newStyle = true,
            )
            hookCloneAppDrawable(isClone)
        }
    }

    object AppBadgeC13 : YukiBaseHooker() {
        override fun onHook() {
            hookApplyFlags(
                isShortcut = prefs(ModulePrefs).getBoolean("remove_app_shortcut_badge", false),
                isWork = prefs(ModulePrefs).getBoolean("remove_app_work_badge", false),
                isClone = prefs(ModulePrefs).getBoolean("remove_app_clone_badge", false),
                newStyle = false,
            )
        }
    }
}
