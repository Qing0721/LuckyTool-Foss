package com.fosstool.app.hook.scope.exsystemservice

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.fosstool.app.hook.utils.OplusBuildUtlils
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.LogUtils
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.UnitType

class EnableGameRunInBackground : YukiBaseHooker() {
    private val tag = "EnableGameRunInBackground"
    private val fgServicePkg = "com.oplus.exsystemservice"
    private val fgServiceCls = "com.oplus.backgroundstream.RouteForegroundService"
    private val fgServiceAction = "oplus.intent.action.BACKGROUND_STREAM_SERVICE"
    private val mirageOptionsClass = "com.oplus.miragewindow.OplusMirageOptions"
    private val mirageManagerClass = "com.oplus.miragewindow.OplusMirageWindowManager"

    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("enable_game_run_in_background", false)) return
        val osVersionCode = try { OplusBuildUtlils().getOSVersionCode ?: 0 } catch (_: Throwable) { 0 }
        if (osVersionCode < 27) return

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            val classes = dexKitBridge.findClass {
                matcher {
                    addFieldForType(ListClass.name)
                    methods {
                        add {
                            paramCount(0)
                            returnType(BooleanType.name)
                        }
                        add {
                            paramCount(0)
                            returnType(UnitType.name)
                        }
                        add {
                            paramTypes(ContextClass.name)
                            returnType(UnitType.name)
                        }
                    }
                    usingStrings("HangUpUtil", "isSupportBackgroundHangUp")
                }
            }.checkDataList("$tag Cls")
            if (classes.isEmpty()) return@create

            dexKitBridge.findMethod {
                searchInClass(classes)
                matcher {
                    paramCount(0)
                    returnType(BooleanType.name)
                    usingStrings("isSupportBackgroundHangUp")
                }
            }.apply {
                checkDataList("$tag Support")
                val member = firstOrNullSafe() ?: return@apply
                val clazz = member.className.toClassOrNull(appClassLoader) ?: return@apply

                clazz.method {
                    name = member.methodName
                    emptyParam()
                    returnType = BooleanType
                }.ignored().hook { replaceToTrue() }

                clazz.method {
                    param(ContextClass)
                    returnType = UnitType
                }.ignored().hook {
                    before {
                        runCatching {
                            if (osVersionCode >= 34) {
                                startMirageWindowMode()
                            } else {
                                args().first().cast<Context>()?.let { startRouteForegroundService(it) }
                            }
                        }.onFailure {
                            LogUtils.e(tag, "invoke", "$it")
                        }
                        resultNull()
                    }
                }
            }
        }
    }

    private fun startMirageWindowMode() {
        val cl = appClassLoader
        val optionsClass = runCatching {
            if (cl != null) Class.forName(mirageOptionsClass, false, cl)
            else Class.forName(mirageOptionsClass)
        }.getOrNull() ?: return
        val makeBgMethod = optionsClass.getDeclaredMethod("makeBackgroundStreamModeOptions")
        val optionsInstance = makeBgMethod.invoke(null)
        val toBundleMethod = optionsClass.getDeclaredMethod("toBundle")
        val bundle = toBundleMethod.invoke(optionsInstance) as Bundle

        val managerClass = runCatching {
            if (cl != null) Class.forName(mirageManagerClass, false, cl)
            else Class.forName(mirageManagerClass)
        }.getOrNull() ?: return
        val getInstanceMethod = managerClass.getDeclaredMethod("getInstance")
        val managerInstance = getInstanceMethod.invoke(null)
        val startMethod = managerClass.getDeclaredMethod(
            "startMirageWindowMode",
            Intent::class.java,
            Bundle::class.java,
        )
        startMethod.invoke(managerInstance, null, bundle)
    }

    private fun startRouteForegroundService(context: Context) {
        val intent = Intent(fgServiceAction).apply {
            setPackage(fgServicePkg)
            component = ComponentName(fgServicePkg, fgServiceCls)
        }
        context.startForegroundService(intent)
    }
}
