package com.fosstool.app.hook.scope.systemui

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Field

object LockScreenCarriers : YukiBaseHooker() {
    override fun onHook() {
        if (SDK >= A14) loadHooker(LockScreenCarrierV14)
        else loadHooker(LockScreenCarrierV13)
    }

    private object LockScreenCarrierV14 : YukiBaseHooker() {
        override fun onHook() {
            val userFont =
                prefs(ModulePrefs).getBoolean("statusbar_carriers_use_user_typeface", false)
            val isRemove = prefs(ModulePrefs).getBoolean("remove_statusbar_carriers", false)

            "com.oplus.systemui.statusbar.widget.OplusStatCarrierTextController"
                .toClassOrNull(appClassLoader)?.let { c ->
                    c.method { name = "onViewAttached" }.ignored().hook {
                        after {
                            if (isRemove) {
                                (c.findField("mView")?.get(instance) as? TextView)
                                    ?.isVisible = false
                            }
                        }
                    }
                    c.method { name = "setVisible" }.ignored().hook {
                        before {
                            if (isRemove && args.isNotEmpty()) args[0] = false
                        }
                    }
                    c.method { name = "updateCarrierInfo" }.ignored().hook {
                        after {
                            if (isRemove) {
                                (c.findField("mView")?.get(instance) as? TextView)
                                    ?.isVisible = false
                            }
                        }
                    }
                }

            "com.oplus.systemui.statusbar.widget.OplusStatCarrierText"
                .toClassOrNull(appClassLoader)?.let { c ->
                    c.declaredConstructors.filter { it.parameterCount == 2 }.forEach { ctor ->
                        runCatching {
                            XposedBridge.hookMethod(ctor, object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    if (userFont) (param.thisObject as? TextView)?.typeface =
                                        Typeface.DEFAULT_BOLD
                                }
                            })
                        }
                    }
                    c.method { name = "onConfigurationChanged" }.ignored().hook {
                        after {
                            if (userFont) (instance as? TextView)?.typeface = Typeface.DEFAULT_BOLD
                        }
                    }
                }
        }
    }

    private object LockScreenCarrierV13 : YukiBaseHooker() {
        override fun onHook() {
            val userFont =
                prefs(ModulePrefs).getBoolean("statusbar_carriers_use_user_typeface", false)
            val isRemove = prefs(ModulePrefs).getBoolean("remove_statusbar_carriers", false)

            "com.oplusos.systemui.statusbar.widget.StatOperatorNameView"
                .toClassOrNull(appClassLoader)?.let { c ->
                    c.declaredConstructors.filter { it.parameterCount == 3 }.forEach { ctor ->
                        runCatching {
                            XposedBridge.hookMethod(ctor, object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    if (userFont) (param.thisObject as? TextView)?.typeface =
                                        Typeface.DEFAULT_BOLD
                                }
                            })
                        }
                    }
                    c.method { name = "onConfigurationChanged" }.ignored().hook {
                        after {
                            if (userFont) (instance as? TextView)?.typeface = Typeface.DEFAULT_BOLD
                        }
                    }
                    c.method { name = "updateCarrierInfo"; superClass() }.ignored().hook {
                        after {
                            if (isRemove) (instance as? TextView)?.isVisible = false
                        }
                    }
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
