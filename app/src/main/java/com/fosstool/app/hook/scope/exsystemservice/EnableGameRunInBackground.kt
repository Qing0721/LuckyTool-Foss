package com.fosstool.app.hook.scope.exsystemservice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.fosstool.app.hook.utils.OplusBuildUtlils
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.LogUtils
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.UnitType

object EnableGameRunInBackground : YukiBaseHooker() {
    private const val TAG = "EnableGameRunInBackground"
    private const val FG_SERVICE_PKG = "com.oplus.exsystemservice"
    private const val FG_SERVICE_CLS = "com.oplus.backgroundstream.RouteForegroundService"
    private const val FG_SERVICE_ACTION = "oplus.intent.action.BACKGROUND_STREAM_SERVICE"
    private const val MIRAGE_OPTIONS_CLASS = "com.oplus.miragewindow.OplusMirageOptions"
    private const val MIRAGE_MANAGER_CLASS = "com.oplus.miragewindow.OplusMirageWindowManager"

    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("enable_game_run_in_background", false)) return
        val osVersionCode = try { OplusBuildUtlils().getOSVersionCode ?: 0 } catch (_: Throwable) { 0 }
        if (osVersionCode < 27) return

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    returnType(BooleanType.name)
                    usingStrings("isSupportBackgroundHangUp")
                }
            }.apply {
                checkDataList("$TAG findMethod")
                val member = first()
                member.className.toClass().apply {
                    method {
                        name = member.methodName
                        returnType = BooleanType
                    }.hook {
                        replaceToTrue()
                    }
                    method {
                        param(ContextClass)
                        returnType = UnitType
                    }.hook {
                        replaceUnit {
                            val context = args().first().cast<Context>()
                            if (context != null) {
                                runCatching {
                                    if (osVersionCode >= 34) {
                                        startMirageWindowMode()
                                    } else {
                                        startRouteForegroundService(context)
                                    }
                                }.getOrElse {
                                    LogUtils.e(TAG, "invoke", "$it")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun startMirageWindowMode() {
        val optionsClass = Class.forName(MIRAGE_OPTIONS_CLASS)
        val makeBgMethod = optionsClass.getDeclaredMethod("makeBackgroundStreamModeOptions")
        val optionsInstance = makeBgMethod.invoke(null)
        val toBundleMethod = optionsClass.getDeclaredMethod("toBundle")
        val bundle = toBundleMethod.invoke(optionsInstance) as Bundle

        val managerClass = Class.forName(MIRAGE_MANAGER_CLASS)
        val getInstanceMethod = managerClass.getDeclaredMethod("getInstance")
        val managerInstance = getInstanceMethod.invoke(null)
        val startMethod = managerClass.getDeclaredMethod(
            "startMirageWindowMode",
            Intent::class.java,
            Bundle::class.java
        )
        startMethod.invoke(managerInstance, null, bundle)
    }

    private fun startRouteForegroundService(context: Context) {
        val intent = Intent(FG_SERVICE_ACTION).apply {
            setPackage(FG_SERVICE_PKG)
            component = ComponentName(FG_SERVICE_PKG, FG_SERVICE_CLS)
        }
        context.startForegroundService(intent)
    }
}
