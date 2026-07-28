package com.fosstool.app.hook.scope.systemui

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getScreenOrientation
import java.lang.reflect.Field

object EnableNotificationAlignBothSides : YukiBaseHooker() {

    private var qsPanelPaddingPx = 0
    override fun onHook() {

        "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow"
            .toClassOrNull(appClassLoader)?.let { c ->
                arrayOf(
                    "onFinishInflate",
                    "onLayout",
                    "reInflateViews",
                    "onConfigurationChanged",
                    "onUiModeChanged",
                    "onNotificationUpdated"
                ).forEach { methodName ->
                    c.method { name = methodName }.ignored().hook {
                        after {
                            (instance as? ViewGroup)?.setViewWidth()
                        }
                    }
                }
            }

        if (SDK >= A13) loadHooker(OtherNotification) else loadHooker(OtherNotificationC12)
    }

    private object OtherNotification : YukiBaseHooker() {
        override fun onHook() {
            VariousClass(
                "com.android.systemui.media.KeyguardMediaController",
                "com.android.systemui.media.controls.ui.KeyguardMediaController",
                "com.android.systemui.media.controls.ui.controller.KeyguardMediaController"
            ).toClassOrNull(appClassLoader)
                ?.method { name = "setVisibility"; paramCount = 2 }?.ignored()?.hook {
                    before {
                        val viewGroup = args.getOrNull(0) as? ViewGroup ?: return@before
                        val visible = args.getOrNull(1) as? Int ?: return@before
                        val count = viewGroup.childCount
                        if ((visible == 0) && (count > 0)) {
                            if (viewGroup.width != 0) viewGroup.setViewWidth()
                        }
                    }
                }

            VariousClass(
                "com.oplusos.systemui.statusbar.notification.row.UbiquitousExpandableRow",
                "com.oplus.systemui.statusbar.notification.row.UbiquitousExpandableRow"
            ).toClassOrNull(appClassLoader)?.let { c ->
                arrayOf("onFinishInflate", "onLayout", "reInflateViews").forEach { methodName ->
                    c.method { name = methodName }.ignored().hook {
                        after {
                            (instance as? ViewGroup)?.setViewWidth()
                        }
                    }
                }
            }

            "com.oplus.systemui.plugins.seedling.notification.NotificationSeedingController"
                .toClassOrNull(appClassLoader)?.let { c ->
                    arrayOf(
                        "onCreateView",
                        "onUpdate",
                        "refreshNotificationPosition",
                        "updateNotifSeedingViews"
                    ).forEach { methodName ->
                        c.method { name = methodName }.ignored().hook {
                            after {
                                (c.findField("parent")?.get(instance) as? ViewGroup)?.setViewWidth()
                            }
                        }
                    }
                }

            "com.oplus.systemui.statusbar.notification.customcard.OplusCustomRow"
                .toClassOrNull(appClassLoader)?.let { c ->
                    arrayOf("onFinishInflate", "onLayout", "onConfigurationChanged")
                        .forEach { methodName ->
                            c.method { name = methodName }.ignored().hook {
                                after {
                                    (instance as? ViewGroup)?.setViewWidth()
                                }
                            }
                        }
                }
        }
    }

    private object OtherNotificationC12 : YukiBaseHooker() {
        override fun onHook() {
            "com.oplusos.systemui.media.OplusMediaHost"
                .toClassOrNull(appClassLoader)?.let { c ->
                    c.method { name = "updateViewVisibility" }.ignored().hook {
                        before {
                            val hostView =
                                c.findField("hostView")?.get(instance) as? ViewGroup
                                    ?: return@before
                            val visible = hostView.visibility
                            val count = hostView.childCount
                            if ((visible == 0) && (count > 0)) {
                                if (hostView.width != 0) hostView.setViewWidth()
                            }
                        }
                    }
                }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun View.setViewWidth() {
        if (qsPanelPaddingPx == 0) qsPanelPaddingPx = resources.getDimensionPixelSize(
            resources.getIdentifier("qs_header_panel_side_padding", "dimen", packageName)
        )
        getScreenOrientation(this) {
            if (layoutParams != null) layoutParams = ViewGroup.LayoutParams(layoutParams).apply {
                width = if (it) resources.displayMetrics.widthPixels - (qsPanelPaddingPx * 2)
                else -1
            }
        }
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
