package com.fosstool.app.hook.scope.settings

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.PackageInfoClass
import com.fosstool.app.R
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.formatDate
import com.fosstool.app.utils.getAppVersion
import com.fosstool.app.utils.openMarketIntent

object HookAppDetails : YukiBaseHooker() {
    @SuppressLint("DiscouragedApi", "SetTextI18n")
    override fun onHook() {
        val isPackName = prefs(ModulePrefs).getBoolean("show_package_name_in_app_details", false)
        val isLastUpdateTime =
            prefs(ModulePrefs).getBoolean("show_last_update_time_in_app_details", false)
        val isEnableCopy =
            prefs(ModulePrefs).getBoolean("enable_long_press_to_copy_in_app_details", false)
        val isIconMarket = prefs(ModulePrefs).getBoolean("click_icon_open_market_page", false)
        val isSdkVersion =
            prefs(ModulePrefs).getBoolean("show_sdk_version_in_app_details", false) ||
                prefs(ModulePrefs).getBoolean("show_sdk_in_app_details", false)
        val isFirstInstall =
            prefs(ModulePrefs).getBoolean("show_first_install_time_in_app_details", false)
        val isInstallSource =
            prefs(ModulePrefs).getBoolean("show_install_source_in_app_details", false)

        "com.oplus.settings.feature.appmanager.AppInfoFeature".toClass().apply {
            method { name = "setAppLabelAndIcon";paramCount = 1 }.hook {
                after {
                    val mRootView = field { name = "mRootView" }.get(instance).cast<View>()
                        ?: return@after
                    val appButtonsPreferenceController = args().first().any() ?: return@after
                    val instrumentedPreferenceFragment =
                        appButtonsPreferenceController.current().field { name = "mFragment" }
                            .any() ?: return@after
                    val packageInfo = instrumentedPreferenceFragment.current().field {
                        type = PackageInfoClass
                    }.cast<PackageInfo>() ?: return@after
                    val context = mRootView.context
                    val appIcon = mRootView.findViewById<ImageView>(
                        context.resources.getIdentifier(
                            "app_icon", "id", this@HookAppDetails.packageName
                        )
                    )
                    val appSize = mRootView.findViewById<TextView>(
                        context.resources.getIdentifier(
                            "app_size", "id", this@HookAppDetails.packageName
                        )
                    )
                    val packName = packageInfo.packageName
                    val appVers = context.getAppVersion(packName, false)
                    if (appVers.size < 3) return@after
                    val version =
                        if (appVers[2] == "null") "${appVers[0]}(${appVers[1]})" else "${appVers[0]}(${appVers[1]})_${appVers[2]}"
                    val versionText = context.getString(
                        context.resources.getIdentifier(
                            "version_text", "string", this@HookAppDetails.packageName
                        ), version
                    )
                    context.injectModuleAppResources()
                    val updateStr = formatDate("YYYY/MM/dd HH:mm:ss", packageInfo.lastUpdateTime)
                    val updateTime =
                        if (isLastUpdateTime) "\n${context.getString(R.string.last_update_time)} $updateStr" else ""
                    val firstInstallStr = if (isFirstInstall) {
                        "\n${context.getString(R.string.show_first_install_time_in_app_details)}: " +
                            formatDate("YYYY/MM/dd HH:mm:ss", packageInfo.firstInstallTime)
                    } else ""
                    val sdkStr = if (isSdkVersion) {
                        val appInfo = packageInfo.applicationInfo
                        val minSdk = appInfo?.minSdkVersion ?: 0
                        val targetSdk = appInfo?.targetSdkVersion ?: 0
                        "\nSDK: min=$minSdk target=$targetSdk"
                    } else ""
                    val sourceStr = if (isInstallSource) {
                        val pm = context.packageManager
                        val installerPkg = runCatching {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                pm.getInstallSourceInfo(packName).installingPackageName
                            } else {
                                @Suppress("DEPRECATION")
                                pm.getInstallerPackageName(packName)
                            }
                        }.getOrNull() ?: "null"
                        val displayName = if (installerPkg != "null") {
                            runCatching {
                                val ai = pm.getApplicationInfo(installerPkg, 0)
                                pm.getApplicationLabel(ai)?.toString()?.takeIf { it.isNotBlank() }
                            }.getOrNull() ?: installerPkg
                        } else installerPkg
                        "\n${context.getString(R.string.show_install_source_in_app_details)}: $displayName"
                    } else ""
                    if (isIconMarket) appIcon.setOnClickListener {
                        it.context.openMarketIntent(packName)
                    }
                    appSize.apply {
                        if (isEnableCopy) setTextIsSelectable(true)
                        if (isPackName || isLastUpdateTime || isSdkVersion || isFirstInstall || isInstallSource) {
                            val base = if (isPackName) "$packName\n$versionText" else versionText
                            text = "$base$updateTime$firstInstallStr$sdkStr$sourceStr"
                        }
                    }
                }
            }
        }
    }
}
