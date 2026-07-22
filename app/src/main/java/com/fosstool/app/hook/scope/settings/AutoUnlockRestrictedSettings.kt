package com.fosstool.app.hook.scope.settings

import android.app.AppOpsManager
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getOSVersionCode

object AutoUnlockRestrictedSettings : YukiBaseHooker() {
    private const val OP_ACCESS_RESTRICTED_SETTINGS = 119

    override fun onHook() {
        if (SDK < A13) return
        if (!prefs(ModulePrefs).getBoolean("auto_unlock_restricted_settings", false)) return

        hookRestrictedPreferenceHelper()

        if (getOSVersionCode < 34) {
            hookViaDexKit()
        }

        hookAppOps119()

        runCatching {
            "com.android.settingslib.RestrictedLockUtilsInternal".toClass().apply {
                method {
                    name { it.contains("has", ignoreCase = true) || it.contains("check", ignoreCase = true) }
                    returnType = BooleanType
                }.hookAll {
                    after {
                        val n = method.name
                        if (n.contains("Restricted", ignoreCase = true) ||
                            n.contains("Admin", ignoreCase = true)
                        ) {
                            result = false
                        }
                    }
                }
            }
        }
    }

    private fun hookRestrictedPreferenceHelper() {
        val names = listOf(
            "com.oplus.settings.widget.preference.RestrictedPreferenceHelper",
            "com.android.settingslib.widget.RestrictedPreferenceHelper",
        )
        for (cls in names) {
            runCatching {
                cls.toClass().apply {
                    method { name = "performClick" }.hookAll {
                        before {
                            val host = instance ?: return@before
                            clearRestrictedState(host)
                        }
                    }
                    method {
                        name = "isDisabledByAdmin"
                        returnType = BooleanType
                    }.hookAll {
                        after { result = false }
                    }
                    method {
                        name = "isRestricted"
                        returnType = BooleanType
                    }.hookAll {
                        after { result = false }
                    }
                }
            }
        }
    }

    private fun clearRestrictedState(host: Any) {
        runCatching {
            host.current().method { name = "setDisabledByAdmin" }.call(null)
        }
        runCatching {
            for (f in host.javaClass.declaredFields) {
                f.isAccessible = true
                val n = f.name
                when {
                    f.type == Boolean::class.javaPrimitiveType || f.type == Boolean::class.javaObjectType -> {
                        if (n.contains("Disabled", ignoreCase = true) ||
                            n.contains("Restricted", ignoreCase = true)
                        ) {
                            f.set(host, false)
                        }
                    }
                    n.contains("Admin", ignoreCase = true) -> f.set(host, null)
                }
            }
        }
    }

    private fun hookViaDexKit() {
        runCatching {
            DexkitUtils.create(appInfo.sourceDir) { bridge ->
                bridge.findClass {
                    matcher {
                        fields {
                            add {
                                type = "android.app.AppOpsManager"
                            }
                        }
                        methods {
                            add {
                                paramCount = 0
                                returnType = BooleanType.name
                            }
                        }
                    }
                }.forEach { data ->
                    runCatching {
                        data.name.toClass().apply {
                            method {
                                emptyParam()
                                returnType = BooleanType
                            }.hookAll {
                                after {
                                    result = false
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun hookAppOps119() {
        runCatching {
            "android.app.AppOpsManager".toClass().apply {
                listOf("noteOpNoThrow", "checkOpNoThrow", "noteOp", "checkOp").forEach { mName ->
                    runCatching {
                        method {
                            name = mName
                            paramCount(3..5)
                        }.hookAll {
                            before {
                                val op = runCatching { args().first().int() }.getOrNull()
                                if (op == OP_ACCESS_RESTRICTED_SETTINGS) {
                                    result = AppOpsManager.MODE_ALLOWED
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
