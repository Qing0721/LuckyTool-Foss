package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.packageinstaller.AllowReplaceInstall
import com.fosstool.app.hook.scope.packageinstaller.AutoClickInstallButton
import com.fosstool.app.hook.scope.packageinstaller.AutoClickUnInstallButton
import com.fosstool.app.hook.scope.packageinstaller.DisableStartAppDetail
import com.fosstool.app.hook.scope.packageinstaller.ForceInstallButtonDisplay
import com.fosstool.app.hook.scope.packageinstaller.HookPackageInstallerFeature
import com.fosstool.app.hook.scope.packageinstaller.RemoveInstallAds
import com.fosstool.app.hook.scope.packageinstaller.ShowMoreApkPackageInformation
import com.fosstool.app.hook.scope.packageinstaller.ShowPackageNameAndVersionCode
import com.fosstool.app.hook.scope.packageinstaller.SkipApkScan
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getAppSet

object HookPackageInstaller : YukiBaseHooker() {
    override fun onHook() {
        val appSet = getAppSet(ModulePrefs, packageName)

        if (appSet[2] == "null") return

        loadHooker(HookPackageInstallerFeature)

        if (prefs(ModulePrefs).getBoolean("skip_apk_scan", false)) {
            loadHooker(SkipApkScan(appSet[2]))
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
        if (prefs(ModulePrefs).getBoolean("show_packagename_and_versioncode", false)) {
            loadHooker(ShowPackageNameAndVersionCode)
        }
        loadHooker(ForceInstallButtonDisplay)
        loadHooker(DisableStartAppDetail)
        loadHooker(ShowMoreApkPackageInformation)
    }
}
