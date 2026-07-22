package com.fosstool.app.hook.scope.settings

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.net.Uri
import android.view.Menu
import android.view.MenuItem
import com.fosstool.app.hook.utils.OplusBuildUtlils
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.android.PackageInfoClass

object EnableMultiAppQuickJump : YukiBaseHooker() {
    private const val MENU_ITEM_QUICK_JUMP = 999
    private const val MENU_ITEM_OPEN_MARKET = 900
    private const val MULTIAPP_PKG = "com.oplus.multiapp"
    private const val MULTIAPP_ACTIVITY = "com.oplus.multiapp.ui.settings.ActivitySettingsActivity"

    override fun onHook() {
        val osVersionCode = try { OplusBuildUtlils().getOSVersionCode ?: 0 } catch (_: Throwable) { 0 }
        val className = "com.android.settings.applications.appinfo.AppInfoDashboardFragment"
        try {
            className.toClass().apply {
                method { name = "onCreateOptionsMenu" }.hook {
                    before {
                        val menu = args().first().cast<Menu>() ?: return@before
                        val packageInfo = instance.current().field {
                            type = PackageInfoClass
                        }.cast<PackageInfo>() ?: return@before
                        val appInfo = packageInfo.applicationInfo
                        val isSystemApp = appInfo != null &&
                            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        val context = instance.current().method {
                            name = "getContext"; superClass()
                        }.invoke<android.content.Context>() ?: return@before
                        if (prefs(com.fosstool.app.utils.ModulePrefs)
                                .getBoolean("enable_quick_open_market_page", false)) {
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
                method { name = "onOptionsItemSelected" }.hook {
                    after {
                        val item = args().first().cast<MenuItem>() ?: return@after
                        val packageInfo = instance.current().field {
                            type = PackageInfoClass
                        }.cast<PackageInfo>() ?: return@after
                        val appInfo = packageInfo.applicationInfo
                        val isSystemApp = appInfo != null &&
                            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                        val context = instance.current().method {
                            name = "getContext"; superClass()
                        }.invoke<android.content.Context>() ?: return@after
                        val pkgName = packageInfo.packageName
                        when (item.itemId) {
                            MENU_ITEM_OPEN_MARKET -> {
                                if (prefs(com.fosstool.app.utils.ModulePrefs)
                                        .getBoolean("enable_quick_open_market_page", false)) {
                                    try {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("market://details?id=$pkgName")
                                        ).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Throwable) {
                                        YLog.error(
                                            "EnableMultiAppQuickJump: open market failed for $pkgName",
                                            tag = "LuckyTool"
                                        )
                                    }
                                }
                            }
                            MENU_ITEM_QUICK_JUMP -> {
                                if (isSystemApp) return@after
                                if (!isMultiAppQuickJumpEnabled()) return@after
                                val label = getAppLabel(context, pkgName) ?: pkgName
                                try {
                                    val intent = Intent().apply {
                                        setClassName(MULTIAPP_PKG, MULTIAPP_ACTIVITY)
                                        setPackage(MULTIAPP_PKG)
                                        putExtra("title", label)
                                        putExtra("pkgName", pkgName)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Throwable) {
                                    YLog.error(
                                        "EnableMultiAppQuickJump: launch failed for $pkgName",
                                        tag = "LuckyTool"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            YLog.error(
                "EnableMultiAppQuickJump: $className not found",
                tag = "LuckyTool"
            )
        }
    }

    private fun isMultiAppQuickJumpEnabled(): Boolean {
        val p = prefs(com.fosstool.app.utils.ModulePrefs)
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
        } catch (_: Throwable) { null }
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
        } catch (_: Throwable) { null }
    }
}
