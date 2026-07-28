package com.fosstool.app.hook.scope.settings

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.param.HookParam
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Field

object AutoUnlockRestrictedSettings : YukiBaseHooker() {
    private const val OP_ACCESS_RESTRICTED_SETTINGS = 119

    private const val ECM_SETTING_IDENTIFIER = "android:bind_accessibility_service"

    private const val ECM_SERVICE = "ecm_enhanced_confirmation"

    private val RESTRICTED_LOCK_FALSE_METHODS = setOf(
        "hasBaseUserRestriction",
        "isRestricted",
        "isDisabledByAdmin",
    )

    override fun onHook() {
        if (SDK < A13) return
        if (!prefs(ModulePrefs).getBoolean("auto_unlock_restricted_settings", false)) return

        hookRestrictedPreferenceHelper()

        if (getOSVersionCode < 34) {
            hookViaDexKit()
        }

        "com.android.settingslib.RestrictedLockUtilsInternal".toClassOrNull(appClassLoader)
            ?.declaredMethods
            ?.filter {
                (it.returnType == Boolean::class.javaPrimitiveType ||
                    it.returnType == java.lang.Boolean::class.java) &&
                    it.name in RESTRICTED_LOCK_FALSE_METHODS
            }
            ?.forEach { m ->
                runCatching {
                    XposedBridge.hookMethod(m, object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            param.result = false
                        }
                    })
                }
            }
    }

    private fun hookRestrictedPreferenceHelper() {

        val ecmMode = getOSVersionCode >= 34
        val names = listOf(
            "com.oplus.settings.widget.preference.RestrictedPreferenceHelper",
            "com.android.settingslib.widget.RestrictedPreferenceHelper",
        )
        for (cls in names) {
            cls.toClassOrNull(appClassLoader)?.let { clazz ->
                clazz.method { name = "performClick" }.ignored().hook {
                    before {
                        clearRestrictedState(instance)
                        if (ecmMode) unlockViaEcm(this)
                    }
                }
                clazz.method { name = "isDisabledByAdmin" }.ignored().hook {
                    after { result = false }
                }
                clazz.method { name = "isRestricted" }.ignored().hook {
                    after { result = false }
                }
            }
        }
    }

    private fun clearRestrictedState(host: Any) {
        runCatching {
            host.javaClass.methods.firstOrNull { it.name == "setDisabledByAdmin" }
                ?.invoke(host, null)
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

                val cls = bridge.findClass {
                    matcher {
                        fields {
                            addForType(ContextClass.name)
                            addForType(StringClass.name)
                            addForType(BooleanType.name)
                            addForType(IntType.name)
                        }
                        methods {
                            add { paramCount = 0; returnType = BooleanType.name }
                            add { paramCount = 0; returnType = UnitType.name }
                            add { paramTypes(BooleanType.name); returnType = BooleanType.name }
                        }
                        usingStrings("RestrictedPreferenceHelper")
                    }
                }.checkDataList("RestrictedPreferenceHelper").firstOrNullSafe() ?: return@create

                val md = bridge.findMethod {
                    matcher {
                        paramCount = 0
                        returnType = BooleanType.name
                        addCaller {
                            name = "performClick"
                            returnType = UnitType.name
                        }
                        addUsingField { type(BooleanType.name) }
                    }
                }.checkDataList("AutoUnlockRestrictedSettings findMethod", onlyOne = false)
                    .filter { it.className == cls.name }.firstOrNull() ?: return@create

                md.className.toClassOrNull(appClassLoader)
                    ?.method { name = md.methodName; emptyParam(); returnType = BooleanType }
                    ?.ignored()
                    ?.hook { before { unlockRestrictedPreference(this) } }
            }
        }
    }

    private fun unlockRestrictedPreference(param: HookParam) {
        val host = param.instance ?: return
        val hostClass = host.javaClass

        val boolFields = hostClass.collectFields { it.type == Boolean::class.javaPrimitiveType }
        if (boolFields.isEmpty()) return

        val adminDisabled = boolFields
            .firstOrNull { it.name.contains("Admin", ignoreCase = true) }
            ?.let { runCatching { it.getBoolean(host) }.getOrDefault(false) } ?: false
        if (adminDisabled) return

        val appOpsField = boolFields.firstOrNull {
            it.name.contains("AppOps", ignoreCase = true) ||
                it.name.contains("Ecm", ignoreCase = true) ||
                it.name.contains("Restricted", ignoreCase = true)
        }
        val restricted = appOpsField
            ?.let { runCatching { it.getBoolean(host) }.getOrDefault(false) }
            ?: boolFields.any { runCatching { it.getBoolean(host) }.getOrDefault(false) }
        if (!restricted) return

        val context = hostClass.collectFields { ContextClass.isAssignableFrom(it.type) }
            .firstNotNullOfOrNull { runCatching { it.get(host) as? Context }.getOrNull() } ?: return
        val stringFields = hostClass.collectFields { it.type == String::class.java }
        val packageName = (stringFields.firstOrNull { it.name == "packageName" }
            ?: stringFields.firstOrNull())
            ?.let { runCatching { it.get(host) as? String }.getOrNull() } ?: ""

        grantRestrictedSettingsOp(context, packageName)

        hostClass.declaredMethods.firstOrNull {
            it.parameterCount == 1 &&
                it.parameterTypes[0] == Boolean::class.javaPrimitiveType &&
                it.returnType == Boolean::class.javaPrimitiveType
        }?.let { setter ->
            runCatching {
                setter.isAccessible = true
                setter.invoke(host, false)
            }
        }

        param.result = false
    }

    private fun unlockViaEcm(param: HookParam) {
        val host = param.instance ?: return
        val hostClass = host.javaClass

        val context = hostClass.collectFields { ContextClass.isAssignableFrom(it.type) }
            .firstNotNullOfOrNull { runCatching { it.get(host) as? Context }.getOrNull() } ?: return
        val intent = hostClass.collectFields { Intent::class.java.isAssignableFrom(it.type) }
            .firstNotNullOfOrNull { runCatching { it.get(host) as? Intent }.getOrNull() } ?: return
        val packageName = intent.getStringExtra("android.intent.extra.PACKAGE_NAME") ?: return

        grantRestrictedSettingsOp(context, packageName)

        runCatching {
            hostClass.methods.firstOrNull {
                it.name == "setDisabledByEcm" && it.parameterCount == 1 &&
                    Intent::class.java.isAssignableFrom(it.parameterTypes[0])
            }?.also { it.isAccessible = true }?.invoke(host, null)
        }

        param.result = false
    }

    private fun grantRestrictedSettingsOp(context: Context, packageName: String) {
        runCatching { doGrantRestrictedSettingsOp(context, packageName) }
    }

    private fun ecmApisEnabled(): Boolean = runCatching {
        Class.forName("android.permission.flags.Flags")
            .getDeclaredMethod("enhancedConfirmationModeApisEnabled")
            .also { it.isAccessible = true }
            .invoke(null) as? Boolean ?: false
    }.getOrDefault(false)

    private fun clearEcmRestriction(context: Context, packageName: String) {
        val ecm = runCatching { context.getSystemService(ECM_SERVICE) }.getOrNull() ?: return
        val cls = ecm.javaClass
        val restricted = cls.methods.firstOrNull {
            it.name == "isRestricted" && it.parameterCount == 2 &&
                it.parameterTypes[0] == String::class.java &&
                it.parameterTypes[1] == String::class.java
        }?.let {
            runCatching {
                it.isAccessible = true
                it.invoke(ecm, packageName, ECM_SETTING_IDENTIFIER) as? Boolean
            }.getOrNull()
        } ?: false
        if (!restricted) return

        val clearAllowed = cls.methods.firstOrNull {
            it.name == "isClearRestrictionAllowed" && it.parameterCount == 1 &&
                it.parameterTypes[0] == String::class.java
        }?.let {
            runCatching {
                it.isAccessible = true
                it.invoke(ecm, packageName) as? Boolean
            }.getOrNull()
        } ?: false
        if (!clearAllowed) {
            runCatching {
                cls.methods.firstOrNull {
                    it.name == "setClearRestrictionAllowed" && it.parameterCount == 1 &&
                        it.parameterTypes[0] == String::class.java
                }?.also { it.isAccessible = true }?.invoke(ecm, packageName)
            }
        }
        runCatching {
            cls.methods.firstOrNull {
                it.name == "clearRestriction" && it.parameterCount == 1 &&
                    it.parameterTypes[0] == String::class.java
            }?.also { it.isAccessible = true }?.invoke(ecm, packageName)
        }
    }

    private fun doGrantRestrictedSettingsOp(context: Context, packageName: String) {
        if (packageName.isBlank()) return
        if (ecmApisEnabled()) {
            clearEcmRestriction(context, packageName)
            return
        }
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return
        val opStr = AppOpsManager::class.java
            .getDeclaredField("OPSTR_ACCESS_RESTRICTED_SETTINGS")
            .also { it.isAccessible = true }
            .get(null) as? String ?: return
        val uid = runCatching {
            context.packageManager.getPackageUid(packageName, 0)
        }.getOrNull() ?: return
        val mode = AppOpsManager::class.java.getDeclaredMethod(
            "noteOpNoThrow",
            String::class.java,
            Int::class.javaPrimitiveType,
            String::class.java,
            String::class.java,
            String::class.java,
        ).also { it.isAccessible = true }
            .invoke(appOps, opStr, uid, packageName, null, null) as? Int ?: return
        if (mode == AppOpsManager.MODE_ALLOWED || mode == AppOpsManager.MODE_DEFAULT) return
        AppOpsManager::class.java.getDeclaredMethod(
            "setMode",
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            String::class.java,
            Int::class.javaPrimitiveType,
        ).also { it.isAccessible = true }
            .invoke(
                appOps,
                OP_ACCESS_RESTRICTED_SETTINGS,
                uid,
                packageName,
                AppOpsManager.MODE_ALLOWED,
            )
    }

    private fun Class<*>.collectFields(predicate: (Field) -> Boolean): List<Field> {
        val result = ArrayList<Field>()
        var c: Class<*>? = this
        while (c != null && c != Any::class.java) {
            c.declaredFields.forEach { f ->
                if (predicate(f)) {
                    f.isAccessible = true
                    result.add(f)
                }
            }
            c = c.superclass
        }
        return result
    }
}
