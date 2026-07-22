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
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.hook.utils.SystemPropertiesUtils
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object HookOplusOta : YukiBaseHooker() {

    private const val REQUEST_CODE = 10000

    override fun onHook() {


        loadHooker(SystemPropertiesOverrideEngineHooker(mode = Mode.RM0_T))

        if (prefs(ModulePrefs).getBoolean("remove_ota_notify_install_success", false)) {
            DexkitUtils.create(appInfo.sourceDir) { bridge ->
                val clsName = bridge.findMethod {
                    matcher { usingStrings("NotificationHelper") }
                }.checkDataList("HookOplusOta.hn0(0) class").firstOrNull()?.className
                val target = bridge.findMethod {
                    matcher { usingStrings("notifyInstallSuccess") }
                }.checkDataList("HookOplusOta.hn0(0) method").firstOrNull()
                if (clsName != null && target != null) {
                    clsName.toClass().method { name = target.methodName }.hook {
                        before { result = null }
                    }
                }
            }
        }

        if (prefs(ModulePrefs).getBoolean("remove_ota_auto_download_dialog", false)) {
            DexkitUtils.create(appInfo.sourceDir) { bridge ->
                val clsName = bridge.findMethod {
                    matcher { usingStrings("OTADialogHelper") }
                }.checkDataList("HookOplusOta.hn0(1) class").firstOrNull()?.className
                val target = bridge.findMethod {
                    matcher { usingStrings("auto_download_network_type") }
                }.checkDataList("HookOplusOta.hn0(1) method").firstOrNull()
                if (clsName != null && target != null) {
                    clsName.toClass().method { name = target.methodName }.hook {
                        before { result = null }
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

        "com.oplus.otaui.activity.EntryActivity".toClass().apply {
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

                    try {
                        val sp = activity.getSharedPreferences("state_info", 0)
                        val otaVersion = SystemPropertiesUtils(null).get("ro.build.version.ota", "") ?: ""
                        sp.edit().putString("realOtaVersion", otaVersion).commit()
                    } catch (e: Throwable) {
                        YLog.error("HookOplusOta: prefs state_info error: ${e.message}", tag = "LuckyTool")
                    }

                    val uri = data.data ?: return@before
                    try {
                        activity.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    } catch (e: Throwable) {
                        YLog.error("HookOplusOta: takePersistableUriPermission error: ${e.message}", tag = "LuckyTool")
                    }

                    val path = uri.path ?: return@before
                    val fileName = path.substringAfterLast("/")
                    if (!fileName.contains("ovl_update")) {
                        Toast.makeText(activity, "not ovl_update", Toast.LENGTH_SHORT).show()
                        return@before
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
                        return@before
                    }

                    val className = opexClassName ?: return@before
                    try {
                        val opexClass = className.toClass()
                        val pkgListInfoCls = "com.oplus.ota.db.PackageListInfo".toClass()

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
                                    returnType = "com.oplus.ota.opex.OpexPackageHelper\$OpexCopyResultCode".toClass()
                                }.get().invoke<Any>(activity, packageListInfo, index)
                                val msg = "$name -> $result"
                                YLog.debug("HookOplusOta: $msg", tag = "LuckyTool")
                                Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show()
                            }
                            cacheDir.deleteRecursively()
                            result = null
                        }
                    } catch (e: Throwable) {
                        YLog.error("HookOplusOta: Opex install error: ${e.message}", tag = "LuckyTool")
                    }
                }
            }
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
                val cls = bridge.findClass {
                    matcher { usingStrings("ABUpdateUtils") }
                }
                if (cls.isNotEmpty()) {
                    cls.first().name.toClass().apply {
                        method {
                            param(java.io.File::class.java, StringClass)
                            returnType = UnitType
                        }.hookAll {
                            after {
                                val file = args().first().cast<java.io.File>() ?: return@after
                                val arg1 = args(1).any()
                                if (arg1 !is ArrayList<*>) return@after
                                @Suppress("UNCHECKED_CAST")
                                val list = arg1 as ArrayList<String>
                                if (file.exists() && file.name.contains("downgrade")) {
                                    list.removeAll { it.contains("forbid_ota_local_update") }
                                }
                                val idxForbid = list.indexOfFirst { it.contains("forbid_ota_local_update") }
                                if (idxForbid >= 0) list[idxForbid] = "forbid_ota_local_update=false"
                                val idxRoot = list.indexOfFirst { it.contains("ota_root_or_debug") }
                                if (idxRoot >= 0) list[idxRoot] = "ota_root_or_debug=false"
                                list.removeAll { it.contains("ota_root_or_debug") }
                                SystemPropertiesUtils(null).set("sys.ota.grant_ota_local_update", "true")
                            }
                        }
                    }
                }
            }

            runCatching {
                val cls = bridge.findClass {
                    matcher { usingStrings("LocalPcakgeInfoUtil") }
                }
                if (cls.isNotEmpty()) {
                    cls.first().name.toClass().apply {
                        method {
                            param(IntType)
                            returnType = UnitType
                        }.hookAll {
                            before {
                                val instance = instance<Any>() ?: return@before
                                if (instance !is ArrayList<*>) return@before
                                @Suppress("UNCHECKED_CAST")
                                val list = instance as ArrayList<String>
                                var stringArg = ""
                                for (i in 0..5) {
                                    val a = try { args(i).any() } catch (_: Throwable) { null }
                                    if (a is String) { stringArg = a; break }
                                }
                                if (stringArg.contains("downgrade")) {
                                    list.removeAll { it.contains("forbid_ota_local_update") }
                                } else {
                                    val idxForbid = list.indexOfFirst { it.contains("forbid_ota_local_update") }
                                    if (idxForbid >= 0) list[idxForbid] = "forbid_ota_local_update=false"
                                }
                                val idxRoot = list.indexOfFirst { it.contains("ota_root_or_debug") }
                                if (idxRoot >= 0) list[idxRoot] = "ota_root_or_debug=false"
                                list.removeAll { it.contains("ota_root_or_debug") }
                                SystemPropertiesUtils(null).set("sys.ota.grant_ota_local_update", "true")
                            }
                        }
                    }
                }
            }

            runCatching {
                val cls = bridge.findClass {
                    matcher {
                        usingStrings("ABUpdateManager", "payload_properties")
                        fields { addForType("android.os.PowerManager\$WakeLock") }
                    }
                }
                if (cls.isEmpty()) {
                    bridge.findClass {
                        matcher { usingStrings("ABUpdateManager", "payload_properties") }
                    }.also { fallback ->
                        if (fallback.isNotEmpty()) hookAbUpdateManagerSwitchSlot(fallback.first().name)
                    }
                } else {
                    hookAbUpdateManagerSwitchSlot(cls.first().name)
                }
            }
        }
    }

    private fun hookAbUpdateManagerSwitchSlot(className: String) {
        runCatching {
            className.toClass().apply {
                method {
                    paramCount(4..5)
                    returnType = UnitType
                }.hookAll {
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
}
