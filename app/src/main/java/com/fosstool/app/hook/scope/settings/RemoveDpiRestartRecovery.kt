package com.fosstool.app.hook.scope.settings

import android.content.Context
import android.provider.Settings
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.useFirst
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import de.robv.android.xposed.XposedHelpers
import kotlin.math.max
import kotlin.math.min

object RemoveDpiRestartRecovery : YukiBaseHooker() {
    override fun onHook() {

        "com.oplus.settings.widget.preference.OplusDensityPreference".toClassOrNull(appClassLoader)
            ?.method { name = "onPreferenceChange"; paramCount = 2 }
            ?.ignored()
            ?.hook {
                after {
                    val newValue = args.getOrNull(1)?.toString() ?: return@after
                    val context = runCatching {
                        XposedHelpers.callMethod(instance, "getContext") as? Context
                    }.getOrNull() ?: return@after
                    val displayMetrics = context.applicationContext.resources.displayMetrics
                    val density = min(displayMetrics.widthPixels, displayMetrics.heightPixels) *
                        160 / max(newValue.toIntOrNull() ?: return@after, 320)
                    val forced = max(density, 120)
                    Settings.Secure.putString(
                        context.contentResolver,
                        "display_density_forced",
                        forced.toString(),
                    )
                    runCatching { XposedHelpers.callMethod(instance, "notifyChanged") }
                }
            }

        hookSettingsUtilsRestore()
    }

    private fun hookSettingsUtilsRestore() {
        DexkitUtils.create(appInfo.sourceDir) { bridge ->
            val classes = bridge.findClass {
                matcher {
                    addMethod { paramTypes(ContextClass.name, BooleanType.name) }
                    addMethod {
                        paramTypes(
                            StringClass.name,
                            IntType.name,
                            IntType.name,
                            BooleanType.name,
                        )
                        usingStrings("restoreCompassPhoneDisplayDensity")
                    }
                    addMethod {
                        paramTypes(ContextClass.name, StringClass.name, IntType.name)
                        usingStrings("restorePhoneDisplayDensity")
                    }
                    usingStrings("SettingsUtils")
                }
            }.checkDataList("RemoveDpiRestartRecovery Clazz", onlyOne = false)
            if (classes.isEmpty()) return@create

            bridge.findMethod {
                searchInClass(classes)
                matcher {
                    paramTypes(ContextClass.name, BooleanType.name)
                    addInvoke {
                        paramTypes(
                            StringClass.name,
                            IntType.name,
                            IntType.name,
                            BooleanType.name,
                        )
                        usingStrings("restoreCompassPhoneDisplayDensity")
                    }
                    addInvoke {
                        paramTypes(ContextClass.name, StringClass.name, IntType.name)
                        usingStrings("restorePhoneDisplayDensity")
                    }
                }
            }.useFirst("RemoveDpiRestartRecovery Method") { md ->
                md.className.toClassOrNull(appClassLoader)
                    ?.method { name = md.methodName; param(ContextClass, BooleanType) }
                    ?.ignored()
                    ?.hook { intercept() }
            }
        }
    }
}
