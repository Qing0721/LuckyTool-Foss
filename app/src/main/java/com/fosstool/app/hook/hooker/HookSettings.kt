package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.settings.AllowDisablingSystemApps
import com.fosstool.app.hook.scope.settings.AppSpecificMediaVolume
import com.fosstool.app.hook.scope.settings.AutoJumpAccessibilitySettings
import com.fosstool.app.hook.scope.settings.AutoUnlockRestrictedSettings
import com.fosstool.app.hook.scope.settings.CustomizeDeviceOtaCardBackground
import com.fosstool.app.hook.scope.settings.CustomizeDeviceSharingPageParameters
import com.fosstool.app.hook.scope.settings.DarkModeList
import com.fosstool.app.hook.scope.settings.DisableOTGAutoOffSettings
import com.fosstool.app.hook.scope.settings.EnableCustomAppLanguage
import com.fosstool.app.hook.scope.settings.EnableMultiAppQuickJump
import com.fosstool.app.hook.scope.settings.EnableStatusBarClockFormat
import com.fosstool.app.hook.scope.settings.EnableSwipeUpNavigationGesture
import com.fosstool.app.hook.scope.settings.FixDefaultAppJumpProblem
import com.fosstool.app.hook.scope.settings.ForceDisplayAutoLaunchJumpOption
import com.fosstool.app.hook.scope.settings.ForceDisplayBottomGoogleSettings
import com.fosstool.app.hook.scope.settings.ForceDisplayContentRecommend
import com.fosstool.app.hook.scope.settings.ForceDisplayDisabledAppsManager
import com.fosstool.app.hook.scope.settings.ForceDisplayGoogleAutoFill
import com.fosstool.app.hook.scope.settings.ForceDisplayPasswordManagementSetting
import com.fosstool.app.hook.scope.settings.ForceDisplayProcessManagement
import com.fosstool.app.hook.scope.settings.ForceDisplaySettingsFeatureFlags
import com.fosstool.app.hook.scope.settings.HookAppDetails
import com.fosstool.app.hook.scope.settings.HookIris5Controller
import com.fosstool.app.hook.scope.settings.HookSettingsFeature
import com.fosstool.app.hook.scope.settings.ProcessorDetailPreference
import com.fosstool.app.hook.scope.settings.RemoveDeviceNameChangeLimit
import com.fosstool.app.hook.scope.settings.RemoveDpiRestartRecovery
import com.fosstool.app.hook.scope.settings.RemoveSettingsBottomLaboratory
import com.fosstool.app.hook.scope.settings.RemoveTopAccountDisplay
import com.fosstool.app.hook.utils.OplusBuildUtlils
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker
import com.fosstool.app.hook.utils.SystemPropertiesOverrideEngineHooker.Mode
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object HookSettings : YukiBaseHooker() {
    override fun onHook() {

        loadHooker(SystemPropertiesOverrideEngineHooker(mode = Mode.BOTH))

        loadHooker(HookSettingsFeature)

        loadHooker(HookAppDetails)
        loadHooker(AutoUnlockRestrictedSettings)

        if (prefs(ModulePrefs).getBoolean("dark_mode_list_enable", false)) {
            loadHooker(DarkModeList)
        }
        if (prefs(ModulePrefs).getBoolean("remove_top_account_display", false)) {
            loadHooker(RemoveTopAccountDisplay)
        }
        if (prefs(ModulePrefs).getBoolean("enable_video_memc_frame_insertion", false) &&
            prefs(ModulePrefs).getBoolean("video_frame_insertion_support_2K120", false)
        ) {
            loadHooker(HookIris5Controller)
        }
        if (prefs(ModulePrefs).getBoolean("remove_dpi_restart_recovery", false)) {
            loadHooker(RemoveDpiRestartRecovery)
        }
        if (prefs(ModulePrefs).getBoolean("force_display_bottom_google_settings", false)) {
            loadHooker(ForceDisplayBottomGoogleSettings)
        }
        if (prefs(ModulePrefs).getBoolean("remove_settings_bottom_laboratory", false)) {
            loadHooker(RemoveSettingsBottomLaboratory)
        }
        if (prefs(ModulePrefs).getBoolean("enable_statusbar_clock_format", false)) {
            loadHooker(EnableStatusBarClockFormat)
        }
        if (prefs(ModulePrefs).getBoolean("customize_device_sharing_page_parameters", false)) {
            if (SDK >= A13) loadHooker(CustomizeDeviceSharingPageParameters)
        }
        if (prefs(ModulePrefs).getBoolean("customize_device_ota_card_background", false)) {
            loadHooker(CustomizeDeviceOtaCardBackground)
        }
        if (prefs(ModulePrefs).getBoolean("force_display_process_management", false)) {
            loadHooker(ForceDisplayProcessManagement)
        }
        if (prefs(ModulePrefs).getBoolean("allow_disabling_system_apps", false)) {
            loadHooker(AllowDisablingSystemApps)
        }
        if (prefs(ModulePrefs).getBoolean("force_display_disabled_apps_manager", false)) {
            loadHooker(ForceDisplayDisabledAppsManager)
        }
        if (prefs(ModulePrefs).getBoolean("force_display_content_recommend", false)) {
            loadHooker(ForceDisplayContentRecommend)
        }
        if (prefs(ModulePrefs).getBoolean("enable_custom_app_language", false) && SDK >= 34) {
            loadHooker(EnableCustomAppLanguage)
        }

        if (prefs(ModulePrefs).getBoolean("enable_app_specific_media_volume", false)) {
            loadHooker(AppSpecificMediaVolume)
        }
        if (prefs(ModulePrefs).getString("set_processor_click_page", "0") == "3") {
            loadHooker(ProcessorDetailPreference)
        }
        if (prefs(ModulePrefs).getBoolean("remove_device_name_change_limit", false) &&
            (try { OplusBuildUtlils().getOSVersionCode } catch (_: Throwable) { null } ?: 0) >= 30
        ) {
            loadHooker(RemoveDeviceNameChangeLimit)
        }
        if (prefs(ModulePrefs).getBoolean("disable_cn_special_edition_setting", false) &&
            prefs(ModulePrefs).getBoolean("fix_default_app_jump_problem", false)
        ) {
            loadHooker(FixDefaultAppJumpProblem)
        }
        if (prefs(ModulePrefs).getBoolean("disable_cn_special_edition_setting", false) &&
            prefs(ModulePrefs).getBoolean("force_display_auto_launch_jump_option", false)
        ) {
            loadHooker(ForceDisplayAutoLaunchJumpOption)
        }
        if (prefs(ModulePrefs).getBoolean("enable_google_auto_fill", false)) {
            loadHooker(ForceDisplayGoogleAutoFill)
        }
        if (prefs(ModulePrefs).getBoolean("force_display_password_management_settings", false)) {
            loadHooker(ForceDisplayPasswordManagementSetting)
        }
        if (prefs(ModulePrefs).getBoolean("screen_physics_size_shown_cm", false) ||
            prefs(ModulePrefs).getBoolean("disable_device_admin_verification_dialog", false) ||
            prefs(ModulePrefs).getBoolean("enable_touch_membrane_protector_mode", false)
        ) {
            loadHooker(ForceDisplaySettingsFeatureFlags)
        }
        if (prefs(ModulePrefs).getBoolean("enable_swipe_up_navigation_gesture", false) &&
            (try { OplusBuildUtlils().getOSVersionCode } catch (_: Throwable) { null } ?: 0) >= 30
        ) {
            loadHooker(EnableSwipeUpNavigationGesture)
        }
        if (prefs(ModulePrefs).getBoolean("disable_otg_auto_off", false) &&
            (try { OplusBuildUtlils().getOSVersionCode } catch (_: Throwable) { null } ?: 0) >= 30
        ) {
            loadHooker(DisableOTGAutoOffSettings)
        }
        if (prefs(ModulePrefs).getBoolean("auto_jump_accessibility_settings", false)) {
            loadHooker(AutoJumpAccessibilitySettings)
        }
        if (prefs(ModulePrefs).getBoolean("enable_multi_app_quick_jump", false) ||
            prefs(ModulePrefs).getBoolean("enable_app_clone_quick_jump", false) ||
            prefs(ModulePrefs).getBoolean("enable_quick_open_market_page", false)
        ) {
            loadHooker(EnableMultiAppQuickJump)
        }

    }
}
