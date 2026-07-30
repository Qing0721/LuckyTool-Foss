package com.fosstool.app.hook.scope.systemui

import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.dp
import com.fosstool.app.utils.getOSVersionCode
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

@Suppress("unused")
object CustomNotificationBackgroundTransparency : YukiBaseHooker() {

    private const val BG_VIEW =
        "com.android.systemui.statusbar.notification.row.NotificationBackgroundView"
    private const val BG_VIEW_EXT =
        "com.oplus.systemui.statusbar.notification.row.NotificationBackgroundViewExtImp"
    private const val ROWS_BLUR_MANAGER = "com.oplus.systemui.blur.OplusRowsBlurManager"
    private const val EXPANDABLE_ROW =
        "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow"
    private const val SEEDLING_ITEM_ROW =
        "com.oplus.systemui.plugins.seedling.notification.widget.SeedlingItemRow"

    private var customAlpha = -1
    private var blurEffect = false

    private var mediaPanelBlur = false

    override fun onHook() {
        if (getOSVersionCode < 25) return
        customAlpha = prefs(ModulePrefs).getInt("custom_notification_background_transparency", -1)
        dataChannel.wait<Int>("custom_notification_background_transparency") { customAlpha = it }
        blurEffect =
            prefs(ModulePrefs).getBoolean("enable_notification_background_blur_effect", false)
        dataChannel.wait<Boolean>("enable_notification_background_blur_effect") { blurEffect = it }

        hookBackgroundViewDraw()
        hookOplusStyle()
        hookGroupState()
        if (getOSVersionCode < 34) {
            hookDrawBlur()
            hookDecideBlurDrawable()
            hookBlurMediaPanel()
            hookSeedlingItemRow()
        }
    }

    private fun hookBackgroundViewDraw() {
        val c = BG_VIEW.toClassOrNull(appClassLoader)
        if (c == null) {
            YLog.error("CustomNotificationBackgroundTransparency: $BG_VIEW not found", tag = "LuckyTool")
            return
        }
        c.method { name = "draw"; paramCount = 2 }.ignored().hook {
            before {
                if (customAlpha < 0 || blurEffect) return@before
                (args.lastOrNull() as? Drawable)?.alpha = customAlpha * 25
            }
        }
    }

    private fun hookOplusStyle() {
        val c = BG_VIEW_EXT.toClassOrNull(appClassLoader)
        if (c == null) {
            YLog.error(
                "CustomNotificationBackgroundTransparency: $BG_VIEW_EXT not found",
                tag = "LuckyTool"
            )
            return
        }
        c.method { name = "getOplusStyle"; superClass() }.ignored().hook {
            before {
                if (customAlpha < 0) return@before
                if (!blurEffect) resultFalse() else resultTrue()
            }
        }
    }

    private fun hookDrawBlur() {
        BG_VIEW_EXT.toClassOrNull(appClassLoader)
            ?.method { name = "drawBlur"; superClass() }?.ignored()?.hook {
                before {
                    if (customAlpha < 0) return@before
                    if (!blurEffect) resultFalse() else resultTrue()
                }
            }
    }

    private fun hookDecideBlurDrawable() {
        BG_VIEW_EXT.toClassOrNull(appClassLoader)
            ?.method { name = "decideBlurDrawable" }?.ignored()?.hook {
                before {
                    if (customAlpha < 0 || mediaPanelBlur) return@before
                    val delegate = runCatching {
                        XposedHelpers.callMethod(instance, "getRowBlurDelegate")
                    }.getOrNull() ?: return@before
                    runCatching { XposedHelpers.callMethod(delegate, "setBlurType", 1) }
                }
                after {
                    if (customAlpha < 0) return@after
                    val drawable = result as? Drawable ?: return@after
                    if (!drawable.isBackgroundBlurDrawable()) return@after
                    val radius = (customAlpha * 25).dp
                    val current = drawable.javaClass.findField("mBlurRadius")?.get(drawable) as? Int
                    if (current != radius) drawable.setBlurRadius(radius)
                }
            }
    }

    private fun hookBlurMediaPanel() {
        ROWS_BLUR_MANAGER.toClassOrNull(appClassLoader)
            ?.method { name = "blurMediaPanel" }?.ignored()?.hook {
                before {
                    if (customAlpha < 0) return@before
                    mediaPanelBlur = args.getOrNull(0) as? Boolean ?: false
                }
            }
    }

    private fun hookGroupState() {
        EXPANDABLE_ROW.toClassOrNull(appClassLoader)
            ?.method { name = "updateBackgroundForGroupState" }?.ignored()?.hook {
                before {
                    if (customAlpha < 0 || !blurEffect) return@before
                    instance.javaClass.findField("mShowGroupBackgroundWhenExpanded")
                        ?.set(instance, true)
                }
            }
    }

    private fun hookSeedlingItemRow() {
        val c = SEEDLING_ITEM_ROW.toClassOrNull(appClassLoader) ?: return
        c.method { name = "initBackground" }.ignored().hook {
            after {
                if (customAlpha < 0) return@after
                val view = instance as? View ?: return@after
                @Suppress("DiscouragedApi")
                val resId = view.resources.getIdentifier(
                    "notification_seed_action_rounded_bg", "drawable", packageName
                )
                if (resId == 0) return@after
                val drawable = runCatching { ContextCompat.getDrawable(view.context, resId) }
                    .getOrNull() ?: return@after
                val mutated = drawable.mutate().apply { alpha = customAlpha * 25 }
                val background = c.findField("mBackgroundNormal")?.get(instance) ?: return@after
                runCatching {
                    XposedHelpers.callMethod(background, "setCustomBackground", mutated)
                }
                runCatching { XposedHelpers.callMethod(background, "setTint", 0) }
            }
        }
    }

    private fun Drawable.isBackgroundBlurDrawable(): Boolean {
        var cls: Class<*>? = javaClass
        while (cls != null) {
            if (cls.name == "com.android.internal.graphics.drawable.BackgroundBlurDrawable") return true
            cls = cls.superclass
        }
        return false
    }

    private fun Any.setBlurRadius(blurRadius: Int) {
        runCatching { XposedHelpers.callMethod(this, "setBlurRadius", blurRadius) }
    }

    private fun Class<*>.findField(name: String): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            runCatching { return cls.getDeclaredField(name).also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }
}
