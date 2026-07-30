package com.fosstool.app.hook.scope.camera

import android.app.Activity
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.BundleClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.highcapable.yukihookapi.hook.type.java.StringClass

object EnableCameraDebugUiOption : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            dexKitBridge.findMethod {
                matcher {
                    declaredClass {
                        fields { addForType("java.util.concurrent.ExecutorService") }
                        methods {
                            add {
                                paramCount(0)
                                returnType(BooleanType.name)
                            }
                        }
                    }
                    paramCount(0)
                    returnType(BooleanType.name)
                    usingStrings("iq_config_set", "hal_config_set")
                }
            }.apply {
                checkDataList("EnableCameraDebugUiOption configSet")
                val member = firstOrNullSafe() ?: return@apply
                member.className.toClassOrNull(appClassLoader)
                    ?.method {
                        name = member.methodName
                        emptyParam()
                    }
                    ?.ignored()
                    ?.hook { replaceToTrue() }
            }

            dexKitBridge.findMethod {
                matcher {
                    declaredClass {
                        fields {
                            addForType(ContextClass.name)
                            addForType("long[]")
                        }
                        methods {
                            add {
                                paramTypes(LongType.name)
                                returnType(BooleanType.name)
                            }
                            add {
                                paramTypes(StringClass.name)
                                returnType(BooleanType.name)
                            }
                        }
                    }
                    paramTypes(LongType.name)
                    returnType(BooleanType.name)
                    usingNumbers(3600000)
                    usingStrings("NetworkAuthenticationUtils")
                }
            }.apply {
                checkDataList("EnableCameraDebugUiOption networkAuth")
                val member = firstOrNullSafe() ?: return@apply
                member.className.toClassOrNull(appClassLoader)
                    ?.method {
                        name = member.methodName
                        param(LongType)
                    }
                    ?.ignored()
                    ?.hook { replaceToFalse() }
            }
        }

        "com.oplus.camera.setting.CameraDebugActivity".toClassOrNull(appClassLoader)
            ?.method {
                name = "onCreate"
                param(BundleClass)
            }
            ?.ignored()
            ?.hook {
                before {
                    val activity = instance as? Activity ?: return@before

                    val sp = activity.getSharedPreferences(
                        activity.packageName + "_preferences", 0,
                    )
                    sp.edit().putBoolean("key_has_checked_auth_connection", true).commit()
                }
            }
    }
}
