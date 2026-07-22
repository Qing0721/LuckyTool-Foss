package com.fosstool.app.hook.scope.systemui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.DrawableClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.fosstool.app.R
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.closeScreen
import com.fosstool.app.utils.safeOfNull

object LockScreenBottomButton : YukiBaseHooker() {
    override fun onHook() {
        if (SDK < A14) {
            loadHooker(LockScreenBottomButtonC13)
            return
        }

        var leftButton =
            prefs(ModulePrefs).getBoolean("remove_lock_screen_bottom_left_button", false)
        dataChannel.wait<Boolean>("remove_lock_screen_bottom_left_button") { leftButton = it }
        var rightButton =
            prefs(ModulePrefs).getBoolean("remove_lock_screen_bottom_right_camera", false)
        dataChannel.wait<Boolean>("remove_lock_screen_bottom_right_camera") { rightButton = it }
        var autoCloseScreen = prefs(ModulePrefs).getBoolean(
            "lock_screen_switch_flashlight_auto_close_screen", false
        )
        dataChannel.wait<Boolean>("lock_screen_switch_flashlight_auto_close_screen") {
            autoCloseScreen = it
        }

        "com.android.systemui.keyguard.ui.binder.KeyguardBottomAreaViewBinder".toClass().apply {
            method { name = "updateButton" }.hook {
                before {
                    if ((leftButton || rightButton).not()) return@before
                    val view = args().first().cast<View>() ?: return@before
                    when (safeOfNull { view.resources.getResourceEntryName(view.id) }) {
                        "start_button" -> if (leftButton) {
                            view.isVisible = false
                            resultNull()
                        }

                        "end_button" -> if (rightButton) {
                            view.isVisible = false
                            resultNull()
                        }
                    }
                }
            }
        }

        "com.android.systemui.keyguard.domain.interactor.KeyguardQuickAffordanceInteractor".toClass()
            .apply {
                method { name = "onQuickAffordanceTriggered" }.hook {
                    after {
                        if (leftButton || !autoCloseScreen) return@after
                        val context = field { name = "appContext" }.get(instance).cast<Context>()
                            ?: return@after
                        closeScreen(context)
                    }
                }
            }
    }

    object LockScreenBottomButtonC13 : YukiBaseHooker() {
        override fun onHook() {
            var leftButton =
                prefs(ModulePrefs).getBoolean("remove_lock_screen_bottom_left_button", false)
            dataChannel.wait<Boolean>("remove_lock_screen_bottom_left_button") { leftButton = it }
            var rightButton =
                prefs(ModulePrefs).getBoolean("remove_lock_screen_bottom_right_camera", false)
            dataChannel.wait<Boolean>("remove_lock_screen_bottom_right_camera") { rightButton = it }

            var useFlashLight = prefs(ModulePrefs).getBoolean(
                "lock_screen_bottom_left_button_replace_with_flashlight", false
            )
            dataChannel.wait<Boolean>("lock_screen_bottom_left_button_replace_with_flashlight") {
                useFlashLight = it
            }
            var autoCloseScreen = prefs(ModulePrefs).getBoolean(
                "lock_screen_switch_flashlight_auto_close_screen", false
            )
            dataChannel.wait<Boolean>("lock_screen_switch_flashlight_auto_close_screen") {
                autoCloseScreen = it
            }

            "com.android.systemui.statusbar.phone.KeyguardBottomAreaView".toClass().apply {
                method { name = "onFinishInflate" }.hook {
                    before {
                        if (!useFlashLight) return@before
                        instance<ViewGroup>().context.injectModuleAppResources()
                    }
                }
                method { name = "updateLeftAffordanceIcon" }.hook {
                    after {
                        if (!useFlashLight) return@after
                        val context = instance<ViewGroup>().context
                        method { name = "updateLeftAffordanceVisibility" }.get(instance).call()
                        val mFlashlightController = field { name = "mFlashlightController" }
                            .get(instance).any()
                        val isEnable = mFlashlightController?.getIsEnable() ?: false
                        val resId = if (isEnable) R.drawable.affordance_flashlight_on
                        else R.drawable.affordance_flashlight
                        val drawable = safeOfNull {
                            ResourcesCompat.getDrawable(context.resources, resId, null)
                        }
                        field { name = "mLeftAffordanceView";superClass() }.get(instance).any()
                            ?.current()?.method {
                                name = "setImageDrawable";param(DrawableClass, BooleanType)
                                superClass()
                            }?.call(drawable, !isEnable)
                    }
                }
                method { name = "updateLeftAffordanceVisibility" }.hook {
                    after {
                        if (leftButton) {
                            field { name = "mLeftAffordanceView";superClass() }.get(instance)
                                .cast<View>()?.isVisible = false
                            return@after
                        }
                        if (useFlashLight) {
                            field { name = "mLeftAffordanceView";superClass() }.get(instance)
                                .cast<ImageView>()?.isVisible = true
                        }
                    }
                }
                method { name = "launchLeftAffordance" }.hook {
                    before {
                        if (!useFlashLight) return@before
                        method { name = "baseLaunchLeftAffordance" }.get(instance).call()
                        val mFlashlightController =
                            field { name = "mFlashlightController" }.get(instance).any()
                        val isEnable = mFlashlightController?.getIsEnable() ?: true
                        mFlashlightController?.setFlashlight(!isEnable)
                        method { name = "updateLeftAffordanceIcon" }.get(instance).call()
                        if (autoCloseScreen) closeScreen(instance<ViewGroup>().context)
                        resultNull()
                    }
                }
                method { name = "updateCameraVisibility" }.hook {
                    before {
                        if (!rightButton) return@before
                        field { name = "mRightAffordanceView";superClass() }.get(instance)
                            .cast<ImageView>()?.isVisible = false
                        resultNull()
                    }
                }
            }
        }
    }

    private fun Any.getIsEnable(): Boolean? {
        return this.current().method { name = "isEnabled" }.invoke<Boolean>()
    }

    private fun Any.setFlashlight(status: Boolean) {
        this.current().method { name = "setFlashlight" }.call(status)
    }
}
