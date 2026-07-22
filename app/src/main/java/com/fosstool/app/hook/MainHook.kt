package com.fosstool.app.hook

import android.os.Build.VERSION_CODES.R
import android.os.Build.VERSION_CODES.S
import android.os.Build.VERSION_CODES.S_V2
import android.os.Build.VERSION_CODES.TIRAMISU
import android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE
import android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.xposed.bridge.event.YukiXposedEvent
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import com.fosstool.app.hook.hooker.HookAod
import com.fosstool.app.hook.hooker.HookAndroid
import com.fosstool.app.hook.hooker.HookAudioEffectCenter
import com.fosstool.app.hook.hooker.HookAutoStart
import com.fosstool.app.hook.hooker.HookBattery
import com.fosstool.app.hook.hooker.HookBeaconLink
import com.fosstool.app.hook.hooker.HookBrowser
import com.fosstool.app.hook.hooker.HookCalendar
import com.fosstool.app.hook.hooker.HookCamera
import com.fosstool.app.hook.hooker.HookCloudService
import com.fosstool.app.hook.hooker.HookDialogRelated
import com.fosstool.app.hook.hooker.HookDirectUI
import com.fosstool.app.hook.hooker.HookEngineerMode
import com.fosstool.app.hook.hooker.HookEyeProtect
import com.fosstool.app.hook.hooker.HookFileManager
import com.fosstool.app.hook.hooker.HookFingerPrintRelated
import com.fosstool.app.hook.hooker.HookGallery
import com.fosstool.app.hook.hooker.HookGesture
import com.fosstool.app.hook.hooker.HookGestureRelated
import com.fosstool.app.hook.hooker.HookHealth
import com.fosstool.app.hook.hooker.HookIncallUI
import com.fosstool.app.hook.hooker.HookLauncher
import com.fosstool.app.hook.hooker.HookContactsScope
import com.fosstool.app.hook.hooker.HookBluetoothScope
import com.fosstool.app.hook.hooker.HookAtlasScope
import com.fosstool.app.hook.hooker.HookAccessoryScope
import com.fosstool.app.hook.hooker.HookLockScreen
import com.fosstool.app.hook.hooker.HookMarket
import com.fosstool.app.hook.hooker.HookMediaController
import com.fosstool.app.hook.hooker.HookMcs
import com.fosstool.app.hook.hooker.HookMiscellaneous
import com.fosstool.app.hook.hooker.HookMultiApp
import com.fosstool.app.hook.hooker.HookMyDevices
import com.fosstool.app.hook.hooker.HookNfc
import com.fosstool.app.hook.hooker.HookNotificationManager
import com.fosstool.app.hook.hooker.HookOShare
import com.fosstool.app.hook.hooker.HookOplusGames
import com.fosstool.app.hook.hooker.HookOplusMMS
import com.fosstool.app.hook.hooker.HookOplusOta
import com.fosstool.app.hook.hooker.HookOtherApp
import com.fosstool.app.hook.hooker.HookPackageInstaller
import com.fosstool.app.hook.hooker.HookPermissionController
import com.fosstool.app.hook.hooker.HookPhone
import com.fosstool.app.hook.hooker.HookPhoneManager
import com.fosstool.app.hook.hooker.HookPictorial
import com.fosstool.app.hook.hooker.HookQuickSearchBox
import com.fosstool.app.hook.hooker.HookSafeCenter
import com.fosstool.app.hook.hooker.HookScreenshot
import com.fosstool.app.hook.hooker.HookSecurityPermission
import com.fosstool.app.hook.hooker.HookSettings
import com.fosstool.app.hook.hooker.HookSmartSidebar
import com.fosstool.app.hook.hooker.HookSoundRecorder
import com.fosstool.app.hook.hooker.HookSau
import com.fosstool.app.hook.hooker.HookSpeechAssist
import com.fosstool.app.hook.hooker.HookStatusBar
import com.fosstool.app.hook.hooker.HookTeleService
import com.fosstool.app.hook.hooker.HookThemeStore
import com.fosstool.app.hook.hooker.HookUIEngine
import com.fosstool.app.hook.hooker.HookWeather
import com.fosstool.app.hook.hooker.HookWirelessSettings
import com.fosstool.app.hook.hooker.StatusBarNotifiyLimit
import com.fosstool.app.hook.scope.CorePatch.CorePatchForR
import com.fosstool.app.hook.scope.CorePatch.CorePatchForS
import com.fosstool.app.hook.scope.CorePatch.CorePatchForT
import com.fosstool.app.hook.scope.CorePatch.CorePatchForU
import com.fosstool.app.hook.scope.CorePatch.CorePatchForV
import com.fosstool.app.hook.scope.alarmclock.AlarmClockWidget
import com.fosstool.app.hook.scope.android.DisableFlagSecure
import com.fosstool.app.hook.scope.exsystemservice.EnableGameRunInBackground
import com.fosstool.app.hook.scope.systemui.HookSystemUIFeature
import com.fosstool.app.hook.scope.systemui.LockScreenClock
import com.fosstool.app.hook.scope.wirelesssettings.WlanSla
import com.fosstool.app.hook.statusbar.StatusBarBattery
import com.fosstool.app.hook.statusbar.StatusBarClock
import com.fosstool.app.hook.statusbar.StatusBarControlCenter
import com.fosstool.app.hook.statusbar.StatusBarIcon
import com.fosstool.app.hook.statusbar.StatusBarLayout
import com.fosstool.app.hook.statusbar.StatusBarNetWorkSpeed
import com.fosstool.app.hook.statusbar.StatusBarNotify
import com.fosstool.app.hook.statusbar.StatusBarTile
import com.fosstool.app.utils.SDK
import de.robv.android.xposed.IXposedHookZygoteInit
import de.robv.android.xposed.callbacks.XC_LoadPackage

@InjectYukiHookWithXposed(isUsingResourcesHook = false)
object MainHook : IYukiHookXposedInit {
    override fun onInit() = configs {
        debugLog {
            tag = "LuckyTool"
            isEnable = true
            isRecord = true
            elements(TAG, PRIORITY, PACKAGE_NAME, USER_ID)
        }
        isDebug = false
    }

    override fun onHook() = encase {
        loadSystem(HookAndroid)

        loadApp("com.android.systemui", HookSystemUIFeature)
        loadApp("com.android.systemui", HookStatusBar)
        loadApp("com.android.systemui", StatusBarClock)
        loadApp("com.android.systemui", StatusBarNetWorkSpeed)
        loadApp("com.android.systemui", StatusBarNotify)
        loadApp("com.coloros.phonemanager", HookPhoneManager)

        loadApp("com.android.systemui", StatusBarNotifiyLimit)
        loadApp("com.oplus.notificationmanager", HookNotificationManager)

        loadApp("com.android.systemui", StatusBarIcon)
        loadApp("com.android.systemui", StatusBarControlCenter)
        loadApp("com.android.systemui", StatusBarTile)
        loadApp("com.android.systemui", StatusBarLayout)
        loadApp("com.android.systemui", StatusBarBattery)

        loadApp("com.coloros.alarmclock", AlarmClockWidget)
        loadApp("com.oppo.launcher", "com.android.launcher") {
            loadHooker(HookLauncher)
        }

        loadApp("com.oplus.aod", HookAod)
        loadApp("com.oplus.uiengine", HookUIEngine)
        loadApp("com.android.systemui", HookLockScreen)
        loadApp("com.oplus.keyguard.clock.base", LockScreenClock)
        loadApp("com.oplus.screenshot", HookScreenshot)
        loadApp("com.oplus.screenshot", DisableFlagSecure)
        loadApp("com.android.systemui", DisableFlagSecure)
        loadApp("com.oplus.appplatform", DisableFlagSecure)

        loadApp("com.oplus.safecenter", "com.coloros.safecenter") {
            loadHooker(HookSafeCenter)
        }
        loadApp("com.android.packageinstaller", HookPackageInstaller)
        loadApp("com.android.systemui", "com.coloros.securepay", "com.oplus.exsystemservice") {
            loadHooker(HookDialogRelated)
        }
        loadApp("com.android.systemui", HookGestureRelated)
        loadApp("com.android.systemui", HookFingerPrintRelated)
        loadApp("com.android.systemui", "com.android.externalstorage", "com.oplus.exsystemservice") {
            loadHooker(HookMiscellaneous)
        }
        loadApp("com.oplus.exsystemservice", EnableGameRunInBackground)

        loadApp("com.oplus.battery", HookBattery)
        loadApp("com.android.settings", HookSettings)
        loadApp("com.oneplus.camera", "com.oplus.camera") {
            loadHooker(HookCamera)
        }
        loadApp("com.coloros.gallery3d", HookGallery)
        loadApp("com.heytap.themestore", "com.oplus.themestore") {
            loadHooker(HookThemeStore)
        }
        loadApp("com.heytap.cloud", HookCloudService)
        loadApp("com.oplus.games", "com.oplus.cosa") {
            loadHooker(HookOplusGames)
        }
        loadApp("com.oplus.ota", HookOplusOta)
        loadApp("com.heytap.pictorial", HookPictorial)
        loadApp("com.android.mms", HookOplusMMS)
        loadApp("com.heytap.browser", HookBrowser)
        loadApp("com.oplus.gesture", HookGesture)
        loadApp("com.android.permissioncontroller", HookPermissionController)
        loadApp("com.coloros.directui", HookDirectUI)
        loadApp("com.heytap.quicksearchbox", HookQuickSearchBox)
        loadApp("com.heytap.market", HookMarket)
        loadApp("com.coloros.weather2", HookWeather)
        loadApp("com.ruet_cse_1503050.ragib.appbackup.pro", "ru.kslabs.ksweb", "com.dv.adm") {
            loadHooker(HookOtherApp)
        }

        loadApp("com.oplus.beaconlink", HookBeaconLink)
        loadApp("com.coloros.calendar", HookCalendar)
        loadApp("com.oplus.engineermode", HookEngineerMode)
        loadApp("com.oplus.eyeprotect", HookEyeProtect)
        loadApp("com.coloros.filemanager", HookFileManager)
        loadApp("com.heytap.health", HookHealth)
        loadApp("com.android.contacts") { loadHooker(HookContactsScope) }
        loadApp("com.android.bluetooth") { loadHooker(HookBluetoothScope) }
        loadApp("com.oplus.atlas") { loadHooker(HookAtlasScope) }
        loadApp("com.heytap.accessory") { loadHooker(HookAccessoryScope) }
        loadApp("com.heytap.mcs", HookMcs)
        loadApp("com.heytap.mydevices", HookMyDevices)
        loadApp("com.android.nfc", HookNfc)
        loadApp("com.coloros.oshare", HookOShare)
        loadApp("com.oplus.securitypermission", HookSecurityPermission)
        loadApp("com.coloros.smartsidebar", HookSmartSidebar)
        loadApp("com.coloros.soundrecorder", "com.oplus.audiomonitor") {
            loadHooker(HookSoundRecorder)
        }
        loadApp("com.heytap.speechassist", HookSpeechAssist)
        loadApp("com.android.phone", "com.android.incallui") {
            loadHooker(HookTeleService)
        }
        loadApp("com.android.phone") {
            loadHooker(HookPhone)
        }
        loadApp("com.android.incallui") {
            loadHooker(HookIncallUI)
        }
        loadApp("com.oplus.wirelesssettings", "com.android.bluetooth") {
            loadHooker(HookWirelessSettings)
        }
        loadApp("com.oplus.wirelesssettings", WlanSla)
        loadApp("com.oplus.multiapp", HookMultiApp)
        loadApp("com.oplus.sau", HookSau)
        loadApp("com.oplus.mediacontroller", HookMediaController)
        loadApp("com.oplus.audio.effectcenter", HookAudioEffectCenter)

        loadApp("com.android.systemui", HookAutoStart)
    }

    override fun onXposedEvent() {
        YukiXposedEvent.onHandleLoadPackage { lpparam: XC_LoadPackage.LoadPackageParam ->
            run {
                if (lpparam.packageName == "android" && lpparam.processName == "android") {
                    when (SDK) {
                        VANILLA_ICE_CREAM, 36 -> CorePatchForV().handleLoadPackage(lpparam)
                        UPSIDE_DOWN_CAKE -> CorePatchForU().handleLoadPackage(lpparam)
                        TIRAMISU -> CorePatchForT().handleLoadPackage(lpparam)
                        S, S_V2 -> CorePatchForS().handleLoadPackage(lpparam)
                        R -> CorePatchForR().handleLoadPackage(lpparam)
                        else -> YLog.error("[CorePatch] Unsupported Version of Android -> $SDK")
                    }
                }
            }
        }
        YukiXposedEvent.onInitZygote { startupParam: IXposedHookZygoteInit.StartupParam ->
            run {
                when (SDK) {
                    VANILLA_ICE_CREAM, 36 -> CorePatchForV().initZygote(startupParam)
                    UPSIDE_DOWN_CAKE -> CorePatchForU().initZygote(startupParam)
                    TIRAMISU -> CorePatchForT().initZygote(startupParam)
                    S, S_V2 -> CorePatchForS().initZygote(startupParam)
                    R -> CorePatchForR().initZygote(startupParam)
                }
            }
        }
    }
}
