package com.fosstool.app.ui.fragment

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.preference.DropDownPreference
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import com.fosstool.app.R
import com.fosstool.app.ui.fragment.base.BaseScopePreferenceFeagment
import com.highcapable.yukihookapi.hook.factory.dataChannel
import com.luckyzyx.colorpicker.ColorPickerPreference
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.checkPackName
import com.fosstool.app.utils.getBoolean
import com.fosstool.app.utils.getUsers
import com.fosstool.app.utils.openApp

class OplusAlarmClock : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusAlarmClock
    override val scopes = arrayOf("com.coloros.alarmclock")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.alarm_clock_widget_red_one_mode)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "alarmclock_widget_redone_mode"
                entries = ctx.resources.getStringArray(R.array.statusbar_control_center_clock_red_one_mode_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.coloros.alarmclock").put(key, newValue)
                    true
                }
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class OplusBeaconLink : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusBeaconLink
    override val scopes = arrayOf("com.oplus.beaconlink")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_beacon_link_time_limit)
                key = "remove_beacon_link_time_limit"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class OplusCalendar : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusCalendar
    override val scopes = arrayOf("com.coloros.calendar")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_holiday_page_feed)
                key = "remove_holiday_page_information_flow"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_almanac_page_feed)
                key = "remove_almanac_page_information_flow"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_constellation_page_feed)
                key = "remove_horoscope_page_information_flow"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class OplusEngineerMode : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusEngineerMode
    override val scopes = arrayOf("com.oplus.engineermode")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.unlock_hidden_options)
                key = "unlock_some_hidden_options"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class OplusEyeProtect : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusEyeProtect
    override val scopes = arrayOf("com.oplus.eyeprotect")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_eyeprotect_paper_texture_support)
                summary = ctx.getString(R.string.need_restart_system)
                key = "enable_eyeprotect_paper_texture_support"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class OplusFileManager : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusFileManager
    override val scopes = arrayOf("com.coloros.filemanager")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_file_save_word_limit)
                key = "remove_word_limit_for_saving_files"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_compress_file_word_limit)
                key = "remove_word_limit_for_compress_files"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_rename_file_word_limit)
                key = "remove_word_limit_for_label_name_files"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class OplusHealth : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusHealth
    override val scopes = arrayOf("com.heytap.health")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_root_detection_dialog)
                key = "remove_health_root_check_dialog"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class OplusLinker : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusLinker
    override val scopes = arrayOf("com.android.contacts", "com.android.bluetooth")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_enable_iphone_shared_support)
                summary = ctx.getString(R.string.need_restart_system)
                key = "force_enable_iphone_shared_support"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class OplusMcs : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusMcs
    override val scopes = arrayOf("com.heytap.mcs")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.custom_system_message_region_preset)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "custom_system_message_region_defaults"
                entries = arrayOf(ctx.getString(R.string.common_words_default), "CN", "IN", "US")
                entryValues = arrayOf("", "CN", "IN", "US")
                setDefaultValue("")
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class OplusMyDevices : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusMyDevices
    override val scopes = arrayOf("com.heytap.mydevices")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_enable_fn_nas)
                key = "force_enable_feiniu_cloud_nas_option"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class OplusNfc : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusNfc
    override val scopes = arrayOf("com.android.nfc")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.scan_nfc_tag_auto_click_button)
                summary = ctx.getString(R.string.need_restart_system)
                key = "scan_nfc_tag_auto_click"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
}

class OplusOShare : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusOShare
    override val scopes = arrayOf("com.coloros.oshare")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_oshare_close_countdown)
                key = "remove_oshare_close_countdown"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class OplusPermissionControllerUI : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusPermissionController
    override val scopes = arrayOf("com.android.permissioncontroller")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.unlock_default_desktop_limit)
                key = "unlock_default_desktop_limit"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_storage_permission_exception_dialog)
                key = "remove_storage_permission_exception_dialog"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
}

class OplusPhoneManagerUI : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusPhoneManager
    override val scopes = arrayOf("com.coloros.phonemanager", "com.coloros.securepay")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_virus_risk_notification)
                key = "remove_virus_risk_notification_in_phone_manager"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_secure_pay_found_virus_dialog)
                key = "remove_secure_pay_found_virus_dialog"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_virus_whitelist_countdown)
                key = "remove_countdown_add_virus_app_whitelist"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class OplusSecuritypPermission : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusSecuritypPermission
    override val scopes = arrayOf("com.oplus.securitypermission")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_malicious_app_intercept)
                summary = ctx.getString(R.string.need_restart_system)
                key = "disable_malicious_app_intercept"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.use_old_version_app_jump_dialog)
                key = "app_start_dialog_use_old_version"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_always_allow_app_start_dialog)
                summary = ctx.getString(R.string.need_restart_system)
                key = "enable_always_allow_app_start_dialog"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.remove_always_allow_app_start_list)
                key = "remove_always_allow_app_start_list"
                isVisible = ctx.getBoolean(com.fosstool.app.utils.ModulePrefs, "enable_always_allow_app_start_dialog", false)
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    runCatching {
                        val userIds = ArrayList(getUsers().mapNotNull { it.toIntOrNull() })
                        if (userIds.isEmpty()) userIds.add(0)
                        ctx.dataChannel("android").put("remove_always_allow_app_start_list", userIds)
                        Toast.makeText(ctx, R.string.common_words_remove, Toast.LENGTH_SHORT).show()
                    }
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.auto_unlock_app_permission_management_limit)
                key = "auto_unlock_app_ecm_permission_restrict"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
}

class OplusSmartSidebar : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusSmartSidebar
    override val scopes = arrayOf("com.coloros.smartsidebar")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_enable_sidebar_auto_hide)
                key = "force_enable_buoy_automatically_hides"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.unlock_transfer_station)
                key = "unlock_transfer_dock"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.unlock_recent_files)
                key = "unlock_recent_files"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_run_in_background)
                key = "enable_run_in_background"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class OplusSoundRecorder : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusSoundRecorder
    override val scopes = arrayOf("com.coloros.soundrecorder", "com.oplus.audiomonitor")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_third_party_call_recording)
                key = "enable_record_calls_on_third_party_apps"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.expand_voip_recorder_whitelist)
                key = "expand_voip_recorder_whitelist"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class OplusSpeechAssist : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusSpeechAssist
    override val scopes = arrayOf("com.heytap.speechassist")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_enable_xiaobu_call)
                key = "force_enable_ai_speechassist_call"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class OplusTeleService : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusTeleService
    override val scopes = arrayOf("com.android.phone", "com.android.incallui")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_display_5g_switch)
                key = "force_display_five_g_switch"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_display_volte_hd_call)
                key = "force_display_volte_calls"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_display_preferred_network_type)
                key = "force_display_preferred_network_type"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
}

class OplusWirelessSettings : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusWirelessSettings
    override val scopes = arrayOf("com.oplus.wirelesssettings", "com.android.bluetooth")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_wifi_detail_show_gateway)
                key = "enable_wifi_details_display_gateway"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class SoundRelated : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_miscellaneous_to_soundRelated
    override val scopes = arrayOf("com.android.systemui", "com.android.settings")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_headphone_high_volume_warning)
                summary = ctx.getString(R.string.disable_headphone_high_volume_warning_summary)
                key = "disable_headphone_high_volume_warning"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SeekBarPreference(ctx).apply {
                title = ctx.getString(R.string.media_volume_level)
                summary = ctx.getString(R.string.media_volume_level_summary)
                key = "media_volume_level"
                setDefaultValue(0)
                max = 50
                min = 0
                showSeekBarValue = true
                updatesContinuously = false
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_super_volume_mode)
                key = "enable_super_volume_mode"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_call_super_volume_mode)
                key = "enable_super_volume_mode_for_calls"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.volume_level_min_zero)
                key = "minimum_volume_level_can_be_zero"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_app_specific_media_volume)
                summary = ctx.getString(R.string.enable_app_specific_media_volume_summary)
                key = "enable_app_specific_media_volume"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_volume_bar_thickness)
                key = "disable_volume_bar_thickness"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.set_volume_bar_display_position)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "set_volume_bar_display_position"
                entries = ctx.resources.getStringArray(R.array.set_volume_bar_display_position_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SeekBarPreference(ctx).apply {
                title = ctx.getString(R.string.custom_volume_dialog_background_transparency)
                key = "custom_volume_dialog_background_transparency"
                setDefaultValue(-1)
                max = 10
                min = -1
                showSeekBarValue = true
                updatesContinuously = false
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_volume_bar_percent_display)
                key = "enable_volume_bar_percent_display"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            if (ctx.getBoolean(ModulePrefs, "enable_volume_bar_percent_display", false)) {
                add(ColorPickerPreference(ctx).apply {
                    title = ctx.getString(R.string.custom_volume_bar_percent_color)
                    key = "custom_volume_bar_percent_color"
                    setDefaultValue(-1)
                    isIconSpaceReserved = false
                })
            }
        }
    }
    override fun isEnableRestartMenu(): Boolean = true
}
