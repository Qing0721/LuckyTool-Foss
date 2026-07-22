package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ApplicationInfoClass
import com.highcapable.yukihookapi.hook.type.android.ContentResolverClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object HookSettingsFeature : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(HookSysFeature)
        loadHooker(HookAppFeatureProvider)
        loadHooker(HookExpUst)
    }

    private object HookSysFeature : YukiBaseHooker() {
        override fun onHook() {
            val memcVideo =
                prefs(ModulePrefs).getBoolean("force_display_video_memc_frame_insertion", false) ||
                    prefs(ModulePrefs).getBoolean("enable_video_memc_frame_insertion", false)
            val rgbBall =
                prefs(ModulePrefs).getBoolean("enable_screen_color_temperature_rgb_ball", false) ||
                    prefs(ModulePrefs).getBoolean("enable_screen_color_temperature_rgb_palette", false)

            DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
                dexKitBridge.findClass {
                    matcher {
                        fields {
                            addForType(BooleanClass.name)
                        }
                        methods {
                            add { paramCount(0);returnType(BooleanType.name) }
                            add { paramTypes(ContextClass.name);returnType(BooleanType.name) }
                            add { paramTypes(StringClass.name);returnType(BooleanType.name) }
                        }
                        usingStrings("SysFeatureUtils")
                    }
                }.apply {
                    checkDataList("HookSysFeature")
                    val member = first()
                    member.name.toClass().apply {
                        method { param(StringClass);returnType = BooleanType }.hookAll {
                            before {
                                when (args().first().string()) {
                                    "oplus.software.video.rm_memc" -> if (memcVideo) resultFalse()
                                    "oplus.software.display.pixelworks_enable" -> if (memcVideo) resultTrue()
                                    "oplus.software.display.rgb_ball_support" -> if (rgbBall) resultTrue()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private object HookExpUst : YukiBaseHooker() {
        override fun onHook() {
            val neverTimeout = prefs(ModulePrefs).getBoolean("enable_show_never_timeout", false)

            DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
                dexKitBridge.findClass {
                    matcher {
                        methods {
                            add { returnType(StringClass.name) }
                            add { returnType(BooleanType.name) }
                            add { returnType(ApplicationInfoClass.name) }
                            add { paramTypes(StringClass.name) }
                            add { paramTypes(IntType.name) }
                            add { paramTypes(IntType.name, StringClass.name) }
                            add { paramTypes(StringClass.name) }
                            add { paramTypes(StringClass.name, StringClass.name) }
                        }
                        usingStrings("screen_off_timeout")
                    }
                }.apply {
                    checkDataList("HookExpUst")
                    val member = first()
                    member.name.toClass().apply {
                        method { param(IntType);returnType = BooleanType }.hookAll {
                            before {
                                when (args().first().int()) {
                                    11 -> if (SDK < A13 && neverTimeout) resultTrue()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private object HookAppFeatureProvider : YukiBaseHooker() {
        override fun onHook() {
            val isDisableCN =
                prefs(ModulePrefs).getBoolean("disable_cn_special_edition_setting", false)
            val neverTimeout = prefs(ModulePrefs).getBoolean("enable_show_never_timeout", false)
            val processorDetail = prefs(ModulePrefs).getString("set_processor_click_page", "0")
            val processManagement =
                prefs(ModulePrefs).getBoolean("force_display_process_management", false)

            DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
                dexKitBridge.findClass {
                    matcher {
                        methods {
                            add {
                                paramTypes(
                                    ContentResolverClass.name, StringClass.name, BooleanType.name
                                )
                                returnType(BooleanType.name)
                            }
                            add {
                                paramTypes(
                                    ContentResolverClass.name, StringClass.name, IntType.name
                                )
                                returnType(IntType.name)
                            }
                            add {
                                paramTypes(
                                    ContentResolverClass.name, StringClass.name, StringClass.name
                                )
                                returnType(StringClass.name)
                            }
                            add {
                                paramTypes(ContentResolverClass.name, StringClass.name)
                                returnType(ListClass.name)
                            }
                            add {
                                paramTypes(ContentResolverClass.name, StringClass.name)
                                returnType(BooleanType.name)
                            }
                        }
                        usingStrings("AppFeatureProviderUtils")
                    }
                }.apply {
                    checkDataList("HookAppFeatureProvider")
                    val member = first()
                    member.name.toClass().apply {
                        method {
                            param(ContentResolverClass, StringClass)
                            returnType = BooleanType
                        }.hook {
                            before {
                                when (args().last().string()) {
                                    "com.android.settings.cn_version" -> if (isDisableCN) resultFalse()
                                    "com.android.settings.show_never_timeout" -> if (neverTimeout) resultTrue()
                                    "com.android.settings.processor_detail" -> if (processorDetail != "0") resultTrue()
                                    "com.android.settings.processor_detail_gen2" -> if (processorDetail == "2") resultTrue()
                                    "com.android.settings.ultimate_cleanup" -> if (processManagement) resultTrue()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
