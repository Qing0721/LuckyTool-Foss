package com.fosstool.app.hook.scope.settings

import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveDeviceNameChangeLimit : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 30) return

        "com.oplus.settings.feature.deviceinfo.aboutphone.PhoneNameVerifyUtil".toClassOrNull(appClassLoader)?.apply {
            method { name = "activeVerifyPhoneName" }.ignored().hook {
                before {
                    callOnSuccess(args.lastOrNull())
                    result = null
                }
            }
            method { name = "timeScheduleVerifyPhoneName" }.ignored().hook { intercept() }
        }
        "com.oplus.settings.utils.WirelessDeviceVerifyUtils".toClassOrNull(appClassLoader)
            ?.method { name = "activeVerifyPhoneName" }
            ?.ignored()
            ?.hook {
                before {
                    callOnSuccess(args.lastOrNull())
                    result = null
                }
            }

        "com.oplus.settings.utils.OplusDeviceInfoUtils".toClassOrNull(appClassLoader)
            ?.method { name = "getVerifyNameCondition" }
            ?.ignored()
            ?.hook { replaceToFalse() }
    }

    private fun callOnSuccess(callback: Any?) {
        if (callback == null) return
        try {
            val method = callback.javaClass.methods.find {
                it.name == "onSuccess" && it.parameterTypes.size == 1
            }
            method?.isAccessible = true
            method?.invoke(callback, null)
        } catch (_: Throwable) {
        }
    }
}
