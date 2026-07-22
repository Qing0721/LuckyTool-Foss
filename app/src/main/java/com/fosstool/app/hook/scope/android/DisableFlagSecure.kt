package com.fosstool.app.hook.scope.android

import android.os.Build
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.utils.ModulePrefs
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.util.function.BiConsumer
import java.util.function.BiPredicate

object DisableFlagSecure : YukiBaseHooker() {
    private var deoptimizeMethod: Method? = null
    private var captureSecureField: Field? = null

    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("disable_flag_secure", false)) return

        try {
            deoptimizeMethod =
                XposedBridge::class.java.getDeclaredMethod("deoptimizeMethod", Member::class.java)
        } catch (e: Throwable) {
            YLog.debug("DisableFlagSecure: deoptimizeMethod resolve failed -> $e")
        }

        when (packageName) {
            "com.oplus.screenshot" -> hookScreenshotApp()
            "com.android.systemui", "com.oplus.appplatform" -> hookSecureBufferApps()
            else -> hookSystemServer()
        }
    }

    private fun hookScreenshotApp() {
        if (Build.VERSION.SDK_INT >= 35) {
            runCatching {
                val cls = Class.forName(
                    "com.oplus.screenshot.OplusScreenCapture\$CaptureArgs\$Builder",
                    false,
                    appClassLoader
                )
                val m = cls.getDeclaredMethod("setUid", Long::class.javaPrimitiveType)
                XposedBridge.hookMethod(m, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.args[0] = -1L
                    }
                })
            }.onFailure {
                if (it !is ClassNotFoundException) {
                    YLog.debug("DisableFlagSecure: OplusScreenCapture setUid failed -> $it")
                }
            }
        }
        hookSecureBufferApps()
    }

    private fun hookSecureBufferApps() {
        if (Build.VERSION.SDK_INT >= 31) {
            runCatching {
                val clsName = if (Build.VERSION.SDK_INT >= 34) {
                    "android.window.ScreenCapture\$ScreenshotHardwareBuffer"
                } else {
                    "android.view.SurfaceControl\$ScreenshotHardwareBuffer"
                }
                val cls = Class.forName(clsName, false, appClassLoader)
                val m = cls.getDeclaredMethod("containsSecureLayers")
                XposedBridge.hookMethod(m, XC_MethodReplacement.returnConstant(false))
            }.onFailure {
                if (it !is ClassNotFoundException) {
                    YLog.debug("DisableFlagSecure: containsSecureLayers failed -> $it")
                }
            }
        }
        if (packageName == "com.android.systemui" || Build.VERSION.SDK_INT in 31..33) {
            runCatching { hookScreenCaptureArgs(appClassLoader) }
                .onFailure { YLog.debug("DisableFlagSecure: hook ScreenCapture failed -> $it") }
        }
    }

    private fun hookSystemServer() {
        runCatching {
            deoptimize("com.android.server.wm.WindowStateAnimator", "createSurfaceLocked")
            deoptimize("com.android.server.wm.WindowManagerService", "relayoutWindow")
            for (i in 0 until 20) {
                runCatching {
                    val c = Class.forName(
                        "com.android.server.wm.RootWindowContainer\$\$ExternalSyntheticLambda$i",
                        false,
                        appClassLoader
                    )
                    if (BiConsumer::class.java.isAssignableFrom(c)) deoptimize(c, "accept")
                }
                runCatching {
                    val c = Class.forName(
                        "com.android.server.wm.DisplayContent\$\$ExternalSyntheticLambda$i",
                        false,
                        appClassLoader
                    )
                    if (BiPredicate::class.java.isAssignableFrom(c)) deoptimize(c, "test")
                }
            }
        }.onFailure {
            YLog.debug("DisableFlagSecure: deoptimize system server failed -> $it")
        }

        if (Build.VERSION.SDK_INT >= 35) {
            runCatching {
                val wms = Class.forName(
                    "com.android.server.wm.WindowManagerService", false, appClassLoader
                )
                val cb = Class.forName(
                    "android.window.IScreenRecordingCallback", false, appClassLoader
                )
                val m = wms.getDeclaredMethod("registerScreenRecordingCallback", cb)
                XposedBridge.hookMethod(m, XC_MethodReplacement.returnConstant(false))
            }.onFailure {
                YLog.debug("DisableFlagSecure: hook WindowManagerService failed -> $it")
            }
        }

        if (Build.VERSION.SDK_INT >= 34) {
            runCatching {
                val atms = Class.forName(
                    "com.android.server.wm.ActivityTaskManagerService", false, appClassLoader
                )
                val binder = Class.forName("android.os.IBinder", false, appClassLoader)
                val obs = Class.forName(
                    "android.app.IScreenCaptureObserver", false, appClassLoader
                )
                val m = atms.getDeclaredMethod("registerScreenCaptureObserver", binder, obs)
                XposedBridge.hookMethod(m, XC_MethodReplacement.DO_NOTHING)
            }.onFailure {
                YLog.debug("DisableFlagSecure: hook ActivityTaskManagerService failed -> $it")
            }
        }

        runCatching { hookScreenCaptureArgs(appClassLoader) }
            .onFailure { YLog.debug("DisableFlagSecure: hook ScreenCapture failed -> $it") }

        if (Build.VERSION.SDK_INT < 34) {
            runCatching {
                val ams = Class.forName(
                    "com.android.server.am.ActivityManagerService", false, appClassLoader
                )
                val m = ams.getDeclaredMethod(
                    "checkPermission",
                    String::class.java,
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType
                )
                XposedBridge.hookMethod(m, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        if (param.args[0] == "android.permission.CAPTURE_BLACKOUT_CONTENT") {
                            param.args[0] = "android.permission.READ_FRAME_BUFFER"
                        }
                    }
                })
            }.onFailure {
                YLog.debug("DisableFlagSecure: hook ActivityManagerService failed -> $it")
            }
        }

        runCatching {
            val displayCls = if (Build.VERSION.SDK_INT >= 34) {
                Class.forName("com.android.server.display.DisplayControl", false, appClassLoader)
            } else {
                android.view.SurfaceControl::class.java
            }
            val methodName =
                if (Build.VERSION.SDK_INT >= 35) "createVirtualDisplay" else "createDisplay"
            val m = displayCls.getDeclaredMethod(
                methodName,
                String::class.java,
                Boolean::class.javaPrimitiveType
            )
            val bootCl = displayCls.classLoader
            val sysCl = appClassLoader
            XposedBridge.hookMethod(m, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (Build.VERSION.SDK_INT < 34 && sysCl != null && bootCl != null) {
                        for (el in Throwable().stackTrace) {
                            try {
                                if (el.methodName == "createVirtualDisplayLocked") {
                                    val cl = sysCl.loadClass(el.className).classLoader
                                    if (cl == bootCl) return
                                }
                            } catch (_: ClassNotFoundException) {
                            }
                        }
                    }
                    param.args[1] = true
                }
            })
        }.onFailure {
            YLog.debug("DisableFlagSecure: hook DisplayControl failed -> $it")
        }

        runCatching {
            val cls = Class.forName(
                "com.android.server.display.VirtualDisplayAdapter", false, appClassLoader
            )
            XposedBridge.hookAllMethods(cls, "createVirtualDisplayLocked", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val args = param.args
                    if (args.size <= 2) return
                    val uid = args[2] as? Int ?: return
                    if (uid < 10000 || args[1] != null) {
                        for (i in 3 until args.size) {
                            val v = args[i]
                            if (v is Int) {
                                args[i] = v or 4
                                return
                            }
                        }
                    }
                }
            })
        }.onFailure {
            YLog.debug("DisableFlagSecure: hook VirtualDisplayAdapter failed -> $it")
        }

        runCatching {
            val ws = Class.forName("com.android.server.wm.WindowState", false, appClassLoader)
            val bootCl = ws.classLoader
            val sysCl = appClassLoader
            val m = ws.getDeclaredMethod("isSecureLocked")
            XposedBridge.hookMethod(m, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (isFromFrameworkSurfaceCreate(bootCl, sysCl)) return
                    param.result = false
                }
            })
        }.onFailure {
            YLog.debug("DisableFlagSecure: hook WindowState failed -> $it")
        }

        runCatching {
            val cls = Class.forName(
                "com.android.server.wm.OplusLongshotMainWindow", false, appClassLoader
            )
            XposedBridge.hookAllMethods(
                cls,
                "hasSecure",
                XC_MethodReplacement.returnConstant(false)
            )
        }.onFailure {
            if (it !is ClassNotFoundException) {
                YLog.debug("DisableFlagSecure: hook Oplus failed -> $it")
            }
        }
    }

    private fun isFromFrameworkSurfaceCreate(bootCl: ClassLoader?, sysCl: ClassLoader?): Boolean {
        if (bootCl == null || sysCl == null) return false
        val names = arrayOf("setInitialSurfaceControlProperties", "createSurfaceLocked")
        if (Build.VERSION.SDK_INT >= 34) {
            return try {
                val walker = java.lang.StackWalker.getInstance(
                    java.lang.StackWalker.Option.RETAIN_CLASS_REFERENCE
                )
                walker.walk { stream ->
                    stream.anyMatch { frame ->
                        val decl = frame.declaringClass
                        if (decl == null || decl.classLoader != bootCl) false
                        else names.any { it == frame.methodName }
                    }
                }
            } catch (_: Throwable) {
                false
            }
        }
        for (el in Throwable().stackTrace) {
            if (el.methodName !in names) continue
            try {
                if (sysCl.loadClass(el.className).classLoader == bootCl) return true
            } catch (_: ClassNotFoundException) {
            }
        }
        return false
    }

    private fun hookScreenCaptureArgs(cl: ClassLoader?) {
        val sdk = Build.VERSION.SDK_INT
        val full = sdkIntFull()
        val (hookCls, fieldOwner, fieldName) = when {
            sdk >= 36 && full >= 3600001 -> Triple(
                Class.forName("android.window.ScreenCaptureInternal", false, cl),
                Class.forName("android.window.ScreenCaptureInternal\$CaptureArgs", false, cl),
                "mSecureContentPolicy"
            )
            sdk >= 34 -> Triple(
                Class.forName("android.window.ScreenCapture", false, cl),
                Class.forName("android.window.ScreenCapture\$CaptureArgs", false, cl),
                "mCaptureSecureLayers"
            )
            else -> Triple(
                android.view.SurfaceControl::class.java,
                Class.forName("android.view.SurfaceControl\$CaptureArgs", false, cl),
                "mCaptureSecureLayers"
            )
        }
        val field = fieldOwner.getDeclaredField(fieldName)
        field.isAccessible = true
        captureSecureField = field
        val useInt = sdk >= 36 && full >= 3600001
        val hooker = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                val obj = param.args.getOrNull(0) ?: return
                try {
                    val f = captureSecureField ?: return
                    if (useInt) f.set(obj, 1) else f.set(obj, true)
                } catch (e: IllegalAccessException) {
                    YLog.debug("DisableFlagSecure: ScreenCaptureHooker failed -> $e")
                }
            }
        }
        for (name in arrayOf("nativeCaptureDisplay", "nativeCaptureLayers")) {
            for (m in hookCls.declaredMethods) {
                if (m.name == name) XposedBridge.hookMethod(m, hooker)
            }
        }
    }

    private fun sdkIntFull(): Int {
        if (Build.VERSION.SDK_INT < 36) return Build.VERSION.SDK_INT * 100000
        return runCatching {
            Build.VERSION::class.java.getField("SDK_INT_FULL").getInt(null)
        }.getOrDefault(Build.VERSION.SDK_INT * 100000)
    }

    private fun deoptimize(className: String, method: String) {
        runCatching {
            deoptimize(Class.forName(className, false, appClassLoader), method)
        }
    }

    private fun deoptimize(cls: Class<*>?, method: String) {
        try {
            val dm = deoptimizeMethod ?: return
            cls?.declaredMethods?.forEach {
                if (it.name == method) {
                    dm.invoke(null, it)
                    YLog.debug("DisableFlagSecure deoptimized $it")
                }
            }
        } catch (e: Exception) {
            YLog.debug(e.toString())
        }
    }
}
