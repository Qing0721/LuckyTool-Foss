package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.android.ADBInstallConfirm
import com.fosstool.app.hook.scope.android.AllowUntrustedTouch
import com.fosstool.app.hook.scope.android.AppSplashScreen
import com.fosstool.app.hook.scope.android.BatteryOptimizationWhitelist
import com.fosstool.app.hook.scope.android.DarkModeService
import com.fosstool.app.hook.scope.android.DisableAccessibilityWarningDialog
import com.fosstool.app.hook.scope.android.DisableMaliciousAppIntercept
import com.fosstool.app.hook.scope.android.DisableFlagSecure
import com.fosstool.app.hook.scope.android.EnableKeepNotificationWhenAppStop
import com.fosstool.app.hook.scope.android.EnableRecordCallsOnThirdPartyApps
import com.fosstool.app.hook.scope.android.ForceAllAppsSupportSplitScreen
import com.fosstool.app.hook.scope.android.ForceEnable32BitSupport
import com.fosstool.app.hook.scope.android.FullBrightnessMinRefresh
import com.fosstool.app.hook.scope.android.HideAppIntent
import com.fosstool.app.hook.scope.android.HookNotificationManager
import com.fosstool.app.hook.scope.android.HookOplusMemcHelper
import com.fosstool.app.hook.scope.android.HookWindowManagerService
import com.fosstool.app.hook.scope.android.MediaVolumeLevel
import com.fosstool.app.hook.scope.android.MultiApp
import com.fosstool.app.hook.scope.android.RemoveAccessDeviceLogDialog
import com.fosstool.app.hook.scope.android.RemoveAppUninstallButtonBlackList
import com.fosstool.app.hook.scope.android.RemovePasswordTimeoutVerification
import com.fosstool.app.hook.scope.android.RemoveStatusBarTopNotification
import com.fosstool.app.hook.scope.android.RemoveVPNActiveNotification
import com.fosstool.app.hook.scope.android.RemoveAlwaysAllowAppStartList
import com.fosstool.app.hook.scope.android.RemoveGmsUsageRestrictions
import com.fosstool.app.hook.scope.android.ReplaceSystemRootStateDetection
import com.fosstool.app.hook.scope.android.RunFloatingWindowTasksInForeground
import com.fosstool.app.hook.scope.android.ScrollToTopWhiteList
import com.fosstool.app.hook.scope.android.SetAppUpdateDotDisplayMode
import com.fosstool.app.hook.scope.android.SystemEnableVolumeKeyControlFlashlight
import com.fosstool.app.hook.scope.launcher.CustomAppFloatingWindowDisplayMode
import com.fosstool.app.hook.scope.launcher.CustomMultiWindowDisplayUpperLimit
import com.fosstool.app.hook.scope.wirelesssettings.WlanSla
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode

object HookAndroid : YukiBaseHooker() {

    override fun onHook() {
        loadHooker(SystemPropertiesOverrideEngineHooker(mode = Mode.BOTH))

        loadHooker(RemoveStatusBarTopNotification)

        loadHooker(EnableKeepNotificationWhenAppStop)

        loadHooker(RemoveVPNActiveNotification)

        loadHooker(HookNotificationManager)

        loadHooker(WlanSla)

        loadHooker(HookWindowManagerService)

        loadHooker(HookOplusMemcHelper)

        loadHooker(MediaVolumeLevel)

        loadHooker(MultiApp)

        if (prefs(ModulePrefs).getBoolean("disable_accessibility_warning_dialog", false) &&
            getOSVersionCode >= 38
        ) {
            loadHooker(DisableAccessibilityWarningDialog)
        }

        if (prefs(ModulePrefs).getBoolean("disable_malicious_app_intercept", false) &&
            getOSVersionCode >= 38
        ) {
            loadHooker(DisableMaliciousAppIntercept)
        }

        loadHooker(ADBInstallConfirm)

        loadHooker(RemovePasswordTimeoutVerification)

        if (getOSVersionCode >= 26) loadHooker(AppSplashScreen)

        loadHooker(DisableFlagSecure())

        if (getOSVersionCode >= 23) loadHooker(AllowUntrustedTouch)

        loadHooker(CustomAppFloatingWindowDisplayMode)

        loadHooker(CustomMultiWindowDisplayUpperLimit)

        loadHooker(DarkModeService)

        loadHooker(BatteryOptimizationWhitelist)

        if (getOSVersionCode >= 26) loadHooker(ScrollToTopWhiteList)

        if (getOSVersionCode >= 26) loadHooker(RemoveAccessDeviceLogDialog)

        loadHooker(SystemEnableVolumeKeyControlFlashlight)

        if (getOSVersionCode in 26..33) loadHooker(ForceAllAppsSupportSplitScreen)

        if (getOSVersionCode >= 26) loadHooker(RemoveAppUninstallButtonBlackList)

        loadHooker(HideAppIntent)

        loadHooker(EnableRecordCallsOnThirdPartyApps)

        if (getOSVersionCode >= 33) loadHooker(SetAppUpdateDotDisplayMode)

        loadHooker(RunFloatingWindowTasksInForeground)

        loadHooker(ForceEnable32BitSupport)

        loadHooker(RemoveGmsUsageRestrictions)

        loadHooker(ReplaceSystemRootStateDetection)

        loadHooker(FullBrightnessMinRefresh)

        loadHooker(RemoveAlwaysAllowAppStartList)

    }
}
