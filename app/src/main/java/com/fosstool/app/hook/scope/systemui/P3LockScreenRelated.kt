package com.fosstool.app.hook.scope.systemui

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.res.Resources
import android.os.Handler
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.buildOf
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getOSVersionCode
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

object RemovePowerMenuSosButton : YukiBaseHooker() {
    override fun onHook() {

        VariousClass(
            "com.oplusos.systemui.controls.GlobalActionsUtils",
            "com.oplusos.systemui.common.util.GlobalActionsUtils",
            "com.oplus.systemui.common.util.GlobalActionsUtils",
        ).toClassOrNull(appClassLoader)
            ?.method { name = "isShowSosButton"; returnType = BooleanType }?.ignored()?.hook { replaceToFalse() }

        val shutdownView = VariousClass(
            "com.oplusos.systemui.controls.OplusShutdownView",
            "com.oplus.systemui.shutdown.OplusShutdownView",
        ).toClassOrNull(appClassLoader)
        if (shutdownView == null) {
            YLog.error("RemovePowerMenuSosButton: OplusShutdownView not found")
        } else {
            shutdownView.method { name = "isShowEmergency" }.ignored().hook { replaceToFalse() }
        }
    }
}

object HideLockScreenStatusBarDisplay : YukiBaseHooker() {
    override fun onHook() {
        "com.android.systemui.statusbar.phone.KeyguardStatusBarView"
            .toClassOrNull(appClassLoader)?.let { c ->
                c.method { name = "setVisibility" }.ignored().hook {
                    before {
                        if (args.isNotEmpty()) args[0] = View.GONE
                    }
                }
                c.method { name = "onFinishInflate" }.ignored().hook {
                    after {
                        (instance as? View)?.isVisible = false
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
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "powerOnScreen" }.ignored().hook { before { } }
            c.method { name = "shouldWakeup"; returnType = BooleanType }.ignored().hook { replaceToTrue() }
            c.method { name = "needWakeUpScreen"; returnType = BooleanType }.ignored().hook { replaceToTrue() }
        }
    }
}

object RemoveStartRecordingOrCastingDialog : YukiBaseHooker() {
    override fun onHook() {
        "com.android.systemui.mediaprojection.MediaProjectionServiceHelper"
            .toClassOrNull(appClassLoader)
            ?.method { name = "hasProjectionPermission"; returnType = BooleanType }?.ignored()?.hook { replaceToTrue() }
    }
}

object RunFloatingWindowTasksInForeground : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.zoom.zoomstate.ZoomStateManager"
            .toClassOrNull(appClassLoader)
            ?.method { name = "requestChangeZoomTask" }?.ignored()?.hook {
                before {
                    for (i in 0 until minOf(4, args.size)) {
                        if (args[i] is Boolean) {
                            args[i] = true
                            break
                        }
                    }
                }
            }

        "com.oplus.zoom.ui.floathandle.FloatHandleController"
            .toClassOrNull(appClassLoader)
            ?.method { name = "onTaskMovedToFront" }?.ignored()?.hook {
                before {
                    result = null
                }
            }
    }
}

object ForceShowToastIcon : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplus.systemui.toast.OplusSystemUIToast",
            "com.oplusos.systemui.toast.OplusSystemUIToast",
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "hasIcon"; returnType = BooleanType }.ignored().hook { replaceToTrue() }
            c.method { name = "showIcon"; returnType = BooleanType }.ignored().hook { replaceToTrue() }
            c.method { name = "isShowIcon"; returnType = BooleanType }.ignored().hook { replaceToTrue() }
        }
    }
}

object ShowManualLockButtonPowerMenu : YukiBaseHooker() {

    private const val SHUTDOWN_VIEW_CONTROL = "com.oplus.systemui.shutdown.ShutdownViewControl"
    private const val GLOBAL_ACTIONS_DIALOG = "com.oplus.systemui.shutdown.OplusGlobalActionsDialog"
    private const val KEYGUARD_UPDATE_MONITOR = "com.android.keyguard.KeyguardUpdateMonitor"
    private const val KEYGUARD_VIEW_MEDIATOR = "com.android.systemui.keyguard.KeyguardViewMediator"
    private const val OS_BINDER_CACHE_UTILS = "com.android.systemui.oplusutils.OsBinderCacheUtils"
    private const val LOCK_PATTERN_UTILS = "com.android.internal.widget.LockPatternUtils"
    private const val FLAVOR_ONE_FEATURE = "com.oplusos.systemui.common.feature.FlavorOneFeatureOption"

    override fun onHook() {
        val newPipeline = getOSVersionCode >= 35

        val control = SHUTDOWN_VIEW_CONTROL.toClassOrNull(appClassLoader)
        if (control == null) {
            YLog.error("ShowManualLockButtonPowerMenu: ShutdownViewControl not found")
        } else if (newPipeline) {

            control.constructor { param(ContextClass) }.ignored().hook {
                after {
                    if (isFlavorOneDevice()) return@after
                    val context = args.getOrNull(0) as? Context ?: return@after
                    applyManuallyLock(instance ?: return@after, context, canBeSeenByBiometrics(context))
                }
            }
        } else {

            control.method { name = "initManuallyLock" }.ignored().hook {
                after {
                    if (isFlavorOneDevice()) return@after
                    val context = args.getOrNull(0) as? Context ?: return@after
                    val self = instance ?: return@after
                    val canBeSeen = runCatching {
                        XposedHelpers.callMethod(self, "manuallyLockCanBeSeen", context) as? Boolean
                    }.getOrNull() ?: return@after
                    applyManuallyLock(self, context, canBeSeen)
                }
            }
        }

        GLOBAL_ACTIONS_DIALOG.toClassOrNull(appClassLoader)
            ?.method { name = "showOrHideDialog" }?.ignored()?.hook {
                after {
                    if (isFlavorOneDevice()) return@after
                    val self = instance ?: return@after
                    val ext = runCatching { XposedHelpers.getObjectField(self, "mExt") }
                        .getOrNull() ?: return@after
                    val lockPatternUtils =
                        runCatching { XposedHelpers.getObjectField(self, "mLockPatternUtils") }.getOrNull()
                    val dialog = runCatching { XposedHelpers.getObjectField(self, "mDialog") }.getOrNull()
                    val shutdownViewControl =
                        runCatching { XposedHelpers.getObjectField(self, "mShutdownViewControl") }.getOrNull()
                    runCatching {
                        if (newPipeline) {

                            val listener = XposedHelpers.getObjectField(ext, "mOnManuallyLock")
                            XposedHelpers.setObjectField(ext, "mLockPatternUtils", lockPatternUtils)
                            XposedHelpers.setObjectField(ext, "mDialog", dialog)
                            if (shutdownViewControl != null) {
                                XposedHelpers.callMethod(
                                    shutdownViewControl, "setOnManuallyLockListener", listener
                                )
                            }
                        } else {

                            XposedHelpers.callMethod(
                                ext, "setForManuallyLock", lockPatternUtils, dialog, shutdownViewControl
                            )
                            XposedHelpers.callMethod(ext, "registerForManuallyLock")
                        }
                    }
                }
            }

        KEYGUARD_UPDATE_MONITOR.toClassOrNull(appClassLoader)?.let { monitor ->

            monitor.constructor { }.ignored().hook {
                after {
                    if (isFlavorOneDevice()) return@after
                    val self = instance ?: return@after
                    val context = runCatching {
                        XposedHelpers.getObjectField(self, "mContext") as? Context
                    }.getOrNull() ?: return@after
                    val handler = runCatching {
                        XposedHelpers.getObjectField(self, "mHandler") as? Handler
                    }.getOrNull() ?: return@after
                    val ex = manuallyLockEx() ?: return@after
                    runCatching { XposedHelpers.callMethod(ex, "onCreate", context, handler) }
                }
            }

            monitor.method { name = "setKeyguardGoingAway" }.ignored().hook {
                before {
                    if (isFlavorOneDevice()) return@before
                    val locked = isManuallyLockedOn() ?: return@before
                    if ((args.getOrNull(0) as? Boolean) == true && locked) clearManuallyLockedOn()
                }
            }
        }

        KEYGUARD_VIEW_MEDIATOR.toClassOrNull(appClassLoader)
            ?.method { name = "handleStartKeyguardExitAnimation" }?.ignored()?.hook {
                after {
                    if (isFlavorOneDevice()) return@after
                    if (isManuallyLockedOn() == true) clearManuallyLockedOn()
                }
            }
    }

    private fun applyManuallyLock(instance: Any, context: Context, canBeSeen: Boolean) {
        val locked = isManuallyLockedOn() ?: return
        val userId = currentUserId()
        val strongAuth = runCatching {
            val lpu = LOCK_PATTERN_UTILS.toClassOrNull(appClassLoader)
                ?.getConstructor(Context::class.java)?.newInstance(context) ?: return@runCatching null
            XposedHelpers.callMethod(lpu, "getStrongAuthForUser", userId) as? Int
        }.getOrNull()
        val cr = context.contentResolver
        val childrenMode = Settings.Global.getInt(cr, "children_mode_on", 0) == 1
        val studyCenter = Settings.Global.getInt(cr, "STUDY_CENTER_MODE", 0) == 1

        val show = canBeSeen && !locked && strongAuth != 1 && !(childrenMode && studyCenter)
        runCatching { XposedHelpers.setObjectField(instance, "mShouldShowManuallyLock", show) }
        runCatching {
            val view = XposedHelpers.getObjectField(instance, "mOplusShutdownView") ?: return
            XposedHelpers.callMethod(view, "setManuallyLockEnable", show)
        }
    }

    @SuppressLint("MissingPermission")
    private fun canBeSeenByBiometrics(context: Context): Boolean {
        val userId = currentUserId()
        val cr = context.contentResolver
        val faceOk = runCatching {
            val fm = context.getSystemService("face") ?: return@runCatching false
            (XposedHelpers.callMethod(fm, "hasEnrolledTemplates", userId) as? Boolean == true) &&
                Settings.Secure.getInt(cr, "oplus_customize_face_unlock_switch", -1) == 1
        }.getOrDefault(false)
        if (faceOk) return true
        return runCatching {
            val fp = context.getSystemService("fingerprint") ?: return@runCatching false
            val hardware = XposedHelpers.callMethod(fp, "isHardwareDetected") as? Boolean == true
            val enrolled = XposedHelpers.callMethod(fp, "hasEnrolledFingerprints", userId) as? Boolean == true
            val switchOn = Settings.Secure.getInt(cr, "oplus_customize_fingerprint_unlock_switch", -1) == 1
            if (!hardware || !enrolled || !switchOn) return@runCatching false
            val dpm = context.getSystemService(DevicePolicyManager::class.java)
                ?: return@runCatching true
            val flags = runCatching {
                XposedHelpers.callMethod(dpm, "getKeyguardDisabledFeatures", null, userId) as? Int
            }.getOrNull() ?: 0

            (flags and 32) == 0
        }.getOrDefault(false)
    }

    private fun currentUserId(): Int = runCatching {
        val c = OS_BINDER_CACHE_UTILS.toClassOrNull(appClassLoader) ?: return@runCatching 0
        XposedHelpers.callStaticMethod(c, "getCurrentUserId") as? Int ?: 0
    }.getOrDefault(0)

    private fun manuallyLockEx(): Any? = runCatching {
        val depEx = "com.android.systemui.DependencyEx".toClassOrNull(appClassLoader) ?: return null
        val holder = depEx.declaredFields.firstOrNull { it.type == depEx }
            ?.also { it.isAccessible = true }?.get(null) ?: return null
        val target = "com.android.systemui.shutdown.ShutDownDependencyEx"
            .toClassOrNull(appClassLoader) ?: return null
        val dep = XposedHelpers.callMethod(holder, "getDependency", target) ?: return null
        XposedHelpers.callMethod(dep, "getOplusManuallyLockEx")
    }.getOrNull()

    private fun isManuallyLockedOn(): Boolean? = runCatching {
        XposedHelpers.callMethod(manuallyLockEx() ?: return null, "isManuallyLockedOn") as? Boolean
    }.getOrNull()

    private fun clearManuallyLockedOn() {
        runCatching {
            XposedHelpers.callMethod(manuallyLockEx() ?: return, "setManuallyLockedOn", false)
        }
    }

    private fun isFlavorOneDevice(): Boolean = runCatching {
        val c = FLAVOR_ONE_FEATURE.toClassOrNull(appClassLoader) ?: return@runCatching false
        val receiver = if (SDK >= A14) {
            c.declaredFields.firstOrNull { it.name == "INSTANCE" }?.also { it.isAccessible = true }?.get(null)
        } else null
        val r = if (receiver != null) {
            XposedHelpers.callMethod(receiver, "isFlavorOneDevice")
        } else {
            XposedHelpers.callStaticMethod(c, "isFlavorOneDevice")
        }
        r as? Boolean == true
    }.getOrDefault(false)
}

object ForceDisplayClockStyleOptions : YukiBaseHooker() {
    override fun onHook() {
        if (SDK != A13) return

        VariousClass(
            "com.oplusos.systemui.keyguard.keyguardsetting.KeyguardLauncherPageProvider",
            "com.oplus.systemui.keyguard.keyguardsetting.KeyguardLauncherPageProvider",
        ).toClassOrNull(appClassLoader)
            ?.method { name = "initKeyguardLandClockPf" }?.ignored()?.hook {
                before {
                    if (!isFlavorTwoDevice()) return@before
                    val self = instance ?: return@before

                    @Suppress("UNCHECKED_CAST")
                    val list = args.getOrNull(0) as? ArrayList<Any>
                    val context = runCatching {
                        XposedHelpers.callMethod(self, "getContext") as? Context
                    }.getOrNull() ?: return@before
                    val title = runCatching {
                        context.getString(
                            context.resources.getIdentifier(
                                "oplus_keyguard_land_clock_type_title", "string",
                                ForceDisplayClockStyleOptions.packageName
                            )
                        )
                    }.getOrNull() ?: return@before

                    val bean = runCatching {
                        XposedHelpers.callMethod(
                            self, "createPerfrenceBean",
                            "TYPE_PREFRENCE_JUMP", "key_keyguard_land_clock_screen",
                            70, title, "key_keyguard_category"
                        )
                    }.getOrNull() ?: return@before

                    runCatching {
                        XposedHelpers.callMethod(bean, "setIntentPackage", "com.android.systemui")
                    }
                    runCatching {
                        XposedHelpers.callMethod(
                            bean, "setIntentClass",
                            "com.oplus.systemui.keyguard.keyguardsetting.KeyguardLandClockActivity"
                        )
                    }
                    runCatching {
                        val map = XposedHelpers.getObjectField(self, "preferenceHashMap")
                        XposedHelpers.callMethod(
                            self, "addPreferenceMap", map, "key_keyguard_land_clock_screen", bean
                        )
                    }
                    list?.add(bean)

                    resultNull()
                }
            }
    }

    private fun isFlavorTwoDevice(): Boolean = runCatching {
        val c = "com.oplusos.systemui.common.feature.FlavorTwoFeatureOption"
            .toClassOrNull(appClassLoader) ?: return@runCatching false
        XposedHelpers.callStaticMethod(c, "isFlavorTwoDevice") as? Boolean == true
    }.getOrDefault(false)
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
        "com.android.systemui.shared.clocks.ClockRegistry"
            .toClassOrNull(appClassLoader)
            ?.method { name = "getSettings" }?.ignored()?.hook {
                after {
                    val mode = style
                    if (mode == "0") return@after
                    val settings = result ?: return@after
                    val clockId = runCatching {
                        XposedHelpers.callMethod(settings, "getClockId") as? String
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
                    if (provider.toClassOrNull(appClassLoader) == null) return@after
                    val built = listOf(
                        "com.android.systemui.plugins.clocks.ClockSettings",
                        "com.android.systemui.plugins.ClockSettings",
                    ).firstNotNullOfOrNull { cn ->
                        val cls = cn.toClassOrNull(appClassLoader) ?: return@firstNotNullOfOrNull null
                        runCatching {
                            cls.buildOf(provider, null) { paramCount(2) }
                        }.getOrNull()
                    }
                    if (built != null) result = built
                }
            }

        "com.oplus.systemui.keyguard.clock.ClockSwitchHelper"
            .toClassOrNull(appClassLoader)
            ?.method { name = "buildAllClockProviders" }?.ignored()?.hook {
                before {
                    if (style == "0") return@before
                    val ctx = runCatching {
                        instance.javaClass.findField("mContext")?.get(instance) as? Context
                    }.getOrNull() ?: return@before
                    val inflater = LayoutInflater.from(ctx)
                    val arg0 = args.getOrNull(0)
                    val names = listOf(
                        "com.oplus.systemui.shared.clocks.SingleClockProvider",
                        "com.oplus.systemui.shared.clocks.DualClockProvider",
                        "com.oplus.systemui.shared.clocks.RedHorizontalSingleClockProvider",
                        "com.oplus.systemui.shared.clocks.RedHorizontalDualClockProvider",
                    )
                    val list = ArrayList<Any>()
                    for (n in names) {
                        val cls = n.toClassOrNull(appClassLoader) ?: continue
                        val inst = runCatching {
                            cls.buildOf(ctx, inflater, arg0) { paramCount(3) }
                        }.getOrNull()
                        if (inst != null) list.add(inst)
                    }
                    if (list.isNotEmpty()) result = list
                }
            }
    }

    private fun hookV81() {
        "com.android.keyguard.clock.SettingsWrapper"
            .toClassOrNull(appClassLoader)
            ?.method { name = "getLockScreenCustomClockFace" }?.ignored()?.hook {
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
                    if (mapped.toClassOrNull(appClassLoader) != null) result = mapped
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

object StatusbarCustomCarrierDisplayText : YukiBaseHooker() {
    override fun onHook() {
        val text = prefs(ModulePrefs).getString("statusbar_custom_carrier_display_text", "") ?: ""
        if (text.isEmpty()) return
        dataChannel.wait<String>("statusbar_custom_carrier_display_text") { }
        VariousClass(
            "com.oplusos.systemui.statusbar.widget.StatOperatorNameView",
            "com.oplus.systemui.statusbar.widget.OplusStatCarrierText",
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "setText" }.ignored().hook {
                before {
                    if (args.isNotEmpty()) args[0] = text
                }
            }
            c.method { name = "updateCarrierInfo" }.ignored().hook {
                after {
                    runCatching { (instance as? TextView)?.text = text }
                }
            }
            c.method { name = "onConfigurationChanged" }.ignored().hook {
                after {
                    runCatching { (instance as? TextView)?.text = text }
                }
            }
        }
    }
}

object LockScreenShowRealChargingTechnology : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("lock_screen_show_real_charging_technology", false)) return
        val classes = listOf(
            "com.oplus.charge.util.ChargeUtil",
            "com.oplusos.systemui.keyguard.charginganim.ChargingAnimationImpl",
            "com.oplus.charge.viewmodel.OplusChargeAnimImpl",
            "com.oplus.systemui.keyguard.charginganim.siphonanim.viewmodel.OplusChargeAnimFlavorOneImpl",
        )
        for (className in classes) {
            className.toClassOrNull(appClassLoader)?.let { c ->
                c.method { name = "showTechnology"; returnType = BooleanType }.ignored().hook { replaceToTrue() }
                c.method { name = "isShowTechnology"; returnType = BooleanType }.ignored().hook { replaceToTrue() }
                c.method { name = "showTechnology" }.ignored().hook { replaceToTrue() }
                c.method { name = "isShowTechnology" }.ignored().hook { replaceToTrue() }
            }
        }
    }
}

object ReplaceChargingTechnologyDrawingStyle : YukiBaseHooker() {
    override fun onHook() {
        val style = prefs(ModulePrefs).getString("replace_charging_technology_drawing_style", "0") ?: "0"
        if (style == "0") return
        val value = style.toIntOrNull() ?: 0
        VariousClass(
            "com.oplus.charge.util.ChargeUtil",
            "com.oplusos.systemui.keyguard.charginganim.ChargingAnimationImpl",
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "getTechnologyStyle"; returnType = IntType }.ignored().hook { replaceTo(value) }
            c.method { name = "getChargeTechStyle"; returnType = IntType }.ignored().hook { replaceTo(value) }
            c.method { name = "getTechnologyStyle" }.ignored().hook { replaceTo(value) }
            c.method { name = "getChargeTechStyle" }.ignored().hook { replaceTo(value) }
        }
    }
}
