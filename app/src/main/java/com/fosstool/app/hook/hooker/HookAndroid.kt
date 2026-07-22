package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.android.ADBInstallConfirm
import com.fosstool.app.hook.scope.android.AllowUntrustedTouch
import com.fosstool.app.hook.scope.android.AppSplashScreen
import com.fosstool.app.hook.scope.android.BatteryOptimizationWhitelist
import com.fosstool.app.hook.scope.android.DarkModeService
import com.fosstool.app.hook.scope.android.DisableAccessibilityWarningDialog
import com.fosstool.app.hook.scope.android.DisableDynamicRefreshRate
import com.fosstool.app.hook.scope.android.DisableFlagSecure
import com.fosstool.app.hook.scope.android.EnableKeepNotificationWhenAppStop
import com.fosstool.app.hook.scope.android.FixBatteryHealthDataDisplay
import com.fosstool.app.hook.scope.android.ForceAllAppsSupportSplitScreen
import com.fosstool.app.hook.scope.android.FullBrightnessMinRefresh
import com.fosstool.app.hook.scope.android.GameFeatureOverrides
import com.fosstool.app.hook.scope.android.HideAppIntent
import com.fosstool.app.hook.scope.android.HookNotificationManager
import com.fosstool.app.hook.scope.android.HookOplusMemcHelper
import com.fosstool.app.hook.scope.android.HookSystemProperties
import com.fosstool.app.hook.scope.android.HookWindowManagerService
import com.fosstool.app.hook.scope.android.MediaVolumeLevel
import com.fosstool.app.hook.scope.android.MultiApp
import com.fosstool.app.hook.scope.android.RemoveAccessDeviceLogDialog
import com.fosstool.app.hook.scope.android.RemoveAppUninstallButtonBlackList
import com.fosstool.app.hook.scope.android.RemovePasswordTimeoutVerification
import com.fosstool.app.hook.scope.android.RemoveStatusBarTopNotification
import com.fosstool.app.hook.scope.android.RemoveSystemPromptIcon
import com.fosstool.app.hook.scope.android.RemoveSystemScreenshotDelay
import com.fosstool.app.hook.scope.android.RemoveVPNActiveNotification
import com.fosstool.app.hook.scope.android.ReducePowerMenuDisplayDelay
import com.fosstool.app.hook.scope.android.RemoveAlwaysAllowAppStartList
import com.fosstool.app.hook.scope.android.ReplaceSystemRootStateDetection
import com.fosstool.app.hook.scope.android.ScreenColorTemperatureRGBPalette
import com.fosstool.app.hook.scope.android.ScrollToTopWhiteList
import com.fosstool.app.hook.scope.android.SetAppUpdateDotDisplayMode
import com.fosstool.app.hook.scope.android.SuperVolumeMode
import com.fosstool.app.hook.scope.android.SystemEnableVolumeKeyControlFlashlight
import com.fosstool.app.hook.scope.android.ZoomWindow
import com.fosstool.app.hook.scope.launcher.CustomAppFloatingWindowDisplayMode
import com.fosstool.app.hook.scope.launcher.CustomMultiWindowDisplayUpperLimit
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode
import com.fosstool.app.utils.ModulePrefs


object HookAndroid : YukiBaseHooker() {

    override fun onHook() {
        loadHooker(SystemPropertiesOverrideEngineHooker(mode = Mode.BOTH))

        loadHooker(RemoveStatusBarTopNotification)

        loadHooker(RemoveSystemPromptIcon)

        loadHooker(EnableKeepNotificationWhenAppStop)

        loadHooker(RemoveVPNActiveNotification)

        loadHooker(HookNotificationManager)

        loadHooker(HookWindowManagerService)

        loadHooker(HookSystemProperties)

        loadHooker(HookOplusMemcHelper)

        loadHooker(SuperVolumeMode)

        loadHooker(GameFeatureOverrides)

        loadHooker(MediaVolumeLevel)

        loadHooker(MultiApp)

        if (prefs(ModulePrefs).getBoolean("disable_accessibility_warning_dialog", false)) {
            loadHooker(DisableAccessibilityWarningDialog)
        }

        loadHooker(ADBInstallConfirm)

        loadHooker(RemovePasswordTimeoutVerification)

        loadHooker(RemoveSystemScreenshotDelay)

        loadHooker(AppSplashScreen)

        loadHooker(DisableFlagSecure)

        loadHooker(AllowUntrustedTouch)

        loadHooker(ZoomWindow)

        loadHooker(CustomAppFloatingWindowDisplayMode)

        loadHooker(CustomMultiWindowDisplayUpperLimit)

        loadHooker(DarkModeService)

        loadHooker(BatteryOptimizationWhitelist)

        loadHooker(ScrollToTopWhiteList)

        loadHooker(RemoveAccessDeviceLogDialog)

        loadHooker(FixBatteryHealthDataDisplay)

        loadHooker(DisableDynamicRefreshRate)

        loadHooker(SystemEnableVolumeKeyControlFlashlight)

        loadHooker(ForceAllAppsSupportSplitScreen)

        loadHooker(RemoveAppUninstallButtonBlackList)

        loadHooker(ScreenColorTemperatureRGBPalette)

        loadHooker(HideAppIntent)

        loadHooker(SetAppUpdateDotDisplayMode)




        loadHooker(ReducePowerMenuDisplayDelay)

        loadHooker(ReplaceSystemRootStateDetection)

        loadHooker(FullBrightnessMinRefresh)

        loadHooker(RemoveAlwaysAllowAppStartList)



    }
}
