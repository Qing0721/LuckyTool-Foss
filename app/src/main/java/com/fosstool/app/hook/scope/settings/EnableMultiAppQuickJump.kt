package com.fosstool.app.hook.scope.settings

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import com.fosstool.app.hook.utils.OplusBuildUtlils
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object EnableMultiAppQuickJump : YukiBaseHooker() {
    private const val MENU_ITEM_QUICK_JUMP = 999
    private const val MENU_ITEM_OPEN_MARKET = 900
    private const val MULTIAPP_PKG = "com.oplus.multiapp"
    private const val MULTIAPP_ACTIVITY = "com.oplus.multiapp.ui.settings.ActivitySettingsActivity"

    override fun onHook() {
        val osVersionCode = try {
            OplusBuildUtlils().getOSVersionCode ?: 0
        } catch (_: Throwable) {
            0
        }
        val clazz = "com.android.settings.applications.appinfo.AppInfoDashboardFragment"
            .toClassOrNull(appClassLoader) ?: return

        clazz.method { name = "onCreateOptionsMenu" }.ignored().hook {
            after {
                val menu = args.getOrNull(0) as? Menu ?: return@after
                val packageInfo = findPackageInfo(instance) ?: return@after
                val appInfo = packageInfo.applicationInfo
                val isSystemApp = appInfo != null &&
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val context = getFragmentContext(instance) ?: return@after
                if (prefs(ModulePrefs).getBoolean("enable_quick_open_market_page", false)) {
                    menu.add(0, MENU_ITEM_OPEN_MARKET, 0, "Open Market")
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                }
                if (!isSystemApp && osVersionCode >= 27 && isMultiAppQuickJumpEnabled()) {
                    val title = getMultiAppLabel(context) ?: MULTIAPP_PKG
                    menu.add(0, MENU_ITEM_QUICK_JUMP, 0, title)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                }
            }
        }

        clazz.method { name = "onOptionsItemSelected" }.ignored().hook {
            before {
                val item = args.getOrNull(0) as? MenuItem ?: return@before
                val packageInfo = findPackageInfo(instance) ?: return@before
                val appInfo = packageInfo.applicationInfo
                val isSystemApp = appInfo != null &&
                    (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                val context = getFragmentContext(instance) ?: return@before
                val pkgName = packageInfo.packageName
                when (item.itemId) {
                    MENU_ITEM_OPEN_MARKET -> {
                        if (prefs(ModulePrefs).getBoolean("enable_quick_open_market_page", false)) {
                            runCatching {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=$pkgName")
                                ).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                                }
                                context.startActivity(intent)
                            }
                        }
                    }
                    MENU_ITEM_QUICK_JUMP -> {
                        if (isSystemApp) return@before
                        if (!isMultiAppQuickJumpEnabled()) return@before
                        val label = getAppLabel(context, pkgName) ?: pkgName
                        runCatching {
                            val intent = Intent().apply {
                                setClassName(MULTIAPP_PKG, MULTIAPP_ACTIVITY)
                                setPackage(MULTIAPP_PKG)
                                putExtra("title", label)
                                putExtra("pkgName", pkgName)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }
    }

    private fun findPackageInfo(host: Any): PackageInfo? = runCatching {
        var c: Class<*>? = host.javaClass
        while (c != null) {
            val f = c.declaredFields.firstOrNull { PackageInfo::class.java.isAssignableFrom(it.type) }
            if (f != null) {
                f.isAccessible = true
                return@runCatching f.get(host) as? PackageInfo
            }
            c = c.superclass
        }
        null
    }.getOrNull()

    private fun getFragmentContext(host: Any): android.content.Context? = runCatching {
        var c: Class<*>? = host.javaClass
        while (c != null) {
            val m = c.declaredMethods.firstOrNull {
                it.name == "getContext" && it.parameterCount == 0
            }
            if (m != null) {
                m.isAccessible = true
                return@runCatching m.invoke(host) as? android.content.Context
            }
            c = c.superclass
        }
        null
    }.getOrNull()

    private fun isMultiAppQuickJumpEnabled(): Boolean {
        val p = prefs(ModulePrefs)
        return p.getBoolean("enable_multi_app_quick_jump", false) ||
            p.getBoolean("enable_app_clone_quick_jump", false)
    }

    private fun getMultiAppLabel(context: android.content.Context): CharSequence? {
        return try {
            val pm = context.packageManager
            val appInfo = if (android.os.Build.VERSION.SDK_INT < 33) {
                pm.getApplicationInfo(MULTIAPP_PKG, 0)
            } else {
                pm.getApplicationInfo(
                    MULTIAPP_PKG, android.content.pm.PackageManager.ApplicationInfoFlags.of(0L)
                )
            }
            pm.getApplicationLabel(appInfo)
        } catch (_: Throwable) {
            null
        }
    }

    private fun getAppLabel(context: android.content.Context, pkgName: String): CharSequence? {
        return try {
            val pm = context.packageManager
            val appInfo = if (android.os.Build.VERSION.SDK_INT < 33) {
                pm.getApplicationInfo(pkgName, 0)
            } else {
                pm.getApplicationInfo(
                    pkgName, android.content.pm.PackageManager.ApplicationInfoFlags.of(0L)
                )
            }
            pm.getApplicationLabel(appInfo)
        } catch (_: Throwable) {
            null
        }
    }
}
