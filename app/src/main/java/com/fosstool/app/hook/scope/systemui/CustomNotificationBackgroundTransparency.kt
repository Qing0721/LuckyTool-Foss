package com.fosstool.app.hook.scope.systemui

import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.view.View
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.hasMethod
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.ThemeUtils.isNightMode
import com.fosstool.app.utils.getOSVersionCode
import com.fosstool.app.utils.safeOfNan

@Suppress("unused")
object CustomNotificationBackgroundTransparency : YukiBaseHooker() {

    private var defaultNotifyPanelTintList: ColorStateList? = null
    private var defaultNotifyPanelElevation = -1f
    private var customAlpha = -1

    override fun onHook() {
        if (getOSVersionCode < 25) return
        customAlpha = prefs(ModulePrefs).getInt("custom_notification_background_transparency", -1)
        dataChannel.wait<Int>("custom_notification_background_transparency") {
            customAlpha = it
        }

        val isOld =
            "com.android.systemui.statusbar.notification.row.NotificationBackgroundView".toClassOrNull()
                ?.hasMethod {
                    name = "drawCustom"
                    paramCount = 2
                } ?: false

        if (!isOld) "com.oplusos.systemui.statusbar.notification.row.OplusNotificationBackgroundView".toClass()
            .apply {
                method { name = "drawRegionBlur";paramCount = 2 }.remedys {
                    method { name = "draw";paramCount = 2 }
                }.hook {
                    before {
                        if (customAlpha < 0) return@before
                        modifyNotifyPanelAlpha(instance(), args().last().cast<Drawable>())
                    }
                }
                method { name = "draw";paramCount = 2;superClass() }.hook {
                    before {
                        if (customAlpha < 0) return@before
                        modifyNotifyPanelAlpha(instance(), args().last().cast<Drawable>())
                    }
                }
            }

        if (isOld) "com.android.systemui.statusbar.notification.row.NotificationBackgroundView".toClass()
            .apply {
                method { name = "draw";paramCount = 2 }.hook {
                    before { modifyNotifyPanelAlpha(instance(), args().last().cast<Drawable>()) }
                }
                method { name = "drawCustom";paramCount = 2 }.hook {
                    before { modifyNotifyPanelAlpha(instance(), args().last().cast<Drawable>()) }
                }.ignoredHookingFailure()
            }

        "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow".toClass()
            .apply {
                method { name = "updateBackgroundForGroupState";emptyParam() }.hook {
                    before {
                        if (customAlpha < 0) return@before
                        field { name = "mShowGroupBackgroundWhenExpanded" }.get(instance).setTrue()
                    }
                }
            }
    }

    private fun modifyNotifyPanelAlpha(view: View?, drawable: Drawable?, isTint: Boolean = false) {
        if (view == null) return
        if (defaultNotifyPanelTintList == null) defaultNotifyPanelTintList = view.backgroundTintList
        if (defaultNotifyPanelElevation < 0f) defaultNotifyPanelElevation = view.elevation
        val currentColor = if (view.context.isNightMode) 0xFF404040.toInt() else 0xFFFAFAFA.toInt()
        when {

            isTint.not() && view.parent?.parent?.javaClass?.name?.contains("ChildrenContainer") == true -> {
                drawable?.alpha = 0
            }

            else -> {
                currentColor.colorAlphaOf(customAlpha / 10.0F).also {
                    if (isTint) view.backgroundTintList = ColorStateList.valueOf(it)
                    else drawable?.setTint(it)
                }
            }
        }
        view.elevation = if (customAlpha >= 0) 0f else defaultNotifyPanelElevation
    }

    private fun Int.colorAlphaOf(value: Float) =
        safeOfNan { (255.coerceAtMost(0.coerceAtLeast((value * 255).toInt())) shl 24) + (0x00ffffff and this) }

}
