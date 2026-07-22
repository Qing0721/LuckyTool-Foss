package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContentResolverClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs

object ForceDisplaySettingsFeatureFlags : YukiBaseHooker() {
    override fun onHook() {
        val screenPhysicsSize =
            prefs(ModulePrefs).getBoolean("screen_physics_size_shown_cm", false)
        val disableDeviceAdmin =
            prefs(ModulePrefs).getBoolean("disable_device_admin_verification_dialog", false)
        val touchMembrane =
            prefs(ModulePrefs).getBoolean("enable_touch_membrane_protector_mode", false)

        if (!screenPhysicsSize && !disableDeviceAdmin && !touchMembrane) return

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
                checkDataList("ForceDisplaySettingsFeatureFlags")
                val member = first()
                member.name.toClass().apply {
                    method {
                        param(ContentResolverClass, StringClass)
                        returnType = BooleanType
                    }.hook {
                        before {
                            when (args().last().string()) {
                                "com.android.settings.screen_physics_size_cm" ->
                                    if (screenPhysicsSize) resultTrue()
                                "com.android.settings.verification_dialog.disable" ->
                                    if (disableDeviceAdmin) resultTrue()
                                "feature.super_settings_smart_touch.support" ->
                                    if (touchMembrane) resultTrue()
                            }
                        }
                    }
                }
            }
        }
    }
}
