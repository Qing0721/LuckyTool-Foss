package com.fosstool.app.hook.scope.systemui

import android.graphics.Typeface
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.allViews
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

object LockScreenChargingComponent : YukiBaseHooker() {
    override fun onHook() {

        when (getOSVersionCode) {
            in 34..Int.MAX_VALUE -> loadHooker(ChargingComponentC14)
            in 30..33 -> loadHooker(ChargingComponentC135)
            in 26..29 -> loadHooker(ChargingComponentC13)
            else -> loadHooker(ChargingComponentC12)
        }
    }

    private object ChargingComponentC14 : YukiBaseHooker() {
        override fun onHook() {
            var userTypeface =
                prefs(ModulePrefs).getBoolean("lock_screen_charging_use_user_typeface", false)
            dataChannel.wait<Boolean>("lock_screen_charging_use_user_typeface") {
                userTypeface = it
            }
            var textLogo =
                prefs(ModulePrefs).getString("set_lock_screen_charging_text_logo_style", "0")
            dataChannel.wait<String>("set_lock_screen_charging_text_logo_style") { textLogo = it }
            var showWattage =
                prefs(ModulePrefs).getBoolean("force_lock_screen_charging_show_wattage", false)
            dataChannel.wait<Boolean>("force_lock_screen_charging_show_wattage") {
                showWattage = it
            }
            val showTech =
                prefs(ModulePrefs).getBoolean("lock_screen_show_real_charging_technology", false)

            val levelViews = listOf(
                "com.oplus.charge.view.ChargeLevelAndLogoView",
                "com.oplus.charge.view.FrameChargeLevelAndLogoView",
                "com.oplus.systemui.keyguard.charginganim.siphonanim.view.ChargeLevelAndLogoFlavorOneView",
            )
            for (viewName in levelViews) {
                viewName.toClassOrNull(appClassLoader)?.let { c ->
                    c.declaredMethods.filter {
                        it.parameterCount == 1 &&
                            Typeface::class.java.isAssignableFrom(it.parameterTypes[0])
                    }.forEach { m ->
                        runCatching {
                            XposedBridge.hookMethod(m, object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    if (!userTypeface) return
                                    (param.thisObject as? LinearLayout)?.allViews?.forEach {
                                        if (it is TextView) it.typeface = Typeface.DEFAULT_BOLD
                                    }
                                }
                            })
                        }
                    }
                    c.method { name = "showTextLogo" }.ignored().hook {
                        before {
                            when (textLogo) {
                                "1" -> result = true
                                "2" -> result = false
                                else -> return@before
                            }
                        }
                    }
                    c.method { name = "showCNChargeTechLogo" }.ignored().hook {
                        before {
                            when (textLogo) {
                                "1" -> result = true
                                "2" -> result = false
                                else -> return@before
                            }
                        }
                    }
                }
            }

            "com.oplus.charge.util.ChargeUtil"
                .toClassOrNull(appClassLoader)?.let { util ->
                    util.method { name = "showWattage" }.ignored().hook {
                        before {
                            if (!showWattage) return@before
                            val chargeInfoObserver = args.getOrNull(0)
                            if (chargeInfoObserver != null) {
                                val wattage = runCatching {
                                    XposedHelpers.callMethod(chargeInfoObserver, "getChargeWattage")
                                        ?.toString()?.toIntOrNull()
                                }.getOrNull()
                                if (wattage != null && wattage != 0) {
                                    result = true
                                    return@before
                                }
                            }
                            result = true
                        }
                    }
                    if (showWattage) {
                        util.method { name = "getShowWattage" }.ignored().hook { replaceToTrue() }
                        util.method { name = "getShowWattageForFrameCharge" }.ignored().hook { replaceToTrue() }
                        util.method { name = "isSupportShowWattage" }.ignored().hook { replaceToTrue() }
                    }
                    if (showTech) {
                        util.method { name = "showTechnology" }.ignored().hook { replaceToTrue() }
                        util.method { name = "isShowTechnology" }.ignored().hook { replaceToTrue() }
                    }
                }

            "com.oplus.charge.viewmodel.OplusChargeAnimImpl"
                .toClassOrNull(appClassLoader)?.let { anim ->
                    if (showWattage) {
                        anim.method { name = "showWattage" }.ignored().hook { replaceToTrue() }
                        anim.method { name = "getShowWattage" }.ignored().hook { replaceToTrue() }
                    }
                    if (showTech) {
                        anim.method { name = "showTechnology" }.ignored().hook { replaceToTrue() }
                    }
                }
            "com.oplus.systemui.keyguard.charginganim.siphonanim.viewmodel.OplusChargeAnimFlavorOneImpl"
                .toClassOrNull(appClassLoader)?.let { flavor ->
                    if (showWattage) {
                        flavor.method { name = "showWattage" }.ignored().hook { replaceToTrue() }
                        flavor.method { name = "getShowWattage" }.ignored().hook { replaceToTrue() }
                    }
                    if (showTech) {
                        flavor.method { name = "showTechnology" }.ignored().hook { replaceToTrue() }
                    }
                }
        }
    }

    private object ChargingComponentC135 : YukiBaseHooker() {
        override fun onHook() {
            var userTypeface =
                prefs(ModulePrefs).getBoolean("lock_screen_charging_use_user_typeface", false)
            dataChannel.wait<Boolean>("lock_screen_charging_use_user_typeface") {
                userTypeface = it
            }
            var textLogo =
                prefs(ModulePrefs).getString("set_lock_screen_charging_text_logo_style", "0")
            dataChannel.wait<String>("set_lock_screen_charging_text_logo_style") { textLogo = it }
            var showTech =
                prefs(ModulePrefs).getBoolean("lock_screen_show_real_charging_technology", false)
            dataChannel.wait<Boolean>("lock_screen_show_real_charging_technology") { showTech = it }
            var showWattage =
                prefs(ModulePrefs).getBoolean("force_lock_screen_charging_show_wattage", false)
            dataChannel.wait<Boolean>("force_lock_screen_charging_show_wattage") {
                showWattage = it
            }

            "com.oplus.charge.view.ChargeLevelAndLogoView"
                .toClassOrNull(appClassLoader)?.let { c ->
                    hookTypefaceSetter(c) { userTypeface }
                    c.method { name = "showTextLogo" }.ignored().hook {
                        before {
                            when (textLogo) {
                                "1" -> result = true
                                "2" -> result = false
                            }
                        }
                    }
                }

            "com.oplus.charge.util.ChargeUtil"
                .toClassOrNull(appClassLoader)?.let { c ->
                    c.method { name = "showWattage" }.ignored().hook {
                        before {
                            if (!showWattage) return@before
                            val observer = args.getOrNull(0) ?: return@before
                            val wattage = runCatching {
                                XposedHelpers.callMethod(observer, "getChargeWattage")
                                    ?.toString()?.trim()?.toIntOrNull()
                            }.getOrNull()
                            if (wattage != null && wattage != 0) resultTrue()
                        }
                    }
                    if (showTech) {
                        c.method { name = "showTechnology" }.ignored().hook { replaceToTrue() }
                    }
                    c.method { name = "getTechnologyStr"; superClass() }.ignored().hook {
                        before {
                            if (!showTech) return@before
                            technologyStrOf(args.lastOrNull())?.let { result = it }
                        }
                    }
                }

            "com.oplus.charge.viewmodel.OplusChargeAnimImpl"
                .toClassOrNull(appClassLoader)?.let { c ->
                    c.method { name = "getTechnologyStr"; superClass() }.ignored().hook {
                        before {
                            if (!showTech) return@before
                            technologyStrOf(args.lastOrNull())?.let { result = it }
                        }
                    }
                }

            "com.oplus.systemui.keyguard.charginganim.siphonanim.viewmodel.OplusChargeAnimFlavorOneImpl"
                .toClassOrNull(appClassLoader)?.let { c ->
                    c.method { name = "getTechnologyStr" }.ignored().hook {
                        before {
                            if (!showTech) return@before
                            technologyStrOf(args.getOrNull(0))?.let { result = it }
                        }
                    }
                }

            "com.oplus.systemui.keyguard.charginganim.siphonanim.view.ChargeLevelAndLogoFlavorOneView"
                .toClassOrNull(appClassLoader)?.let { c ->
                    hookTypefaceSetter(c) { userTypeface }
                    c.method { name = "showTextLogo" }.ignored().hook {
                        before {
                            when (textLogo) {
                                "1" -> result = true
                                "2" -> result = false
                            }
                        }
                    }
                }
        }
    }

    private object ChargingComponentC13 : YukiBaseHooker() {
        override fun onHook() {
            var userTypeface =
                prefs(ModulePrefs).getBoolean("lock_screen_charging_use_user_typeface", false)
            dataChannel.wait<Boolean>("lock_screen_charging_use_user_typeface") {
                userTypeface = it
            }
            var warpCharge =
                prefs(ModulePrefs).getString("set_lock_screen_warp_charging_style", "0")
            dataChannel.wait<String>("set_lock_screen_warp_charging_style") { warpCharge = it }
            var textLogo =
                prefs(ModulePrefs).getString("set_lock_screen_charging_text_logo_style", "0")
            dataChannel.wait<String>("set_lock_screen_charging_text_logo_style") { textLogo = it }
            var showWattage =
                prefs(ModulePrefs).getBoolean("force_lock_screen_charging_show_wattage", false)
            dataChannel.wait<Boolean>("force_lock_screen_charging_show_wattage") {
                showWattage = it
            }

            "com.oplusos.systemui.keyguard.charginganim.siphonanim.ChargingLevelAndLogoView"
                .toClassOrNull(appClassLoader)?.let { c ->
                    c.method { name = "updatePowerFormat" }.ignored().hook {
                        after {
                            if (!userTypeface) return@after
                            (instance as? LinearLayout)?.allViews?.forEach {
                                if (it is TextView) it.typeface = Typeface.DEFAULT_BOLD
                            }
                        }
                    }
                    c.method { name = "showTextLogo" }.ignored().hook {
                        before {
                            if (warpCharge != "2") return@before
                            when (textLogo) {
                                "1" -> result = true
                                "2" -> result = false
                                else -> return@before
                            }
                        }
                    }
                }

            "com.oplusos.systemui.keyguard.charginganim.ChargingAnimationImpl"
                .toClassOrNull(appClassLoader)?.let { c ->
                    c.method { name = "isMaxWattageMatchs" }.ignored().hook {
                        before {
                            if (warpCharge != "2") return@before
                            val mChargerWattage =
                                c.findField("mChargerWattage")?.get(instance) as? Int ?: 0
                            if (showWattage && (mChargerWattage != 0)) result = true
                        }
                    }
                }

            "com.oplusos.systemui.keyguard.charginganim.siphonanim.flavorone.ChargingLevelAndLogoViewForFlavorOneVfx"
                .toClassOrNull(appClassLoader)?.let { c ->
                    c.method { name = "setTypeface" }.ignored().hook {
                        after {
                            if (!userTypeface) return@after
                            (instance as? LinearLayout)?.allViews?.forEach {
                                if (it is TextView) it.typeface = Typeface.DEFAULT_BOLD
                            }
                        }
                    }
                    c.method { name = "showTextLogo" }.ignored().hook {
                        before {
                            if (warpCharge != "2") return@before
                            when (textLogo) {
                                "1" -> result = true
                                "2" -> result = false
                                else -> return@before
                            }
                        }
                    }
                }
        }
    }

    private object ChargingComponentC12 : YukiBaseHooker() {
        override fun onHook() {
            var userTypeface =
                prefs(ModulePrefs).getBoolean("lock_screen_charging_use_user_typeface", false)
            dataChannel.wait<Boolean>("lock_screen_charging_use_user_typeface") {
                userTypeface = it
            }
            var textLogo =
                prefs(ModulePrefs).getString("set_lock_screen_charging_text_logo_style", "0")
            dataChannel.wait<String>("set_lock_screen_charging_text_logo_style") { textLogo = it }
            var showWattage =
                prefs(ModulePrefs).getBoolean("force_lock_screen_charging_show_wattage", false)
            dataChannel.wait<Boolean>("force_lock_screen_charging_show_wattage") {
                showWattage = it
            }

            "com.oplusos.systemui.keyguard.charginganim.siphonanim.ChargingLevelAndLogoView"
                .toClassOrNull(appClassLoader)?.let { c ->
                    c.method { name = "updatePowerFormat" }.ignored().hook {
                        after {
                            if (!userTypeface) return@after
                            (instance as? LinearLayout)?.allViews?.forEach {
                                if (it is TextView) it.typeface = Typeface.DEFAULT_BOLD
                            }
                        }
                    }
                    c.method { name = "isLocaleZhCN" }.ignored().hook {
                        before {
                            when (textLogo) {
                                "1" -> result = true
                                "2" -> result = false
                                else -> return@before
                            }
                        }
                    }
                }

            "com.oplusos.systemui.keyguard.charginganim.ChargingAnimationImpl"
                .toClassOrNull(appClassLoader)?.let { c ->
                    if (showWattage) {
                        c.method { name = "isSupportShowWattage" }.ignored().hook { replaceToTrue() }
                    }
                }
        }
    }

    private fun hookTypefaceSetter(c: Class<*>, enabled: () -> Boolean) {
        c.declaredMethods.filter {
            it.parameterCount == 1 && Typeface::class.java.isAssignableFrom(it.parameterTypes[0])
        }.forEach { m ->
            runCatching {
                XposedBridge.hookMethod(m, object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        if (!enabled()) return
                        (param.thisObject as? LinearLayout)?.allViews?.forEach {
                            if (it is TextView) it.typeface = Typeface.DEFAULT_BOLD
                        }
                    }
                })
            }
        }
    }

    private fun technologyStrOf(observer: Any?): String? {
        if (observer == null) return null
        return runCatching {
            val tech = XposedHelpers.callMethod(observer, "getmChargerTechnology") as? Int
                ?: return null
            val pps = XposedHelpers.callMethod(observer, "getmPpsState") as? Int ?: return null
            val wireless =
                XposedHelpers.callMethod(observer, "ismIsWirelessCharge") as? Boolean ?: return null
            chargeTechnologyName(wireless, tech, pps)
        }.getOrNull()
    }

    private fun chargeTechnologyName(isWireless: Boolean, tech: Int, ppsState: Int): String =
        when (tech) {
            20 -> if (isWireless) "AirSVOOC2" else "SUPERVOOC2.0"
            25 -> if (isWireless) "AirVOOC" else "VOOC Beta Pro"
            30 -> if (isWireless) "AirSVOOC" else "SUPERVOOC Athena Foreign Pro"
            0 -> when (ppsState) {
                1 -> "PrivatePPS"
                2 -> "PublicPPS"
                3 -> "PublicUFCS"
                4 -> "PrivateUFCS"
                else -> "Normal"
            }

            1 -> if (isWireless) "AirVOOC" else "VOOC"
            2 -> if (isWireless) "AirSVOOC" else "SUPERVOOC"
            3 -> "PD"
            4 -> "QC"
            5 -> "PPS"
            6 -> "UFCS"
            else -> "[$tech]"
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
