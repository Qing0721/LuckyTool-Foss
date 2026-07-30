package com.fosstool.app.hook.hooker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.hook.utils.OplusBuildUtlils
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker
import com.fosstool.app.hook.utils.SystemPropertiesUtils
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getAppSet
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.net.Inet4Address
import java.net.Inet6Address
import org.json.JSONObject

private fun Class<*>.findMethod(name: String, paramCount: Int? = null): Method? {
    var c: Class<*>? = this
    while (c != null && c != Any::class.java) {
        c.declaredMethods.firstOrNull {
            it.name == name && (paramCount == null || it.parameterCount == paramCount)
        }?.let { return it.apply { isAccessible = true } }
        c = c.superclass
    }
    return null
}

private fun Class<*>.findMethods(name: String, paramCount: Int? = null): List<Method> {
    val result = mutableListOf<Method>()
    var c: Class<*>? = this
    while (c != null && c != Any::class.java) {
        c.declaredMethods.filter {
            it.name == name && (paramCount == null || it.parameterCount == paramCount)
        }.forEach { result.add(it.apply { isAccessible = true }) }
        c = c.superclass
    }
    return result
}

private fun Class<*>.findField(name: String): Field? {
    var c: Class<*>? = this
    while (c != null && c != Any::class.java) {
        c.declaredFields.firstOrNull { it.name == name }?.let { return it.apply { isAccessible = true } }
        c = c.superclass
    }
    return null
}

private fun Method.hookBefore(block: (XC_MethodHook.MethodHookParam) -> Unit) {
    runCatching {
        XposedBridge.hookMethod(this, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) = block(param)
        })
    }
}

private fun Method.hookAfter(block: (XC_MethodHook.MethodHookParam) -> Unit) {
    runCatching {
        XposedBridge.hookMethod(this, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) = block(param)
        })
    }
}

private fun Method.replaceConst(value: Any?) {
    runCatching { XposedBridge.hookMethod(this, XC_MethodReplacement.returnConstant(value)) }
}

private fun Method.interceptNull() {
    hookBefore { it.result = null }
}

object HookBeaconLink : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_beacon_link_time_limit", false)) return
        val os = try {
            OplusBuildUtlils(appClassLoader).getOSVersionCode ?: 0
        } catch (_: Throwable) {
            0
        }
        if (os < 33) return
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            val cls = dexKitBridge.findClass {
                matcher {
                    fields {
                        add {
                            type = StringClass.name
                            addReadMethod { returnType = "java.util.HashMap" }
                        }
                        add {
                            type = LongType.name
                            addWriteMethod {
                                paramTypes(ContextClass.name, StringClass.name, StringClass.name)
                            }
                            addReadMethod {
                                paramTypes(ContextClass.name, StringClass.name, StringClass.name)
                            }
                        }
                        count = 2
                    }
                }
            }.checkDataList("HookBeaconLink.class")
                .firstOrNullSafe()?.name?.toClassOrNull(appClassLoader) ?: return@create

            val boolStr = cls.declaredMethods.firstOrNull {
                it.parameterCount == 1 &&
                    it.parameterTypes[0] == String::class.java &&
                    (it.returnType == java.lang.Boolean.TYPE || it.returnType == Boolean::class.java)
            }
            if (boolStr != null) {
                boolStr.replaceConst(true)
                return@create
            }
            val ctor = cls.declaredConstructors.firstOrNull {
                it.parameterCount == 2 &&
                    it.parameterTypes[0] == String::class.java &&
                    (it.parameterTypes[1] == java.lang.Long.TYPE || it.parameterTypes[1] == Long::class.java)
            } ?: return@create
            XposedBridge.hookMethod(ctor, object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (param.args.isNotEmpty()) param.args[param.args.lastIndex] = 0L
                }
            })
        }
    }
}

object HookCalendar : YukiBaseHooker() {
    override fun onHook() {
        val removeHoliday = prefs(ModulePrefs).getBoolean("remove_holiday_page_information_flow", false) ||
            prefs(ModulePrefs).getBoolean("remove_holiday_page_feed", false)
        val removeAlmanac = prefs(ModulePrefs).getBoolean("remove_almanac_page_information_flow", false) ||
            prefs(ModulePrefs).getBoolean("remove_almanac_page_feed", false)
        val removeHoroscope = prefs(ModulePrefs).getBoolean("remove_horoscope_page_information_flow", false) ||
            prefs(ModulePrefs).getBoolean("remove_constellation_page_feed", false)
        if (!removeHoliday && !removeAlmanac && !removeHoroscope) return

        if (removeHoliday) {
            "com.coloros.calendar.app.specialholiday.SpecialHolidayWebViewDetailViewModel"
                .toClassOrNull(appClassLoader)
                ?.let { cls ->
                    val m = cls.findMethod("buildHolidayH5UrlInner")
                    if (m != null) m.hookAfter { it.result = "" }
                    else cls.findMethods("buildHolidayH5UrlInner").forEach { it.hookAfter { p -> p.result = "" } }
                }
        }
        if (removeAlmanac) {
            "com.android.calendar.module.subscription.almanac.adapter.AlmanacPagesAdapter".toClassOrNull(appClassLoader)?.let { cls ->
                cls.findMethods("onCreateViewHolder").forEach { m ->
                    m.hookBefore { param ->
                        for (i in 3 downTo 0) {
                            val v = param.args.getOrNull(i)
                            if (v is Int) {
                                param.args[i] = 0
                                break
                            }
                        }
                    }
                }
                cls.findMethods("getItemCount").forEach { it.hookAfter { p -> p.result = 0 } }
            }
        }
        if (removeHoroscope) {
            "com.android.calendar.module.subscription.horoscope.HoroscopeFragment".toClassOrNull(appClassLoader)
                ?.findMethods("onViewCreated")
                ?.forEach { m ->
                    m.hookAfter { param ->
                        runCatching {
                            val inst = param.thisObject
                            val v = inst as? android.view.View
                                ?: inst?.javaClass?.methods
                                    ?.firstOrNull { it.name == "getView" && it.parameterCount == 0 }
                                    ?.invoke(inst) as? android.view.View
                            v?.visibility = android.view.View.GONE
                        }
                    }
                }
        }
    }
}

object HookEngineerMode : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("unlock_some_hidden_options", false) &&
            !prefs(ModulePrefs).getBoolean("unlock_engineer_mode_hidden_options", false)
        ) return

        val helper = "com.oplus.engineermode.impl.SecrecyServiceHelper".toClassOrNull(appClassLoader)
            ?: return
        helper.findMethod("isSecrecySupported")?.replaceConst(true)
        helper.findMethod("getSecrecyState")?.replaceConst(false)
    }
}

object HookEyeProtect : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("enable_eyeprotect_paper_texture_support", false)) return
        if (SDK < A13) return
        loadHooker(SystemPropertiesOverrideEngineHooker(mode = SystemPropertiesOverrideEngineHooker.Mode.RM0_Q))
        val featureMap = mapOf(
            "oplus.software.display.eyeprotect_paper_texture_support" to true,
            "oplus.software.display.smart_color_temperature_rhythm_health_support" to true,
        )
        "com.oplus.content.OplusFeatureConfigManager".toClassOrNull(appClassLoader)
            ?.findMethod("hasFeature", 1)
            ?.hookBefore { param ->
                val key = param.args.getOrNull(0) as? String ?: return@hookBefore
                featureMap[key]?.let { param.result = it }
            }
    }
}

object HookFileManager : YukiBaseHooker() {
    override fun onHook() {
        val removeSave = prefs(ModulePrefs).getBoolean("remove_word_limit_for_saving_files", false)
        val removeCompress = prefs(ModulePrefs).getBoolean("remove_word_limit_for_compress_files", false)
        val removeRename = prefs(ModulePrefs).getBoolean("remove_word_limit_for_label_name_files", false)
        if (!removeSave && !removeCompress && !removeRename) return
        val os = try {
            OplusBuildUtlils(appClassLoader).getOSVersionCode ?: 0
        } catch (_: Throwable) {
            0
        }
        if (os < 37) return
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            if (removeSave) {

                dexKitBridge.findClass {
                    matcher {
                        className = "com.oplus.filemanager.picker.controller.ActionModeController"
                    }
                }.checkDataList("HookFileManager.save", onlyOne = false).findField {
                    matcher {
                        type = "int"
                        addReadMethod {
                            paramCount = 0
                            returnType = "void"
                        }
                        addReadMethod {
                            paramCount = 4
                            returnType = "void"
                        }
                    }
                }.firstOrNull()?.let { fd ->
                    val cls = fd.declaredClassName.toClassOrNull(appClassLoader) ?: return@let
                    val maxField = cls.findField(fd.fieldName) ?: return@let
                    for (ctor in cls.declaredConstructors) {
                        if (ctor.parameterCount != 1) continue
                        XposedBridge.hookMethod(ctor, object : XC_MethodHook() {
                            override fun afterHookedMethod(param: MethodHookParam) {
                                val inst = param.thisObject ?: return
                                runCatching { maxField.set(inst, 9999) }
                            }
                        })
                    }
                }
            }
            if (removeCompress) {
                hookDialogMaxCount(
                    dexKitBridge.findClass {
                        matcher {
                            methods {
                                add { name = "onTextChanged" }
                                add {
                                    paramTypes("android.widget.EditText", "android.text.InputFilter")
                                }
                            }
                            usingStrings("CompressConfirmDialog")
                        }
                    }.checkDataList("HookFileManager.compress", onlyOne = false),
                    "HookFileManager.compress",
                )
            }
            if (removeRename) {
                hookDialogMaxCount(
                    dexKitBridge.findClass {
                        matcher {
                            methods {
                                add { name = "onActivityResume" }
                                add { name = "onTextChanged" }
                            }
                            usingStrings("BaseFileNameDialog")
                        }
                    }.checkDataList("HookFileManager.rename", onlyOne = false),
                    "HookFileManager.rename",
                )
            }
        }
    }

    private fun hookDialogMaxCount(
        classList: org.luckypray.dexkit.result.ClassDataList,
        tag: String,
    ) {
        if (classList.isEmpty()) return
        classList.findMethod {
            matcher {
                paramCount = 0
                returnType = "int"
                usingNumbers(50)
            }
        }.apply {
            checkDataList("$tag.maxCount")
            val member = firstOrNullSafe() ?: return@apply
            member.className.toClassOrNull(appClassLoader)
                ?.findMethod(member.methodName, 0)
                ?.replaceConst(9999)
        }
    }
}

object HookHealth : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_health_root_check_dialog", false) &&
            !prefs(ModulePrefs).getBoolean("remove_health_root_detection_dialog", false)
        ) return

        val safety = "com.heytap.health.safety.safetycheck.SafetyCheckManager".toClassOrNull(appClassLoader)
            ?: return
        for (m in safety.declaredMethods) {
            if (m.parameterCount == 1 &&
                android.app.Activity::class.java.isAssignableFrom(m.parameterTypes[0])
            ) {
                m.interceptNull()
            }
        }
    }
}

object HookContactsScope : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_Q
            )
        )
    }
}

object HookBluetoothScope : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_Q
            )
        )
    }
}

object HookAtlasScope : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.BOTH
            )
        )
    }
}

object HookAccessoryScope : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_T
            )
        )
    }
}

object HookMcs : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_T,
                includeRegionDefaults = true,
            )
        )
    }
}

object HookMyDevices : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("force_enable_feiniu_cloud_nas_option", false) &&
            !prefs(ModulePrefs).getBoolean("support_fn_nas", false)
        ) return
        val propKey = "ro.oplus.feiniunas.support"
        fun hookProps(cls: Class<*>) {
            cls.findMethod("get", 2)?.hookAfter { param ->
                if (param.args.getOrNull(0) as? String == propKey) param.result = "true"
            }
            cls.findMethod("get", 1)?.hookAfter { param ->
                if (param.args.getOrNull(0) as? String == propKey) param.result = "true"
            }
            cls.findMethod("getBoolean", 2)?.hookBefore { param ->
                if (param.args.getOrNull(0) as? String == propKey) param.result = true
            }
        }
        "android.os.SystemProperties".toClassOrNull(appClassLoader)?.let { hookProps(it) }
        "com.oplus.wrapper.os.SystemProperties".toClassOrNull(appClassLoader)?.let { hookProps(it) }
    }
}

object HookNfc : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("scan_nfc_tag_auto_click", false) &&
            !prefs(ModulePrefs).getBoolean("scan_nfc_tag_auto_click_button", false)
        ) return
        val action = "com.oplus.nfc.dispatch.TagDetectedNotification.ACTION_PROCESS_TAG"
        fun nfcShowBefore(param: XC_MethodHook.MethodHookParam) {
            val ctx = param.args.getOrNull(0) as? Context ?: return
            val dispatcherIntent = param.args.getOrNull(1) as? Intent ?: return
            val componentType = param.args.getOrNull(2) as? Int ?: 0
            val intent = Intent().apply {
                setAction(action)
                putExtra("dispatcherIntent", dispatcherIntent)
                putExtra("componentType", componentType)
                setPackage("com.android.nfc")
            }
            runCatching {
                PendingIntent.getBroadcast(
                    ctx,
                    System.currentTimeMillis().toInt(),
                    intent,
                    201326592,
                ).send()
            }
        }

        val nfcCls = "com.oplus.nfc.dispatch.TagDetectedNotification".toClassOrNull(appClassLoader)
            ?: return
        val show = nfcCls.findMethod("show")
        if (show != null) show.hookBefore { nfcShowBefore(it) }
        else nfcCls.findMethods("show").forEach { it.hookBefore { p -> nfcShowBefore(p) } }
    }
}

object HookOShare : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_oshare_close_countdown", false)) return
        val os = try {
            OplusBuildUtlils(appClassLoader).getOSVersionCode ?: 0
        } catch (_: Throwable) {
            0
        }
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            if (os >= 27) {

                val configClasses = dexKitBridge.findClass {
                    matcher { usingStrings("OShareFeatureConfig") }
                }.checkDataList("HookOShare.OShareFeatureConfig")
                if (configClasses.isNotEmpty()) {
                    dexKitBridge.findMethod {
                        searchInClass(configClasses)
                        matcher {
                            paramTypes(ContextClass.name)
                            returnType = LongType.name
                            usingStrings("getSwitchTimeOut")
                        }
                    }.apply {
                        checkDataList("HookOShare.getSwitchTimeOut")
                        firstOrNullSafe()?.let { data ->
                            data.className.toClassOrNull(appClassLoader)
                                ?.findMethod(data.methodName, 1)
                                ?.replaceConst(0L)
                        }
                    }
                }
            }

            val spClasses = dexKitBridge.findClass {
                matcher { usingStrings("SpUtils", "share_config") }
            }.checkDataList("HookOShare.SpUtils")
            if (spClasses.isEmpty()) return@create
            dexKitBridge.findMethod {
                searchInClass(spClasses)
                matcher {
                    paramTypes(ContextClass.name, LongType.name)
                    usingStrings("updateLastTurnOnTime", "key_last_turn_on_time")
                }
            }.apply {
                checkDataList("HookOShare.updateLastTurnOnTime")
                firstOrNullSafe()?.let { data ->
                    data.className.toClassOrNull(appClassLoader)
                        ?.findMethod(data.methodName, 2)
                        ?.hookBefore { param ->
                            if (param.args.isNotEmpty()) param.args[param.args.lastIndex] = 0L
                        }
                }
            }
        }
    }
}

object HookSecurityPermission : YukiBaseHooker() {
    override fun onHook() {
        val useOldDialog = prefs(ModulePrefs).getBoolean("app_start_dialog_use_old_version", false) ||
            prefs(ModulePrefs).getBoolean("use_old_version_app_jump_dialog", false)
        val enableAlwaysAllow = prefs(ModulePrefs).getBoolean("enable_always_allow_app_start_dialog", false)
        val autoUnlock = prefs(ModulePrefs).getBoolean("auto_unlock_app_ecm_permission_restrict", false)
        if (!useOldDialog && !enableAlwaysAllow && !autoUnlock) return

        if (useOldDialog) {
            "com.oplusos.securitypermission.permission.ui.AppStartConfirmDialogActivity"
                .toClassOrNull(appClassLoader)
                ?.let { cls ->
                    val m = cls.findMethod("onCreate") ?: cls.findMethods("onCreate").firstOrNull()
                    m?.hookBefore { param ->
                        val activity = param.thisObject as? android.app.Activity ?: return@hookBefore
                        activity.intent?.putExtra("activity_start_confirm_version", 0)
                    }
                }
        }

        if (autoUnlock) {
            "com.oplusos.securitypermission.permission.PermissionGroupsActivity"
                .toClassOrNull(appClassLoader)
                ?.let { cls ->
                    val m = cls.findMethod("onCreate") ?: cls.findMethods("onCreate").firstOrNull()
                    m?.hookAfter { param ->
                        val activity = param.thisObject as? android.app.Activity ?: return@hookAfter
                        val pkg = activity.intent?.getStringExtra("packageName")
                            ?: activity.intent?.getStringExtra("mPackageName")
                            ?: return@hookAfter
                        clearEcmRestriction(activity, pkg)
                    }
                }
        }

        if (enableAlwaysAllow) {
            val source = appInfo.sourceDir ?: return
            DexkitUtils.create(source) { dexKitBridge ->

                val permissionClasses = dexKitBridge.findClass {
                    matcher {
                        fields { addForType("android.os.ISecurityPermissionService") }
                        usingStrings("OplusPermissionManager")
                    }
                }.checkDataList("HookSecurityPermission.enableAlwaysAllow.scope", onlyOne = false)
                if (permissionClasses.isNotEmpty()) {
                    dexKitBridge.findMethod {
                        searchInClass(permissionClasses)
                        matcher {
                            paramTypes("android.os.Bundle")
                            usingStrings("OplusPermissionManager", "putActivityStartWhiteList")
                        }
                    }.apply {
                        checkDataList("HookSecurityPermission.enableAlwaysAllow.whitelist")
                        firstOrNullSafe()?.let { data ->
                            data.className.toClassOrNull(appClassLoader)?.findMethod(data.methodName, 1)
                                ?.hookBefore { param ->
                                    (param.args.getOrNull(0) as? android.os.Bundle)?.remove("valid_time")
                                }
                        }
                    }
                }

                val dialogClasses = dexKitBridge.findClass {
                    matcher {
                        fields {
                            addForType("android.content.res.Configuration")
                            addForType("android.content.ComponentCallbacks")
                            addForType("android.content.DialogInterface\$OnClickListener")
                        }
                        usingStrings("COUIAlertDialogBuilder")
                    }
                }.checkDataList("HookSecurityPermission.enableAlwaysAllow.dialog", onlyOne = false)
                dialogClasses.findMethod {
                    matcher {
                        paramTypes(
                            "int",
                            "android.content.DialogInterface\$OnClickListener",
                            "boolean",
                        )
                        addUsingField { type("int") }
                        usingNumbers(android.R.id.button3)
                    }
                }.apply {
                    checkDataList("HookSecurityPermission.enableAlwaysAllow.setButton")
                    firstOrNullSafe()?.let { data ->
                        data.className.toClassOrNull(appClassLoader)?.findMethod(data.methodName, 3)
                            ?.hookBefore { param ->
                                val ctx = (param.thisObject as? Context)
                                    ?: param.args.filterIsInstance<Context>().firstOrNull()
                                    ?: appContext
                                    ?: return@hookBefore
                                val allow30 = ctx.resources.getIdentifier(
                                    "app_start_dialog_allow_30", "string", ctx.packageName,
                                )
                                val always = ctx.resources.getIdentifier(
                                    "app_start_dialog_always_allow", "string", ctx.packageName,
                                )
                                if (allow30 <= 0 || always <= 0) return@hookBefore
                                for (i in param.args.indices) {
                                    if (param.args[i] is Int && param.args[i] == allow30) {
                                        param.args[i] = always
                                    }
                                }
                            }
                    }
                }
            }
        }
    }

    private fun clearEcmRestriction(context: Context, packageName: String) {
        runCatching {
            val appOps = context.getSystemService(android.app.AppOpsManager::class.java) ?: return
            val opStr = runCatching {
                android.app.AppOpsManager::class.java.getField("OPSTR_ACCESS_RESTRICTED_SETTINGS")
                    .get(null) as? String
            }.getOrNull()
            if (opStr != null) {
                val uid = runCatching {
                    context.packageManager.getApplicationInfo(packageName, 0).uid
                }.getOrNull() ?: return@runCatching
                runCatching {
                    appOps.javaClass.getMethod(
                        "setMode",
                        String::class.java,
                        Int::class.javaPrimitiveType,
                        String::class.java,
                        Int::class.javaPrimitiveType,
                    ).invoke(appOps, opStr, uid, packageName, android.app.AppOpsManager.MODE_ALLOWED)
                }
            }
        }
        runCatching {
            val ecm = context.getSystemService("ecm_enhanced_confirmation") ?: return@runCatching
            val cls = ecm.javaClass
            val isRestricted = cls.methods.firstOrNull {
                it.name == "isRestricted" && it.parameterCount == 2
            }
            val restricted = isRestricted?.invoke(ecm, packageName, "android:bind_accessibility_service") as? Boolean
            if (restricted == true) {
                val isClearAllowed = cls.methods.firstOrNull {
                    it.name == "isClearRestrictionAllowed" && it.parameterCount == 1
                }?.invoke(ecm, packageName) as? Boolean
                if (isClearAllowed != true) {
                    cls.methods.firstOrNull {
                        it.name == "setClearRestrictionAllowed" && it.parameterCount == 1
                    }?.invoke(ecm, packageName)
                }
                cls.methods.firstOrNull {
                    it.name == "clearRestriction" && it.parameterCount == 1
                }?.invoke(ecm, packageName)
            }
        }
    }
}

object HookSmartSidebar : YukiBaseHooker() {
    private const val CLS_FEATURE =
        "com.coloros.edgepanel.utils.EdgePanelFeatureOption"
    private const val CLS_UTILS =
        "com.coloros.edgepanel.utils.EdgePanelUtils"
    private const val CLS_TOOL_HELPER =
        "com.oplus.smartsidebar.panelview.edgepanel.data.entrybeans.ToolEntryHelper"
    private val TOOL_CLASSES = arrayOf(
        "com.oplus.smartsidebar.panelview.edgepanel.data.entrybeans.models.tools.BackgroundRunTool",
        "com.oplus.smartsidebar.panelview.edgepanel.data.entrybeans.models.tools.GTModelTool",
        "com.oplus.smartsidebar.panelview.edgepanel.data.entrybeans.models.tools.CleanStorageTool",
    )

    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_Q
            )
        )
        val autoHide = prefs(ModulePrefs).getBoolean("force_enable_buoy_automatically_hides", false)
        val transferDock = prefs(ModulePrefs).getBoolean("unlock_transfer_dock", false)
        val recentFiles = prefs(ModulePrefs).getBoolean("unlock_recent_files", false)
        val fluidCloud = prefs(ModulePrefs).getBoolean("unlock_fluid_cloud", false)
        val runInBg = prefs(ModulePrefs).getBoolean("enable_run_in_background", false)
        val os = try {
            OplusBuildUtlils(appClassLoader).getOSVersionCode ?: 0
        } catch (_: Throwable) {
            0
        }

        val sidebarVersionCode = getAppSet(ModulePrefs, packageName)[1].toLongOrNull() ?: 0L

        if ((transferDock || recentFiles || fluidCloud) && SDK == 33 &&
            sidebarVersionCode >= 14000000L
        ) {
            CLS_FEATURE.toClassOrNull(appClassLoader)?.let { cls ->
                cls.findMethod("loadFeatureOption")?.hookAfter {
                    if (recentFiles) setStaticBool(cls, "IS_SHIELD_FILE_BAG", false)
                    if (fluidCloud) setStaticBool(cls, "IS_SHIELD_FLUID_CLOUD", false)
                    if (transferDock) setStaticBool(cls, "IS_SHIELD_TRANSFER_DOCK", false)
                }
            }
        }

        if (autoHide && SDK == 31) {
            CLS_UTILS.toClassOrNull(appClassLoader)?.let { cls ->
                cls.findMethods("isMetaDataSupportByPackage", 2).forEach { m ->
                    m.hookAfter { param ->
                        val pkg = param.args.getOrNull(0) as? String ?: return@hookAfter
                        val key = param.args.getOrNull(1) as? String
                            ?: param.args.lastOrNull() as? String
                            ?: return@hookAfter
                        if (pkg == "com.android.systemui" && key == "sidebar_gesture_support") {
                            param.result = true
                        }
                    }
                }
                cls.findMethods("isMetaDataSupportByPackage").forEach { m ->
                    m.hookAfter { param ->
                        if (param.args.size < 2) return@hookAfter
                        val strs = param.args.filterIsInstance<String>()
                        if (strs.contains("com.android.systemui") && strs.contains("sidebar_gesture_support")) {
                            param.result = true
                        }
                    }
                }
            }
        }

        if (runInBg && os >= 27) {
            for (name in TOOL_CLASSES) {
                name.toClassOrNull(appClassLoader)?.let { cls ->
                    cls.findMethod("isToolAvailable")?.replaceConst(true)
                }
            }
        }
    }

    private fun setStaticBool(cls: Class<*>, fieldName: String, value: Boolean) {
        runCatching {
            val f = cls.findField(fieldName) ?: return
            f.isAccessible = true
            f.set(null, value)
        }
    }
}

class HookSoundRecorder : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_T
            )
        )
        val enableRecord = prefs(ModulePrefs).getBoolean("enable_record_calls_on_third_party_apps", false)
        val os = try {
            OplusBuildUtlils(appClassLoader).getOSVersionCode ?: 0
        } catch (_: Throwable) {
            0
        }
        if (enableRecord && os == 30) {
            "com.soundrecorder.base.utils.BaseUtil".toClassOrNull(appClassLoader)
                ?.let { cls ->
                    cls.findMethod("isRealme")?.replaceConst(true)
                }
        }
    }
}

object HookSpeechAssist : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("force_enable_ai_speechassist_call", false)) return

        val os = try {
            OplusBuildUtlils(appClassLoader).getOSVersionCode ?: 0
        } catch (_: Throwable) {
            0
        }
        if (os < 30) return

        val bean = "com.heytap.speechassist.aicall.setting.config.AiCallCommonBean"
            .toClassOrNull(appClassLoader) ?: return
        bean.findMethod("getSupportAiCall")?.replaceConst(true)
        bean.findMethod("getSupportAiCallV2")?.replaceConst(true)
    }
}

class HookTeleService : YukiBaseHooker() {
    private val propHideNr = "ro.oplus.radio.hide_nr_switch"

    override fun onHook() {
        val force5G = prefs(ModulePrefs).getBoolean("force_display_five_g_switch", false) ||
            prefs(ModulePrefs).getBoolean("force_display_5g_switch", false)
        val forceVoLTE = prefs(ModulePrefs).getBoolean("force_display_volte_calls", false) ||
            prefs(ModulePrefs).getBoolean("force_display_volte_hd_call", false)
        val forceNetworkType = prefs(ModulePrefs).getBoolean("force_display_preferred_network_type", false)
        if (!force5G && !forceVoLTE && !forceNetworkType) return

        if (force5G) {
            fun hookHideNr(cls: Class<*>) {
                cls.findMethod("getInt", 2)?.hookBefore { param ->
                    if (param.args.getOrNull(0) as? String == propHideNr) param.result = -1
                }
                cls.findMethod("get", 2)?.hookAfter { param ->
                    if (param.args.getOrNull(0) as? String == propHideNr) param.result = "-1"
                }
            }
            "android.os.SystemProperties".toClassOrNull(appClassLoader)?.let { hookHideNr(it) }
            "com.oplus.wrapper.os.SystemProperties".toClassOrNull(appClassLoader)?.let { hookHideNr(it) }
        }

        if (forceVoLTE || forceNetworkType) {
            "com.android.simsettings.activity.OplusSimInfoActivity".toClassOrNull(appClassLoader)?.let { sim ->
                if (forceVoLTE) {
                    val hook: (XC_MethodHook.MethodHookParam) -> Unit = hook@{ param ->
                        val first = param.args.getOrNull(0) as? Int ?: return@hook
                        if (first != 1) return@hook
                        for (i in 1..2) {
                            if (param.args.getOrNull(i) is Boolean) {
                                param.args[i] = true
                                return@hook
                            }
                        }
                    }
                    val methods = sim.findMethods("changeVolteSwitchConfig", 3)
                    methods.forEach { it.hookBefore(hook) }
                }
                if (forceNetworkType) {
                    val hook: (XC_MethodHook.MethodHookParam) -> Unit = hook@{ param ->
                        val first = param.args.getOrNull(0) as? Int ?: return@hook
                        if (first != 1) return@hook
                        for (i in 1..2) {
                            if (param.args.getOrNull(i) is Boolean) {
                                param.args[i] = true
                                return@hook
                            }
                        }
                    }
                    val methods = sim.findMethods("changeNetworkModeConfig", 3)
                    methods.forEach { it.hookBefore(hook) }
                }
            }
        }
    }
}

class HookWirelessSettings : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("enable_wifi_details_display_gateway", false) &&
            !prefs(ModulePrefs).getBoolean("enable_wifi_detail_show_gateway", false)
        ) return

        val cls = listOf(
            "com.oplus.wirelesssettings.wifi.detail.WifiAddressController",
            "com.oplus.wirelesssettings.wifi.detail2.WifiAddressController",
        ).firstNotNullOfOrNull { it.toClassOrNull(appClassLoader) } ?: return
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            val classes = dexKitBridge.findClass {
                matcher { className = cls.name }
            }.checkDataList("HookWirelessSettings.WifiAddressController")
            if (classes.isEmpty()) return@create
            dexKitBridge.findMethod {
                searchInClass(classes)
                matcher {
                    paramCount = 0
                    usingStrings("updateIpInfo")
                }
            }.apply {
                checkDataList("HookWirelessSettings.updateIpInfo")
                firstOrNullSafe()?.let { data ->
                    data.className.toClassOrNull(appClassLoader)
                        ?.findMethod(data.methodName, 0)
                        ?.hookAfter { injectWifiGateway(it.thisObject) }
                }
            }
        }
    }

    private fun injectWifiGateway(controller: Any?) {
        controller ?: return
        runCatching {
            val fields = controller.javaClass.declaredFields
            val screen = fields.firstOrNull {
                it.type.name.endsWith("PreferenceScreen")
            }?.also { it.isAccessible = true }?.get(controller) ?: return
            val findPref = screen.javaClass.methods.firstOrNull {
                it.name == "findPreference" &&
                    it.parameterTypes.size == 1 &&
                    it.parameterTypes[0] == String::class.java
            } ?: return
            val ctx = fields.firstOrNull {
                Context::class.java.isAssignableFrom(it.type)
            }?.also { it.isAccessible = true }?.get(controller) as? Context
            if (ctx == null) return
            val cm = ctx.getSystemService(ConnectivityManager::class.java) ?: return
            @Suppress("DEPRECATION")
            val wm = ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                ?: return
            val network = runCatching {
                WifiManager::class.java.getMethod("getCurrentNetwork").invoke(wm)
            }.getOrNull() as? android.net.Network ?: return
            val lp = cm.getLinkProperties(network) ?: return
            var v4: String? = null
            var v6: String? = null
            for (route in lp.routes) {
                if (!route.isDefaultRoute) continue
                val gw = route.gateway?.hostAddress ?: continue
                when (route.destination?.address) {
                    is Inet4Address -> v4 = gw
                    is Inet6Address -> v6 = gw
                }
            }
            fun setGw(key: String, gateway: String?) {
                if (gateway.isNullOrEmpty()) return
                val pref = findPref.invoke(screen, key) ?: return
                val isEnabled = runCatching {
                    pref.javaClass.getMethod("isEnabled").invoke(pref) as Boolean
                }.getOrDefault(false)
                if (!isEnabled) return
                val title = runCatching {
                    pref.javaClass.getMethod("getTitle").invoke(pref)?.toString()
                }.getOrNull() ?: return
                runCatching {
                    pref.javaClass.getMethod("setSummary", CharSequence::class.java)
                        .invoke(pref, "$title\n$gateway")
                }
            }
            setGw("current_ipv4_address", v4)
            setGw("current_ipv6_address", v6)
        }
    }
}

object HookMultiApp : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_Q
            )
        )
        loadHooker(com.fosstool.app.hook.scope.multiapp.RemoveMultiAppBlacklist)
    }
}

object HookMediaController : YukiBaseHooker() {
    private const val KEY_STATIC_VOICE_PRINT_SHOW = "staticVoicePrintShow"

    override fun onHook() {
        val forceRipple =
            prefs(ModulePrefs).getBoolean("force_enable_media_music_fluid_cloud_ripple", false)
        if (!forceRipple) return
        val osVersionCode = try { OplusBuildUtlils().getOSVersionCode ?: 0 } catch (_: Throwable) { 0 }
        if (osVersionCode < 33) return
        "com.oplus.pantanal.seedling.util.SeedlingTool".toClassOrNull(appClassLoader)?.let { cls ->
            for (m in cls.declaredMethods) {
                if (!java.lang.reflect.Modifier.isStatic(m.modifiers)) continue
                if (m.parameterCount !in 1..8) continue
                m.hookBefore { param ->
                    var json: JSONObject? = null
                    if (param.args.size > 1 && param.args[1] is JSONObject) {
                        json = param.args[1] as JSONObject
                    } else {
                        for (i in 0 until minOf(6, param.args.size)) {
                            val v = param.args[i]
                            if (v is JSONObject) { json = v; break }
                        }
                    }
                    json ?: return@hookBefore
                    if (json.optBoolean(KEY_STATIC_VOICE_PRINT_SHOW, true)) {
                        runCatching { json.put(KEY_STATIC_VOICE_PRINT_SHOW, false) }
                    }
                }
            }
        }
    }
}

object HookSau : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_T
            )
        )
    }
}

class HookAudioMonitor : YukiBaseHooker() {
    private data class VoipApp(val packageName: String, val displayName: String, val activity: String? = null)

    private val voipApps = listOf(
        VoipApp("com.tencent.mobileqq", "QQ"),
        VoipApp("com.tencent.mm", "微信"),
        VoipApp("com.tencent.wework", "企业微信"),
        VoipApp("com.tencent.tim", "Tim"),
        VoipApp("com.ss.android.lark", "飞书"),
        VoipApp(
            "com.ss.android.ugc.aweme",
            "抖音",
            "com.bytedance.android.xr.fusion.XrAvCallActivity",
        ),
    )
    private val voipWhitelistPkgs = voipApps.map { it.packageName }

    companion object {
        private const val CLS_VOIP =
            "com.oplus.audiomonitor.voiprecord.OplusVoipRecorderService"
        private const val CLS_APP = "com.oplus.audiomonitor.AudioApplication"
        private const val PROP_VOIP_WHITE =
            "ro.oplus.audio.voip_record_white_app_support"
        private const val PREF_RECORD_CALLS = "enable_record_calls_on_third_party_apps"
        private const val PREF_EXPAND_VOIP = "expand_voip_recorder_whitelist"
        private const val UNKNOWN_RECORD = "未知应用录音"
        private const val SP_SUFFIX = "_preferences"
        private const val SP_KEY_ENABLE = "enable_record_app"
        private const val PKG_MOBILEQQ = "com.tencent.mobileqq"
        private const val PKG_MM = "com.tencent.mm"
        private const val ACT_AV = "com.tencent.av.ui.AVActivity"
        private const val ACT_DOUYIN = "com.bytedance.android.xr.fusion.XrAvCallActivity"
    }

    override fun onHook() {
        val os = try {
            OplusBuildUtlils(appClassLoader).getOSVersionCode ?: 0
        } catch (_: Throwable) {
            0
        }
        val enableRecord = prefs(ModulePrefs).getBoolean(PREF_RECORD_CALLS, false)
        val expandVoip = prefs(ModulePrefs).getBoolean(PREF_EXPAND_VOIP, false)

        if (enableRecord && os == 30) {
            hookPathAOnStartCommand()
        }
        if (expandVoip && os >= 31) {
            hookPathBOnCreate()
            hookPathBBooleanAllow()
            hookPathBUnknownName()
            hookPathBRecordWrapperList()
        }
    }

    private fun hookPathAOnStartCommand() {
        val cls = CLS_VOIP.toClassOrNull(appClassLoader) ?: return
        val m = cls.findMethod("onStartCommand")
        m?.hookBefore { param ->
            val supported = runCatching {
                SystemPropertiesUtils(appClassLoader)
                    .getBoolean(PROP_VOIP_WHITE, false) == true
            }.getOrDefault(false)
            if (supported) return@hookBefore
            val cmd = "setprop $PROP_VOIP_WHITE true"
            val rootOk = runCatching {
                com.fosstool.app.utils.ShellUtils.checkRootPermission()
            }.getOrDefault(false)
            if (rootOk) {
                runCatching {
                    com.fosstool.app.utils.ShellUtils.execCommand(cmd, true, false)
                }
            } else {
                val ctx = param.thisObject as? android.content.Context
                    ?: appContext
                ctx?.let {
                    runCatching {
                        android.widget.Toast.makeText(it, "No Root!", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun hookPathBOnCreate() {
        val cls = CLS_VOIP.toClassOrNull(appClassLoader) ?: return
        val m = cls.findMethod("onCreate")
        if (m != null) {
            m.hookBefore { param ->
                val inst = param.thisObject ?: return@hookBefore
                injectVoipArrayLists(inst)
            }
        } else {
            cls.findMethods("onCreate").forEach { mm ->
                mm.hookBefore { param ->
                    val inst = param.thisObject ?: return@hookBefore
                    injectVoipArrayLists(inst)
                }
            }
        }
    }

    private fun hookPathBBooleanAllow() {
        val cls = CLS_VOIP.toClassOrNull(appClassLoader) ?: return
        cls.declaredMethods.filter {
            it.returnType == java.lang.Boolean.TYPE || it.returnType == Boolean::class.java
        }.forEach { m ->
            m.hookAfter { param ->
                val pkg = readStringField(param.thisObject) ?: return@hookAfter
                if (voipWhitelistPkgs.contains(pkg)) {
                    param.result = true
                }
            }
        }
    }

    private fun hookPathBUnknownName() {
        val cls = CLS_VOIP.toClassOrNull(appClassLoader) ?: return
        cls.declaredMethods.filter { it.returnType == String::class.java }.forEach { m ->
            m.hookAfter { param ->
                val result = param.result as? String ?: return@hookAfter
                if (!result.contains(UNKNOWN_RECORD)) return@hookAfter
                val pkg = readStringField(param.thisObject) ?: return@hookAfter
                val name = voipApps.firstOrNull { it.packageName == pkg }?.displayName
                    ?: return@hookAfter
                param.result = result.replace(UNKNOWN_RECORD, name)
            }
        }
    }

    private fun hookPathBRecordWrapperList() {
        val source = appInfo.sourceDir ?: return
        DexkitUtils.create(source) { bridge ->
            val wrapperData = bridge.findClass {
                matcher { usingStrings("OplusRecordWrapper") }
            }.checkDataList("HookAudioMonitor.OplusRecordWrapper", onlyOne = false)
                .firstOrNull()
            val wrapperCls = wrapperData?.let { it.name.toClassOrNull(appClassLoader) }
                ?: "com.oplus.audiomonitor.voiprecord.OplusRecordWrapper".toClassOrNull(appClassLoader)
                ?: return@create

            val appNameField = wrapperCls.declaredFields.firstOrNull {
                it.type == String::class.java && !java.lang.reflect.Modifier.isStatic(it.modifiers)
            }?.also { it.isAccessible = true }
            val statusField = wrapperCls.declaredFields.firstOrNull {
                (it.type == java.lang.Boolean.TYPE || it.type == Boolean::class.java) &&
                    !java.lang.reflect.Modifier.isStatic(it.modifiers)
            }?.also { it.isAccessible = true }

            val listMethods = bridge.findMethod {
                matcher {
                    paramTypes("java.util.List")
                    returnType = "java.util.List"
                    usingStrings("enable_record_app", "com.tencent.mobileqq", "com.tencent.mm")
                }
            }.checkDataList("HookAudioMonitor.SwitchAppList", onlyOne = false)

            val hookTargets = if (listMethods.isNotEmpty()) {
                listMethods.mapNotNull { data ->
                    data.className.toClassOrNull(appClassLoader)?.findMethod(data.methodName, 1)
                }
            } else {
                listOfNotNull(
                    CLS_APP.toClassOrNull(appClassLoader),
                    CLS_VOIP.toClassOrNull(appClassLoader)
                ).flatMap { c ->
                    c.declaredMethods.filter {
                        it.parameterCount == 1 &&
                            java.util.List::class.java.isAssignableFrom(it.parameterTypes[0]) &&
                            java.util.List::class.java.isAssignableFrom(it.returnType)
                    }
                }
            }

            hookTargets.forEach { method ->
                method.hookBefore { param ->
                    val ctx = appContext ?: return@hookBefore
                    val sp = ctx.getSharedPreferences(
                        ctx.packageName + SP_SUFFIX,
                        Context.MODE_PRIVATE,
                    )
                    val enabled = sp.getString(SP_KEY_ENABLE, "")
                        ?.split("#")
                        ?.filter { it.isNotEmpty() }
                        ?: emptyList()
                    val out = ArrayList<Any>()
                    val enabledJoined = StringBuilder()
                    for ((index, app) in voipApps.withIndex()) {
                        val on = enabled.contains(app.packageName)
                        val wrapper = createRecordWrapper(wrapperCls, ctx, app.packageName, on)
                            ?: continue
                        appNameField?.set(wrapper, app.displayName)
                        statusField?.set(wrapper, true)
                        out.add(wrapper)
                        if (on) {
                            if (enabledJoined.isNotEmpty()) enabledJoined.append("#")
                            enabledJoined.append(app.packageName)
                        } else if (index == 0 && enabled.isEmpty()) {
                        }
                    }
                    if (enabledJoined.isNotEmpty()) {
                        sp.edit().putString(SP_KEY_ENABLE, enabledJoined.toString()).apply()
                    }
                    if (out.isNotEmpty()) param.result = out
                }
            }
        }
    }

    private fun createRecordWrapper(
        wrapperCls: Class<*>,
        ctx: Context,
        packageName: String,
        enabled: Boolean,
    ): Any? {
        val ctors = wrapperCls.declaredConstructors
        for (ctor in ctors) {
            ctor.isAccessible = true
            val types = ctor.parameterTypes
            val args: Array<Any?> = when {
                types.size == 3 &&
                    Context::class.java.isAssignableFrom(types[0]) &&
                    types[1] == String::class.java &&
                    (types[2] == java.lang.Boolean.TYPE || types[2] == Boolean::class.java) ->
                    arrayOf(ctx, packageName, enabled)

                types.size == 2 &&
                    types[0] == String::class.java &&
                    (types[1] == java.lang.Boolean.TYPE || types[1] == Boolean::class.java) ->
                    arrayOf(packageName, enabled)

                types.size == 1 && types[0] == String::class.java ->
                    arrayOf(packageName)

                types.isEmpty() -> emptyArray()
                else -> continue
            }
            val inst = runCatching { ctor.newInstance(*args) }.getOrNull() ?: continue
            return inst
        }
        return null
    }

    private fun readStringField(inst: Any?): String? {
        if (inst == null) return null
        for (f in inst.javaClass.declaredFields) {
            if (f.type != String::class.java) continue
            f.isAccessible = true
            val v = runCatching { f.get(inst) as? String }.getOrNull() ?: continue
            if (v.contains('.') && v.split('.').size >= 2) return v
        }
        return null
    }

    @Suppress("UNCHECKED_CAST")
    private fun injectVoipArrayLists(inst: Any) {
        val fields = inst.javaClass.declaredFields
        for (f in fields) {
            if (!java.util.ArrayList::class.java.isAssignableFrom(f.type)) continue
            f.isAccessible = true
            val list = runCatching { f.get(inst) as? java.util.ArrayList<Any?> }.getOrNull()
                ?: continue
            val asStrings = list.mapNotNull { it as? String }
            if (asStrings.contains(PKG_MOBILEQQ) && asStrings.contains(PKG_MM)) {
                list.clear()
                list.addAll(voipWhitelistPkgs)
            }
            if (list.any { it == ACT_AV }) {
                if (!list.contains(ACT_DOUYIN)) list.add(ACT_DOUYIN)
            }
        }
    }
}

object HookAudioEffectCenter : YukiBaseHooker() {
    private const val CLASS_MGR =
        "com.oplus.audio.effectcenter.manager.SpatializerManager"
    private const val CLASS_DEF =
        "com.oplus.audio.effectcenter.manager.SpatializerDefine"

    override fun onHook() {
        val enable = prefs(ModulePrefs)
            .getBoolean("enable_record_calls_on_third_party_apps", false)
        val os = try {
            OplusBuildUtlils(appClassLoader).getOSVersionCode ?: 0
        } catch (_: Throwable) {
            0
        }
        if (!enable || os != 30) return

        CLASS_MGR.toClassOrNull(appClassLoader)?.let { mgr ->
            val setSpk = mgr.findMethod("setSpkVolParam")
            setSpk?.hookBefore { param ->
                val inst = param.thisObject ?: return@hookBefore
                val arg0 = param.args.getOrNull(0) as? Int ?: return@hookBefore
                val mode = runCatching {
                    inst.current().field { name = "mSpatializerMode" }.any()
                        as? java.util.concurrent.atomic.AtomicBoolean
                }.getOrNull() ?: return@hookBefore
                val spkVol = runCatching {
                    inst.current().field { name = "mSpatializerSpkVol" }.any()
                        as? java.util.concurrent.atomic.AtomicInteger
                }.getOrNull() ?: return@hookBefore
                val spatDev = runCatching {
                    inst.current().field { name = "mSpatDeviceManager" }.any()
                }.getOrNull() ?: return@hookBefore

                val device = runCatching {
                    spatDev.current().method {
                        name = "getDeviceForMusicStream"
                        emptyParam()
                    }.invoke<Int>()
                }.getOrNull() ?: return@hookBefore

                if (arg0 == spkVol.get()) return@hookBefore
                if (device != 2 && mode.get()) return@hookBefore

                val defCls = CLASS_DEF.toClassOrNull(appClassLoader) ?: return@hookBefore
                val paramIdx = defCls.findField("PARAM_SET_SPAT_VOLUME_INDEX")?.let { f ->
                    f.isAccessible = true
                    f.get(null) as? Int
                } ?: return@hookBefore

                runCatching {
                    inst.current().method {
                        name = "setParameterImp"
                        paramCount = 3
                    }.call(paramIdx, arg0, spkVol.get())
                }
                param.result = null
            }
        }
    }
}

object HookIncallUI : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.RM0_Q
            )
        )
    }
}

object HookPhone : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(
            com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker(
                mode = com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode.BOTH
            )
        )
    }
}
