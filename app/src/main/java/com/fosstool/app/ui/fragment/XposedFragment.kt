package com.fosstool.app.ui.fragment

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.view.MenuProvider
import androidx.core.widget.NestedScrollView
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.drake.net.utils.scopeDialog
import com.drake.net.utils.scopeLife
import com.drake.net.utils.withMain
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.highcapable.yukihookapi.hook.xposed.prefs.ui.ModulePreferenceFragment
import com.fosstool.app.R
import com.fosstool.app.ui.activity.MainActivity
import com.fosstool.app.ui.fragment.base.BaseScopePreferenceFeagment
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.ThemeUtils
import com.fosstool.app.utils.arraySummaryDot
import com.fosstool.app.utils.checkPackName
import com.fosstool.app.utils.dialogCentered
import com.fosstool.app.utils.dp
import com.fosstool.app.utils.fixIconSize
import com.fosstool.app.utils.getAppLabel
import com.fosstool.app.utils.getAppVersion
import com.fosstool.app.utils.navigatePage
import com.fosstool.app.utils.restartMain
import com.fosstool.app.utils.setPrefsIconRes
import com.fosstool.app.utils.setupMenuProvider
import com.fosstool.app.utils.toast
import kotlinx.coroutines.Dispatchers
import java.util.Arrays

class XposedFragment : ModulePreferenceFragment(), MenuProvider {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        setupMenuProvider(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreatePreferencesInModuleApp(savedInstanceState: Bundle?, rootKey: String?) {}

    private fun init() {
        val dialog = MaterialAlertDialogBuilder(requireActivity(), dialogCentered).apply {
            setTitle(getString(R.string.common_words_loading))
            setView(LinearLayout(context).apply {
                addView(LinearProgressIndicator(context).apply {
                    layoutParams =
                        LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
                    setPadding(20.dp, 20.dp, 20.dp, 0)
                    isIndeterminate = true
                })
            })
        }.create()
        scopeDialog(dialog, false, Dispatchers.Default) {
            if (preferenceScreen != null) return@scopeDialog
            val destination = findNavController().currentBackStack.value.lastOrNull()?.destination
            if (!destination.toString().contains(this@XposedFragment::class.java.simpleName)) {
                return@scopeDialog
            }
            preferenceScreen = preferenceManager.createPreferenceScreen(requireActivity()).apply {
                getPreferences(context).forEachIndexed { index, preference ->
                    try {
                        if (preferenceScreen != null) preferenceScreen = preferenceScreen.apply {
                            addPreference(preference)
                        } else addPreference(preference)
                    } catch (_: Throwable) {
                        withMain { context.toast("Error: $index ${preference.key}") }
                    }
                }
            }
        }
    }

    private fun getPreferences(context: Context): List<Preference> {
        return listOf(
            Preference(context).apply {
                key = "android"
                setPrefsIconRes(android.R.mipmap.sym_def_app_icon) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.allow_untrusted_touch),
                    getString(R.string.set_ltpo_refresh_rate_mode)
                )
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_xposed_to_android, title)
                    true
                }
            },
            Preference(context).apply {
                key = "StatusBar"
                setPrefsIconRes("com.android.systemui") { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = getString(R.string.StatusBar)
                summary = arraySummaryDot(
                    getString(R.string.StatusBarNotice),
                    getString(R.string.StatusBarIcon),
                    getString(R.string.StatusBarClock)
                )
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_xposed_to_statusBar, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.android.launcher"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = getString(R.string.Desktop)
                summary = arraySummaryDot(
                    getString(R.string.AppBadgeRelated),
                    getString(R.string.FolderLayoutRelated),
                    getString(R.string.launcher_layout_related)
                )
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_xposed_to_launcher, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.oplus.aod"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = getString(R.string.AodRelated)
                summary = arraySummaryDot(
                    getString(R.string.remove_aod_music_whitelist),
                    getString(R.string.remove_aod_notification_icon_whitelist)
                )
                isVisible = SDK >= A13 && context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_aod, title)
                    true
                }
            },
            Preference(context).apply {
                key = "LockScreen"
                setPrefsIconRes("com.android.systemui") { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = getString(R.string.LockScreen)
                summary = arraySummaryDot(
                    getString(R.string.remove_lock_screen_redone),
                    getString(R.string.remove_lock_screen_bottom_right_camera)
                )
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_xposed_to_lockScreen, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.android.packageinstaller"
                setPrefsIconRes(key) { resource, show ->
                    icon = fixIconSize(resource)
                    isIconSpaceReserved = show
                }
                title = getString(R.string.Application)
                summary = arraySummaryDot(
                    getString(R.string.corepatch), getString(R.string.skip_apk_scan)
                )
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_xposed_to_application, title)
                    true
                }
            },
            Preference(context).apply {
                key = "Miscellaneous"
                setPrefsIconRes("com.android.systemui") { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = getString(R.string.Miscellaneous)
                summary = arraySummaryDot(
                    getString(R.string.FloatingWindowDialogRelated),
                    getString(R.string.FingerPrintRelated),
                    getString(R.string.SoundRelated)
                )
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_xposed_to_miscellaneous, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.oplus.screenshot"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = getString(R.string.Screenshot)
                summary = arraySummaryDot(
                    getString(R.string.remove_system_screenshot_delay),
                    getString(R.string.remove_screenshot_privacy_limit)
                )
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_xposed_to_screenshot, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.oplus.battery"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.open_battery_health),
                    getString(R.string.open_screen_power_save)
                )
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_battery, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.coloros.alarmclock"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.alarm_clock_widget_red_one_mode))
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusAlarmClock, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.android.settings"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.remove_top_account_display),
                    getString(R.string.remove_dpi_restart_recovery)
                )
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_settings, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.oplus.wirelesssettings"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.enable_wifi_detail_show_gateway))
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusWirelessSettings, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.android.incallui"
                setPrefsIconRes("com.android.phone") { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel("com.android.phone")
                summary = arraySummaryDot(
                    getString(R.string.force_display_5g_switch),
                    getString(R.string.force_display_volte_hd_call),
                    getString(R.string.force_display_preferred_network_type)
                )
                isVisible = context.checkPackName(key) || context.checkPackName("com.android.phone")
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusTeleService, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.android.mms"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary =
                    arraySummaryDot(getString(R.string.remove_verification_code_floating_window))
                isVisible = SDK >= A13 && context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusMMS, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.heytap.browser"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.remove_ads_from_download_dialog),
                    getString(R.string.remove_ads_from_weather_page),
                )
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusBrowser, title)
                    true
                }
            },
            Preference(context).apply {
                val isOneplusCamera = context.checkPackName("com.oneplus.camera")
                key = if (isOneplusCamera) "com.oneplus.camera" else "com.oplus.camera"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.remove_watermark_word_limit),
                    getString(R.string.enable_10_bit_image_support)
                )
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_xposed_to_camera, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.coloros.gallery3d"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.enable_watermark_editing),
                    getString(R.string.replace_oneplus_model_watermark)
                )
                isVisible = SDK >= A13 && context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusGallery, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.oplus.games"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.remove_root_check),
                    getString(R.string.enable_developer_page)
                )
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_xposed_to_oplusGames, title)
                    true
                }
            },
            Preference(context).apply {
                val isHeytap = context.checkPackName("com.heytap.themestore")
                key = if (isHeytap) "com.heytap.themestore" else "com.oplus.themestore"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.unlock_themestore_vip))
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_xposed_to_themeStore, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.heytap.market"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.remove_market_splash_page_app_recommend),
                    getString(R.string.remove_market_update_page_app_recommend)
                )
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusMarket, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.heytap.cloud"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.remove_network_limit))
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_xposed_to_cloudService, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.oplus.ota"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.unlock_local_upgrade),
                    getString(R.string.restore_ota_update_verity)
                )
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusOta, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.heytap.pictorial"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.remove_image_save_watermark),
                    getString(R.string.remove_video_save_watermark)
                )
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusPictorial, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.oplus.gesture"
                setPrefsIconRes(key) { resource, show ->
                    icon = fixIconSize(resource)
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.enable_volume_key_control_flashlight),
                    getString(R.string.force_enable_aon_gestures)
                )
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusGesture, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.heytap.speechassist"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.force_enable_xiaobu_call))
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusSpeechAssist, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.coloros.directui"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.remove_app_recommend_card),
                )
                isVisible = context.checkPackName(key) && context.checkPackName(
                    "com.coloros.colordirectservice"
                )
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusBreenoTouch, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.heytap.quicksearchbox"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.remove_app_recommend_card),
                )
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusSearchBox, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.coloros.weather2"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.remove_ads_from_weather))
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusWeather, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.coloros.calendar"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.remove_holiday_page_feed),
                    getString(R.string.remove_almanac_page_feed)
                )
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusCalendar, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.coloros.smartsidebar"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.enable_run_in_background),
                    getString(R.string.unlock_transfer_station)
                )
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusSmartSidebar, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.coloros.phonemanager"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.remove_payment_protection_virus_dialog),
                    getString(R.string.remove_virus_risk_notification)
                )
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusPhoneManager, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.heytap.health"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.remove_root_detection_dialog))
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusHealth, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.coloros.soundrecorder"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.enable_third_party_call_recording))
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusSoundRecorder, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.oplus.eyeprotect"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.enable_eyeprotect_paper_texture_support))
                isVisible = SDK >= A13 && context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusEyeProtect, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.oplus.beaconlink"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.remove_beacon_link_time_limit))
                isVisible = SDK >= A13 && context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusBeaconLink, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.android.nfc"
                setPrefsIconRes(key) { resource, show ->
                    icon = fixIconSize(resource)
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.scan_nfc_tag_auto_click_button))
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusNfc, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.coloros.oshare"
                setPrefsIconRes(key) { resource, show ->
                    icon = fixIconSize(resource)
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.remove_oshare_close_countdown))
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusOShare, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.android.permissioncontroller"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.unlock_default_desktop_limit),
                    getString(R.string.remove_storage_permission_exception_dialog)
                )
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusPermissionController, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.oplus.linker"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.force_enable_iphone_shared_support))
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusLinker, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.oplus.securitypermission"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.disable_malicious_app_intercept),
                    getString(R.string.enable_always_allow_app_start_dialog)
                )
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusSecuritypPermission, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.coloros.filemanager"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(
                    getString(R.string.remove_file_save_word_limit),
                    getString(R.string.remove_rename_file_word_limit)
                )
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusFileManager, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.oplus.engineermode"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.unlock_hidden_options))
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusEngineerMode, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.heytap.mydevices"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.force_enable_fn_nas))
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusMyDevices, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.heytap.mcs"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.custom_system_message_region_preset))
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_oplusMcs, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.ruet_cse_1503050.ragib.appbackup.pro"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.remove_pro_license))
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_xposed_to_alphaBackupPro, title)
                    true
                }
            },
            Preference(context).apply {
                key = "ru.kslabs.ksweb"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.remove_pro_license))
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_ksWeb, title)
                    true
                }
            },
            Preference(context).apply {
                key = "com.dv.adm"
                setPrefsIconRes(key) { resource, show ->
                    icon = resource
                    isIconSpaceReserved = show
                }
                title = context.getAppLabel(key)
                summary = arraySummaryDot(getString(R.string.adm_unlock_pro))
                isVisible = context.checkPackName(key)
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_function_to_ADM, title)
                    true
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        init()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.add(0, 1, 0, getString(R.string.menu_search)).apply {
            setIcon(R.drawable.ic_baseline_search_24)
            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            if (ThemeUtils.isNightMode(resources.configuration)) {
                iconTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
        menu.add(0, 2, 0, getString(R.string.menu_reboot)).apply {
            setIcon(R.drawable.ic_baseline_refresh_24)
            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            if (ThemeUtils.isNightMode(resources.configuration)) {
                iconTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
        menu.add(0, 3, 0, getString(R.string.menu_versioninfo)).apply {
            setIcon(R.drawable.ic_baseline_extension_24)
            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            if (ThemeUtils.isNightMode(resources.configuration)) {
                iconTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == 1) {
            showSearchDialog()
        }
        if (menuItem.itemId == 2) (activity as MainActivity).restartMain()
        if (menuItem.itemId == 3) requireActivity().bottomSheet()
        return true
    }

    private data class SearchItem(
        val title: String,
        val summary: String,
        val category: String,
        val icon: Drawable?,
        val navAction: Int,
        val navTitle: String,
        val prefKey: String?
    )

    private val scopeFragmentClasses: List<Pair<String, Class<*>>> by lazy {
        listOf(
            "android" to Android::class.java,
            "StatusBar" to StatusBar::class.java,
            "com.android.launcher" to Launcher::class.java,
            "com.oplus.aod" to Aod::class.java,
            "LockScreen" to LockScreen::class.java,
            "com.android.packageinstaller" to Application::class.java,
            "Miscellaneous" to Miscellaneous::class.java,
            "com.oplus.screenshot" to Screenshot::class.java,
            "com.oplus.battery" to Battery::class.java,
            "com.coloros.alarmclock" to OplusAlarmClock::class.java,
            "com.android.settings" to Settings::class.java,
            "com.oplus.wirelesssettings" to OplusWirelessSettings::class.java,
            "com.android.incallui" to OplusTeleService::class.java,
            "com.android.mms" to OplusMMS::class.java,
            "com.heytap.browser" to OplusBrowser::class.java,
            "com.oplus.camera" to Camera::class.java,
            "com.coloros.gallery3d" to OplusGallery::class.java,
            "com.oplus.games" to OplusGames::class.java,
            "com.heytap.themestore" to ThemeStore::class.java,
            "com.heytap.market" to OplusMarket::class.java,
            "com.heytap.cloud" to CloudService::class.java,
            "com.oplus.ota" to OplusOta::class.java,
            "com.heytap.pictorial" to OplusPictorial::class.java,
            "com.oplus.gesture" to OplusGesture::class.java,
            "com.heytap.speechassist" to OplusSpeechAssist::class.java,
            "com.coloros.directui" to OplusBreenoTouch::class.java,
            "com.heytap.quicksearchbox" to OplusSearchBox::class.java,
            "com.coloros.weather2" to OplusWeather::class.java,
            "com.coloros.calendar" to OplusCalendar::class.java,
            "com.coloros.smartsidebar" to OplusSmartSidebar::class.java,
            "com.coloros.phonemanager" to OplusPhoneManagerUI::class.java,
            "com.heytap.health" to OplusHealth::class.java,
            "com.coloros.soundrecorder" to OplusSoundRecorder::class.java,
            "com.oplus.eyeprotect" to OplusEyeProtect::class.java,
            "com.oplus.beaconlink" to OplusBeaconLink::class.java,
            "com.android.nfc" to OplusNfc::class.java,
            "com.coloros.oshare" to OplusOShare::class.java,
            "com.android.permissioncontroller" to OplusPermissionControllerUI::class.java,
            "com.oplus.linker" to OplusLinker::class.java,
            "com.oplus.securitypermission" to OplusSecuritypPermission::class.java,
            "com.coloros.filemanager" to OplusFileManager::class.java,
            "com.oplus.engineermode" to OplusEngineerMode::class.java,
            "com.heytap.mydevices" to OplusMyDevices::class.java,
            "com.heytap.mcs" to OplusMcs::class.java,
            "com.ruet_cse_1503050.ragib.appbackup.pro" to AlphaBackupPro::class.java,
            "ru.kslabs.ksweb" to KsWeb::class.java,
            "com.dv.adm" to ADM::class.java
        )
    }

    private fun collectPreferences(group: androidx.preference.PreferenceGroup, items: MutableList<SearchItem>, category: String, navAction: Int) {
        for (i in 0 until group.preferenceCount) {
            val pref = group.getPreference(i)
            if (pref is androidx.preference.PreferenceCategory) {
                collectPreferences(pref, items, category, navAction)
            } else if (pref.isVisible) {
                val title = pref.title?.toString() ?: continue
                val summary = pref.summary?.toString() ?: ""
                items.add(SearchItem(title, summary, category, pref.icon, navAction, category, pref.key))
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun showSearchDialog() {
        val context = requireContext()
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_search_result, null)
        val dialog = MaterialAlertDialogBuilder(context).apply {
            setView(view)
        }.create()
        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val editText = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.search_edit_text)
        val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.search_result_list)

        val allItems = mutableListOf<SearchItem>()
        scopeFragmentClasses.forEach { (key, clazz) ->
            val parentPref = preferenceScreen?.findPreference<Preference>(key)
                ?: preferenceScreen?.findPreference<Preference>(clazz.simpleName)
            if (parentPref != null && !parentPref.isVisible) return@forEach

            try {
                val frag = clazz.getDeclaredConstructor().newInstance() as BaseScopePreferenceFeagment
                val catTitle = parentPref?.title?.toString() ?: clazz.simpleName
                val navAction = frag.navAction
                if (navAction != 0) {
                    for (pref in frag.h0(context)) {
                        if (pref is androidx.preference.PreferenceCategory) continue
                        if (!pref.isVisible) continue
                        val title = pref.title?.toString() ?: continue
                        val summary = pref.summary?.toString() ?: ""
                        allItems.add(SearchItem(title, summary, catTitle, pref.icon, navAction, catTitle, pref.key))
                    }
                }
            } catch (_: Throwable) {
            }
        }

        val adapter = SearchResultAdapter(emptyList()) { item ->
            dialog.dismiss()
            val bundle = Bundle().apply {
                putCharSequence("title_label", item.navTitle)
                putString("search_pref_title", item.title)
                item.prefKey?.let { putString("search_pref_key", it) }
            }
            navigatePage(item.navAction, bundle)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val keyword = s?.toString()?.trim()?.lowercase() ?: ""
                val filtered = if (keyword.isBlank()) emptyList()
                else allItems.filter {
                    it.title.lowercase().contains(keyword) ||
                        it.summary.lowercase().contains(keyword) ||
                        it.category.lowercase().contains(keyword)
                }
                adapter.updateData(filtered)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
        editText.requestFocus()
    }

    private inner class SearchResultAdapter(
        private var items: List<SearchItem>,
        private val onClick: (SearchItem) -> Unit
    ) : RecyclerView.Adapter<SearchResultAdapter.ViewHolder>() {

        fun updateData(newItems: List<SearchItem>) {
            items = newItems
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_search_result_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.bind(item, onClick)
        }

        override fun getItemCount() = items.size

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val icon: com.google.android.material.imageview.ShapeableImageView = view.findViewById(R.id.item_icon)
            private val title: android.widget.TextView = view.findViewById(R.id.item_title)
            private val summary: android.widget.TextView = view.findViewById(R.id.item_summary)
            private val category: android.widget.TextView = view.findViewById(R.id.item_category)

            fun bind(item: SearchItem, onClick: (SearchItem) -> Unit) {
                if (item.icon != null) {
                    icon.visibility = View.VISIBLE
                    icon.setImageDrawable(item.icon)
                } else {
                    icon.visibility = View.GONE
                }
                title.text = item.title
                if (item.summary.isNotBlank()) {
                    summary.visibility = View.VISIBLE
                    summary.text = item.summary
                } else {
                    summary.visibility = View.GONE
                }
                category.text = item.category
                itemView.setOnClickListener { onClick(item) }
            }
        }
    }

    private fun Context.bottomSheet() {
        scopeLife {
            val xposedScope = resources.getStringArray(R.array.xposed_scope)
            Arrays.sort(xposedScope)
            var str = getString(R.string.scope_version_info)
            xposedScope.forEach {
                val arrayList = getAppVersion(it)
                if (arrayList.isEmpty()) return@forEach
                str += "\n\n${getAppLabel(it)} - $it - ${arrayList[0]}(${arrayList[1]})[${arrayList[2]}]"
            }
            val nestedScrollView = NestedScrollView(this@bottomSheet).apply {
                setPadding(10.dp, 20.dp, 10.dp, 20.dp)
                addView(TextView(context).apply {
                    textSize = 16F
                    text = str
                })
            }
            val bottomSheetDialog = BottomSheetDialog(this@bottomSheet)
            bottomSheetDialog.setContentView(nestedScrollView)
            bottomSheetDialog.show()
        }
    }
}
