package com.fosstool.app.hook.scope.settings

import android.content.Context
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object DisableOTGAutoOffSettings : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplus.settings.feature.othersettings.controller.OtgConnectionOpenedPreferenceController",
            "com.oplus.settings.feature.spfunction.OtgConnectionOpenedPreferenceController",
        ).toClassOrNull(appClassLoader)
            ?.method { name = "isPreferenceSupport"; paramCount = 1 }
            ?.ignored()
            ?.hook {
                before {
                    val context = args.getOrNull(0) as? Context ?: return@before
                    val supported = invokeIsUSBOtgEnabled(context)
                    if (supported != null) result = supported
                }
            }
    }

    private fun invokeIsUSBOtgEnabled(context: Context): Boolean? {
        return try {
            val clazz = Class.forName(
                "com.oplus.customize.OplusCustomizeRestrictionManager",
                false,
                context.classLoader,
            )
            val getInstance = clazz.methods.find {
                it.name == "getInstance" && it.parameterTypes.size == 1 &&
                    Context::class.java.isAssignableFrom(it.parameterTypes[0])
            }
            getInstance?.isAccessible = true
            val instance = getInstance?.invoke(null, context) ?: return null
            val isDisabled = instance.javaClass.methods.find {
                it.name == "isUSBOtgDisabled" && it.parameterTypes.isEmpty()
            }
            isDisabled?.isAccessible = true
            val disabled = isDisabled?.invoke(instance) as? Boolean ?: return null
            !disabled
        } catch (_: Throwable) {
            null
        }
    }
}
