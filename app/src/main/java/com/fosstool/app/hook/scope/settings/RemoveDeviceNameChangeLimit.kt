package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.utils.getOSVersionCode

object RemoveDeviceNameChangeLimit : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 30) return

        try {
            "com.oplus.settings.feature.deviceinfo.aboutphone.PhoneNameVerifyUtil".toClass()
                .apply {
                    method { name = "activeVerifyPhoneName" }.hook {
                        before {
                            callOnSuccess(args().last().any())
                            intercept()
                        }
                    }
                    method { name = "timeScheduleVerifyPhoneName" }.hook {
                        intercept()
                    }
                }
        } catch (e: Throwable) {
            YLog.error(
                "RemoveDeviceNameChangeLimit: PhoneNameVerifyUtil not found",
                tag = "LuckyTool"
            )
        }
        try {
            "com.oplus.settings.utils.WirelessDeviceVerifyUtils".toClass().apply {
                method { name = "activeVerifyPhoneName" }.hook {
                    before {
                        callOnSuccess(args().last().any())
                        intercept()
                    }
                }
            }
        } catch (e: Throwable) {
            YLog.error(
                "RemoveDeviceNameChangeLimit: WirelessDeviceVerifyUtils not found",
                tag = "LuckyTool"
            )
        }
        try {
            "com.oplus.settings.utils.OplusDeviceInfoUtils".toClass().apply {
                method { name = "getVerifyNameCondition" }.hook {
                    intercept()
                }
            }
        } catch (e: Throwable) {
            YLog.error(
                "RemoveDeviceNameChangeLimit: OplusDeviceInfoUtils not found",
                tag = "LuckyTool"
            )
        }
    }

    private fun callOnSuccess(callback: Any?) {
        if (callback == null) return
        try {
            val method = callback.javaClass.methods.find {
                it.name == "onSuccess" && it.parameterTypes.size == 1
            }
            method?.isAccessible = true
            method?.invoke(callback, null)
        } catch (_: Throwable) {}
    }
}
