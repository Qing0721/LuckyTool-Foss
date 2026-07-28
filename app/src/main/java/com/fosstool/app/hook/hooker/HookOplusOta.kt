package com.fosstool.app.hook.hooker

import android.app.Activity
import android.content.Intent
import android.view.Menu
import android.widget.Toast
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.IntentClass
import com.highcapable.yukihookapi.hook.type.java.ArrayListClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.hook.utils.SystemPropertiesUtils
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.useFirst
import com.fosstool.app.utils.ModulePrefs
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Method

object HookOplusOta : YukiBaseHooker() {

    private const val REQUEST_CODE = 10000

    override fun onHook() {

        loadHooker(SystemPropertiesOverrideEngineHooker(mode = Mode.RM0_T))

        if (prefs(ModulePrefs).getBoolean("remove_ota_notify_install_success", false)) {
            DexkitUtils.create(appInfo.sourceDir) { bridge ->

                val classes = bridge.findClass {
                    matcher {
                        usingStrings(
                            "NotificationHelper",
                            "ota_notify_new_channel_id",
                            "ota_notify_new_channel_default_id"
                        )
                    }
                }.checkDataList("HookOplusOta.NotificationHelper", onlyOne = false)
                if (classes.isNotEmpty()) {

                    bridge.findMethod {
                        searchInClass(classes)
                        matcher {
                            paramCount = 0
                            usingStrings("notifyInstallSuccess")
                        }
                    }.useFirst("HookOplusOta.notifyInstallSuccess", onlyOne = false) { md ->
                        md.className.toClassOrNull(appClassLoader)
                            ?.method { name = md.methodName; emptyParam() }
                            ?.ignored()
                            ?.hook { intercept() }
                    }
                }
            }
        }

        if (prefs(ModulePrefs).getBoolean("remove_ota_auto_download_dialog", false)) {
            DexkitUtils.create(appInfo.sourceDir) { bridge ->

                val classes = bridge.findClass {
                    matcher {
                        addMethod { paramTypes("android.view.Window") }
                        addMethod {
                            paramTypes(
                                ContextClass.name,
                                "android.content.DialogInterface\$OnClickListener"
                            )
                        }
                        usingStrings("OTADialogHelper", "auto_download_network_type")
                    }
                }.checkDataList("HookOplusOta.OTADialogHelper", onlyOne = false)
                if (classes.isNotEmpty()) {

                    bridge.findMethod {
                        searchInClass(classes)
                        matcher {
                            returnType = UnitType.name
                            usingStrings("auto_download_network_type")
                        }
                    }.useFirst("HookOplusOta.AutoDownloadDialog", onlyOne = false) { md ->
                        md.className.toClassOrNull(appClassLoader)
                            ?.method { name = md.methodName; returnType = UnitType }
                            ?.ignored()
                            ?.hook { intercept() }
                    }
                }
            }
        }

        if (prefs(ModulePrefs).getBoolean("remove_ota_local_update_verity", false)) {
            removeOtaLocalUpdateVerity()
        }

        if (!prefs(ModulePrefs).getBoolean("enable_opex_local_install", false)) return
        if (SystemPropertiesUtils(null).getBoolean("oplus.opex.merge", false) != true) return
        if (com.fosstool.app.utils.getOSVersionCode < 30) return

        var opexClassName: String? = null
        DexkitUtils.create(appInfo.sourceDir) { bridge ->
            bridge.findClass {
                matcher {
                    usingStrings("OpexPackageHelper")
                    methods {
                        add {
                            paramTypes(
                                ContextClass.name,
                                "com.oplus.ota.db.PackageListInfo",
                                IntType.name
                            )
                            returnType = "com.oplus.ota.opex.OpexPackageHelper\$OpexCopyResultCode"
                        }
                    }
                }
            }.apply {
                checkDataList("HookOplusOta")
                if (isNotEmpty()) opexClassName = first().name
            }
        }
        if (opexClassName == null) return

        "com.oplus.otaui.activity.EntryActivity".toClassOrNull(appClassLoader)?.apply {
            val menuHooked = runCatching {
                val m = findMethod("onCreateOptionsMenu", 1) ?: return@runCatching
                XposedBridge.hookMethod(m, object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val activity = param.thisObject as? Activity ?: return
                        val menu = param.args.getOrNull(0) as? Menu ?: return
                        menu.add(0, REQUEST_CODE, 0, "Opex")
                        menu.findItem(REQUEST_CODE)?.setOnMenuItemClickListener {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                            }
                            activity.startActivityForResult(intent, REQUEST_CODE)
                            true
                        }
                    }
                })
            }.isSuccess
            if (!menuHooked) {
                method {
                    name = "onCreateOptionsMenu"
                    param(Menu::class.java)
                    returnType = BooleanType
                }.hook {
                    after {
                        val activity = instance<Activity>()
                        val menu = args().first().cast<Menu>() ?: return@after
                        menu.add(0, REQUEST_CODE, 0, "Opex")
                        menu.findItem(REQUEST_CODE)?.setOnMenuItemClickListener {
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                            }
                            activity.startActivityForResult(intent, REQUEST_CODE)
                            true
                        }
                    }
                }
            }
            val resultHooked = runCatching {
                val m = findMethod("onActivityResult", 3) ?: return@runCatching
                XposedBridge.hookMethod(m, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val requestCode = param.args.getOrNull(0) as? Int ?: return
                        val resultCode = param.args.getOrNull(1) as? Int ?: return
                        val data = param.args.getOrNull(2) as? Intent
                        if (requestCode != REQUEST_CODE || resultCode != Activity.RESULT_OK || data == null) return
                        val activity = param.thisObject as? Activity ?: return
                        if (installOpexFromUri(activity, data, opexClassName)) {
                            param.result = null
                        }
                    }
                })
            }.isSuccess
            if (!resultHooked) {
                method {
                    name = "onActivityResult"
                    param(IntType, IntType, IntentClass)
                }.hook {
                    before {
                        val requestCode = args().first().int()
                        val resultCode = args(1).int()
                        val data = args(2).cast<Intent>()
                        if (requestCode != REQUEST_CODE || resultCode != Activity.RESULT_OK || data == null) return@before
                        val activity = instance<Activity>()
                        if (installOpexFromUri(activity, data, opexClassName)) {
                            result = null
                        }
                    }
                }
            }
        }
    }

    private fun installOpexFromUri(activity: Activity, data: Intent, opexClassName: String?): Boolean {
        try {
            val sp = activity.getSharedPreferences("state_info", 0)
            val otaVersion = SystemPropertiesUtils(null).get("ro.build.version.ota", "") ?: ""
            sp.edit().putString("realOtaVersion", otaVersion).commit()
        } catch (e: Throwable) {
            YLog.error("HookOplusOta: prefs state_info error: ${e.message}", tag = "LuckyTool")
        }

        val uri = data.data ?: return false
        try {
            activity.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: Throwable) {
            YLog.error("HookOplusOta: takePersistableUriPermission error: ${e.message}", tag = "LuckyTool")
        }

        val path = uri.path ?: return false
        val fileName = path.substringAfterLast("/")
        if (!fileName.contains("ovl_update")) {
            Toast.makeText(activity, "not ovl_update", Toast.LENGTH_SHORT).show()
            return false
        }

        val cacheDir = File(activity.cacheDir, "opexs_cache")
        if (cacheDir.exists()) cacheDir.deleteRecursively()
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val cacheFile = File(cacheDir, fileName)
        if (!cacheFile.exists()) cacheFile.createNewFile()
        try {
            val input = activity.contentResolver.openInputStream(uri)
            if (input != null) {
                copyStream(input, FileOutputStream(cacheFile))
            }
        } catch (e: Throwable) {
            YLog.error("HookOplusOta: copy file error: ${e.message}", tag = "LuckyTool")
            return false
        }

        val className = opexClassName ?: return false
        return try {
            val opexClass = className.toClassOrNull(appClassLoader) ?: return false
            val pkgListInfoCls = "com.oplus.ota.db.PackageListInfo".toClassOrNull(appClassLoader) ?: return false

            val packageListInfo = opexClass.method {
                param(StringClass)
                returnType = pkgListInfoCls
            }.get().invoke<Any>(cacheDir.path)

            if (packageListInfo != null) {
                val files = cacheDir.listFiles { file ->
                    file.name.contains("ovl_update")
                }?.toList() ?: emptyList()
                files.forEachIndexed { index, file ->
                    val name = file.name.substringBefore(".")
                    val result = opexClass.method {
                        param(ContextClass, pkgListInfoCls, IntType)
                        returnType = "com.oplus.ota.opex.OpexPackageHelper\$OpexCopyResultCode".toClassOrNull(appClassLoader)
                    }.get().invoke<Any>(activity, packageListInfo, index)
                    val msg = "$name -> $result"
                    YLog.debug("HookOplusOta: $msg", tag = "LuckyTool")
                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                }
                cacheDir.deleteRecursively()
                true
            } else false
        } catch (e: Throwable) {
            YLog.error("HookOplusOta: Opex install error: ${e.message}", tag = "LuckyTool")
            false
        }
    }

    private fun copyStream(input: InputStream, output: FileOutputStream) {
        val buffer = ByteArray(8192)
        var len: Int
        while (true) {
            len = input.read(buffer)
            if (len <= 0) break
            output.write(buffer, 0, len)
        }
        input.close()
        output.close()
    }

    private fun removeOtaLocalUpdateVerity() {
        DexkitUtils.create(appInfo.sourceDir) { bridge ->

            runCatching {
                bridge.findClass {
                    matcher {
                        methods {
                            add { paramCount(0); returnType(BooleanType.name) }
                            add { paramTypes(File::class.java.name, StringClass.name) }
                            add { paramTypes(ListClass.name) }
                        }
                        usingStrings("ABUpdateUtils")
                    }
                }.useFirst("HookOplusOta.ABUpdateUtils", onlyOne = false) { cd ->
                    cd.name.toClassOrNull(appClassLoader)?.apply {
                        method {
                            param(File::class.java, StringClass)
                        }.hookAll {
                            after {
                                val file = args().first().cast<File>() ?: return@after
                                @Suppress("UNCHECKED_CAST")
                                val list = result as? ArrayList<String> ?: return@after
                                if (!file.exists()) return@after
                                rewriteLocalUpdateVerityList(list, file.name.contains("downgrade"))
                            }
                        }
                    }
                }
            }

            runCatching {
                bridge.findClass {
                    matcher {
                        methods {
                            add { returnType(BooleanType.name) }
                            add { paramTypes(StringClass.name); returnType(StringClass.name) }
                            add { paramCount(4); returnType(IntType.name) }
                        }
                        usingStrings("LocalPcakgeInfoUtil")
                    }
                }.useFirst("HookOplusOta.LocalPcakgeInfoUtil", onlyOne = false) { cd ->
                    cd.name.toClassOrNull(appClassLoader)?.apply {
                        method {
                            paramCount = 2
                            param { it.contains(ContextClass) && it.contains(StringClass) }
                            returnType { it == ListClass || it == ArrayListClass }
                        }.hookAll {
                            after {
                                val stringArg = args.firstOrNull { it is String } as? String ?: ""
                                @Suppress("UNCHECKED_CAST")
                                val list = result as? ArrayList<String> ?: return@after
                                rewriteLocalUpdateVerityList(list, stringArg.contains("downgrade"))
                            }
                        }
                    }
                }
            }

            runCatching {
                val classes = bridge.findClass {
                    matcher {
                        fields {
                            addForType("android.os.UpdateEngine")
                            addForType("android.os.PowerManager\$WakeLock")
                        }
                        methods {
                            add { paramCount(0); returnType(IntType.name) }
                            add { paramCount(0); returnType(UnitType.name) }
                            add {
                                paramCount(4..5)
                                returnType(UnitType.name)
                                usingStrings("SWITCH_SLOT_ON_REBOOT")
                            }
                        }
                        usingStrings("ABUpdateManager", "payload_properties")
                    }
                }.checkDataList("HookOplusOta.ABUpdateManager", onlyOne = false)
                if (classes.isNotEmpty()) {
                    bridge.findMethod {
                        searchInClass(classes)
                        matcher {
                            paramCount(4..5)
                            returnType = UnitType.name
                            usingStrings("SWITCH_SLOT_ON_REBOOT")
                        }
                    }.useFirst("HookOplusOta.SWITCH_SLOT_ON_REBOOT", onlyOne = false) { md ->
                        hookAbUpdateManagerSwitchSlot(md.className, md.methodName, md.paramCount)
                    }
                }
            }
        }
    }

    private fun rewriteLocalUpdateVerityList(list: ArrayList<String>, isDowngrade: Boolean) {
        if (isDowngrade) {
            list.removeAll { it.contains("forbid_ota_local_update") }
            list.removeAll { it.contains("ota_root_or_debug") }
        } else {
            val idxForbid = list.indexOfFirst { it.contains("forbid_ota_local_update") }
            if (idxForbid >= 0) list[idxForbid] = "forbid_ota_local_update=false"
            val idxRoot = list.indexOfFirst { it.contains("ota_root_or_debug") }
            if (idxRoot >= 0) list[idxRoot] = "ota_root_or_debug=false"
            list.removeAll { it.contains("from_version") }
        }
        SystemPropertiesUtils(null).set("sys.ota.grant_ota_local_update", "true")
    }

    private fun hookAbUpdateManagerSwitchSlot(className: String, methodName: String, paramSize: Int) {
        runCatching {
            className.toClassOrNull(appClassLoader)?.apply {
                method {
                    name = methodName
                    paramCount = paramSize
                    returnType = UnitType
                }.ignored().hook {
                    before {
                        rewritePayloadPropertiesArgs()
                    }
                }
            }
        }
    }

    private fun com.highcapable.yukihookapi.hook.param.HookParam.rewritePayloadPropertiesArgs() {
        val last = try { args().last().any() } catch (_: Throwable) { null } ?: return
        when (last) {
            is Array<*> -> {
                @Suppress("UNCHECKED_CAST")
                val arr = last as? Array<String> ?: return
                val list = arr.toMutableList()
                list.removeAll { it.contains("forbid_ota_local_update") }
                list.removeAll { it.contains("ota_root_or_debug") }
                args().last().set(list.toTypedArray())
            }
            is ArrayList<*> -> {
                @Suppress("UNCHECKED_CAST")
                val list = last as ArrayList<String>
                list.removeAll { it.contains("forbid_ota_local_update") }
                list.removeAll { it.contains("ota_root_or_debug") }
            }
            else -> return
        }
        SystemPropertiesUtils(null).set("sys.ota.grant_ota_local_update", "true")
    }

    private fun Class<*>.findMethod(name: String, paramCount: Int): Method? {
        var c: Class<*>? = this
        while (c != null && c != Any::class.java) {
            c.declaredMethods.firstOrNull { it.name == name && it.parameterCount == paramCount }
                ?.let { return it.apply { isAccessible = true } }
            c = c.superclass
        }
        return null
    }
}