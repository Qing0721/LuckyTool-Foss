package com.fosstool.app.hook.scope.android

import android.content.Context
import android.provider.Settings
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object HookWindowManagerService : YukiBaseHooker() {

    private const val KEY_DENSITY_FORCED = "display_density_forced"

    override fun onHook() {
        var isDpi = prefs(ModulePrefs).getBoolean("remove_dpi_restart_recovery", false)
        dataChannel.wait<Boolean>("remove_dpi_restart_recovery") { isDpi = it }

        val wmsCls = "com.android.server.wm.OplusWindowManagerService".toClassOrNull(appClassLoader)
        if (wmsCls == null) {
            YLog.error("HookWindowManagerService: OplusWindowManagerService not found")
        } else {
            wmsCls.method {
                name = "clearForcedDisplayDensityForUser"
                paramCount = 2
                superClass()
            }.ignored().hook {
                before { if (isDpi) resultNull() }
            }
        }

        val displayWindowSettings =
            "com.android.server.wm.DisplayWindowSettings".toClassOrNull(appClassLoader)
        if (displayWindowSettings == null) {
            YLog.error("HookWindowManagerService: DisplayWindowSettings not found")
        } else {
            displayWindowSettings.method {
                name = "setForcedDensity"
                paramCount(2..3)
            }.ignored().hookAll {
                before {
                    if (!isDpi) return@before
                    val density = args(1).any() as? Int ?: return@before
                    if (density != 0) return@before

                    val wms = findFieldByType(instance, "com.android.server.wm.WindowManagerService")
                        ?: return@before
                    val context = findContext(wms) ?: return@before
                    val forced = readForcedDensity(context) ?: return@before
                    args(1).set(forced)
                }
            }
        }

        if (SDK >= 34) {
            val displayContentExt =
                "com.android.server.wm.DisplayContentExtImpl".toClassOrNull(appClassLoader)
            if (displayContentExt == null) {
                YLog.error("HookWindowManagerService: DisplayContentExtImpl not found")
            } else {
                displayContentExt.method {
                    name = "setForcedDisplayInfoForWmSize"
                    paramCount = 5
                }.ignored().hook {
                    before {
                        if (!isDpi) return@before
                        val last = args().last().any() ?: return@before
                        val context = findContext(last) ?: return@before
                        val forced = readForcedDensity(context) ?: return@before
                        args(2).set(forced)
                    }
                }
            }
        }

        val switchImpl =
            "com.android.server.wm.OplusResolutionSwitchImpl".toClassOrNull(appClassLoader)
        if (switchImpl == null) {
            YLog.error("HookWindowManagerService: OplusResolutionSwitchImpl not found")
            return
        }

        val resetDensity = switchImpl.method {
            name = "resetDensityIfNeed"
            superClass()
        }.ignored().give()

        if (resetDensity != null) {
            switchImpl.method {
                name = "resetDensityIfNeed"
                superClass()
            }.ignored().hook {
                before { if (isDpi) resultNull() }
            }
            return
        }

        switchImpl.method {
            name = "onResolutionSettingsChange"
            paramCount = 1
            superClass()
        }.ignored().hook {
            before { if (isDpi) args(0).set(false) }
        }

        switchImpl.method {
            name = "onFakeResolutionSettingsChange"
            paramCount = 1
            superClass()
        }.ignored().hook {
            before { if (isDpi) args(0).set(false) }
        }
    }

    private fun readForcedDensity(context: Context): Int? = runCatching {
        Settings.Secure.getString(context.contentResolver, KEY_DENSITY_FORCED)?.toIntOrNull()
    }.getOrNull()

    private fun findContext(host: Any): Context? = runCatching {
        val field = host.javaClass.declaredFields
            .firstOrNull { Context::class.java.isAssignableFrom(it.type) } ?: return@runCatching null
        field.isAccessible = true
        field.get(host) as? Context
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
