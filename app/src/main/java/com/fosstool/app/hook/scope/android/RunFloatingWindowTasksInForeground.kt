package com.fosstool.app.hook.scope.android

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.os.UserHandle
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.utils.ModulePrefs
import de.robv.android.xposed.XposedHelpers

object RunFloatingWindowTasksInForeground : YukiBaseHooker() {

    private const val ATMS = "com.android.server.wm.ActivityTaskManagerService"
    private const val DUMMY_ACTION = "android.intent.action.OPLUS_MIRAGE_CAR_DUMMY"
    private const val MIRAGE_DISPLAY_SERVICE =
        "com.android.server.display.OplusMirageDisplayManagerService"

    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("run_floating_window_tasks_in_foreground", false)) return

        val cls = "com.android.server.wm.OplusMirageWindowManagerService"
            .toClassOrNull(appClassLoader)
        if (cls == null) {
            YLog.error("RunFloatingWindowTasksInForeground: OplusMirageWindowManagerService not found")
            return
        }

        cls.method { name = "startActivityToMirageDisplay" }.ignored().hook {
            before {
                val target = args(0).any() as? Parcelable ?: return@before
                val displayId = args(1).any() as? Int ?: 0
                val bundle = args().last().any() as? Bundle ?: return@before
                val host = instance

                val atms = findFieldByType(host, ATMS) ?: return@before
                val context = findContext(atms) ?: return@before

                val options = runCatching {
                    XposedHelpers.callStaticMethod(
                        ActivityOptions::class.java, "fromBundle", bundle
                    ) as? ActivityOptions
                }.getOrNull() ?: ActivityOptions.makeBasic()

                options.setLaunchDisplayId(displayId)

                Handler(Looper.getMainLooper()).post {
                    launchOnForeground(target, context, options, host, displayId)
                }
                resultNull()
            }
        }
    }

    private fun launchOnForeground(
        target: Parcelable,
        context: Context,
        options: ActivityOptions,
        host: Any?,
        displayId: Int,
    ) {
        try {
            var pendingIntent: PendingIntent? = null
            val intent: Intent? = when (target) {
                is Intent -> target
                is PendingIntent -> {
                    pendingIntent = target
                    runCatching {
                        XposedHelpers.callMethod(target, "getIntent") as? Intent
                    }.getOrNull()
                }

                else -> null
            }
            if (intent != null) {
                if (intent.action == DUMMY_ACTION) {
                    runCatching {
                        XposedHelpers.setIntField(host, "mRealCarDisplayId", displayId)
                    }
                } else {
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (pendingIntent == null) {
                        val uid = runCatching {
                            intent.getIntExtra("TASKINFO_UID", -1)
                        }.getOrDefault(-1)
                        val user = if (uid > 0 && uid.toString().startsWith("999")) {
                            runCatching {
                                XposedHelpers.callStaticMethod(
                                    UserHandle::class.java, "getUserHandleForUid", uid
                                ) as? UserHandle
                            }.getOrNull()
                        } else null
                        if (user != null) {
                            runCatching {
                                XposedHelpers.callMethod(
                                    context, "startActivityAsUser",
                                    intent, options.toBundle(), user
                                )
                            }.onFailure { context.startActivity(intent, options.toBundle()) }
                        } else {
                            context.startActivity(intent, options.toBundle())
                        }
                    } else {
                        pendingIntent.send(
                            context,
                            0,
                            null as Intent?,
                            null as PendingIntent.OnFinished?,
                            null as Handler?,
                            null as String?,
                            options.toBundle()
                        )
                    }
                }
            }
        } catch (_: ActivityNotFoundException) {

        } catch (e: Throwable) {
            YLog.debug("RunFloatingWindowTasksInForeground: launch failed -> $e")
        }

        runCatching {
            val svc = MIRAGE_DISPLAY_SERVICE.toClassOrNull(appClassLoader)
            if (svc != null) {
                val inst = XposedHelpers.callStaticMethod(svc, "getInstance")
                if (inst != null) XposedHelpers.callMethod(inst, "notifyCastSuccess", displayId)
            }
        }
    }

    private fun findContext(host: Any): Context? = runCatching {
        var current: Class<*>? = host.javaClass
        var found: Context? = null
        while (current != null && current != Any::class.java && found == null) {
            val field = current.declaredFields
                .firstOrNull { Context::class.java.isAssignableFrom(it.type) }
            if (field != null) {
                field.isAccessible = true
                found = field.get(host) as? Context
            }
            current = current.superclass
        }
        found
    }.getOrNull()

    private fun findFieldByType(host: Any?, typeName: String): Any? {
        if (host == null) return null
        return runCatching {
            var current: Class<*>? = host.javaClass
            var found: Any? = null
            while (current != null && current != Any::class.java && found == null) {
                val field = current.declaredFields.firstOrNull { it.type.name == typeName }
                if (field != null) {
                    field.isAccessible = true
                    found = field.get(host)
                }
                current = current.superclass
            }
            found
        }.getOrNull()
    }
}
