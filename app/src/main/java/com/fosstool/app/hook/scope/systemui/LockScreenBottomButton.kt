package com.fosstool.app.hook.scope.systemui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.fosstool.app.R
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.closeScreen
import com.fosstool.app.utils.safeOfNull
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

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

        "com.android.systemui.keyguard.ui.binder.KeyguardBottomAreaViewBinder"
            .toClassOrNull(appClassLoader)?.let { binder ->

                val hasExact = binder.hasMethod("updateButton")
                val finder = if (hasExact) {
                    binder.method { name = "updateButton"; superClass() }
                } else {
                    binder.method { name { it.startsWith("updateButton") } }
                }
                finder.ignored().hook {
                    before {
                        if ((leftButton || rightButton).not()) return@before
                        val view = args.getOrNull(0) as? View ?: return@before
                        when (safeOfNull { view.resources.getResourceEntryName(view.id) }) {
                            "start_button" -> if (leftButton) {
                                view.isVisible = false
                                result = null
                            }

                            "end_button" -> if (rightButton) {
                                view.isVisible = false
                                result = null
                            }
                        }
                    }
                }
            }

        "com.oplus.systemui.keyguard.data.quickaffordance.OplusFlashlightQuickAffordanceConfig"
            .toClassOrNull(appClassLoader)?.let { c ->
                c.method { name = "onTriggered" }.ignored().hook {
                    after {
                        if (leftButton || !autoCloseScreen) return@after
                        val context = (c.findField("context")?.get(instance) as? Context)
                            ?: (c.findField("appContext")?.get(instance) as? Context)
                            ?: return@after
                        closeScreen(context)
                    }
                }
            }
    }

    private fun Class<*>.hasMethod(name: String): Boolean {
        var cls: Class<*>? = this
        while (cls != null) {
            if (cls.declaredMethods.any { it.name == name }) return true
            cls = cls.superclass
        }
        return false
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

            "com.android.systemui.statusbar.phone.KeyguardBottomAreaView"
                .toClassOrNull(appClassLoader)?.let { c ->
                    c.method { name = "onFinishInflate" }.ignored().hook {
                        before {
                            if (!useFlashLight) return@before
                            (instance as? ViewGroup)?.context?.injectModuleAppResources()
                        }
                    }
                    c.method { name = "updateLeftAffordanceIcon" }.ignored().hook {
                        after {
                            if (!useFlashLight) return@after
                            val context = (instance as? ViewGroup)?.context ?: return@after
                            runCatching {
                                XposedHelpers.callMethod(instance, "updateLeftAffordanceVisibility")
                            }
                            val mFlashlightController =
                                c.findField("mFlashlightController")?.get(instance)
                            val isEnable = mFlashlightController?.getIsEnable() ?: false
                            val resId = if (isEnable) R.drawable.affordance_flashlight_on
                            else R.drawable.affordance_flashlight
                            val drawable = safeOfNull {
                                ResourcesCompat.getDrawable(context.resources, resId, null)
                            }
                            val leftView =
                                c.findField("mLeftAffordanceView")?.get(instance)
                            runCatching {
                                XposedHelpers.callMethod(leftView, "setImageDrawable", drawable, !isEnable)
                            }
                        }
                    }
                    c.method { name = "updateLeftAffordanceVisibility" }.ignored().hook {
                        after {
                            if (leftButton) {
                                (c.findField("mLeftAffordanceView")
                                    ?.get(instance) as? View)?.isVisible = false
                                return@after
                            }
                            if (useFlashLight) {
                                (c.findField("mLeftAffordanceView")
                                    ?.get(instance) as? ImageView)?.isVisible = true
                            }
                        }
                    }
                    c.method { name = "launchLeftAffordance" }.ignored().hook {
                        before {
                            if (!useFlashLight) return@before
                            runCatching {
                                XposedHelpers.callMethod(instance, "baseLaunchLeftAffordance")
                            }
                            val mFlashlightController =
                                c.findField("mFlashlightController")?.get(instance)
                            val isEnable = mFlashlightController?.getIsEnable() ?: true
                            mFlashlightController?.setFlashlight(!isEnable)
                            runCatching {
                                XposedHelpers.callMethod(instance, "updateLeftAffordanceIcon")
                            }
                            if (autoCloseScreen) {
                                (instance as? ViewGroup)?.context?.let { closeScreen(it) }
                            }
                            result = null
                        }
                    }
                    c.method { name = "updateCameraVisibility" }.ignored().hook {
                        before {
                            if (!rightButton) return@before
                            (c.findField("mRightAffordanceView")
                                ?.get(instance) as? ImageView)?.isVisible = false
                            result = null
                        }
                    }
                }
        }
    }

    private fun Any.getIsEnable(): Boolean? {
        return runCatching {
            XposedHelpers.callMethod(this, "isEnabled") as? Boolean
        }.getOrNull()
    }

    private fun Any.setFlashlight(status: Boolean) {
        runCatching { XposedHelpers.callMethod(this, "setFlashlight", status) }
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
