package com.fosstool.app.hook.statusbar

import android.content.Context
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getOSVersionCode
import com.fosstool.app.utils.getScreenOrientation

@Suppress("UNUSED_VARIABLE", "DiscouragedApi")
object StatusBarLayout : YukiBaseHooker() {
    private var statusBarLeftMargin: Int = 0
    private var statusBarRightMargin: Int = 0
    private var statusBarTopMargin: Int = 0
    private var statusBarBottomMargin: Int = 0

    override fun onHook() {
        if (SDK != A13) return
        var mLeftLayout: LinearLayout? = null
        var mRightLayout: LinearLayout? = null
        var mCenterLayout: LinearLayout?
        var mStatusBar: ViewGroup? = null

        val layoutMode = prefs(ModulePrefs).getString("statusbar_layout_mode", "0")

        val isCompatibleMode =
            prefs(ModulePrefs).getBoolean("statusbar_layout_compatible_mode", false)
        val leftMargin =
            prefs(ModulePrefs).getInt("statusbar_layout_left_margin", 0)
        val rightMargin =
            prefs(ModulePrefs).getInt("statusbar_layout_right_margin", 0)

        fun updateCustomLayout(context: Context) {
            getScreenOrientation(context) {
                if (it) {
                    mLeftLayout?.setPadding(statusBarLeftMargin, 0, 0, 0)
                    mRightLayout?.setPadding(0, 0, statusBarRightMargin, 0)
                    mStatusBar?.setPadding(0, statusBarTopMargin, 0, statusBarBottomMargin)
                } else {
                    mLeftLayout?.setPadding(0, 0, 0, 0)
                    mRightLayout?.setPadding(0, 0, 0, 0)
                }
            }
        }

        fun updateDefaultLayout(context: Context, leftView: ViewGroup?, rightView: ViewGroup?) {
            if (!isCompatibleMode) return
            getScreenOrientation(context) {
                if (it) {
                    leftView?.setPadding(leftMargin, 0, 0, 0)
                    rightView?.setPadding(0, 0, rightMargin, 0)
                } else {
                    leftView?.setPadding(0, 0, 0, 0)
                    rightView?.setPadding(0, 0, 0, 0)
                }
            }
        }

        fun setCustomMargin() {
            if (!isCompatibleMode) return
            if (leftMargin != 0) statusBarLeftMargin = leftMargin
            if (rightMargin != 0) statusBarRightMargin = rightMargin
        }

        "com.android.systemui.ScreenDecorations\$DisplayCutoutView".toClass().apply {
            method {
                name = "boundsFromDirection"
                paramCount = 3
            }.hook {
                before {
                    if (isCompatibleMode) args(1).set(0)
                }
            }
        }

        VariousClass(
            "com.android.systemui.statusbar.phone.CollapsedStatusBarFragment",
            "com.android.systemui.statusbar.phone.fragment.CollapsedStatusBarFragment"
        ).toClass().apply {
            method {
                name = "onViewCreated"
                paramCount = 2
            }.hook {
                after {
                    val phoneStatusBarView = args(0).cast<ViewGroup>()!!
                    val context = phoneStatusBarView.context
                    val res = phoneStatusBarView.resources
                    val statusBarId = res?.getIdentifier(
                        "status_bar", "id",
                        StatusBarLayout.packageName
                    )
                    val statusBarContentsId =
                        res?.getIdentifier("status_bar_contents", "id", StatusBarLayout.packageName)

                    val statusBarLeftSideId =
                        res?.getIdentifier(
                            "status_bar_left_side", "id",
                            StatusBarLayout.packageName
                        )
                    val clockId = res?.getIdentifier("clock", "id", StatusBarLayout.packageName)
                    val systemPromptViewId =
                        res?.getIdentifier("system_prompt_view", "id", StatusBarLayout.packageName)
                    val notificationIconAreaInnerId =
                        res?.getIdentifier(
                            "notification_icon_area_inner", "id",
                            StatusBarLayout.packageName
                        )

                    val systemIconAreaId = res?.getIdentifier(
                        "system_icon_area", "id",
                        StatusBarLayout.packageName
                    )
                    val statusIconsId = res?.getIdentifier(
                        "statusIcons", "id",
                        StatusBarLayout.packageName
                    )
                    val batteryId = res?.getIdentifier("battery", "id", StatusBarLayout.packageName)

                    mStatusBar =
                        statusBarId?.let { phoneStatusBarView.findViewById(it) } ?: return@after
                    val statusBarContents: ViewGroup? =
                        statusBarContentsId?.let { phoneStatusBarView.findViewById(it) }
                    val statusBarLeftSide: ViewGroup? =
                        statusBarLeftSideId?.let { phoneStatusBarView.findViewById(it) }
                    val clock: TextView? = clockId?.let { phoneStatusBarView.findViewById(it) }
                    val systemPromptView: ImageView? =
                        systemPromptViewId?.let { phoneStatusBarView.findViewById(it) }
                    val notificationIconAreaInner: ViewGroup? =
                        notificationIconAreaInnerId?.let { phoneStatusBarView.findViewById(it) }
                    val systemIconArea: ViewGroup? =
                        systemIconAreaId?.let { phoneStatusBarView.findViewById(it) }
                    val statusIcons: ViewGroup? =
                        statusIconsId?.let { phoneStatusBarView.findViewById(it) }
                    val battery: ViewGroup? =
                        batteryId?.let { phoneStatusBarView.findViewById(it) }

                    mLeftLayout = LinearLayout(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f
                        )
                        gravity = Gravity.START or Gravity.CENTER_VERTICAL
                    }

                    mCenterLayout = LinearLayout(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.MATCH_PARENT
                        )
                        gravity = Gravity.CENTER or Gravity.CENTER_VERTICAL
                    }

                    mRightLayout = LinearLayout(context).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f
                        )
                        gravity = Gravity.END or Gravity.CENTER_VERTICAL
                    }

                    if (layoutMode == "0" && isCompatibleMode) {
                        setCustomMargin()
                        updateDefaultLayout(context, statusBarLeftSide, systemIconArea)
                        return@after
                    }
                    if (layoutMode.isBlank() || layoutMode == "0") return@after

                    (clock?.parent as ViewGroup).removeView(clock)
                    (statusBarLeftSide?.parent as ViewGroup).removeView(statusBarLeftSide)
                    (systemIconArea?.parent as ViewGroup).removeAllViews()

                    statusBarLeftSide.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    mLeftLayout?.addView(statusBarLeftSide)
                    mCenterLayout?.addView(clock)
                    mRightLayout?.addView(systemIconArea)

                    statusBarContents?.addView(mLeftLayout, 0)
                    statusBarContents?.addView(mCenterLayout)
                    statusBarContents?.addView(mRightLayout)

                    statusBarLeftMargin = mStatusBar?.paddingLeft!!
                    statusBarRightMargin = mStatusBar?.paddingRight!!
                    statusBarTopMargin = mStatusBar?.paddingTop!!
                    statusBarBottomMargin = mStatusBar?.paddingBottom!!

                    setCustomMargin()
                    updateCustomLayout(context)
                }
            }
            method { name = "onDestroyView" }.hook {
                if (layoutMode != "0" && getOSVersionCode == 26) intercept()
            }
        }

        "com.android.systemui.statusbar.phone.PhoneStatusBarView".toClass().apply {
            method { name = "updateLayoutForCutout" }.hook {
                after {
                    if (isCompatibleMode) updateCustomLayout(instance<ViewGroup>().context)
                }
            }
        }

        "com.oplus.systemui.statusbar.phone.KeyguardStatusBarViewExImpl".toClass().apply {
            method { name = "onFinishInflate" }.hook {
                after {
                    if (isCompatibleMode) field {
                        name {
                            it.startsWith("keyguardStatus")
                            it.endsWith("LeftContView")
                        }
                    }.get(instance).cast<ViewGroup>()?.setPadding(leftMargin, 0, 0, 0)
                }
            }
        }
    }
}
