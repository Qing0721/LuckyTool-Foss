package com.fosstool.app.hook.scope.systemui

import android.telephony.SubscriptionManager
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.hasMethod
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A12
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object MobileDataIconRelated : YukiBaseHooker() {
    override fun onHook() {
        if (SDK >= A14) loadHooker(MobileDataIconRelatedC14)
        else if (SDK >= A12) loadHooker(MobileDataIconRelated)
        else loadHooker(MobileDataIconRelatedC120)
    }

    object MobileDataIconRelated : YukiBaseHooker() {
        override fun onHook() {
            val removeInout = prefs(ModulePrefs).getBoolean("remove_mobile_data_inout", false)
            val removeType = prefs(ModulePrefs).getBoolean("remove_mobile_data_type", false)
            var hideNonNetwork = prefs(ModulePrefs).getBoolean("hide_non_network_card_icon", false)
            dataChannel.wait<Boolean>("hide_non_network_card_icon") { hideNonNetwork = it }
            var hideNoSS = prefs(ModulePrefs).getBoolean("hide_nosim_noservice", false)
            dataChannel.wait<Boolean>("hide_nosim_noservice") { hideNoSS = it }

            VariousClass(
                "com.oplusos.systemui.statusbar.OplusStatusBarMobileView",
                "com.oplus.systemui.statusbar.phone.signal.OplusStatusBarMobileViewExImpl"
            ).toClass().apply {
                method { name = "initViewState" }.hook {
                    after {
                        if (hideNonNetwork) {
                            val state = args().first().any()
                            val subId = state?.current()?.field { name = "subId" }?.int()
                            val subId2 = SubscriptionManager.getDefaultDataSubscriptionId()
                            field { name = "mMobileGroup" }.get(instance)
                                .cast<ViewGroup>()?.isVisible =
                                subId == subId2
                        }
                        if (removeInout) field { name = "mDataActivity" }.get(instance)
                            .cast<View>()?.isVisible = false
                        if (removeType) field {
                            name = "mMobileType"
                            if (SDK < A13) superClass(true)
                        }.get(instance).cast<View>()?.isVisible = false
                    }
                }
                method {
                    name = when (simpleName) {
                        "OplusStatusBarMobileView" -> "updateMobileViewState"
                        "OplusStatusBarMobileViewExImpl" -> "updateState"
                        else -> "updateState"
                    }
                }.hook {
                    after {
                        if (hideNonNetwork) {
                            val state = args().first().any()
                            val subId = state?.current()?.field { name = "subId" }?.int()
                            val subId2 = SubscriptionManager.getDefaultDataSubscriptionId()
                            field { name = "mMobileGroup" }.get(instance)
                                .cast<ViewGroup>()?.isVisible =
                                subId == subId2
                        }
                        if (removeInout) field { name = "mDataActivity" }.get(instance)
                            .cast<View>()?.isVisible = false
                        if (removeType) field {
                            name = "mMobileType"
                            if (SDK < A13) superClass(true)
                        }.get(instance).cast<View>()?.isVisible = false
                    }
                }
            }

            VariousClass(
                "com.oplusos.systemui.ext.StatusBarSignalPolicyExt",
                "com.oplus.systemui.statusbar.phone.signal.OplusStatusBarSignalPolicyExImpl"
            ).toClass().apply {
                method {
                    name = "setNoSims"
                    paramCount = 3
                }.hook {
                    after {
                        if (!hideNoSS) return@after
                        val iconController = if (hasMethod { name = "getIconController" }) method {
                            name = "getIconController"
                        }.get(instance).call()
                        else field { name = "iconController" }.get(instance).any()
                        val slotNoSim = field { name = "slotNoSim" }.get(instance).cast<String>()
                        iconController?.current()?.method {
                            name = "setIconVisibility"
                            paramCount = 2
                            if (simpleName == "StatusBarSignalPolicyExt") superClass()
                        }?.call(slotNoSim, false)
                    }
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

            "com.android.systemui.statusbar.StatusBarMobileView".toClass().apply {
                method { name = "initViewState" }.hook {
                    after {
                        if (hideNonNetwork) {
                            val state = field { name = "mState" }.get(instance).any()
                            val subId = state?.current()?.field { name = "subId" }?.int()
                            val subId2 = SubscriptionManager.getDefaultDataSubscriptionId()
                            field { name = "mMobileGroup" }.get(instance)
                                .cast<ViewGroup>()?.isVisible =
                                subId == subId2
                        }
                        if (removeInout) field { name = "mInoutContainer" }.get(instance)
                            .cast<View>()?.isVisible = false
                        if (removeType) field { name = "mMobileType" }.get(instance)
                            .cast<View>()?.isVisible = false
                    }
                }
                method { name = "updateState" }.hook {
                    after {
                        if (hideNonNetwork) {
                            val state = field { name = "mState" }.get(instance).any()
                            val subId = state?.current()?.field { name = "subId" }?.int()
                            val subId2 = SubscriptionManager.getDefaultDataSubscriptionId()
                            field { name = "mMobileGroup" }.get(instance)
                                .cast<ViewGroup>()?.isVisible =
                                subId == subId2
                        }
                        if (removeInout) field { name = "mInoutContainer" }.get(instance)
                            .cast<View>()?.isVisible = false
                        if (removeType) field { name = "mMobileType" }.get(instance)
                            .cast<View>()?.isVisible = false
                    }
                }
            }

            "com.oplusos.systemui.statusbar.widget.SignalClusterView".toClass().apply {
                method { name = "updateNoSimView" }.hook {
                    after {
                        if (!hideNoSS) return@after
                        val mNoSims = field { name = "mNoSims" }.get(instance).cast<View>()
                        method { name = "animateHide" }.get(instance).call(mNoSims, 8)
                    }
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

            "com.oplus.systemui.statusbar.pipeline.mobile.ui.viewmodel.OplusMobileIconViewModel".toClass().apply {
                method { name = "getMobileActivityResId" }.hook {
                    before { if (removeInout) result = hostStateFlow(0) }
                }
                method { name = "getNetworkTypeIcon" }.hook {
                    before { if (removeType) result = hostStateFlow(0) }
                }
            }

            VariousClass(
                "com.oplus.systemui.statusbar.pipeline.mobile.ui.viewmodel.OplusMobileIconViewModel",
                "com.android.systemui.statusbar.pipeline.mobile.ui.viewmodel.MobileIconViewModel",
                "com.android.systemui.statusbar.pipeline.mobile.ui.viewmodel.LocationBasedMobileViewModel"
            ).toClass().apply {
                method {
                    name = "isVisible"
                    returnType = "kotlinx.coroutines.flow.StateFlow"
                }.hook {
                    before {
                        if (!hideNonNetwork) return@before
                        val subId = field { name = "subscriptionId"; superClass(true) }.get(instance).int()
                        val subId2 = SubscriptionManager.getDefaultDataSubscriptionId()
                        result = hostStateFlow(subId == subId2)
                    }
                }
            }

            "com.oplus.systemui.statusbar.phone.signal.OplusStatusBarSignalPolicy".toClass().apply {
                method { name = "updateSlotIconVisibility" }.hookAll {
                    before {
                        if (!hideNoSS) return@before
                        runCatching {
                            var hit = false
                            for (i in args.indices) {
                                val a = args[i]
                                if (!hit && a is String && a == "nosim_all") hit = true
                                else if (hit && a is Int) { args(i).set(0); break }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hostStateFlow(value: Any): Any = runCatching {
        val hostCl = appClassLoader ?: ClassLoader.getSystemClassLoader()
        val sfk = Class.forName("kotlinx.coroutines.flow.StateFlowKt", false, hostCl)
        val fsk = Class.forName("kotlinx.coroutines.flow.FlowKt__ShareKt", false, hostCl)
        val msf = sfk.getDeclaredMethod("MutableStateFlow", Any::class.java).invoke(null, value)
        fsk.getDeclaredMethod(
            "asStateFlow",
            Class.forName("kotlinx.coroutines.flow.MutableStateFlow", false, hostCl)
        ).invoke(null, msf)
    }.getOrElse {
        MutableStateFlow(value).asStateFlow()
    }
}
