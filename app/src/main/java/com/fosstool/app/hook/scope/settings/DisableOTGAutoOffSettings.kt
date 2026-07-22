package com.fosstool.app.hook.scope.settings

import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType

object DisableOTGAutoOffSettings : YukiBaseHooker() {
    override fun onHook() {
        val restrictionClassName = "com.oplus.customize.OplusCustomizeRestrictionManager"
        try {
            VariousClass(
                "com.oplus.settings.feature.othersettings.controller.OtgConnectionOpenedPreferenceController",
                "com.oplus.settings.feature.spfunction.OtgConnectionOpenedPreferenceController"
            ).toClass().apply {
                method {
                    name = "isPreferenceSupport"
                    param(ContextClass)
                    returnType = BooleanType
                }.hook {
                    before {
                        val context = args().first().any() as? android.content.Context ?: return@before
                        val supported = invokeIsUSBOtgEnabled(restrictionClassName, context)
                        if (supported != null) result = supported
                    }
                }
            }
        } catch (e: Throwable) {
            YLog.error(
                "DisableOTGAutoOffSettings: OtgConnectionOpenedPreferenceController not found",
                tag = "LuckyTool"
            )
        }
    }

    private fun invokeIsUSBOtgEnabled(className: String, context: android.content.Context): Boolean? {
        return try {
            val clazz = Class.forName(className, false, context.classLoader)
            val getInstance = clazz.methods.find {
                it.name == "getInstance" && it.parameterTypes.size == 1 &&
                    android.content.Context::class.java.isAssignableFrom(it.parameterTypes[0])
            }
            getInstance?.isAccessible = true
            val instance = getInstance?.invoke(null, context) ?: return null
            val isDisabled = instance.javaClass.methods.find {
                it.name == "isUSBOtgDisabled" && it.parameterTypes.isEmpty()
            }
            isDisabled?.isAccessible = true
            val disabled = isDisabled?.invoke(instance) as? Boolean ?: return null
            !disabled
        } catch (_: Throwable) { null }
    }
}
