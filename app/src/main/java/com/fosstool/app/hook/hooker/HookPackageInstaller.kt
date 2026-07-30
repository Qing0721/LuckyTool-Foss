package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.packageinstaller.AllowReplaceInstall
import com.fosstool.app.hook.scope.packageinstaller.AutoClickInstallButton
import com.fosstool.app.hook.scope.packageinstaller.AutoClickUnInstallButton
import com.fosstool.app.hook.scope.packageinstaller.DisableStartAppDetail
import com.fosstool.app.hook.scope.packageinstaller.ForceInstallButtonDisplay
import com.fosstool.app.hook.scope.packageinstaller.RemoveInstallAds
import com.fosstool.app.hook.scope.packageinstaller.ShowMoreApkPackageInformation
import com.fosstool.app.hook.scope.packageinstaller.SkipApkScan
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getAppSet

object HookPackageInstaller : YukiBaseHooker() {
    override fun onHook() {
        val appSet = getAppSet(ModulePrefs, packageName)

        if (prefs(ModulePrefs).getBoolean("skip_apk_scan", false)) {
            loadHooker(SkipApkScan(appSet.getOrElse(2) { "null" }))
        }
        if (prefs(ModulePrefs).getBoolean("allow_downgrade_install", false)) {
            loadHooker(AllowReplaceInstall)
        }
        if (prefs(ModulePrefs).getBoolean("remove_install_ads", false)) {
            loadHooker(RemoveInstallAds)
        }
        if (prefs(ModulePrefs).getBoolean("auto_click_install_button", false)) {
            loadHooker(AutoClickInstallButton)
        }
        if (prefs(ModulePrefs).getBoolean("auto_click_uninstall_button", false)) {
            loadHooker(AutoClickUnInstallButton)
        }
        loadHooker(ForceInstallButtonDisplay)
        loadHooker(DisableStartAppDetail)
        loadHooker(ShowMoreApkPackageInformation)
    }
}
