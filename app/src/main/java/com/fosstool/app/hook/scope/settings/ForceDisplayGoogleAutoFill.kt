package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.hook.utils.OplusBuildUtlils
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object ForceDisplayGoogleAutoFill : YukiBaseHooker() {
    override fun onHook() {
        val osVersionCode = try { OplusBuildUtlils().getOSVersionCode ?: 0 } catch (_: Throwable) { 0 }
        val className = "com.oplus.settings.feature.othersettings.input.OplusDefaultAutofillPicker"
        try {
            if (osVersionCode >= 30) {
                className.toClass().apply {
                    method { name = "getCandidates" }.hook {
                        after {
                        }
                    }
                }
            } else {
                DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
                    dexKitBridge.findMethod {
                        matcher {
                            usingStrings("GoogleAutoFillV13")
                        }
                    }.apply {
                        checkDataList("ForceDisplayGoogleAutoFill")
                        val member = first()
                        member.className.toClass().apply {
                            method { name = member.methodName }.hook {
                                after {
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            YLog.error(
                "ForceDisplayGoogleAutoFill: $className not found",
                tag = "LuckyTool"
            )
        }
    }
}
