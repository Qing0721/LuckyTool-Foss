package com.fosstool.app.hook.scope.systemui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.buildOf
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.BooleanType

object RemovePowerMenuSosButton : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.controls.GlobalActionsUtils",
            "com.oplusos.systemui.common.util.GlobalActionsUtils",
            "com.oplus.systemui.common.util.GlobalActionsUtils",
        ).toClass().apply {
            method {
                name = "isShowSosButton"
                returnType = BooleanType
            }.hook { replaceToFalse() }
        }
    }
}

object HideLockScreenStatusBarDisplay : YukiBaseHooker() {
    override fun onHook() {
        "com.android.systemui.statusbar.phone.KeyguardStatusBarView".toClass().apply {
            method { name = "setVisibility" }.hook {
                before {
                    args().first().set(View.GONE)
                }
            }
            method { name = "onFinishInflate" }.hookAll {
                after {
                    instance<View>().isVisible = false
                }
            }
        }
    }
}

object AutoWakeUpFaceUnlockNotification : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.notification.helper.WakeupScreenHelper",
            "com.oplus.systemui.statusbar.notification.helper.WakeupScreenHelper",
            "com.oplus.systemui.notification.interruption.wakeup.WakeupScreenHelper",
        ).toClass().apply {
            method { name = "powerOnScreen" }.hookAll {
                before {
                }
            }
            method {
                name = "shouldWakeup"
                returnType = BooleanType
            }.hook { replaceToTrue() }
            method {
                name = "needWakeUpScreen"
                returnType = BooleanType
            }.hook { replaceToTrue() }
        }
    }
}

object RemoveStartRecordingOrCastingDialog : YukiBaseHooker() {
    override fun onHook() {
        "com.android.systemui.mediaprojection.MediaProjectionServiceHelper".toClass().apply {
            method {
                name = "hasProjectionPermission"
                returnType = BooleanType
            }.hook { replaceToTrue() }
        }
    }
}

object RunFloatingWindowTasksInForeground : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.zoom.zoomstate.ZoomStateManager".toClass().apply {
            method { name = "requestChangeZoomTask" }.hookAll {
                before {
                    for (i in 0..3) {
                        val v = runCatching { args(i).any() }.getOrNull()
                        if (v is Boolean) {
                            args(i).set(true)
                            break
                        }
                    }
                }
            }
        }
    }
}

object ForceShowToastIcon : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplus.systemui.toast.OplusSystemUIToast",
            "com.oplusos.systemui.toast.OplusSystemUIToast",
        ).toClass().apply {
            method {
                name = "hasIcon"
                returnType = BooleanType
            }.hook { replaceToTrue() }
            method {
                name = "showIcon"
                returnType = BooleanType
            }.hook { replaceToTrue() }
            method {
                name = "isShowIcon"
                returnType = BooleanType
            }.hook { replaceToTrue() }
        }
    }
}

object ShowManualLockButtonPowerMenu : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.android.systemui.oplusutils.OsBinderCacheUtils",
            "com.oplus.systemui.oplusutils.OsBinderCacheUtils",
        ).toClass().apply {
            method {
                name = "isSecure"
                returnType = BooleanType
            }.hook { replaceToTrue() }
            method {
                name = "isLockScreenDisabled"
                returnType = BooleanType
            }.hook { replaceToFalse() }
        }
        runCatching {
            "com.android.internal.widget.LockPatternUtils".toClass().apply {
                method {
                    name = "isSecure"
                    returnType = BooleanType
                }.hook {
                    after {
                    }
                }
            }
        }
    }
}

object ForceDisplayClockStyleOptions : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.common.feature.FeatureOption",
            "com.oplus.systemui.common.feature.FeatureOption",
        ).toClass().apply {
            method {
                name = "isSupportClockStyle"
                returnType = BooleanType
            }.hook { replaceToTrue() }
            method {
                name = "isSupportKeyguardClockStyle"
                returnType = BooleanType
            }.hook { replaceToTrue() }
            method {
                name = "isSupportRedHorizontalClock"
                returnType = BooleanType
            }.hook { replaceToTrue() }
        }
    }
}

object LockScreenCustomClockComponentStyle : YukiBaseHooker() {
    private var style = "0"

    override fun onHook() {
        style = prefs(ModulePrefs).getString("lock_screen_custom_clock_component_style", "0") ?: "0"
        dataChannel.wait<String>("lock_screen_custom_clock_component_style") { style = it }

        when {
            SDK == A14 -> hookW81()
            SDK < A14 -> hookV81()
        }
    }

    private fun hookW81() {
        "com.android.systemui.shared.clocks.ClockRegistry".toClassOrNull()?.apply {
            method { name = "getSettings" }.hookAll {
                after {
                    val mode = style
                    if (mode == "0") return@after
                    val settings = result ?: return@after
                    val clockId = runCatching {
                        settings.current().method { name = "getClockId" }.invoke<String>()
                    }.getOrNull() ?: return@after
                    if (clockId == "0") return@after
                    val dual = clockId.contains("DualClock")
                    val provider = when (mode) {
                        "1" -> if (dual)
                            "com.oplus.systemui.shared.clocks.DualClockProvider"
                        else
                            "com.oplus.systemui.shared.clocks.SingleClockProvider"
                        "2" -> if (dual)
                            "com.oplus.systemui.shared.clocks.RedHorizontalDualClockProvider"
                        else
                            "com.oplus.systemui.shared.clocks.RedHorizontalSingleClockProvider"
                        else -> return@after
                    }
                    if (provider.toClassOrNull() == null) return@after
                    val built = listOf(
                        "com.android.systemui.plugins.clocks.ClockSettings",
                        "com.android.systemui.plugins.ClockSettings",
                    ).firstNotNullOfOrNull { cn ->
                        val cls = cn.toClassOrNull() ?: return@firstNotNullOfOrNull null
                        runCatching {
                            cls.buildOf(provider, null) { paramCount(2) }
                        }.getOrNull()
                    }
                    if (built != null) result = built
                }
            }
        }

        "com.oplus.systemui.keyguard.clock.ClockSwitchHelper".toClassOrNull()?.apply {
            method { name = "buildAllClockProviders" }.hookAll {
                before {
                    if (style == "0") return@before
                    val ctx = runCatching {
                        instance.current().field { name = "mContext" }.any() as? Context
                    }.getOrNull() ?: return@before
                    val inflater = LayoutInflater.from(ctx)
                    val arg0 = runCatching { args().first().any() }.getOrNull()
                    val names = listOf(
                        "com.oplus.systemui.shared.clocks.SingleClockProvider",
                        "com.oplus.systemui.shared.clocks.DualClockProvider",
                        "com.oplus.systemui.shared.clocks.RedHorizontalSingleClockProvider",
                        "com.oplus.systemui.shared.clocks.RedHorizontalDualClockProvider",
                    )
                    val list = ArrayList<Any>()
                    for (n in names) {
                        val cls = n.toClassOrNull() ?: continue
                        val inst = runCatching {
                            cls.buildOf(ctx, inflater, arg0) { paramCount(3) }
                        }.getOrNull()
                        if (inst != null) list.add(inst)
                    }
                    if (list.isNotEmpty()) result = list
                }
            }
        }
    }

    private fun hookV81() {
        "com.android.keyguard.clock.SettingsWrapper".toClassOrNull()?.apply {
            method { name = "getLockScreenCustomClockFace" }.hookAll {
                after {
                    val mode = style
                    if (mode == "0") return@after
                    val face = result as? String ?: return@after
                    val dual = face.contains("DualClock")
                    val mapped = when {
                        mode == "1" && !dual ->
                            "com.oplusos.systemui.keyguard.clock.SingleClockController"
                        mode == "2" && !dual ->
                            "com.oplusos.systemui.keyguard.clock.RedHorizontalSingleClockController"
                        mode == "1" && dual ->
                            "com.oplusos.systemui.keyguard.clock.DualClockController"
                        mode == "2" && dual ->
                            "com.oplusos.systemui.keyguard.clock.RedHorizontalDualClockController"
                        else -> return@after
                    }
                    if (mapped.toClassOrNull() != null) result = mapped
                }
            }
        }

    }
}

object StatusbarCustomCarrierDisplayText : YukiBaseHooker() {
    override fun onHook() {
        val text = prefs(ModulePrefs).getString("statusbar_custom_carrier_display_text", "") ?: ""
        if (text.isEmpty()) return
        dataChannel.wait<String>("statusbar_custom_carrier_display_text") { }
        VariousClass(
            "com.oplusos.systemui.statusbar.widget.StatOperatorNameView",
            "com.oplus.systemui.statusbar.widget.OplusStatCarrierText",
        ).toClass().apply {
            method { name = "setText" }.hookAll {
                before {
                    args().first().set(text)
                }
            }
            method { name = "updateCarrierInfo" }.hookAll {
                after {
                    runCatching { instance<TextView>().text = text }
                }
            }
            method { name = "onConfigurationChanged" }.hookAll {
                after {
                    runCatching { instance<TextView>().text = text }
                }
            }
        }
    }
}

object LockScreenShowRealChargingTechnology : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("lock_screen_show_real_charging_technology", false)) return
        VariousClass(
            "com.oplus.charge.util.ChargeUtil",
            "com.oplusos.systemui.keyguard.charginganim.ChargingAnimationImpl",
        ).toClass().apply {
            method {
                name = "showTechnology"
                returnType = BooleanType
            }.hook { replaceToTrue() }
            method {
                name = "isShowTechnology"
                returnType = BooleanType
            }.hook { replaceToTrue() }
        }
    }
}

object ReplaceChargingTechnologyDrawingStyle : YukiBaseHooker() {
    override fun onHook() {
        val style = prefs(ModulePrefs).getString("replace_charging_technology_drawing_style", "0") ?: "0"
        if (style == "0") return
        VariousClass(
            "com.oplus.charge.util.ChargeUtil",
            "com.oplusos.systemui.keyguard.charginganim.ChargingAnimationImpl",
        ).toClass().apply {
            method { name = "getTechnologyStyle"; returnType = IntTypeCompat }.hook {
                replaceTo(style.toIntOrNull() ?: 0)
            }
            method { name = "getChargeTechStyle"; returnType = IntTypeCompat }.hook {
                replaceTo(style.toIntOrNull() ?: 0)
            }
        }
    }

    private val IntTypeCompat = com.highcapable.yukihookapi.hook.type.java.IntType
}
