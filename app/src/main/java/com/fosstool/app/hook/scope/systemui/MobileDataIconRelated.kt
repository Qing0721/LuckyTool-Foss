package com.fosstool.app.hook.scope.systemui

import android.telephony.SubscriptionManager
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

object MobileDataIconRelated : YukiBaseHooker() {
    override fun onHook() {

        val osV = getOSVersionCode
        if (osV >= 34) loadHooker(MobileDataIconRelatedC14)
        else if (osV >= 23) loadHooker(MobileDataIconRelatedC12)
        else loadHooker(MobileDataIconRelatedC120)
    }

    object MobileDataIconRelatedC12 : YukiBaseHooker() {
        override fun onHook() {
            val removeInout = prefs(ModulePrefs).getBoolean("remove_mobile_data_inout", false)
            val removeType = prefs(ModulePrefs).getBoolean("remove_mobile_data_type", false)
            var hideNonNetwork = prefs(ModulePrefs).getBoolean("hide_non_network_card_icon", false)
            dataChannel.wait<Boolean>("hide_non_network_card_icon") { hideNonNetwork = it }
            var hideNoSS = prefs(ModulePrefs).getBoolean("hide_nosim_noservice", false)
            dataChannel.wait<Boolean>("hide_nosim_noservice") { hideNoSS = it }

            VariousClass(
                "com.oplusos.systemui.statusbar.OplusStatusBarMobileView",
                "com.oplus.systemui.statusbar.phone.signal.OplusStatusBarMobileViewExImpl",
            ).toClassOrNull(appClassLoader)?.let { clazz ->
                clazz.method { name = "initViewState" }.ignored().hook {
                    after {
                        applyState(clazz, instance, args, hideNonNetwork, removeInout, removeType)
                    }
                }
                val updateName = when (clazz.simpleName) {
                    "OplusStatusBarMobileView" -> "updateMobileViewState"
                    "OplusStatusBarMobileViewExImpl" -> "updateState"
                    else -> "updateState"
                }
                clazz.method { name = updateName }.ignored().hook {
                    after {
                        applyState(clazz, instance, args, hideNonNetwork, removeInout, removeType)
                    }
                }
            }

            VariousClass(
                "com.oplusos.systemui.ext.StatusBarSignalPolicyExt",
                "com.oplus.systemui.statusbar.phone.signal.OplusStatusBarSignalPolicyExImpl",
            ).toClassOrNull(appClassLoader)?.let { clazz ->
                clazz.method { name = "setNoSims"; paramCount = 3 }.ignored().hook {
                    after {
                        if (!hideNoSS) return@after
                        val iconController = runCatching {
                            clazz.getDeclaredMethod("getIconController").apply { isAccessible = true }
                                .invoke(instance)
                        }.getOrNull()
                            ?: clazz.findField("iconController")?.get(instance)
                            ?: return@after
                        val slotNoSim = clazz.findField("slotNoSim")?.get(instance) as? String
                            ?: return@after
                        runCatching {
                            XposedHelpers.callMethod(iconController, "setIconVisibility", slotNoSim, false)
                        }
                    }
                }
            }
        }

        private fun applyState(
            clazz: Class<*>,
            instance: Any,
            args: Array<Any?>,
            hideNonNetwork: Boolean,
            removeInout: Boolean,
            removeType: Boolean
        ) {
            if (hideNonNetwork) {
                val state = args.getOrNull(0)
                val subId = runCatching {
                    XposedHelpers.getIntField(state, "subId")
                }.getOrNull()
                val subId2 = SubscriptionManager.getDefaultDataSubscriptionId()
                clazz.findField("mMobileGroup")?.get(instance)?.let { group ->
                    (group as? ViewGroup)?.isVisible = subId == subId2
                }
            }
            if (removeInout) {
                clazz.findField("mDataActivity")?.get(instance)?.let { v ->
                    (v as? View)?.isVisible = false
                }
            }
            if (removeType) {
                clazz.findField("mMobileType")
                    ?.get(instance)?.let { v ->
                        (v as? View)?.isVisible = false
                    }
            }
        }
    }

    object MobileDataIconRelatedC120 : YukiBaseHooker() {
        override fun onHook() {
            val removeInout = prefs(ModulePrefs).getBoolean("remove_mobile_data_inout", false)
            val removeType = prefs(ModulePrefs).getBoolean("remove_mobile_data_type", false)
            var hideNonNetwork = prefs(ModulePrefs).getBoolean("hide_non_network_card_icon", false)
            dataChannel.wait<Boolean>("hide_non_network_card_icon") { hideNonNetwork = it }
            var hideNoSS = prefs(ModulePrefs).getBoolean("hide_nosim_noservice", false)
            dataChannel.wait<Boolean>("hide_nosim_noservice") { hideNoSS = it }

            val mobile = "com.android.systemui.statusbar.StatusBarMobileView"
                .toClassOrNull(appClassLoader) ?: return
            mobile.method { name = "initViewState" }.ignored().hook {
                after {
                    applyStateC120(mobile, instance, hideNonNetwork, removeInout, removeType)
                }
            }
            mobile.method { name = "updateState" }.ignored().hook {
                after {
                    applyStateC120(mobile, instance, hideNonNetwork, removeInout, removeType)
                }
            }

            "com.oplusos.systemui.statusbar.widget.SignalClusterView"
                .toClassOrNull(appClassLoader)?.let { clazz ->
                    clazz.method { name = "updateNoSimView" }.ignored().hook {
                        after {
                            if (!hideNoSS) return@after
                            val mNoSims = clazz.findField("mNoSims")?.get(instance) as? View
                                ?: return@after
                            runCatching {
                                clazz.getDeclaredMethod("animateHide", View::class.java, Int::class.java)
                                    .apply { isAccessible = true }
                                    .invoke(instance, mNoSims, 8)
                            }
                        }
                    }
                }
        }

        private fun applyStateC120(
            mobile: Class<*>,
            instance: Any,
            hideNonNetwork: Boolean,
            removeInout: Boolean,
            removeType: Boolean
        ) {
            if (hideNonNetwork) {
                val state = mobile.findField("mState")?.get(instance)
                val subId = runCatching {
                    XposedHelpers.getIntField(state, "subId")
                }.getOrNull()
                val subId2 = SubscriptionManager.getDefaultDataSubscriptionId()
                mobile.findField("mMobileGroup")?.get(instance)?.let { group ->
                    (group as? ViewGroup)?.isVisible = subId == subId2
                }
            }
            if (removeInout) {
                mobile.findField("mInoutContainer")?.get(instance)?.let { v ->
                    (v as? View)?.isVisible = false
                }
            }
            if (removeType) {
                mobile.findField("mMobileType")?.get(instance)?.let { v ->
                    (v as? View)?.isVisible = false
                }
            }
        }
    }

    object MobileDataIconRelatedC14 : YukiBaseHooker() {
        override fun onHook() {
            val removeInout = prefs(ModulePrefs).getBoolean("remove_mobile_data_inout", false)
            val removeType = prefs(ModulePrefs).getBoolean("remove_mobile_data_type", false)
            var hideNonNetwork = prefs(ModulePrefs).getBoolean("hide_non_network_card_icon", false)
            dataChannel.wait<Boolean>("hide_non_network_card_icon") { hideNonNetwork = it }
            var hideNoSS = prefs(ModulePrefs).getBoolean("hide_nosim_noservice", false)
            dataChannel.wait<Boolean>("hide_nosim_noservice") { hideNoSS = it }
            val hostCl = appClassLoader

            if (removeInout || removeType) {
                "com.oplus.systemui.statusbar.pipeline.mobile.ui.viewmodel.OplusMobileIconViewModel"
                    .toClassOrNull(appClassLoader)?.let { clazz ->
                        if (removeInout) {
                            clazz.method { name = "getMobileActivityResId" }.ignored().hook {
                                before {
                                    hostStateFlow(hostCl, 0)?.let { result = it }
                                }
                            }
                        }
                        if (removeType) {
                            clazz.method { name = "getNetworkTypeIcon" }.ignored().hook {
                                before {
                                    hostStateFlow(hostCl, 0)?.let { result = it }
                                }
                            }
                        }
                    }
            }

            if (hideNonNetwork) {
                VariousClass(
                    "com.oplus.systemui.statusbar.pipeline.mobile.ui.viewmodel.OplusMobileIconViewModel",
                    "com.android.systemui.statusbar.pipeline.mobile.ui.viewmodel.MobileIconViewModel",
                    "com.android.systemui.statusbar.pipeline.mobile.ui.viewmodel.LocationBasedMobileViewModel",
                ).toClassOrNull(appClassLoader)?.let { clazz ->

                    clazz.method { name = "isVisible" }.ignored().hook {
                        after {
                            if (!hideNonNetwork) return@after

                            val originFlow = result ?: return@after
                            val boolNow = runCatching {
                                originFlow.javaClass.getMethod("getValue").invoke(originFlow) as? Boolean
                            }.getOrNull() ?: return@after
                            if (!boolNow) return@after
                            val subIdField = clazz.findField("subscriptionId") ?: return@after
                            subIdField.isAccessible = true
                            val subId = subIdField.getInt(instance)
                            val subId2 = SubscriptionManager.getDefaultDataSubscriptionId()
                            hostStateFlow(hostCl, subId == subId2)?.let { result = it }
                        }
                    }
                }
            }

            if (hideNoSS) {
                "com.oplus.systemui.statusbar.phone.signal.OplusStatusBarSignalPolicy"
                    .toClassOrNull(appClassLoader)?.let { clazz ->
                        clazz.method { name = "updateSlotIconVisibility" }.ignored().hook {
                            before {
                                if (!hideNoSS) return@before
                                runCatching {
                                    var hit = false
                                    for (i in args.indices) {
                                        val a = args[i]
                                        if (!hit && a is String && a == "nosim_all") hit = true
                                        else if (hit && a is Int) {
                                            args[i] = 0
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    }
            }
        }

        private fun hostStateFlow(hostCl: ClassLoader?, value: Any): Any? {
            if (hostCl == null) return null
            return runCatching {
                val stateFlowKt =
                    Class.forName("kotlinx.coroutines.flow.StateFlowKt", false, hostCl)
                stateFlowKt.getDeclaredMethod("MutableStateFlow", Any::class.java)
                    .invoke(null, value)
            }.getOrNull()
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
