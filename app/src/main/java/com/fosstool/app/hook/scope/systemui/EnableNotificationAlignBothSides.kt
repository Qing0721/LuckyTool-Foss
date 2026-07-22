package com.fosstool.app.hook.scope.systemui

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.core.view.*
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getScreenOrientation

object EnableNotificationAlignBothSides : YukiBaseHooker() {

    private var qsPanelPaddingPx = 0
    override fun onHook() {
        "com.android.systemui.statusbar.notification.row.ExpandableNotificationRow".toClass()
            .apply {
                method { name = "onFinishInflate" }.hook {
                    after { instance<ViewGroup>().setViewWidth() }
                }
                method { name = "onLayout" }.hook {
                    after { instance<ViewGroup>().setViewWidth() }
                }
            }

        if (SDK >= A13) loadHooker(OtherNotification) else loadHooker(OtherNotificationC12)
    }

    private object OtherNotification : YukiBaseHooker() {
        override fun onHook() {
            VariousClass(
                "com.android.systemui.media.KeyguardMediaController",
                "com.android.systemui.media.controls.ui.KeyguardMediaController"
            ).toClass().apply {
                method { name = "setVisibility";paramCount = 2 }.hook {
                    before {
                        val viewGroup = args().first().cast<ViewGroup>() ?: return@before
                        val visible = args().last().cast<Int>() ?: return@before
                        val count = viewGroup.childCount
                        if ((visible == 0) && (count > 0)) {
                            if (viewGroup.width != 0) viewGroup.setViewWidth()
                        }
                    }
                }
            }

            val ubiquitousExpandableRow =
                "com.oplusos.systemui.statusbar.notification.row.UbiquitousExpandableRow"
            ubiquitousExpandableRow.toClassOrNull()?.apply {
                method { name = "onFinishInflate" }.hook {
                    after { instance<ViewGroup>().setViewWidth() }
                }
                method { name = "onLayout" }.hook {
                    after { instance<ViewGroup>().setViewWidth() }
                }
            }
        }
    }

    private object OtherNotificationC12 : YukiBaseHooker() {
        override fun onHook() {
            "com.oplusos.systemui.media.OplusMediaHost".toClass().apply {
                method { name = "updateViewVisibility" }.hook {
                    before {
                        val hostView = field { name = "hostView";superClass() }.get(instance)
                            .cast<ViewGroup>() ?: return@before
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
}
