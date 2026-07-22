package com.fosstool.app.ui.fragment

import android.content.Intent
import android.os.Build
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.ArraySet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.preference.DropDownPreference
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import androidx.recyclerview.widget.LinearLayoutManager
import com.drake.net.utils.scopeLife
import com.drake.net.utils.withDefault
import com.fosstool.app.databinding.DialogAppSelectorBinding
import com.fosstool.app.ui.adapter.AppSelectorAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.highcapable.yukihookapi.hook.factory.dataChannel
import com.fosstool.app.R
import com.fosstool.app.ui.activity.MainActivity
import com.fosstool.app.ui.fragment.base.BaseScopePreferenceFeagment
import com.fosstool.app.utils.A11
import com.fosstool.app.utils.A12
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.A15
import com.fosstool.app.utils.AppInfo
import com.fosstool.app.utils.FileUtils
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.PackageUtils
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.ShellUtils
import com.fosstool.app.utils.arraySummaryDot
import com.fosstool.app.utils.arraySummaryLine
import com.fosstool.app.utils.checkPackName
import com.fosstool.app.utils.checkResolveActivity
import com.fosstool.app.utils.dialogCentered
import com.fosstool.app.utils.formatDate
import com.fosstool.app.utils.getAppLabel
import com.fosstool.app.utils.getBoolean
import com.fosstool.app.utils.getOSVersionCode
import com.fosstool.app.utils.getString
import com.fosstool.app.utils.getStringSet
import com.fosstool.app.utils.isZh
import com.fosstool.app.utils.jumpBattery
import com.fosstool.app.utils.jumpGesture
import com.fosstool.app.utils.jumpOTA
import com.fosstool.app.utils.jumpPictorial
import com.fosstool.app.utils.navigatePage
import com.fosstool.app.utils.openApp
import com.fosstool.app.utils.putString
import com.fosstool.app.utils.putStringSet
import com.fosstool.app.utils.replaceBlankLine
import com.fosstool.app.utils.toast
import com.luckyzyx.colorpicker.ColorPickerPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Android : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_xposed_to_android
    override val scopes = arrayOf("android")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_gms_usage_restrictions)
                summary = ctx.getString(R.string.need_restart_system)
                key = "remove_gms_usage_restrictions"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getBoolean(ModulePrefs, "remove_gms_usage_restrictions", false)) {
                add(EditTextPreference(ctx).apply {
                    title = ctx.getString(R.string.custom_remote_provisioning_hostname)
                    summary = ctx.getString(R.string.custom_remote_provisioning_hostname_summary)
                    key = "custom_remote_provisioning_hostname"
                    setDefaultValue("remoteprovisioning.grapheneos.org")
                    isIconSpaceReserved = false
                })
            }
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.replace_system_root_state_detection)
                summary = ctx.getString(R.string.need_restart_system)
                key = "replace_system_root_state_detection"
                setDefaultValue(false)
                isVisible = SDK >= A12
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.allow_untrusted_touch)
                key = "allow_untrusted_touch"
                setDefaultValue(false)
                isVisible = SDK >= A12
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_keep_notification_when_app_stop)
                summary = ctx.getString(R.string.need_restart_system)
                key = "enable_keep_notification_when_app_stop"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_long_press_home_key_start_speech_asssist)
                summary = ctx.getString(R.string.need_restart_system)
                key = "disable_long_press_home_key_start_speech_asssist"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.custom_gaussian_blur_level)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "customized_gaussian_blur_effect_level"
                entries = arrayOf(ctx.getString(R.string.common_words_default), "0", "1", "2", "3")
                entryValues = arrayOf("-1", "0", "1", "2", "3")
                setDefaultValue("-1")
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            add(PreferenceCategory(ctx).apply {
                title = "LTPO"
                key = "OplusLTPO"
                isIconSpaceReserved = false
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.set_ltpo_refresh_rate_mode)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "set_ltpo_refresh_rate_mode"
                entries = arrayOf(ctx.getString(R.string.common_words_default), "LTPO", "OplusLTPO")
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getString(ModulePrefs, "set_ltpo_refresh_rate_mode", "0") == "1") {
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.enable_full_brightness_min_refresh_1)
                    summary = ctx.getString(R.string.enable_full_brightness_min_refresh_1_summary)
                    key = "enable_full_brightness_min_refresh_1"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
            }
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_dynamic_refresh_rate)
                summary = ctx.getString(R.string.disable_dynamic_refresh_rate_summary)
                key = "disable_dynamic_refresh_rate"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }
}

class StatusBar : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_xposed_to_statusBar
    override val scopes =
        arrayOf("com.android.systemui", "com.oplus.battery", "com.coloros.phonemanager")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.StatusBarClock)
                summary = arraySummaryDot(
                    ctx.getString(R.string.statusbar_clock_show_second),
                    ctx.getString(R.string.statusbar_clock_show_doublerow),
                    ctx.getString(
                        R.string.statusbar_clock_doublerow_fontsize
                    )
                )
                key = "StatusBarClock"
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_statusBar_to_statusBarClock, title)
                    true
                }
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.StatusBarNetWorkSpeed)
                summary = arraySummaryDot(
                    ctx.getString(R.string.enable_double_row_network_speed),
                    ctx.getString(R.string.set_network_speed)
                )
                key = "StatusBarNetWorkSpeed"
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_statusBar_to_statusBarNetWorkSpeed, title)
                    true
                }
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.StatusBarNotice)
                summary = arraySummaryDot(
                    ctx.getString(R.string.RemoveStatusBarNotifications),
                    ctx.getString(R.string.remove_notification_manager_limit)
                )
                key = "StatusBarNotice"
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_statusBar_to_statusBarNotice, title)
                    true
                }
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.StatusBarIcon)
                summary = arraySummaryDot(
                    ctx.getString(R.string.remove_mobile_data_inout),
                    ctx.getString(R.string.remove_green_dot_privacy_prompt)
                )
                key = "StatusBarIcon"
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_statusBar_to_statusBarIcon, title)
                    true
                }
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.StatusBarControlCenter)
                summary = arraySummaryDot(
                    ctx.getString(R.string.control_center_clock_show_second),
                    ctx.getString(R.string.remove_control_center_clock_red_one)
                )
                key = "StatusBarControlCenter"
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_statusBar_to_statusBarControlCenter, title)
                    true
                }
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.StatusBarTiles)
                summary = arraySummaryDot(
                    ctx.getString(R.string.long_press_wifi_tile_open_the_page),
                    ctx.getString(R.string.fix_tile_align_both_sides)
                )
                key = "StatusBarTiles"
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_statusBar_to_statusBarTiles, title)
                    true
                }
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.StatusBarLayout)
                summary = arraySummaryDot(
                    ctx.getString(R.string.statusbar_layout_mode),
                    ctx.getString(R.string.statusbar_layout_compatible_mode)
                )
                key = "StatusBarLayout"
                isIconSpaceReserved = false
                isVisible = SDK == A13
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_statusBar_to_statusBarLayout, title)
                    true
                }
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.StatusBarBattery)
                summary = arraySummaryDot(
                    ctx.getString(R.string.remove_statusbar_battery_percent),
                    ctx.getString(R.string.use_user_typeface)
                )
                key = "StatusBarBattery"
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_statusBar_to_statusBarBattery, title)
                    true
                }
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.StatusbarEvents)
                key = "StatusbarEvents"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.statusbar_double_click_lock_screen)
                key = "statusbar_double_click_lock_screen"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.vibrate_when_opening_the_statusbar)
                key = "vibrate_when_opening_the_statusbar"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.set_click_statusbar_scroll_to_top_mode)
                summary = arraySummaryLine(
                    ctx.getString(R.string.common_words_current_mode) + ": %s",
                    ctx.getString(R.string.need_restart_system)
                )
                key = "set_click_statusbar_scroll_to_top_mode"
                entries =
                    ctx.resources.getStringArray(R.array.set_click_statusbar_scroll_to_top_mode_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            if (SDK >= A13) {
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.custom_music_fluid_cloud_whitelist)
                    key = "custom_music_fluid_cloud_whitelist"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, _ ->
                        (activity as MainActivity).restart()
                        true
                    }
                })
                if (ctx.getBoolean(ModulePrefs, "custom_music_fluid_cloud_whitelist", false)) {
                    add(SwitchPreference(ctx).apply {
                        title = ctx.getString(R.string.disable_music_fluid_cloud_display)
                        key = "disable_music_fluid_cloud_display"
                        setDefaultValue(false)
                        isIconSpaceReserved = false
                    })
                    add(Preference(ctx).apply {
                        title = ctx.getString(R.string.set_custom_music_fluid_cloud_whitelist)
                        key = "set_custom_music_fluid_cloud_whitelist"
                        summary = (ctx.getStringSet(
                            ModulePrefs, "set_custom_music_fluid_cloud_whitelist", ArraySet()
                        ) ?: emptySet()).toString()
                        isVisible = !ctx.getBoolean(
                            ModulePrefs, "disable_music_fluid_cloud_display", false
                        )
                        isIconSpaceReserved = false
                        setOnPreferenceClickListener {
                            showMusicFluidCloudWhitelistDialog()
                            true
                        }
                    })
                    add(SwitchPreference(ctx).apply {
                        title = ctx.getString(R.string.disable_music_fluid_cloud_blacklist)
                        key = "disable_music_fluid_cloud_blacklist"
                        setDefaultValue(false)
                        isVisible = SDK >= 35
                        isIconSpaceReserved = false
                    })
                }
            }
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_enable_media_music_fluid_cloud_ripple)
                key = "force_enable_media_music_fluid_cloud_ripple"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true

    private fun showMusicFluidCloudWhitelistDialog() {
        val ctx = requireContext()
        val dialogBinding = DialogAppSelectorBinding.inflate(LayoutInflater.from(ctx))
        val dialog = MaterialAlertDialogBuilder(ctx, dialogCentered).apply {
            setTitle(R.string.set_custom_music_fluid_cloud_whitelist)
            setView(dialogBinding.root)
            setPositiveButton(android.R.string.ok) { _, _ ->
                val adapter = dialogBinding.recyclerView.adapter as? AppSelectorAdapter
                val selected = adapter?.getSelected() ?: emptyList()
                val newSet = ArraySet<String>()
                selected.forEach { newSet.add(it) }
                ctx.putStringSet(ModulePrefs, "set_custom_music_fluid_cloud_whitelist", newSet)
                findPreference<Preference>("set_custom_music_fluid_cloud_whitelist")?.summary =
                    newSet.toString()
                (activity as? MainActivity)?.restart()
            }
            setNegativeButton(android.R.string.cancel, null)
            create()
        }.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        loadMusicFluidCloudAppList(ctx, dialogBinding, dialog)
    }

    private fun loadMusicFluidCloudAppList(
        ctx: Context,
        binding: DialogAppSelectorBinding,
        dialog: androidx.appcompat.app.AlertDialog
    ) {
        binding.swipeRefreshLayout.isRefreshing = true
        binding.searchViewLayout.isEnabled = false
        lifecycleScope.launch(Dispatchers.IO) {
            val packageManager = ctx.packageManager
            val appInfos = PackageUtils(packageManager).getInstalledApplications(0)
            val appList = ArrayList<AppInfo>()
            for (info in appInfos) {
                appList.add(
                    AppInfo(
                        info.loadIcon(packageManager),
                        info.loadLabel(packageManager),
                        info.packageName,
                    )
                )
            }
            appList.sortBy { it.appName.toString().lowercase() }
            val current = ctx.getStringSet(
                ModulePrefs, "set_custom_music_fluid_cloud_whitelist", ArraySet()
            ) ?: emptySet()
            withContext(Dispatchers.Main) {
                val adapter = AppSelectorAdapter(ctx, appList) { selected ->
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.isEnabled =
                        selected.isNotEmpty()
                }
                if (current.isNotEmpty()) adapter.setSelected(current)
                binding.recyclerView.apply {
                    this.adapter = adapter
                    layoutManager = LinearLayoutManager(ctx)
                }
                binding.searchView.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        adapter.getFilter.filter(s.toString())
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })
                binding.swipeRefreshLayout.setOnRefreshListener {
                    loadMusicFluidCloudAppList(ctx, binding, dialog)
                }
                binding.swipeRefreshLayout.isRefreshing = false
                binding.searchViewLayout.isEnabled = true
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
            }
        }
    }
}

class StatusBarClock : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_statusBar_to_statusBarClock
    override val scopes = arrayOf("com.android.systemui")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.statusbar_clock_mode)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "statusbar_clock_mode"
                entries = ctx.resources.getStringArray(R.array.statusbar_clock_mode_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getString(ModulePrefs, "statusbar_clock_mode", "0") == "1") {
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_clock_show_year)
                    key = "statusbar_clock_show_year"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_clock_show_month)
                    key = "statusbar_clock_show_month"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_clock_show_day)
                    key = "statusbar_clock_show_day"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_clock_show_week)
                    key = "statusbar_clock_show_week"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_clock_show_period)
                    key = "statusbar_clock_show_period"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_clock_show_double_hour)
                    key = "statusbar_clock_show_double_hour"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_clock_show_second)
                    key = "statusbar_clock_show_second"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_clock_hide_spaces)
                    key = "statusbar_clock_hide_spaces"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_clock_show_doublerow)
                    key = "statusbar_clock_show_doublerow"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, _ ->
                        (activity as MainActivity).restart()
                        true
                    }
                })
                add(DropDownPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_clock_text_alignment)
                    summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                    key = "statusbar_clock_text_alignment"
                    entries =
                        ctx.resources.getStringArray(R.array.statusbar_clock_text_alignment_entries)
                    entryValues = arrayOf("left", "center", "right")
                    setDefaultValue("center")
                    isVisible =
                        ctx.getBoolean(ModulePrefs, "statusbar_clock_show_doublerow", false)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_clock_singlerow_fontsize)
                    summary = ctx.getString(R.string.statusbar_clock_fontsize_summary)
                    key = "statusbar_clock_singlerow_fontsize"
                    setDefaultValue(0)
                    max = 18
                    min = 0
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_clock_doublerow_fontsize)
                    summary = ctx.getString(R.string.statusbar_clock_fontsize_summary)
                    key = "statusbar_clock_doublerow_fontsize"
                    setDefaultValue(0)
                    max = 10
                    min = 0
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
            }
            if (ctx.getString(ModulePrefs, "statusbar_clock_mode", "0") == "2") {
                add(EditTextPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_clock_custom_format)
                    dialogTitle = ctx.getString(R.string.statusbar_clock_custom_format)
                    summary = ctx.getString(
                        ModulePrefs, "statusbar_clock_custom_format", "HH:mm:ss"
                    )
                    dialogMessage = """
                            YYYY/MM/dd -> ${formatDate("YYYY/MM/dd")}
                            Y/M/d/E/a -> ${formatDate("Y/M/d/E/a")}
                            YY/YYYY -> ${formatDate("YY/YYYY")}
                            M/MM/MMM/MMMM/MMMMM -> ${formatDate("M/MM/MMM/MMMM/MMMMM")}
                            d/dd/ddd/dddd -> ${formatDate("d/dd/d号/dd号")}
                            E/EE/EEE/EEEE/EEEEE -> ${formatDate("E/EE/EEE/EEEE/EEEEE")}
                            h/H/k/K -> ${formatDate("h/H/k/K")}
                            HH:mm:ss -> ${formatDate("HH:mm:ss")}
                            m/mm/mmm/mmmm -> ${formatDate("m/mm/mmm/mmmm")}
                            s/ss/sss/ssss -> ${formatDate("s/ss/sss/ssss")}
                            z -> ${formatDate("z")}
                            N -> 初一
                            NN -> 二月初一
                            NNN -> 兔年二月初一
                            NNNN -> 癸卯兔年二月初一
                            FF -> 凌晨/上午/傍晚/晚上
                            GG -> 子时/丑时/寅时/卯时
                        """
.trimIndent()
                    key = "statusbar_clock_custom_format"
                    setDefaultValue("HH:mm:ss")
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        (activity as MainActivity).restart()
                        true
                    }
                })
                add(DropDownPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_clock_text_alignment)
                    summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                    key = "statusbar_clock_text_alignment"
                    entries =
                        ctx.resources.getStringArray(R.array.statusbar_clock_text_alignment_entries)
                    entryValues = arrayOf("left", "center", "right")
                    setDefaultValue("center")
                    val row = ctx.getString(
                        ModulePrefs, "statusbar_clock_custom_format", "HH:mm:ss"
                    )?.takeIf { e -> e.isNotBlank() }?.split("\n")?.size ?: 2
                    isVisible = row >= 2
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_clock_custom_fontsize)
                    summary = ctx.getString(R.string.statusbar_clock_fontsize_summary)
                    key = "statusbar_clock_custom_fontsize"
                    setDefaultValue(0)
                    max = 20
                    min = 0
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
            }
            if (ctx.getString(ModulePrefs, "statusbar_clock_mode", "0") != "0") {
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_clock_custom_minimum_width)
                    key = "statusbar_clock_custom_minimum_width"
                    setDefaultValue(0)
                    max = 100
                    min = 0
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_clock_custom_padding)
                    key = "statusbar_clock_custom_padding"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
                if (ctx.getBoolean(ModulePrefs, "statusbar_clock_custom_padding", false)) {
                    add(SeekBarPreference(ctx).apply {
                        title = ctx.getString(R.string.statusbar_clock_custom_top_padding)
                        key = "statusbar_clock_custom_top_padding"
                        setDefaultValue(0)
                        max = 50
                        min = 0
                        showSeekBarValue = true
                        updatesContinuously = false
                        isIconSpaceReserved = false
                        setOnPreferenceChangeListener { _, newValue ->
                            ctx.dataChannel("com.android.systemui").put(key, newValue)
                            true
                        }
                    })
                    add(SeekBarPreference(ctx).apply {
                        title = ctx.getString(R.string.statusbar_clock_custom_bottom_padding)
                        key = "statusbar_clock_custom_bottom_padding"
                        setDefaultValue(0)
                        max = 50
                        min = 0
                        showSeekBarValue = true
                        updatesContinuously = false
                        isIconSpaceReserved = false
                        setOnPreferenceChangeListener { _, newValue ->
                            ctx.dataChannel("com.android.systemui").put(key, newValue)
                            true
                        }
                    })
                    add(SeekBarPreference(ctx).apply {
                        title = ctx.getString(R.string.statusbar_clock_custom_left_padding)
                        key = "statusbar_clock_custom_left_padding"
                        setDefaultValue(0)
                        max = 50
                        min = 0
                        showSeekBarValue = true
                        updatesContinuously = false
                        isIconSpaceReserved = false
                        setOnPreferenceChangeListener { _, newValue ->
                            ctx.dataChannel("com.android.systemui").put(key, newValue)
                            true
                        }
                    })
                    add(SeekBarPreference(ctx).apply {
                        title = ctx.getString(R.string.statusbar_clock_custom_right_padding)
                        key = "statusbar_clock_custom_right_padding"
                        setDefaultValue(0)
                        max = 50
                        min = 0
                        showSeekBarValue = true
                        updatesContinuously = false
                        isIconSpaceReserved = false
                        setOnPreferenceChangeListener { _, newValue ->
                            ctx.dataChannel("com.android.systemui").put(key, newValue)
                            true
                        }
                    })
                }
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.use_user_typeface)
                    key = "statusbar_clock_user_typeface"
                    setDefaultValue(false)
                    isVisible = ctx.getString(ModulePrefs, "statusbar_clock_mode", "0") != "0"
                    isIconSpaceReserved = false
                })
                if (ctx.getBoolean(ModulePrefs, "statusbar_clock_user_typeface", false)) {
                    add(SwitchPreference(ctx).apply {
                        title = ctx.getString(R.string.use_bold_typeface_style)
                        key = "statusbar_clock_bold_typeface"
                        setDefaultValue(false)
                        isIconSpaceReserved = false
                    })
                }
            }
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class StatusBarNetWorkSpeed : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_statusBar_to_statusBarNetWorkSpeed
    override val scopes = arrayOf("com.android.systemui")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.set_network_speed)
                key = "set_network_speed"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.statusbar_network_layout)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "statusbar_network_layout"
                entries = ctx.resources.getStringArray(R.array.statusbar_network_layout_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.use_user_typeface)
                key = "statusbar_network_user_typeface"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            if (ctx.getString(ModulePrefs, "statusbar_network_layout", "0") != "0") {
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_network_no_second)
                    key = "statusbar_network_no_second"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_network_no_unit)
                    key = "statusbar_network_no_unit"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                if (ctx.getString(ModulePrefs, "statusbar_network_layout", "0") == "1") {
                    add(SwitchPreference(ctx).apply {
                        title = ctx.getString(R.string.statusbar_network_no_space)
                        key = "statusbar_network_no_space"
                        setDefaultValue(false)
                        isIconSpaceReserved = false
                        setOnPreferenceChangeListener { _, newValue ->
                            ctx.dataChannel("com.android.systemui").put(key, newValue)
                            true
                        }
                    })
                }
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.set_network_speed_font_size)
                    key = "set_network_speed_font_size"
                    setDefaultValue(7)
                    max = 8
                    min = 0
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.set_network_speed_padding_bottom)
                    key = "set_network_speed_padding_bottom"
                    setDefaultValue(0)
                    max = 4
                    min = 0
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                if (ctx.getString(ModulePrefs, "statusbar_network_layout", "0") == "2") {
                    add(SeekBarPreference(ctx).apply {
                        title = ctx.getString(R.string.set_network_speed_double_row_spacing)
                        key = "set_network_speed_double_row_spacing"
                        setDefaultValue(-1)
                        max = 6
                        min = -1
                        showSeekBarValue = true
                        updatesContinuously = false
                        isIconSpaceReserved = false
                        setOnPreferenceChangeListener { _, newValue ->
                            ctx.dataChannel("com.android.systemui").put(key, newValue)
                            true
                        }
                    })
                }
            }
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class StatusBarNotifyRemoval : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_statusBarNotice_to_statusBarNotifyRemoval
    override val scopes =
        arrayOf("com.android.systemui", "com.oplus.battery", "com.coloros.phonemanager")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_statusbar_top_notification)
                summary = ctx.getString(R.string.remove_statusbar_top_notification_summary)
                key = "remove_statusbar_top_notification"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_vpn_active_notification)
                summary = ctx.getString(R.string.remove_vpn_active_notification_summary)
                key = "remove_vpn_active_notification"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_statusbar_devmode)
                key = "remove_statusbar_devmode"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_charging_completed)
                key = "remove_charging_completed"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_flashlight_open_notification)
                key = "remove_flashlight_open_notification"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_app_high_battery_consumption_warning)
                summary = ctx.getString(R.string.remove_app_high_battery_consumption_warning_summary)
                key = "remove_app_high_battery_consumption_warning"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_high_performance_mode_notifications)
                key = "remove_high_performance_mode_notifications"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_do_not_disturb_mode_notification)
                key = "remove_do_not_disturb_mode_notification"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_hotspot_power_consumption_notification)
                summary = ctx.getString(R.string.remove_hotspot_power_consumption_notification_summary)
                key = "remove_hotspot_power_consumption_notification"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_smart_rapid_charging_notification)
                key = "remove_smart_rapid_charging_notification"
                setDefaultValue(false)
                isVisible = false
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_notifications_for_mute_notifications)
                key = "remove_notifications_for_mute_notifications"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_virus_risk_notification_in_phone_manager)
                key = "remove_virus_risk_notification_in_phone_manager"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_gt_mode_notification)
                key = "remove_gt_mode_notification"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class StatusBarNotify : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_statusBar_to_statusBarNotice
    override val scopes = arrayOf(
        "com.android.systemui",
        "com.oplus.battery",
        "com.coloros.phonemanager",
        "com.oplus.notificationmanager"
    )

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.RemoveStatusBarNotifications)
                summary = arraySummaryDot(
                    ctx.getString(R.string.remove_statusbar_top_notification),
                    ctx.getString(R.string.remove_statusbar_devmode)
                )
                key = "RemoveStatusBarNotifications"
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_statusBarNotice_to_statusBarNotifyRemoval, title)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.allow_long_press_notification_modifiable)
                key = "allow_long_press_notification_modifiable"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_notification_manager_limit)
                key = "remove_notification_manager_limit"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_high_volume_warning_notifications)
                key = "disable_high_volume_warning_notifications"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_global_notification_simple_banner_mode)
                key = "enable_global_notification_simple_banner_mode"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_notification_cleanup_button)
                key = "remove_notification_cleanup_button"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_small_window_reply_whitelist)
                key = "remove_small_window_reply_whitelist"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getBoolean(ModulePrefs, "remove_small_window_reply_whitelist")) {
                add(EditTextPreference(ctx).apply {
                    title = ctx.getString(R.string.set_small_window_reply_blacklist)
                    dialogTitle = title
                    summary = ctx.getString(
                        ModulePrefs, "set_small_window_reply_blacklist", "None"
                    )
                    if (summary.isNullOrBlank()) summary = "None"
                    dialogMessage = ctx.getString(R.string.set_small_window_reply_blacklist_message)
                    key = "set_small_window_reply_blacklist"
                    setDefaultValue("None")
                    isIconSpaceReserved = false
                    setOnBindEditTextListener {
                        it.setText((summary as String).replaceBlankLine)
                    }
                    setOnPreferenceChangeListener { _, newValue ->
                        val format = (newValue as String).replaceBlankLine
                        summary = format.ifBlank { "None" }
                        ctx.dataChannel("com.android.systemui").put(key, format)
                        true
                    }
                })
            }
            add(SeekBarPreference(ctx).apply {
                title = ctx.getString(R.string.custom_notification_background_transparency)
                summary = ctx.getString(R.string.force_enable_systemui_blur_feature_tips)
                key = "custom_notification_background_transparency"
                setDefaultValue(-1)
                max = 10
                min = -1
                showSeekBarValue = true
                updatesContinuously = false
                isVisible = getOSVersionCode >= 25
                isVisible = false
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class StatusBarIcon : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_statusBar_to_statusBarIcon
    override val scopes = arrayOf("com.android.systemui")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.StatusBarWIFIIcon)
                key = "StatusBarWIFIIcon"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_wifi_data_inout)
                key = "remove_wifi_data_inout"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.StatusBarMobileDataIcon)
                key = "StatusBarMobileDataIcon"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_mobile_data_inout)
                key = "remove_mobile_data_inout"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_mobile_data_type)
                key = "remove_mobile_data_type"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.hide_non_network_card_icon)
                key = "hide_non_network_card_icon"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.hide_inactive_signal_labels_gen2x2)
                key = "hide_inactive_signal_labels_gen2x2"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.hide_nosim_noservice)
                key = "hide_nosim_noservice"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.StatusBarBluetoothIcon)
                key = "StatusBarBluetoothIcon"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.hide_icon_when_bluetooth_not_connected)
                key = "hide_icon_when_bluetooth_not_connected"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.StatusBarOtherIcon)
                key = "StatusBarOtherIcon"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_high_performance_mode_icon)
                key = "remove_high_performance_mode_icon"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_statusbar_securepayment_icon)
                key = "remove_statusbar_securepayment_icon"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_green_dot_privacy_prompt)
                key = "remove_green_dot_privacy_prompt"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_green_capsule_prompt)
                summary = ctx.getString(R.string.remove_green_capsule_prompt_summary)
                key = "remove_green_capsule_prompt"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SeekBarPreference(ctx).apply {
                title = ctx.getString(R.string.custom_fluid_cloud_icon_background_transparency)
                key = "custom_fluid_cloud_icon_background_transparency"
                setDefaultValue(-1)
                max = 10
                min = -1
                showSeekBarValue = true
                updatesContinuously = false
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.StatusBarSmallIconStatus)
                key = "StatusBarSmallIconStatus"
                isVisible = SDK <= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.status_bar_icon_vertical_center)
                key = "status_bar_icon_vertical_center"
                setDefaultValue(false)
                isVisible = SDK <= A13
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class StatusBarControlCenter : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_statusBar_to_statusBarControlCenter
    override val scopes = arrayOf("com.android.systemui")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.ControlCenter_Clock_Related)
                key = "ControlCenter_Clock_Related"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.control_center_clock_show_second)
                key = "control_center_clock_show_second"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.statusbar_control_center_clock_red_one_mode)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "statusbar_control_center_clock_red_one_mode"
                entries =
                    ctx.resources.getStringArray(R.array.statusbar_control_center_clock_red_one_mode_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.statusbar_control_center_clock_colon_style)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "statusbar_control_center_clock_colon_style"
                entries =
                    ctx.resources.getStringArray(R.array.statusbar_control_center_clock_colon_style_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
                isVisible = SDK >= A13
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_control_center_date_comma)
                key = "remove_control_center_date_comma"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.statusbar_control_center_date_show_lunar)
                key = "statusbar_control_center_date_show_lunar"
                setDefaultValue(false)
                isVisible = isZh(ctx)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    (activity as MainActivity).restart()
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.statusbar_control_center_date_fix_width)
                key = "statusbar_control_center_date_fix_width"
                setDefaultValue(false)
                isVisible = SDK >= A13 && isZh(ctx)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            if (ctx.getBoolean(
                    ModulePrefs, "statusbar_control_center_date_show_lunar", false
                )
            ) {
                add(DropDownPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_control_center_date_fix_lunar_horizontal)
                    summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                    key = "statusbar_control_center_date_fix_lunar_horizontal"
                    entries =
                        ctx.resources.getStringArray(R.array.statusbar_control_center_date_fix_lunar_horizontal_entries)
                    entryValues = arrayOf("0", "1", "2")
                    setDefaultValue("0")
                    isVisible = SDK >= A13 && isZh(ctx)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
            }
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_control_center_clock_view)
                key = "remove_control_center_clock_view"
                setDefaultValue(false)
                isVisible = SDK >= A14
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.statusbar_control_center_date_disable_text_scroll)
                key = "statusbar_control_center_date_disable_text_scroll"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            if (ctx.getBoolean(
                    ModulePrefs, "statusbar_control_center_date_show_lunar", false
                )
            ) {
                add(DropDownPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_control_center_date_set_display_mode_horizontal)
                    summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                    key = "statusbar_control_center_date_set_display_mode_horizontal"
                    entries =
                        ctx.resources.getStringArray(R.array.statusbar_control_center_date_fix_lunar_horizontal_entries)
                    entryValues = arrayOf("0", "1", "2")
                    setDefaultValue("0")
                    isVisible = SDK >= A13 && isZh(ctx)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
            }

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.ControlCenterNotificationCenter)
                key = "ControlCenterNotificationCenter"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_notification_align_both_sides)
                key = "enable_notification_align_both_sides"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_notification_importance_classification)
                key = "enable_notification_importance_classification"
                setDefaultValue(false)
                isVisible = SDK < A14
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.ControlCenter_UI_Related)
                key = "ControlCenter_UI_Related"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_control_center_progress_percent_display)
                key = "enable_control_center_progress_percent_display"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getBoolean(ModulePrefs, "enable_control_center_progress_percent_display", false)) {
                add(ColorPickerPreference(ctx).apply {
                    title = ctx.getString(R.string.custom_control_center_progress_percent_color)
                    key = "custom_control_center_progress_percent_color"
                    setDefaultValue(-1)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
            }
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.set_auto_brightness_button_mode)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "set_auto_brightness_button_mode"
                entries =
                    ctx.resources.getStringArray(R.array.statusbar_control_center_auto_brightness_mode_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_control_center_user_switcher)
                key = "remove_control_center_user_switcher"
                setDefaultValue(false)
                isVisible = SDK < A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_control_center_mydevice)
                key = "remove_control_center_mydevice"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.set_control_center_search_button_mode)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "set_control_center_search_button_mode"
                entries =
                    ctx.resources.getStringArray(R.array.set_control_center_search_button_mode_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.remove_control_center_networkwarn)
                summary = arraySummaryLine(
                    ctx.getString(R.string.common_words_current_mode) + ": %s",
                    ctx.getString(R.string.remove_control_center_networkwarn_summary)
                )
                key = "remove_control_center_networkwarn"
                entries =
                    ctx.resources.getStringArray(R.array.statusbar_control_center_networkwarn_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(SeekBarPreference(ctx).apply {
                title = ctx.getString(R.string.custom_control_center_background_transparency)
                summary = ctx.getString(R.string.force_enable_systemui_blur_feature_tips)
                key = "custom_control_center_background_transparency"
                setDefaultValue(-1)
                max = 10
                min = -1
                showSeekBarValue = true
                updatesContinuously = false
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(SeekBarPreference(ctx).apply {
                title = ctx.getString(R.string.custom_control_center_silder_transparency)
                key = "custom_control_center_silder_transparency"
                setDefaultValue(-1)
                max = 10
                min = -1
                showSeekBarValue = true
                updatesContinuously = false
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_notification_background_blur_effect)
                key = "enable_notification_background_blur_effect"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_control_center_edit_button)
                key = "remove_control_center_edit_button"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_control_center_more_button)
                key = "remove_control_center_more_button"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.set_control_center_volume_seekbar_mode)
                summary = ctx.getString(R.string.set_control_center_volume_seekbar_mode_summary)
                key = "set_control_center_volume_seekbar_mode"
                entries = arrayOf(
                    ctx.getString(R.string.common_words_default),
                    ctx.getString(R.string.common_words_current_mode).let { "1" },
                    "2"
                )
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class StatusBarTiles : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_statusBar_to_statusBarTiles
    override val scopes = arrayOf("com.android.systemui")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.enable_nfc_delay_shutdown)
                key = "NfcDelayShutdown"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_nfc_delay_shutdown)
                key = "enable_nfc_delay_shutdown"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            if (ctx.getBoolean(ModulePrefs, "enable_nfc_delay_shutdown", false)) {
                add(EditTextPreference(ctx).apply {
                    title = ctx.getString(R.string.custom_nfc_delay_shutdown_time)
                    summary = ctx.getString(R.string.custom_nfc_delay_shutdown_time_summary)
                    dialogTitle = ctx.getString(R.string.custom_nfc_delay_shutdown_time)
                    key = "custom_nfc_delay_shutdown_time"
                    setDefaultValue("10M")
                    isIconSpaceReserved = false
                })
            }
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.MediaPlayer)
                key = "MediaPlayer"
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.set_media_player_display_mode)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "set_media_player_display_mode"
                entries = ctx.resources.getStringArray(R.array.set_media_player_display_mode_entries)
                entryValues = arrayOf("0", "1", "2", "3")
                setDefaultValue("0")
                isVisible = SDK >= A13
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    ctx.dataChannel("com.android.systemui")
                        .put("set_media_player_display_mode_for_tile_rows", newValue)
                    (activity as MainActivity).restart()
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.auto_expand_tile_rows_horizontal)
                summary = arraySummaryLine(
                    ctx.getString(R.string.auto_expand_tile_rows_horizontal_summary),
                    ctx.getString(R.string.auto_expand_tile_rows_horizontal_summary_2)
                )
                key = "auto_expand_tile_rows_horizontal"
                setDefaultValue(false)
                isVisible = SDK >= A14
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            if (ctx.getString(ModulePrefs, "set_media_player_display_mode") == "1") {
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.force_enable_media_toggle_button)
                    key = "force_enable_media_toggle_button"
                    setDefaultValue(false)
                    isVisible = SDK == A13
                    isIconSpaceReserved = false
                })
            }

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.control_center_custom_gaps_for_special_tile)
                key = "control_center_custom_gaps_for_special_tile"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.control_center_custom_gaps_for_special_tile)
                key = "control_center_custom_gaps_for_special_tile_switch"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getBoolean(ModulePrefs, "control_center_custom_gaps_for_special_tile_switch", false)) {
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.custom_special_tile_top_gap)
                    key = "custom_special_tile_top_gap"
                    setDefaultValue(0)
                    max = 100
                    min = 0
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.custom_special_tile_bottom_gap)
                    key = "custom_special_tile_bottom_gap"
                    setDefaultValue(0)
                    max = 100
                    min = 0
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.decrease_horizontal_brightness_bar_top_gap)
                    key = "decrease_horizontal_brightness_bar_top_gap"
                    setDefaultValue(false)
                    isVisible = SDK >= A12
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        true
                    }
                })
            }

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.TileLongClickEvent)
                key = "TileLongClickEvent"
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.restore_some_tile_long_press_event)
                summary = ctx.getString(R.string.restore_some_tile_long_press_event_summary)
                key = "restore_some_tile_long_press_event"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.TileLayoutRelated)
                key = "TileLayoutRelated"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.fix_tile_align_both_sides)
                summary = ctx.getString(R.string.fix_tile_align_both_sides_summary)
                key = "fix_tile_align_both_sides"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.restore_page_layout_row_count_for_edit_tiles)
                key = "restore_page_layout_row_count_for_edit_tiles"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SeekBarPreference(ctx).apply {
                title = ctx.getString(R.string.custom_tile_background_transparency)
                key = "custom_tile_background_transparency"
                setDefaultValue(-1)
                max = 10
                min = -1
                showSeekBarValue = true
                updatesContinuously = false
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_display_of_device_controls_tiles)
                key = "force_display_of_device_controls_tiles"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.control_center_tile_enable)
                key = "control_center_tile_enable"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getBoolean(ModulePrefs, "control_center_tile_enable", false)) {
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.tile_unexpanded_columns_vertical)
                    key = "tile_unexpanded_columns_vertical"
                    setDefaultValue(6)
                    max = 6
                    min = 1
                    showSeekBarValue = true
                    updatesContinuously = false
                    isVisible = SDK < A13
                    isIconSpaceReserved = false
                })
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.tile_unexpanded_columns_horizontal)
                    key = "tile_unexpanded_columns_horizontal"
                    setDefaultValue(6)
                    max = 8
                    min = 1
                    showSeekBarValue = true
                    updatesContinuously = false
                    isVisible = SDK < A13
                    isIconSpaceReserved = false
                })
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.tile_expanded_columns_vertical)
                    key = "tile_expanded_columns_vertical"
                    setDefaultValue(4)
                    max = 7
                    min = 1
                    showSeekBarValue = true
                    updatesContinuously = false
                    isVisible = SDK < A13
                    isIconSpaceReserved = false
                })
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.tile_expanded_columns_horizontal)
                    key = "tile_expanded_columns_horizontal"
                    setDefaultValue(6)
                    max = 9
                    min = 1
                    showSeekBarValue = true
                    updatesContinuously = false
                    isVisible = SDK < A13
                    isIconSpaceReserved = false
                })
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.tile_unexpanded_columns_vertical)
                    key = "tile_unexpanded_columns_vertical_c13"
                    setDefaultValue(5)
                    max = 6
                    min = 1
                    showSeekBarValue = true
                    updatesContinuously = false
                    isVisible = SDK >= A13
                    isIconSpaceReserved = false
                })
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.tile_expanded_rows_vertical)
                    key = "tile_expanded_rows_vertical_c13"
                    setDefaultValue(3)
                    max = 6
                    min = 1
                    showSeekBarValue = true
                    updatesContinuously = false
                    isVisible = SDK >= A13
                    isIconSpaceReserved = false
                })
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.tile_expanded_columns_vertical)
                    key = "tile_expanded_columns_vertical_c13"
                    setDefaultValue(4)
                    max = 7
                    min = 1
                    showSeekBarValue = true
                    updatesContinuously = false
                    isVisible = SDK >= A13
                    isIconSpaceReserved = false
                })
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.tile_columns_horizontal_c13)
                    key = "tile_columns_horizontal_c13"
                    setDefaultValue(5)
                    max = 6
                    min = 1
                    showSeekBarValue = true
                    updatesContinuously = false
                    isVisible = SDK >= A13
                    isIconSpaceReserved = false
                })
            }

        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class StatusBarLayout : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_statusBar_to_statusBarLayout
    override val scopes = arrayOf("com.android.systemui")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.statusbar_layout_mode)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "statusbar_layout_mode"
                entries = ctx.resources.getStringArray(R.array.statusbar_layout_mode_entries)
                entryValues = arrayOf("0", "1")
                setDefaultValue("0")
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.statusbar_layout_compatible_mode)
                key = "statusbar_layout_compatible_mode"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getBoolean(
                    ModulePrefs, "statusbar_layout_compatible_mode", false
                )
            ) {
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_layout_left_margin)
                    summary = ctx.getString(R.string.statusbar_layout_margin_tip)
                    key = "statusbar_layout_left_margin"
                    setDefaultValue(0)
                    max = 150
                    min = 0
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                })
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_layout_right_margin)
                    summary = ctx.getString(R.string.statusbar_layout_margin_tip)
                    key = "statusbar_layout_right_margin"
                    setDefaultValue(0)
                    max = 150
                    min = 0
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                })
            }
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class StatusBarBattery : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_statusBar_to_statusBarBattery
    override val scopes = arrayOf("com.android.systemui")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_statusbar_battery_percent)
                key = "remove_statusbar_battery_percent"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.use_user_typeface)
                key = "statusbar_power_user_typeface"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.statusbar_power_apply_to_battery_icon)
                summary = ctx.getString(R.string.statusbar_power_apply_to_battery_icon_summary)
                key = "statusbar_power_apply_to_battery_icon"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getBoolean(ModulePrefs, "statusbar_power_user_typeface", false)) {
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.use_bold_typeface_style)
                    key = "statusbar_power_bold_typeface"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.statusbar_power_font_size)
                    summary = ctx.getString(R.string.statusbar_clock_fontsize_summary)
                    key = "statusbar_power_font_size"
                    setDefaultValue(0)
                    max = 10
                    min = 0
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                })
            }
            if (SDK >= A12) {
                add(PreferenceCategory(ctx).apply {
                    title = ctx.getString(R.string.StatusBarBatteryNotify)
                    key = "StatusBarBatteryNotify"
                    isIconSpaceReserved = false
                })
                add(DropDownPreference(ctx).apply {
                    title = ctx.getString(R.string.battery_information_display_mode)
                    summary = arraySummaryLine(
                        ctx.getString(R.string.common_words_current_mode) + ": %s",
                        ctx.getString(R.string.battery_information_display_mode_summary)
                    )
                    key = "battery_information_display_mode"
                    entries =
                        ctx.resources.getStringArray(R.array.statusbar_battery_information_notify_entries)
                    entryValues = arrayOf("0", "1", "2")
                    setDefaultValue("0")
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, newValue ->
                        ctx.dataChannel("com.android.systemui").put(key, newValue)
                        (activity as MainActivity).restart()
                        true
                    }
                })
                if (ctx.getString(
                        ModulePrefs, "battery_information_display_mode", "0"
                    ) != "0"
                ) {
                    add(SwitchPreference(ctx).apply {
                        title = ctx.getString(R.string.battery_information_show_charge)
                        summary = ctx.getString(R.string.battery_information_show_charge_summary)
                        key = "battery_information_show_charge_info"
                        setDefaultValue(false)
                        isIconSpaceReserved = false
                        setOnPreferenceChangeListener { _, newValue ->
                            ctx.dataChannel("com.android.systemui").put(key, newValue)
                            true
                        }
                    })
                    add(DropDownPreference(ctx).apply {
                        title = ctx.getString(R.string.voltage_display_mode)
                        summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                        key = "voltage_display_mode"
                        entries = ctx.resources.getStringArray(R.array.voltage_display_mode_entries)
                        entryValues = arrayOf("0", "1", "2", "3")
                        setDefaultValue("0")
                        isVisible = SDK >= A12
                        isIconSpaceReserved = false
                        setOnPreferenceChangeListener { _, newValue ->
                            ctx.dataChannel("com.android.systemui").put(key, newValue)
                            true
                        }
                    })
                    add(SwitchPreference(ctx).apply {
                        title = ctx.getString(R.string.show_battery_health_degree)
                        key = "show_battery_health_degree"
                        setDefaultValue(false)
                        isVisible = SDK >= A12
                        isIconSpaceReserved = false
                        setOnPreferenceChangeListener { _, newValue ->
                            ctx.dataChannel("com.android.systemui").put(key, newValue)
                            true
                        }
                    })
                    add(SwitchPreference(ctx).apply {
                        title = ctx.getString(R.string.always_show_positive_current)
                        key = "always_show_positive_current"
                        setDefaultValue(false)
                        isVisible = SDK >= A12
                        isIconSpaceReserved = false
                        setOnPreferenceChangeListener { _, newValue ->
                            ctx.dataChannel("com.android.systemui").put(key, newValue)
                            true
                        }
                    })
                    add(SwitchPreference(ctx).apply {
                        title = ctx.getString(R.string.battery_information_show_simple_mode)
                        key = "battery_information_show_simple_mode"
                        setDefaultValue(false)
                        isIconSpaceReserved = false
                        setOnPreferenceChangeListener { _, newValue ->
                            ctx.dataChannel("com.android.systemui").put(key, newValue)
                            true
                        }
                    })
                    add(SwitchPreference(ctx).apply {
                        title = ctx.getString(R.string.battery_information_show_update_time)
                        summary = ctx.getString(R.string.battery_information_show_update_time_summary)
                        key = "battery_information_show_update_time"
                        setDefaultValue(false)
                        isIconSpaceReserved = false
                        setOnPreferenceChangeListener { _, newValue ->
                            ctx.dataChannel("com.android.systemui").put(key, newValue)
                            true
                        }
                    })
                }
            }
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class Launcher : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_xposed_to_launcher
    override val scopes = arrayOf("com.coloros.alarmclock", "com.android.launcher")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.alarmclock_widget_redone_mode)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "alarmclock_widget_redone_mode"
                entries =
                    ctx.resources.getStringArray(R.array.statusbar_control_center_clock_red_one_mode_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.coloros.alarmclock").put(key, newValue)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.allow_app_names_display_multiple_lines)
                key = "allow_app_names_display_multiple_lines"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            if (ctx.getBoolean(ModulePrefs, "allow_app_names_display_multiple_lines", false)) {
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.custom_app_icon_name_line_height)
                    key = "custom_app_icon_name_line_height"
                    setDefaultValue(0)
                    max = 50
                    min = 0
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                })
            }
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.AppBadgeRelated)
                key = "AppBadgeRelated"
                isIconSpaceReserved = false
            })
            if (SDK >= A13) {
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.remove_app_shortcut_badge)
                    key = "remove_app_shortcut_badge"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.remove_app_work_badge)
                    key = "remove_app_work_badge"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.remove_app_clone_badge)
                    key = "remove_app_clone_badge"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
            }
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_the_dot_after_app_update)
                key = "remove_the_dot_after_app_update"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_display_app_update_dot)
                key = "enable_display_app_update_dot"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            if (SDK >= A13) {
                add(DropDownPreference(ctx).apply {
                    title = ctx.getString(R.string.set_app_update_dot_display_mode)
                    key = "set_app_update_dot_display_mode"
                    entries =
                        ctx.resources.getStringArray(R.array.set_app_update_dot_display_mode_entries)
                    entryValues = arrayOf("0", "1", "2")
                    setDefaultValue("0")
                    isIconSpaceReserved = false
                    val mode = ctx.getString(ModulePrefs, "set_app_update_dot_display_mode", "0")
                    summary = buildString {
                        append(ctx.getString(R.string.common_words_current_mode))
                        append(": %s")
                        when (mode) {
                            "1" -> append("\n").append(ctx.getString(R.string.need_restart_system))
                            "2" -> append("\n").append(ctx.getString(R.string.need_restart_scope))
                        }
                    }
                    setOnPreferenceChangeListener { _, _ ->
                        (activity as MainActivity).restart()
                        true
                    }
                })
            }
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.FolderLayoutRelated)
                key = "FolderLayoutRelated"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_folder_preview_background)
                key = "remove_folder_preview_background"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_folder_layout_adjustment)
                key = "enable_folder_layout_adjustment"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getBoolean(ModulePrefs, "enable_folder_layout_adjustment", false)) {
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.set_folder_icon_rows)
                    key = "set_folder_icon_rows"
                    setDefaultValue(3)
                    max = 7
                    min = 3
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                })
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.set_icon_columns_in_folder)
                    key = "set_icon_columns_in_folder"
                    setDefaultValue(3)
                    max = 7
                    min = 3
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.sync_folder_columns_to_preview)
                    key = "sync_folder_columns_to_preview"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
            }
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.PaginationComponentRelated)
                key = "PaginationComponentRelated"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_pagination_component)
                key = "remove_pagination_component"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_folder_pagination_component)
                key = "remove_folder_pagination_component"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_pagination_component_sliding)
                key = "disable_pagination_component_sliding"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.RecentTaskListRelated)
                key = "RecentTaskListRelated"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_stacked_task_layout)
                key = "enable_stacked_task_layout"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getBoolean(ModulePrefs, "enable_stacked_task_layout", false)) {
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.set_task_stacking_level)
                    key = "set_task_stacking_level"
                    setDefaultValue(7)
                    max = 10
                    min = 5
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.fix_current_task_to_the_top)
                    key = "fix_current_task_to_the_top"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
            }
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.long_press_app_icon_open_app_details)
                key = "long_press_app_icon_open_app_details"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_bottom_app_icon_of_recent_task_list)
                key = "remove_bottom_app_icon_of_recent_task_list"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_recent_task_list_clear_button)
                key = "remove_recent_task_list_clear_button"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_recent_task_pin_capsule)
                summary = ctx.getString(R.string.need_restart_system)
                key = "enable_recent_task_pin_capsule"
                setDefaultValue(false)
                isVisible = getOSVersionCode >= 37
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.unlock_task_locks)
                key = "unlock_task_locks"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.allow_locking_unlocking_of_excluded_activity)
                key = "allow_locking_unlocking_of_excluded_activity"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_docker_background)
                key = "enable_docker_background"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_enable_docker_background_blur)
                key = "force_enable_docker_background_blur"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_enable_recent_task_memory_display)
                key = "force_enable_recent_task_memory_display"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_auto_close_folder)
                key = "enable_auto_close_folder"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_widgets_add_request_whitelist)
                key = "remove_widgets_add_request_whitelist"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_launcher_card_name)
                key = "remove_launcher_card_name"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_long_press_app_icon_secondary_menu)
                key = "disable_long_press_app_icon_secondary_menu"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_launcher_indicator_entry)
                key = "enable_launcher_indicator_entry"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_docker_max_number_limit)
                key = "remove_docker_max_number_limit"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_folder_name_input_limit)
                key = "remove_folder_name_input_limit"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_auto_switch_last_task)
                key = "disable_auto_switch_last_task"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(EditTextPreference(ctx).apply {
                title = ctx.getString(R.string.custom_desktop_default_home_page)
                summary = ctx.getString(R.string.custom_desktop_default_home_page_summary)
                key = "custom_desktop_default_home_page"
                setDefaultValue("0")
                isIconSpaceReserved = false
            })
            add(SeekBarPreference(ctx).apply {
                title = ctx.getString(R.string.custom_launcher_app_icon_size)
                key = "custom_launcher_app_icon_size"
                setDefaultValue(0)
                max = 100
                min = 0
                showSeekBarValue = true
                updatesContinuously = false
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_launcher_high_tempreature_protection)
                summary = ctx.getString(R.string.remove_launcher_high_tempreature_protection_summary)
                key = "remove_launcher_high_tempreature_protection"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_all_apps_support_split_screen)
                key = "force_all_apps_support_split_screen"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("android").put(key, newValue as Boolean)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_drawer_layout_adjustment)
                key = "enable_drawer_layout_adjustment"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getBoolean(ModulePrefs, "enable_drawer_layout_adjustment", false)) {
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.set_drawer_icon_columns)
                    key = "set_drawer_icon_columns"
                    setDefaultValue(4)
                    max = 8
                    min = 1
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                })
            }
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.launcher_layout_related)
                key = "DesktopLayoutRelated"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.launcher_layout_enable)
                summary = ctx.getString(R.string.launcher_layout_row_colume)
                key = "launcher_layout_enable"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getBoolean(ModulePrefs, "launcher_layout_enable", false)) {
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.launcher_layout_max_rows)
                    key = "launcher_layout_max_rows"
                    setDefaultValue(6)
                    max = 10
                    min = 1
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                })
                add(SeekBarPreference(ctx).apply {
                    title = ctx.getString(R.string.launcher_layout_max_columns)
                    key = "launcher_layout_max_columns"
                    setDefaultValue(4)
                    max = 8
                    min = 1
                    showSeekBarValue = true
                    updatesContinuously = false
                    isIconSpaceReserved = false
                })
            }
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.launcher_events)
                key = "LauncherEvents"
                isVisible = false
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class Aod : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_aod
    override val scopes = arrayOf("com.android.systemui", "com.oplus.aod", "com.oplus.uiengine")

    private val loadRandomTextFile = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            val path = FileUtils.getDocumentPath(requireActivity(), it)
            if (!path.isNullOrBlank() && path != "null") {
                requireActivity().putString(ModulePrefs, "custom_random_text_file", path)
                findPreference<Preference>("custom_random_text_file")?.summary = path
            }
        }
    }

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.AodRelated)
                key = "AodRelated"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_aod_music_whitelist)
                key = "remove_aod_music_whitelist"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_aod_notification_icon_whitelist)
                key = "remove_aod_notification_icon_whitelist"
                setDefaultValue(false)
                isVisible = SDK == A13
                isIconSpaceReserved = false
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.set_aod_style_mode)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "set_aod_style_mode"
                entries = ctx.resources.getStringArray(R.array.set_aod_style_mode_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isVisible = SDK == A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_enable_screen_off_music_support)
                summary = ctx.getString(R.string.force_enable_screen_off_music_support_summary)
                key = "force_enable_screen_off_music_support"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.set_random_text_display_mode)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "set_random_text_display_mode"
                entries = ctx.resources.getStringArray(R.array.set_random_text_display_mode_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isVisible = SDK >= A12
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (SDK >= A12) {
                val randomMode = ctx.getString(ModulePrefs, "set_random_text_display_mode", "0")
                if (randomMode == "1") {
                    add(Preference(ctx).apply {
                        title = ctx.getString(R.string.custom_random_text_file)
                        key = "custom_random_text_file"
                        summary = ctx.getString(
                            ModulePrefs, "custom_random_text_file", "Null"
                        )
                        isIconSpaceReserved = false
                        isCopyingEnabled = true
                        setOnPreferenceClickListener {
                            loadRandomTextFile.launch("text/plain")
                            true
                        }
                    })
                }
                if (randomMode == "2") {
                    add(EditTextPreference(ctx).apply {
                        title = ctx.getString(R.string.custom_random_text_api)
                        dialogTitle = ctx.getString(R.string.custom_random_text_api)
                        summary = ctx.getString(
                            ModulePrefs, "custom_random_text_api", ""
                        )
                        key = "custom_random_text_api"
                        setDefaultValue("")
                        isIconSpaceReserved = false
                    })
                }
            }
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.set_aod_typeface_mode)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "set_aod_typeface_mode"
                entries = ctx.resources.getStringArray(R.array.set_aod_typeface_mode_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isVisible = SDK >= A12
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (SDK >= A12 && ctx.getString(ModulePrefs, "set_aod_typeface_mode", "0") != "0") {
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.apply_typeface_to_aod_clock)
                    key = "apply_typeface_to_aod_clock"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
            }
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class LockScreen : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_xposed_to_lockScreen
    override val scopes = arrayOf("com.android.systemui")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.LockScreenCarrier)
                key = "LockScreenCarrier"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_statusbar_carriers)
                key = "remove_statusbar_carriers"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.statusbar_carriers_use_user_typeface)
                key = "statusbar_carriers_use_user_typeface"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(EditTextPreference(ctx).apply {
                title = ctx.getString(R.string.statusbar_custom_carrier_display_text)
                key = "statusbar_custom_carrier_display_text"
                setDefaultValue("")
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.hide_lock_screen_status_bar_display)
                key = "hide_lock_screen_status_bar_display"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.auto_wake_up_face_unlock_notification)
                key = "auto_wake_up_face_unlock_notification"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_display_clock_style_options)
                key = "force_display_clock_style_options"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_lock_screen_clock_component)
                key = "remove_lock_screen_clock_component"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.lock_screen_show_real_charging_technology)
                key = "lock_screen_show_real_charging_technology"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(EditTextPreference(ctx).apply {
                title = ctx.getString(R.string.replace_charging_technology_drawing_style)
                key = "replace_charging_technology_drawing_style"
                setDefaultValue("0")
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.LockScreenClockComponent)
                key = "LockScreenClockComponent"
                isIconSpaceReserved = false
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.lock_screen_clock_redone_mode)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "lock_screen_clock_redone_mode"
                entries =
                    ctx.resources.getStringArray(R.array.statusbar_control_center_clock_red_one_mode_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.apply_lock_screen_dual_clock_redone)
                key = "apply_lock_screen_dual_clock_redone"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.lock_screen_custom_clock_component_style)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "lock_screen_custom_clock_component_style"
                entries =
                    ctx.resources.getStringArray(R.array.lock_screen_custom_clock_component_style_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.set_lock_screen_centered)
                summary = ctx.getString(R.string.set_lock_screen_centered_summary)
                key = "set_lock_screen_centered"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.lock_screen_clock_use_user_typeface)
                key = "lock_screen_clock_use_user_typeface"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.LockScreenChargingComponent)
                key = "LockScreenChargingComponent"
                isIconSpaceReserved = false
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.set_lock_screen_warp_charging_style)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "set_lock_screen_warp_charging_style"
                entries =
                    ctx.resources.getStringArray(R.array.set_lock_screen_warp_charging_style_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isVisible = SDK == A13
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    (activity as MainActivity).restart()
                    true
                }
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.set_lock_screen_charging_text_logo_style)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "set_lock_screen_charging_text_logo_style"
                entries =
                    ctx.resources.getStringArray(R.array.set_lock_screen_charging_text_logo_style_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_lock_screen_charging_show_wattage)
                key = "force_lock_screen_charging_show_wattage"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.lock_screen_charging_use_user_typeface)
                key = "lock_screen_charging_use_user_typeface"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.set_full_screen_charging_animation_mode)
                summary = arraySummaryLine(
                    ctx.getString(R.string.common_words_current_mode) + ": %s",
                    ctx.getString(R.string.need_restart_scope)
                )
                key = "set_full_screen_charging_animation_mode"
                entries =
                    ctx.resources.getStringArray(R.array.set_full_screen_charging_animation_mode_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isVisible = getOSVersionCode in 27..29
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.LockScreenButton)
                key = "LockScreenButton"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_top_lock_screen_icon)
                key = "remove_top_lock_screen_icon"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_lock_screen_bottom_left_button)
                key = "remove_lock_screen_bottom_left_button"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    (activity as MainActivity).restart()
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.lock_screen_bottom_left_button_replace_with_flashlight)
                key = "lock_screen_bottom_left_button_replace_with_flashlight"
                setDefaultValue(false)
                isVisible = SDK < A14 && ctx.getBoolean(
                    ModulePrefs, "remove_lock_screen_bottom_left_button", false
                ) == false
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.lock_screen_switch_flashlight_auto_close_screen)
                key = "lock_screen_switch_flashlight_auto_close_screen"
                setDefaultValue(false)
                isVisible = ctx.getBoolean(
                    ModulePrefs, "remove_lock_screen_bottom_left_button", false
                ) == false
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_lock_screen_bottom_right_camera)
                key = "remove_lock_screen_bottom_right_camera"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_lock_screen_close_notification_button)
                key = "remove_lock_screen_close_notification_button"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_lock_screen_bottom_sos_button)
                summary = ctx.getString(R.string.remove_lock_screen_bottom_sos_button_summary)
                key = "remove_lock_screen_bottom_sos_button"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.LockScreenEvent)
                key = "LockScreenEvent"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_72hour_password_verification)
                summary = ctx.getString(R.string.remove_72hour_password_verification_summary)
                key = "remove_72hour_password_verification"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class Screenshot : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_xposed_to_screenshot
    override val scopes = arrayOf("com.oplus.screenshot")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_system_screenshot_delay)
                summary = ctx.getString(R.string.remove_system_screenshot_delay_summary)
                key = "remove_system_screenshot_delay"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_screenshot_privacy_limit)
                summary = ctx.getString(R.string.remove_screenshot_privacy_limit_summary)
                key = "remove_screenshot_privacy_limit"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_flag_secure)
                summary = ctx.getString(R.string.disable_flag_secure_summary)
                key = "disable_flag_secure"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_page_limit_for_long_screenshots)
                summary = ctx.getString(R.string.remove_page_limit_for_long_screenshots_summary)
                key = "remove_page_limit_for_long_screenshots"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_png_save_format)
                key = "enable_png_save_format"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_screenshot_packagename_md5_encrypt)
                key = "disable_screenshot_packagename_md5_encrypt"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class Application : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_xposed_to_application

    override val scopes = arrayOf(
        "com.oplus.battery",
        "com.oplus.safecenter",
        "com.coloros.safecenter",
        "com.android.launcher",
        "com.oppo.launcher",
        "com.android.settings",
        "com.android.packageinstaller",
        "com.android.permissioncontroller"
    )

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.AppStartupRelated)
                key = "AppStartupRelated"
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_splash_screen)
                summary = arraySummaryLine(
                    ctx.getString(R.string.need_restart_system),
                    ctx.getString(R.string.disable_splash_screen_summary)
                )
                key = "disable_splash_screen"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_preload_splash)
                summary = ctx.getString(R.string.disable_preload_splash_summary)
                key = "disable_preload_splash"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.APPRelatedList)
                key = "APPRelatedList"
                isIconSpaceReserved = false
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.custom_config_app_intent_list)
                summary = ctx.getString(R.string.custom_config_app_intent_list_summary)
                key = "custom_config_app_intent_list"
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_application_to_hideAppIntent, title)
                    true
                }
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.dark_mode_support_list)
                summary = ctx.getString(R.string.zoom_window_support_list_summary)
                key = "dark_mode_support_list"
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_application_to_darkModeFragment, title)
                    true
                }
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.set_multi_app_support_mode)
                summary = arraySummaryLine(
                    ctx.getString(R.string.common_words_current_mode) + ": %s",
                    ctx.getString(R.string.set_multi_app_support_mode_summary)
                )
                key = "set_multi_app_support_mode"
                entries = ctx.resources.getStringArray(R.array.set_multi_app_support_mode_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getString(ModulePrefs, "set_multi_app_support_mode", "0") == "1") {
                add(Preference(ctx).apply {
                    title = ctx.getString(R.string.multi_app_custom_list)
                    summary = ctx.getString(R.string.multi_app_custom_list_summary)
                    key = "multi_app_custom_list"
                    isIconSpaceReserved = false
                    setOnPreferenceClickListener {
                        navigatePage(R.id.action_application_to_multiFragment, title)
                        true
                    }
                })
            }
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_multi_app_blacklist)
                summary = ctx.getString(R.string.remove_multi_app_blacklist_summary)
                key = "remove_multi_app_blacklist"
                setDefaultValue(false)
                isVisible = SDK >= A12
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_multi_app_created_num_limit_for_users)
                summary = arraySummaryLine(
                    ctx.getString(R.string.remove_multi_app_created_num_limit_for_users_summary),
                    ctx.getString(R.string.need_restart_system)
                )
                key = "remove_multi_app_created_num_limit_for_users"
                setDefaultValue(false)
                isVisible = SDK >= A12
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_multi_app_created_num_limit_for_apps)
                key = "remove_multi_app_created_num_limit_for_apps"
                setDefaultValue(false)
                isVisible = SDK >= A12
                isIconSpaceReserved = false
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.set_wlan_sla_whitelist_mode)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "set_wlan_sla_whitelist_mode"
                entries = ctx.resources.getStringArray(R.array.set_wlan_sla_whitelist_mode_entries)
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getString(ModulePrefs, "set_wlan_sla_whitelist_mode", "0") != "0") {
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.remove_wlan_sla_blacklist)
                    key = "remove_wlan_sla_blacklist"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
                add(Preference(ctx).apply {
                    title = ctx.getString(R.string.custom_wlan_sla_whitelist)
                    key = "custom_wlan_sla_whitelist"
                    isIconSpaceReserved = false
                })
                add(Preference(ctx).apply {
                    title = ctx.getString(R.string.custom_wlan_sla_game_whitelist)
                    key = "custom_wlan_sla_game_whitelist"
                    isIconSpaceReserved = false
                })
            }
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.zoom_window_support_list)
                summary = ctx.getString(R.string.zoom_window_support_list_summary)
                key = "zoom_window_support_list"
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_application_to_zoomWindowFragment, title)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_enable_multi_window_mode)
                summary = ctx.getString(R.string.need_restart_system)
                key = "force_enable_multi_window_mode"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.AppInstallationRelated)
                summary = ctx.getString(R.string.PackageInstaller_summary)
                key = "PackageInstaller"
                isIconSpaceReserved = false
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.corepatch)
                summary = ctx.getString(R.string.corepatch_summary)
                key = "CorePatch"
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_application_to_corePatch, title)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_enable_32_bit_support)
                summary = ctx.getString(R.string.force_enable_32_bit_support_summary)
                key = "force_enable_32_bit_support"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.fix_install_button_display_exception)
                summary = ctx.getString(R.string.fix_install_button_display_exception_summary)
                key = "fix_install_button_display_exception"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_start_app_detail)
                summary = ctx.getString(R.string.disable_start_app_detail_summary)
                key = "disable_start_app_detail"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.skip_apk_scan)
                summary = ctx.getString(R.string.skip_apk_scan_summary)
                key = "skip_apk_scan"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.allow_downgrade_install)
                summary = ctx.getString(R.string.allow_downgrade_install_summary)
                key = "allow_downgrade_install"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_install_ads)
                summary = ctx.getString(R.string.remove_install_ads_summary)
                key = "remove_install_ads"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.auto_click_install_button)
                key = "auto_click_install_button"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.auto_click_uninstall_button)
                key = "auto_click_uninstall_button"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.show_more_apk_package_information)
                summary = ctx.getString(R.string.show_more_apk_package_information_summary)
                key = "show_more_apk_package_information"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.show_packagename_and_versioncode)
                summary = ctx.getString(R.string.show_packagename_and_versioncode_summary)
                key = "show_packagename_and_versioncode"
                setDefaultValue(false)
                isVisible = false
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.replase_aosp_installer)
                summary = ctx.getString(R.string.replase_aosp_installer_summary)
                key = "replase_aosp_installer"
                setDefaultValue(false)
                isVisible = false
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_adb_install_confirm)
                summary = ctx.getString(R.string.remove_adb_install_confirm_summary)
                key = "remove_adb_install_confirm"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.ApplyOtherRestrictions)
                key = "ApplyOtherRestrictions"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.unlock_startup_limit)
                summary = ctx.getString(R.string.unlock_startup_limit_summary)
                key = "unlock_startup_limit"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.AppDetailsRelated)
                key = "AppDetailsRelated"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.show_package_name_in_app_details)
                key = "show_package_name_in_app_details"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.show_sdk_version_in_app_details)
                key = "show_sdk_version_in_app_details"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.show_first_install_time_in_app_details)
                key = "show_first_install_time_in_app_details"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.show_last_update_time_in_app_details)
                key = "show_last_update_time_in_app_details"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.show_install_source_in_app_details)
                key = "show_install_source_in_app_details"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_long_press_to_copy_in_app_details)
                key = "enable_long_press_to_copy_in_app_details"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.click_icon_open_market_page)
                key = "click_icon_open_market_page"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_multi_app_quick_jump)
                key = "enable_multi_app_quick_jump"
                setDefaultValue(false)
                isVisible = SDK >= A11
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_quick_open_market_page)
                key = "enable_quick_open_market_page"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.allow_disabling_system_apps)
                summary = ctx.getString(R.string.allow_disabling_system_apps_summary)
                key = "allow_disabling_system_apps"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_app_uninstall_button_blacklist)
                key = "remove_app_uninstall_button_blacklist"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_custom_app_language)
                key = "enable_custom_app_language"
                setDefaultValue(false)
                isVisible = SDK >= A14
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class DialogRelated : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_miscellaneous_to_dialogRelated
    override val scopes = arrayOf(
        "com.android.systemui", "com.oplus.exsystemservice", "com.coloros.securepay"
    )

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_duplicate_floating_window)
                summary = ctx.getString(R.string.disable_duplicate_floating_window_summary)
                key = "disable_duplicate_floating_window"
                setDefaultValue(false)
                isVisible = getOSVersionCode >= 26
                isIconSpaceReserved = false

            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_headphone_high_volume_warning)
                summary = ctx.getString(R.string.disable_headphone_high_volume_warning_summary)
                key = "disable_headphone_high_volume_warning"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_low_battery_dialog_warning)
                summary = ctx.getString(R.string.remove_low_battery_dialog_warning_summary)
                key = "remove_low_battery_dialog_warning"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_usb_connect_dialog)
                summary = ctx.getString(R.string.remove_usb_connect_dialog_summary)
                key = "remove_usb_connect_dialog"
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
                title = ctx.getString(R.string.remove_access_device_log_dialog)
                key = "remove_access_device_log_dialog"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_start_recording_or_casting_dialog)
                key = "remove_start_recording_or_casting_dialog"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.run_floating_window_tasks_in_foreground)
                key = "run_floating_window_tasks_in_foreground"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_show_toast_icon)
                key = "force_show_toast_icon"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_accessibility_warning_dialog)
                key = "disable_accessibility_warning_dialog"
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
                summary = ctx.getString(R.string.force_enable_systemui_blur_feature_tips)
                key = "custom_volume_dialog_background_transparency"
                setDefaultValue(-1)
                max = 10
                min = -1
                showSeekBarValue = true
                updatesContinuously = false
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    ctx.dataChannel("com.android.systemui").put(key, newValue)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.reduce_power_menu_display_delay)
                key = "reduce_power_menu_display_delay"
                setDefaultValue(false)
                isVisible = false
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class FingerPrintRelated : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_miscellaneous_to_fingerPrintRelated
    override val scopes = arrayOf("com.android.systemui")
    private val loadFPIcon = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            val path = FileUtils.getDocumentPath(requireActivity(), it)
            requireActivity().putString(
                ModulePrefs, "replace_fingerprint_icon_path", path
            )
            findPreference<Preference>("replace_fingerprint_icon_path")?.summary = path
        }
    }

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.remove_fingerprint_icon_mode)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "remove_fingerprint_icon_mode"
                entries = ctx.resources.getStringArray(R.array.remove_fingerprint_icon_mode_entries)
                entryValues = arrayOf("0", "1", "2", "3")
                setDefaultValue("0")
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.replace_fingerprint_icon_switch)
                summary = ctx.getString(R.string.replace_fingerprint_icon_switch_summary)
                key = "replace_fingerprint_icon_switch"
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getBoolean(ModulePrefs, "replace_fingerprint_icon_switch", false)) {
                add(Preference(ctx).apply {
                    title = ctx.getString(R.string.replace_fingerprint_icon_path)
                    key = "replace_fingerprint_icon_path"
                    summary = ctx.getString(
                        ModulePrefs, "replace_fingerprint_icon_path", "null"
                    )
                    isIconSpaceReserved = false
                    isCopyingEnabled = true
                    setOnPreferenceClickListener {
                        loadFPIcon.launch("image/*")
                        true
                    }
                })
            }
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class Miscellaneous : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_xposed_to_miscellaneous
    override val scopes = arrayOf(
        "com.android.systemui",
        "com.android.externalstorage",
        "com.oplus.exsystemservice",
        "com.coloros.securepay"
    )

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.FloatingWindowDialogRelated)
                summary = arraySummaryDot(
                    ctx.getString(R.string.remove_low_battery_dialog_warning_summary),
                    ctx.getString(R.string.disable_headphone_high_volume_warning)
                )
                key = "FloatingWindowDialogRelated"
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_miscellaneous_to_dialogRelated, title)
                    true
                }
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.FingerPrintRelated)
                summary = arraySummaryDot(
                    ctx.getString(R.string.remove_fingerprint_icon),
                    ctx.getString(R.string.replace_fingerprint_icon_switch)
                )
                key = "FingerPrintRelated"
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_miscellaneous_to_fingerPrintRelated, title)
                    true
                }
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.SoundRelated)
                summary = arraySummaryDot(
                    ctx.getString(R.string.media_volume_level),
                    ctx.getString(R.string.disable_headphone_high_volume_warning)
                )
                key = "SoundRelated"
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_miscellaneous_to_soundRelated, title)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.show_charging_ripple)
                summary = ctx.getString(R.string.show_charging_ripple_summary)
                key = "show_charging_ripple"
                setDefaultValue(false)
                isVisible = SDK >= A12
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_otg_auto_off)
                summary = ctx.getString(R.string.disable_otg_auto_off_summary)
                key = "disable_otg_auto_off"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_storage_limit)
                summary = ctx.getString(R.string.remove_storage_limit_summary)
                key = "remove_storage_limit"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_enable_systemui_blur_feature)
                key = "force_enable_systemui_blur_feature"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_power_menu_sos_button)
                key = "remove_power_menu_sos_button"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.show_manual_lock_button_power_menu)
                key = "show_manual_lock_button_power_menu"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class Settings : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_settings
    override val scopes = arrayOf("com.android.settings")

    private val loadOtaCardImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            val path = FileUtils.getDocumentPath(requireActivity(), it)
            requireActivity().putString(
                ModulePrefs, "customize_device_ota_card_background_path", path
            )
            findPreference<Preference>("customize_device_ota_card_background_path")?.summary = path
        }
    }
    private val loadProcessorImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            val path = FileUtils.getDocumentPath(requireActivity(), it)
            requireActivity().putString(
                ModulePrefs, "customize_processor_image_path", path
            )
            findPreference<Preference>("customize_processor_image_path")?.summary = path
        }
    }

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.settings_connection_sharing)
                key = "settings_connection_sharing"
                isIconSpaceReserved = false
            })
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.settings_lock_screen)
                key = "settings_status_bar"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_statusbar_clock_format)
                summary = ctx.getString(R.string.enable_statusbar_clock_format_summary)
                key = "enable_statusbar_clock_format"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.settings_lock_screen)
                key = "settings_lock_screen"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_show_never_timeout)
                summary = ctx.getString(R.string.enable_show_never_timeout_summary)
                key = "enable_show_never_timeout"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.settings_display)
                key = "settings_display"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_increase_brightness_range)
                summary = ctx.getString(R.string.need_restart_system)
                key = "enable_extra_brightness"
                setDefaultValue(false)
                isVisible = false
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_lowest_auto_brightness_adjustment)
                key = "enable_lowest_allowed_brightness"
                setDefaultValue(false)
                isVisible = false
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_video_memc_frame_insertion)
                summary = ctx.getString(R.string.need_restart_system)
                key = "enable_video_memc_frame_insertion"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            if (ctx.getBoolean(ModulePrefs, "enable_video_memc_frame_insertion", false)) {
                add(Preference(ctx).apply {
                    title = ctx.getString(R.string.custom_video_dynamic_frame_insertion_configuration)
                    key = "custom_video_dynamic_frame_insertion_configuration"
                    isIconSpaceReserved = false
                    setOnPreferenceClickListener {
                        navigatePage(
                            R.id.action_nav_function_to_memcConfig,
                            ctx.getString(R.string.custom_video_dynamic_frame_insertion_configuration)
                        )
                        true
                    }
                })
            }
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.video_frame_insertion_support_2K120)
                summary = ctx.getString(R.string.video_frame_insertion_support_2K120_summary)
                key = "video_frame_insertion_support_2K120"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_screen_rgb_color_temperature_ball)
                summary = ctx.getString(R.string.need_restart_system)
                key = "enable_screen_color_temperature_rgb_ball"
                setDefaultValue(false)
                isVisible = false
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_screen_color_temperature_rgb_palette)
                summary = arraySummaryLine(
                    ctx.getString(R.string.need_restart_system),
                    ctx.getString(R.string.enable_screen_color_temperature_rgb_palette_summary)
                )
                key = "enable_screen_color_temperature_rgb_palette"
                setDefaultValue(false)
                isVisible = getOSVersionCode >= 27
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_screen_color_temperature_rgb_space)
                summary = ctx.getString(R.string.need_restart_system)
                key = "enable_screen_color_temperature_rgb_space"
                setDefaultValue(false)
                isVisible = getOSVersionCode >= 30 && SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_smart_switch_screen_resolution)
                key = "enable_smart_switching_screen_resolutions"
                setDefaultValue(false)
                isVisible = false
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_enable_reduce_bright_colors)
                key = "force_enable_reduce_white_point_value"
                setDefaultValue(false)
                isVisible = false
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_display_video_memc_frame_insertion)
                summary = ctx.getString(R.string.force_display_dc_backlight_mode_summary)
                key = "force_display_video_memc_frame_insertion"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.settings_sound)
                key = "settings_sound"
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_holo_audio)
                key = "enable_holographic_audio"
                setDefaultValue(false)
                isVisible = false
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_clear_voice)
                summary = ctx.getString(R.string.need_restart_system)
                key = "enable_clear_voice"
                setDefaultValue(false)
                isVisible = SDK >= A14
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_sound_sealed_call)
                summary = ctx.getString(R.string.need_restart_system)
                key = "enable_sound_sealed_call"
                setDefaultValue(false)
                isVisible = SDK >= A14
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.settings_application)
                key = "settings_application"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_display_process_management)
                key = "force_display_process_management"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_display_disabled_apps_manager)
                key = "force_display_disabled_apps_manager"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_display_auto_launch_jump_option)
                key = "force_display_auto_launch_jump_option"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.auto_unlock_restricted_settings)
                summary = ctx.getString(R.string.auto_unlock_restricted_settings_summary)
                key = "auto_unlock_restricted_settings"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.auto_jump_accessibility_settings)
                key = "auto_jump_accessibility_settings"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_game_dedicated_memory)
                key = "enable_dedicated_ram_for_games"
                setDefaultValue(false)
                isVisible = false
                isIconSpaceReserved = false
            })
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.settings_password_security)
                key = "settings_password_security"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_display_password_management_setting)
                key = "force_display_password_management_settings"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_device_admin_verification_dialog)
                key = "disable_device_admin_verification_dialog"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.settings_other_settings)
                key = "settings_other_settings"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_touch_membrane_protector_mode)
                key = "enable_touch_membrane_protector_mode"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_swipe_up_navigation_gesture)
                key = "enable_swipe_up_navigation_gesture"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_otg_auto_off)
                summary = ctx.getString(R.string.disable_otg_auto_off_summary)
                key = "disable_otg_auto_off"
                setDefaultValue(false)
                isVisible = SDK >= A14
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_game_acceleration)
                summary = ctx.getString(R.string.need_restart_system)
                key = "enable_game_acceleration"
                setDefaultValue(false)
                isVisible = SDK >= A14
                isIconSpaceReserved = false
            })
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.settings_phone_preference)
                key = "settings_phone_preference"
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.screen_physics_size_shown_cm)
                key = "screen_physics_size_shown_cm"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_device_name_change_limit)
                key = "remove_device_name_change_limit"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.set_processor_click_page)
                summary = "%s"
                key = "set_processor_click_page"
                entries = ctx.resources.getStringArray(R.array.set_processor_click_page_entries)
                entryValues = arrayOf("0", "1", "2", "3")
                setDefaultValue("0")
                isVisible = SDK >= A13
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (SDK >= A13 && ctx.getString(ModulePrefs, "set_processor_click_page", "0") == "3") {
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.custom_processor_image_path_switch)
                    summary = ctx.getString(R.string.custom_processor_introduction_text_summary)
                    key = "custom_processor_image_path_switch"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, _ ->
                        (activity as MainActivity).restart()
                        true
                    }
                })
                if (ctx.getBoolean(ModulePrefs, "custom_processor_image_path_switch", false)) {
                    add(Preference(ctx).apply {
                        title = ctx.getString(R.string.customize_processor_image_path)
                        key = "customize_processor_image_path"
                        summary = ctx.getString(
                            ModulePrefs, "customize_processor_image_path", "Null"
                        )
                        isIconSpaceReserved = false
                        isCopyingEnabled = true
                        setOnPreferenceClickListener {
                            loadProcessorImage.launch("image/*")
                            true
                        }
                    })
                }
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.custom_processor_introduction_text)
                    key = "custom_processor_introduction_text"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
            }
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_mariana_npu_intro_page)
                key = "enable_mariana_npu_introduction_page"
                setDefaultValue(false)
                isVisible = false
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_hasselblad_image_intro_page)
                key = "enable_hasselblad_camera_introduction_page"
                setDefaultValue(false)
                isVisible = false
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_game_architecture_display)
                key = "enable_game_architecture_display"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.customize_device_ota_card_background)
                key = "customize_device_ota_card_background"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getBoolean(ModulePrefs, "customize_device_ota_card_background", false)) {
                add(Preference(ctx).apply {
                    title = ctx.getString(R.string.customize_device_ota_card_background_path)
                    key = "customize_device_ota_card_background_path"
                    summary = ctx.getString(
                        ModulePrefs, "customize_device_ota_card_background_path", "Null"
                    )
                    isIconSpaceReserved = false
                    isCopyingEnabled = true
                    setOnPreferenceClickListener {
                        loadOtaCardImage.launch("image/*")
                        true
                    }
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.hide_card_top_text)
                    key = "hide_ota_card_top_text"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.apply_to_device_sharing_page)
                    key = "apply_device_parameter_sharing_page"
                    setDefaultValue(false)
                    isVisible = SDK >= A14
                    isIconSpaceReserved = false
                })
            }
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.customize_device_sharing_page_parameters)
                key = "customize_device_sharing_page_parameters"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.settings_other_preference)
                key = "settings_other_preference"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_top_account_display)
                key = "remove_top_account_display"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_cn_special_edition_setting)
                key = "disable_cn_special_edition_setting"
                setDefaultValue(false)
                isVisible = isZh(ctx)
                isIconSpaceReserved = false
            })
            if (isZh(ctx) && ctx.getBoolean(ModulePrefs, "disable_cn_special_edition_setting", false)) {
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.fix_default_app_jump_problem)
                    key = "fix_default_app_jump_problem"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
            }
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_display_content_recommend)
                key = "force_display_content_recommend"
                setDefaultValue(false)
                isVisible = isZh(ctx)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_settings_bottom_laboratory)
                key = "remove_settings_bottom_laboratory"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.force_display_bottom_google_settings)
                key = "force_display_bottom_google_settings"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.settings_developer_preference)
                key = "settings_developer_preference"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_dpi_restart_recovery)
                summary = ctx.getString(R.string.remove_dpi_restart_recovery_summary)
                key = "remove_dpi_restart_recovery"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class Battery : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_battery
    override val scopes = arrayOf("com.oplus.battery")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.open_battery_health)
                summary = ctx.getString(R.string.open_battery_health_summary)
                key = "open_battery_health"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getBoolean(ModulePrefs, "open_battery_health", false)) {
                add(EditTextPreference(ctx).apply {
                    title = ctx.getString(R.string.customize_battery_health_data_percentage)
                    summary = ctx.getString(R.string.customize_battery_health_data_percentage_summary)
                    dialogTitle = ctx.getString(R.string.customize_battery_health_data_percentage)
                    key = "customize_battery_health_data_percentage"
                    setDefaultValue("")
                    isIconSpaceReserved = false
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.display_module_calculates_battery_health_data)
                    summary = ctx.getString(R.string.display_module_calculates_battery_health_data_summary)
                    key = "display_module_calculates_battery_health_data"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.fix_battery_health_data_display)
                    summary = ctx.getString(R.string.fix_battery_health_data_display_summary)
                    key = "fix_battery_health_data_display"
                    setDefaultValue(false)
                    isVisible = SDK >= A13
                    isIconSpaceReserved = false
                    setOnPreferenceChangeListener { _, _ ->
                        (activity as MainActivity).restart()
                        true
                    }
                })
            }
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.open_screen_power_save)
                summary = ctx.getString(R.string.open_screen_power_save_summary)
                key = "open_screen_power_save"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_battery_temperature_control)
                summary = ctx.getString(R.string.remove_battery_temperature_control_summary)
                key = "remove_battery_temperature_control"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.performance_mode_and_standby_optimization)
                summary = ctx.getString(R.string.performance_mode_and_standby_optimization_summary)
                key = "performance_mode_and_standby_optimization"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.BatteryOptimization)
                key = "BatteryOptimization"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.restore_default_battery_optimization_whitelist)
                key = "restore_default_battery_optimization_whitelist"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            if (ctx.getBoolean(
                    ModulePrefs, "restore_default_battery_optimization_whitelist", false
                )
            ) {
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.disable_customize_battery_optimization_whiteList)
                    summary =
                        ctx.getString(R.string.disable_customize_battery_optimization_whiteList_summary)
                    key = "disable_customize_battery_optimization_whiteList"
                    setDefaultValue(false)
                    isVisible = false
                    isIconSpaceReserved = false
                })
            }
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_stop_charging_at_80)
                key = "enable_stop_charging_at_80"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_battery_restrict_plugin)
                key = "remove_battery_restrict_plugin"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.show_phone_usage_screen_time)
                key = "show_phone_usage_screen_time"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = jumpBattery(requireActivity())
}

class Camera : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_xposed_to_camera
    override val scopes = arrayOf("com.oneplus.camera", "com.oplus.camera")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_camera_debug_ui_option)
                key = "enable_camera_debug_ui_option"
                setDefaultValue(false)
                isVisible = SDK >= A11
                isIconSpaceReserved = false
            })

            add(Preference(ctx).apply {
                title = ctx.getString(R.string.custom_camera_open_gallery_by_default)
                key = "custom_camera_open_gallery_by_default"
                isVisible = getOSVersionCode >= 26
                isIconSpaceReserved = false
                summary = openGallerySummary(ctx)
                setOnPreferenceClickListener {
                    showOpenGalleryPicker(ctx)
                    true
                }
            })

            if (getOSVersionCode >= 28) {
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.enable_night_scene_30x_zoom)
                    key = "enable_camera_night_zoom_30x"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.enable_video_shooting_wheel_zoom)
                    key = "enable_video_capture_roulette_zoom"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                })
            }

            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_camera_flash_limit)
                key = "remove_camera_flash_limit"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.CameraWaterMark)
                key = "CameraWaterMark"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_watermark_word_limit)
                key = "remove_watermark_word_limit"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_frame_watermark_style)
                key = "enable_frame_watermark_style"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    if (newValue == true) findPreference<SwitchPreference>("enable_hasselblad_watermark_style")?.isChecked = false
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_hasselblad_watermark_style)
                key = "enable_hasselblad_watermark_style"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    if (newValue == true) findPreference<SwitchPreference>("enable_frame_watermark_style")?.isChecked = false
                    true
                }
            })
            add(EditTextPreference(ctx).apply {
                title = ctx.getString(R.string.custom_model_watermark)
                dialogTitle = title
                summary = ctx.getString(
                    ModulePrefs, "custom_model_watermark", "None"
                )
                if (summary.isNullOrBlank()) summary = "None"
                key = "custom_model_watermark"
                setDefaultValue("None")
                isIconSpaceReserved = false
                isVisible = SDK >= A13 && Build.MODEL.contains("RM", true).not()
                setOnPreferenceChangeListener { _, newValue ->
                    summary = (newValue as String).ifBlank { "None" }
                    true
                }
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.CameraFilter)
                key = "CameraFilter"
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_filter_model_limit)
                key = "remove_filter_model_limit"
                setDefaultValue(false)
                isVisible = getOSVersionCode >= 34
                isIconSpaceReserved = false
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.camera_universal_filter_settings)
                key = "camera_universal_filter_settings"
                isVisible = SDK >= A13
                isIconSpaceReserved = false
                summary = filterSelectionSummary(ctx, "camera_universal_filter_settings", universalFilterList)
                setOnPreferenceClickListener {
                    showCameraFilterDialog(
                        ctx, "camera_universal_filter_settings", universalFilterList
                    ) {
                        findPreference<SwitchPreference>("enable_hasselblad_watermark_style")?.isChecked = true
                    }
                    true
                }
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.camera_portrait_filter_settings)
                key = "camera_portrait_filter_settings"
                isVisible = SDK >= A13
                isIconSpaceReserved = false
                summary = filterSelectionSummary(ctx, "camera_portrait_filter_settings", portraitFilterList)
                setOnPreferenceClickListener {
                    showCameraFilterDialog(
                        ctx, "camera_portrait_filter_settings", portraitFilterList
                    )
                    true
                }
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.camera_video_filter_settings)
                key = "camera_video_filter_settings"
                isVisible = SDK >= A13
                isIconSpaceReserved = false
                summary = filterSelectionSummary(ctx, "camera_video_filter_settings", videoFilterList)
                setOnPreferenceClickListener {
                    showCameraFilterDialog(
                        ctx, "camera_video_filter_settings", videoFilterList
                    )
                    true
                }
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.settings_other_preference)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_10_bit_image_support)
                summary = ctx.getString(R.string.enable_10_bit_image_support_summary)
                key = "enable_10_bit_image_support"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)

    private data class FilterEntry(val key: String, val nameRes: Int)

    private val universalFilterList = listOf(
        FilterEntry("master_filter", R.string.camera_filter_master),
        FilterEntry("jiangwen_filter", R.string.camera_filter_jiangwen),
        FilterEntry("grand_tour_filter", R.string.camera_filter_grand_tour),
        FilterEntry("vignette_grain_filter", R.string.camera_filter_vignette_grain),
        FilterEntry("desert_filter", R.string.camera_filter_desert),
        FilterEntry("tol_filter", R.string.camera_filter_tol),
        FilterEntry("os15_zhi_gan_filter", R.string.camera_filter_os15_zhi_gan),
        FilterEntry("jzk_filter", R.string.camera_filter_jzk)
    )

    private val portraitFilterList = listOf(
        FilterEntry("retention", R.string.camera_filter_portrait_keep_color),
        FilterEntry("bokeh_flare_portrait", R.string.camera_filter_light_spot_portrait)
    )

    private val videoFilterList = listOf(
        FilterEntry("color_extraction", R.string.camera_filter_color_extraction),
        FilterEntry("retention", R.string.camera_filter_portrait_keep_color),
        FilterEntry("bokeh_flare_portrait", R.string.camera_filter_light_spot_portrait)
    )

    private fun openGallerySummary(ctx: Context): String {
        val pkg = ctx.getString(ModulePrefs, "custom_camera_open_gallery_by_default", "")
        if (pkg.isNullOrBlank()) {
            return ctx.getString(R.string.custom_camera_open_gallery_by_default_not_set)
        }
        return "${ctx.getAppLabel(pkg)} ($pkg)"
    }

    private fun showOpenGalleryPicker(ctx: Context) {
        val dialogBinding = DialogAppSelectorBinding.inflate(LayoutInflater.from(ctx))
        val dialog = MaterialAlertDialogBuilder(ctx, dialogCentered).apply {
            setTitle(R.string.custom_camera_open_gallery_by_default)
            setView(dialogBinding.root)
            setPositiveButton(android.R.string.ok) { _, _ ->
                val adapter = dialogBinding.recyclerView.adapter as? AppSelectorAdapter
                val selected = adapter?.getSelected()?.firstOrNull().orEmpty()
                ctx.putString(ModulePrefs, "custom_camera_open_gallery_by_default", selected)
                findPreference<Preference>("custom_camera_open_gallery_by_default")?.summary =
                    openGallerySummary(ctx)
                (activity as? MainActivity)?.restart()
            }
            setNeutralButton(R.string.custom_camera_open_gallery_by_default_not_set) { _, _ ->
                ctx.putString(ModulePrefs, "custom_camera_open_gallery_by_default", "")
                findPreference<Preference>("custom_camera_open_gallery_by_default")?.summary =
                    ctx.getString(R.string.custom_camera_open_gallery_by_default_not_set)
                (activity as? MainActivity)?.restart()
            }
            setNegativeButton(android.R.string.cancel, null)
            create()
        }.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialogBinding.swipeRefreshLayout.isRefreshing = true
        dialogBinding.searchViewLayout.isEnabled = false
        lifecycleScope.launch(Dispatchers.IO) {
            val pm = ctx.packageManager
            val appInfos = PackageUtils(pm).getInstalledApplications(0)
            val appList = ArrayList<AppInfo>()
            for (info in appInfos) {
                appList.add(
                    AppInfo(
                        info.loadIcon(pm),
                        info.loadLabel(pm),
                        info.packageName,
                    )
                )
            }
            appList.sortBy { it.appName.toString().lowercase() }
            val current = ctx.getString(ModulePrefs, "custom_camera_open_gallery_by_default", "")
            withContext(Dispatchers.Main) {
                lateinit var adapter: AppSelectorAdapter
                adapter = AppSelectorAdapter(ctx, appList) { selected ->
                    if (selected.size > 1) adapter.setSelected(setOf(selected.last()))
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.isEnabled =
                        selected.isNotEmpty()
                }
                if (!current.isNullOrBlank()) adapter.setSelected(setOf(current))
                dialogBinding.recyclerView.apply {
                    this.adapter = adapter
                    layoutManager = LinearLayoutManager(ctx)
                }
                dialogBinding.searchView.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        adapter.getFilter.filter(s.toString())
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })
                dialogBinding.swipeRefreshLayout.isRefreshing = false
                dialogBinding.searchViewLayout.isEnabled = true
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.isEnabled =
                    !current.isNullOrBlank()
            }
        }
    }

    private fun filterSelectionSummary(
        ctx: Context, key: String, filters: List<FilterEntry>
    ): String {
        val selected = ctx.getStringSet(ModulePrefs, key, ArraySet()) ?: emptySet()
        val names = filters.filter { it.key in selected }.map { ctx.getString(it.nameRes) }
        return if (names.isEmpty()) ctx.getString(R.string.custom_camera_open_gallery_by_default_not_set)
        else arraySummaryDot(*names.toTypedArray())
    }

    private fun showCameraFilterDialog(
        ctx: Context, key: String, filters: List<FilterEntry>, onMasterSelected: (() -> Unit)? = null
    ) {
        val selected = ctx.getStringSet(ModulePrefs, key, ArraySet()) ?: emptySet()
        val labels = filters.map { ctx.getString(it.nameRes) }.toTypedArray<CharSequence>()
        val checked = BooleanArray(filters.size) { filters[it].key in selected }
        MaterialAlertDialogBuilder(ctx, dialogCentered).apply {
            setTitle(ctx.getString(R.string.CameraFilter))
            setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            setPositiveButton(android.R.string.ok) { _, _ ->
                val newSet = ArraySet<String>()
                filters.forEachIndexed { index, entry ->
                    if (checked[index]) newSet.add(entry.key)
                }
                ctx.putStringSet(ModulePrefs, key, newSet)
                val pref = findPreference<Preference>(key)
                pref?.summary = filterSelectionSummary(ctx, key, filters)
                if (onMasterSelected != null && "master_filter" in newSet) onMasterSelected()
                (activity as? MainActivity)?.restart()
            }
            setNegativeButton(android.R.string.cancel, null)
            show()
        }
    }

}

class OplusGallery : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusGallery
    override val scopes = arrayOf("com.coloros.gallery3d")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.GalleryWaterMark)
                key = "GalleryWaterMark"
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.replace_oneplus_model_watermark)
                summary = ctx.getString(R.string.replace_oneplus_model_watermark_summary)
                key = "replace_oneplus_model_watermark"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_ai_master_watermark)
                key = "enable_ai_master_watermark"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_hassel_watermark)
                key = "enable_hassel_watermark"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_privacy_watermark)
                key = "enable_privacy_watermark"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_watermark_editing)
                key = "enable_watermark_editing"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_gallery_watermark_word_limit)
                key = "remove_gallery_watermark_word_limit"
                setDefaultValue(false)
                isVisible = getOSVersionCode in 27..29
                isIconSpaceReserved = false
            })
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.CameraFilter)
                key = "GalleryFilter"
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_jiangwen_filter)
                key = "enable_gallery_jiangwen_filter"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.GalleryEditor)
                key = "GalleryEditor"
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_aigc_elimination_limit)
                key = "remove_aigc_elimination_limit"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_lns_cut_photo)
                summary = ctx.getString(R.string.enable_lns_cut_photo_summary)
                key = "enable_lns_cut_photo"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_photo_listview_senior_picked)
                key = "enable_photo_listview_senior_picked"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(DropDownPreference(ctx).apply {
                title = ctx.getString(R.string.set_photo_view_thumb_line_display_mode)
                summary = ctx.getString(R.string.common_words_current_mode) + ": %s"
                key = "set_photo_view_thumb_line_display_mode"
                entries = arrayOf(
                    ctx.getString(R.string.common_words_default),
                    ctx.getString(R.string.common_words_enable),
                    ctx.getString(R.string.common_words_disable)
                )
                entryValues = arrayOf("0", "1", "2")
                setDefaultValue("0")
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_photo_editor_gif_synthesis)
                key = "enable_photo_editor_gif_synthesis"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_spring_festival_watermark)
                key = "enable_spring_festival_watermark"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_national_day_watermark)
                key = "enable_national_day_watermark"
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

class OplusGames : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_xposed_to_oplusGames
    override val scopes = arrayOf("com.oplus.games", "com.oplus.cosa")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.game_assistant_page)
                key = "game_assistant_page"
                isVisible = ctx.checkPackName("com.oplus.games") && ctx.checkResolveActivity(
                    Intent().setClassName("com.oplus.games", "business.compact.activity.GameBoxCoverActivity")
                )
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    ShellUtils.execCommand(
                        "am start -n com.oplus.games/business.compact.activity.GameBoxCoverActivity", true
                    )
                    true
                }
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.game_space_page)
                key = "game_space_page"
                isVisible = ctx.checkPackName("com.nearme.gamecenter") && ctx.checkResolveActivity(
                    Intent().setClassName(
                        "com.nearme.gamecenter",
                        "com.nearme.gamespace.desktopspace.ui.DesktopSpaceMainActivity"
                    )
                )
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    ShellUtils.execCommand(
                        "am start -n com.nearme.gamecenter/com.nearme.gamespace.desktopspace.ui.DesktopSpaceMainActivity",
                        true
                    )
                    true
                }
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.OplusGamesLayout)
                key = "OplusGamesLayout"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_startup_animation)
                key = "remove_startup_animation"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_welfare_page)
                key = "remove_welfare_page"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_tool_recommendation_card)
                key = "remove_tool_recommendation_card"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.OplusGamesTool)
                key = "OplusGamesTool"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_root_check)
                summary = ctx.getString(R.string.remove_root_check_summary)
                key = "remove_root_check"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_some_vip_limit)
                summary = ctx.getString(R.string.remove_some_vip_limit_summary)
                key = "remove_some_vip_limit"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_developer_page)
                summary = ctx.getString(R.string.enable_developer_page_summary)
                key = "enable_developer_page"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.game_assistant_develop_page)
                key = "game_assistant_develop_page"
                isVisible = ctx.getBoolean(ModulePrefs, "enable_developer_page", false) &&
                    ctx.checkPackName("com.oplus.games")
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    ShellUtils.execCommand(
                        "am start -n com.oplus.games/business.compact.activity.GameDevelopOptionsActivity",
                        true
                    )
                    true
                }
            })
            add(EditTextPreference(ctx).apply {
                title = ctx.getString(R.string.custom_media_player_support)
                dialogTitle = title
                summary = ctx.getString(
                    ModulePrefs, "custom_media_player_support", "None"
                )
                if (summary.isNullOrBlank()) summary = "None"
                dialogMessage = ctx.getString(R.string.custom_media_player_support_message)
                key = "custom_media_player_support"
                setDefaultValue("None")
                isIconSpaceReserved = false
                setOnBindEditTextListener {
                    it.setText((summary as String).replaceBlankLine)
                }
                setOnPreferenceChangeListener { _, newValue ->
                    val format = (newValue as String).replaceBlankLine
                    summary = format.ifBlank { "None" }
                    val packages = if (format.isBlank() || format == "None") {
                        emptySet()
                    } else {
                        format.split("\n").map { it.trim() }.filter { it.isNotBlank() }.toSet()
                    }
                    ctx.putStringSet(ModulePrefs, "custom_media_player_support_list", packages)
                    true
                }
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.custom_barrage_notification_whitelist_list)
                key = "custom_barrage_notification_whitelist_list"
                summary = (ctx.getStringSet(
                    ModulePrefs, "custom_barrage_notification_whitelist_list", ArraySet()
                ) ?: emptySet()).toString()
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    showBarrageNotificationWhitelistDialog()
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_game_run_in_background)
                key = "enable_game_run_in_background"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_game_ai_play)
                key = "enable_game_ai_play"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_game_voice_changer_whitelist)
                key = "remove_game_voice_changer_whitelist"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_game_assistant_temperature_detection)
                summary = ctx.getString(R.string.remove_game_assistant_temperature_detection_summary)
                key = "remove_game_assistant_temperature_detection"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_support_competition_mode)
                key = "enable_support_competition_mode"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_competition_mode_sound)
                key = "remove_competition_mode_sound"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_x_mode_feature)
                key = "enable_x_mode_feature"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_gt_mode_feature)
                key = "enable_gt_mode_feature"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_one_plus_characteristic)
                key = "enable_one_plus_characteristic"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_adreno_gpu_controller)
                key = "enable_adreno_gpu_controller"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_increase_fps_limit_feature)
                key = "enable_increase_fps_limit_feature"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_increase_fps_feature)
                key = "enable_increase_fps_feature"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_optimise_power_feature)
                key = "enable_optimise_power_feature"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_super_resolution_feature)
                key = "enable_super_resolution_feature"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_danmaku_notification_whitelist)
                key = "remove_danmaku_notification_whitelist"
                setDefaultValue(false)
                isVisible = false
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean =
        requireActivity().checkPackName("com.oplus.games") && requireActivity().checkResolveActivity(
            Intent().setClassName(
                "com.oplus.games", "business.compact.activity.GameBoxCoverActivity"
            )
        )

    override fun callOpenMenu() {
        ShellUtils.execCommand(
            "am start -n com.oplus.games/business.compact.activity.GameBoxCoverActivity", true
        )
    }

    private fun showBarrageNotificationWhitelistDialog() {
        val ctx = requireContext()
        val dialogBinding = DialogAppSelectorBinding.inflate(LayoutInflater.from(ctx))
        val dialog = MaterialAlertDialogBuilder(ctx, dialogCentered).apply {
            setTitle(R.string.custom_barrage_notification_whitelist_list)
            setView(dialogBinding.root)
            setPositiveButton(android.R.string.ok) { _, _ ->
                val adapter = dialogBinding.recyclerView.adapter as? AppSelectorAdapter
                val selected = adapter?.getSelected() ?: emptyList()
                val newSet = ArraySet<String>()
                selected.forEach { newSet.add(it) }
                ctx.putStringSet(
                    ModulePrefs, "custom_barrage_notification_whitelist_list", newSet
                )
                findPreference<Preference>("custom_barrage_notification_whitelist_list")?.summary =
                    newSet.toString()
                (activity as? MainActivity)?.restart()
            }
            setNegativeButton(android.R.string.cancel, null)
            create()
        }.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        loadBarrageNotificationAppList(ctx, dialogBinding, dialog)
    }

    private fun loadBarrageNotificationAppList(
        ctx: Context,
        binding: DialogAppSelectorBinding,
        dialog: androidx.appcompat.app.AlertDialog
    ) {
        binding.swipeRefreshLayout.isRefreshing = true
        binding.searchViewLayout.isEnabled = false
        lifecycleScope.launch(Dispatchers.IO) {
            val packageManager = ctx.packageManager
            val appInfos = PackageUtils(packageManager).getInstalledApplications(0)
            val appList = ArrayList<AppInfo>()
            for (info in appInfos) {
                appList.add(
                    AppInfo(
                        info.loadIcon(packageManager),
                        info.loadLabel(packageManager),
                        info.packageName,
                    )
                )
            }
            appList.sortBy { it.appName.toString().lowercase() }
            val current = ctx.getStringSet(
                ModulePrefs, "custom_barrage_notification_whitelist_list", ArraySet()
            ) ?: emptySet()
            withContext(Dispatchers.Main) {
                val adapter = AppSelectorAdapter(ctx, appList) { selected ->
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.isEnabled =
                        selected.isNotEmpty()
                }
                if (current.isNotEmpty()) adapter.setSelected(current)
                binding.recyclerView.apply {
                    this.adapter = adapter
                    layoutManager = LinearLayoutManager(ctx)
                }
                binding.searchView.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        adapter.getFilter.filter(s.toString())
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })
                binding.swipeRefreshLayout.setOnRefreshListener {
                    loadBarrageNotificationAppList(ctx, binding, dialog)
                }
                binding.swipeRefreshLayout.isRefreshing = false
                binding.searchViewLayout.isEnabled = true
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
            }
        }
    }
}

class ThemeStore : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_xposed_to_themeStore
    override val scopes = arrayOf("com.heytap.themestore")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.unlock_themestore_vip)
                summary = ctx.getString(R.string.unlock_themestore_vip_summary)
                key = "unlock_themestore_vip"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class CloudService : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_xposed_to_cloudService
    override val scopes = arrayOf("com.heytap.cloud")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_network_limit)
                summary = ctx.getString(R.string.remove_network_limit_summary)
                key = "remove_network_limit"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_forced_backup_app_list)
                key = "disable_forced_backup_app_list"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class OplusOta : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusOta
    override val scopes = arrayOf("com.oplus.ota")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.unlock_local_upgrade)
                summary = ctx.getString(R.string.unlock_local_upgrade_summary)
                key = "unlock_local_upgrade"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    scopeLife {
                        val command = arrayOf(
                            "settings put global development_settings_enabled 1",
                            "pm clear com.oplus.ota",
                            "settings put global airplane_mode_on 1",
                            "am broadcast --user all -a android.intent.action.AIRPLANE_MODE --ez 'state' 'true'",
                            "am start com.oplus.ota/com.oplus.otaui.activity.EntryActivity"
                        )
                        withDefault { ShellUtils.execCommand(command, true) }
                    }
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                val getStatus = ShellUtils.execCommand(
                    "getprop ro.boot.veritymode", false, true
                )
                val status = if (getStatus.result == 1) "null"
                else getStatus.successMsg.toString().ifBlank { "null" }
                title = ctx.getString(R.string.restore_ota_update_verity)
                summary = ctx.getString(R.string.restore_ota_update_verity_summary, status)
                key = "restore_ota_update_verity"
                isEnabled = status != "enforcing"
                isChecked = status == "enforcing"
                isPersistent = false
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    val value = if (newValue as Boolean) "enforcing" else "\"\""
                    val command = "resetprop ro.boot.veritymode $value"
                    ctx.toast(command)
                    val exec = ShellUtils.execCommand(command, true, true)
                    if (exec.result == 0) {
                        summary = ctx.getString(R.string.restore_ota_update_verity_summary, value)
                    } else (activity as MainActivity).restart()
                    true
                }
            })
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.extract_ota_information)
                summary = ctx.getString(R.string.extract_ota_information_summary)
                key = "extract_ota_information"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_oplusOta_to_extractOTAFragment, title)
                    true
                }
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_opex_local_install)
                key = "enable_opex_local_install"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_ota_notify_install_success)
                key = "remove_ota_notify_install_success"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_ota_auto_download_dialog)
                key = "remove_ota_auto_download_dialog"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_ota_local_update_verity)
                summary = ctx.getString(R.string.remove_ota_local_update_verity_summary)
                key = "remove_ota_local_update_verity"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_dm_verity)
                summary = ctx.getString(R.string.disable_dm_verity_summary)
                key = "disable_dm_verity_verification"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = jumpOTA(requireActivity())
}

class OplusPictorial : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusPictorial
    override val scopes = arrayOf("com.heytap.pictorial")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_image_save_watermark)
                key = "remove_image_save_watermark"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_video_save_watermark)
                key = "remove_video_save_watermark"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = jumpPictorial(requireActivity())
}

class OplusMMS : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusMMS
    override val scopes = arrayOf("com.android.mms")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_verification_code_floating_window)
                key = "remove_verification_code_floating_window"
                setDefaultValue(false)
                isVisible = SDK >= A13
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_mms_bottom_input_box_menu)
                key = "remove_mms_bottom_input_box_menu"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_mms_card_marketing_button)
                key = "remove_mms_card_marketing_button"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class OplusBrowser : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusBrowser
    override val scopes = arrayOf("com.heytap.browser")

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(Preference(ctx).apply {
                title = ctx.getString(R.string.browser_concise_mode)
                isVisible = ctx.checkPackName("com.heytap.browser")
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    try {
                        Intent().apply {
                            setClassName(
                                "com.heytap.browser",
                                "com.heytap.browser.settings.component.BrowserPreferenceActivity"
                            )
                            putExtra(
                                "key.fragment.name",
                                "com.heytap.browser.settings.homepage.HomepagePreferenceFragment"
                            )
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                            addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                            startActivity(this)
                        }
                    } catch (_: Exception) {
                        ctx.toast("Error: Please check your browser version!")
                    }
                    true
                }
            })
            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.common_words_ads)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_ads_from_download_dialog)
                key = "remove_ads_from_download_dialog"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_ads_at_download_page_bottom)
                key = "remove_ads_at_download_page_bottom"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_ads_from_weather_page)
                summary = ctx.getString(R.string.remove_ads_from_weather_page_summary)
                key = "remove_ads_from_weather_page"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_browser_window_limit_number)
                key = "remove_browser_window_limit_number"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_browser_search_bar_app_promotion)
                key = "remove_browser_search_bar_app_promotion"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class OplusGesture : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusGesture
    override val scopes = arrayOf("com.android.systemui", "com.oplus.gesture")
    private val loadLeftImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            val path = FileUtils.getDocumentPath(requireActivity(), it)
            requireActivity().putString(
                ModulePrefs, "replace_side_slider_icon_on_left", path
            )
            findPreference<Preference>("replace_side_slider_icon_on_left")?.summary = path
        }
    }
    private val loadRightImage = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            val path = FileUtils.getDocumentPath(requireActivity(), it)
            requireActivity().putString(
                ModulePrefs, "replace_side_slider_icon_on_right", path
            )
            findPreference<Preference>("replace_side_slider_icon_on_right")?.summary = path
        }
    }

    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_volume_key_control_flashlight)
                summary = ctx.getString(R.string.need_restart_system)
                key = "enable_volume_key_control_flashlight"
                setDefaultValue(false)
                isVisible = getOSVersionCode >= 27
                isIconSpaceReserved = false
            })

            if (SDK >= A13) {
                add(PreferenceCategory(ctx).apply {
                    title = ctx.getString(R.string.AonGesture)
                    key = "AonGesture"
                    isIconSpaceReserved = false
                })
                add(SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.force_enable_aon_gestures)
                    summary = ctx.getString(R.string.force_enable_aon_gestures_summary)
                    key = "force_enable_aon_gestures"
                    setDefaultValue(false)
                    isEnabled =
                        ctx.checkPackName("com.oplus.gesture") && ctx.checkPackName("com.aiunit.aon")
                    isIconSpaceReserved = false
                })
                add(EditTextPreference(ctx).apply {
                    title = ctx.getString(R.string.custom_aon_gesture_scroll_page_whitelist)
                    dialogTitle = title
                    summary = ctx.getString(
                        ModulePrefs, "custom_aon_gesture_scroll_page_whitelist", "None"
                    )
                    if (summary.isNullOrBlank()) summary = "None"
                    dialogMessage = ctx.getString(R.string.custom_aon_gesture_whitelist_tips)
                    key = "custom_aon_gesture_scroll_page_whitelist"
                    setDefaultValue("None")
                    isEnabled = ctx.checkPackName("com.aiunit.aon")
                    isIconSpaceReserved = false
                    setOnBindEditTextListener {
                        it.setText((summary as String).replaceBlankLine)
                    }
                    setOnPreferenceChangeListener { _, newValue ->
                        val format = (newValue as String).replaceBlankLine
                        summary = format.ifBlank { "None" }
                        true
                    }
                })
                add(EditTextPreference(ctx).apply {
                    title = ctx.getString(R.string.custom_aon_gesture_video_whitelist)
                    dialogTitle = title
                    summary = ctx.getString(
                        ModulePrefs, "custom_aon_gesture_video_whitelist", "None"
                    )
                    if (summary.isNullOrBlank()) summary = "None"
                    dialogMessage = ctx.getString(R.string.custom_aon_gesture_whitelist_tips)
                    key = "custom_aon_gesture_video_whitelist"
                    setDefaultValue("None")
                    isEnabled = ctx.checkPackName("com.aiunit.aon")
                    isVisible = false
                    isIconSpaceReserved = false
                    setOnBindEditTextListener {
                        it.setText((summary as String).replaceBlankLine)
                    }
                    setOnPreferenceChangeListener { _, newValue ->
                        val format = (newValue as String).replaceBlankLine
                        summary = format.ifBlank { "None" }
                        true
                    }
                })
            }

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.FullScreenGestureRelated)
                key = "FullScreenGestureRelated"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_back_gesture_confirmation_limit)
                summary = ctx.getString(R.string.remove_back_gesture_confirmation_limit_summary)
                key = "remove_back_gesture_confirmation_limit"
                setDefaultValue(false)
                isVisible = SDK >= 35 && SDK <= 36
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_side_slider)
                key = "remove_side_slider"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_side_slider_black_background)
                key = "remove_side_slider_black_background"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_rotate_screen_button)
                key = "remove_rotate_screen_button"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })

            add(PreferenceCategory(ctx).apply {
                title = ctx.getString(R.string.CustomSideSliderIcon)
                key = "CustomSideSliderIcon"
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.replace_side_slider_icon_switch)
                summary = ctx.getString(R.string.replace_side_slider_icon_switch_summary)
                key = "replace_side_slider_icon_switch"
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            if (ctx.getBoolean(ModulePrefs, "replace_side_slider_icon_switch", false)) {
                add(Preference(ctx).apply {
                    title = ctx.getString(R.string.replace_side_slider_icon_on_left)
                    key = "replace_side_slider_icon_on_left"
                    summary = ctx.getString(
                        ModulePrefs, "replace_side_slider_icon_on_left", "null"
                    )
                    isIconSpaceReserved = false
                    isCopyingEnabled = true
                    setOnPreferenceClickListener {
                        loadLeftImage.launch("image/*")
                        true
                    }
                })
                add(Preference(ctx).apply {
                    title = ctx.getString(R.string.replace_side_slider_icon_on_right)
                    key = "replace_side_slider_icon_on_right"
                    summary = ctx.getString(
                        ModulePrefs, "replace_side_slider_icon_on_right", "null"
                    )
                    isIconSpaceReserved = false
                    isCopyingEnabled = true
                    setOnPreferenceClickListener {
                        loadRightImage.launch("image/*")
                        true
                    }
                })
            }
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = jumpGesture(requireActivity())
}

class OplusBreenoTouch : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusBreenoTouch
    override val scopes = arrayOf("com.coloros.directui", "com.coloros.colordirectservice")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_app_recommend_card)
                key = "remove_touch_app_recommend_card"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class OplusSearchBox : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusSearchBox
    override val scopes = arrayOf("com.heytap.quicksearchbox")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_app_recommend_card)
                key = "remove_searchbox_app_recommend_card"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.searchbox_default_search_local_tab)
                key = "searchbox_default_search_local_tab"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_searchbox_uninstalled_app_suggestions)
                key = "remove_searchbox_uninstalled_app_suggestions"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class OplusMarket : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusMarket
    override val scopes = arrayOf("com.heytap.market")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_market_splash_page_app_recommend)
                key = "remove_market_splash_page_app_recommend"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_market_update_page_app_recommend)
                key = "remove_market_update_download_page_app_recommend"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_market_mine_page_app_recommend)
                key = "remove_market_mine_page_app_recommend"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}

class OplusWeather : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_oplusWeather
    override val scopes = arrayOf("com.coloros.weather2")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.remove_ads_from_weather)
                key = "remove_weather_some_page_bottom_ads"
                setDefaultValue(true)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.disable_weather_jump_browser)
                key = "disable_weather_jump_browser"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.enable_15_day_weather_expand_list)
                key = "enable_15_day_weather_expand_list"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            add(SwitchPreference(ctx).apply {
                title = ctx.getString(R.string.restore_rainfall_cloud_map_page)
                key = "restore_rainfall_cloud_map_page"
                setDefaultValue(false)
                isIconSpaceReserved = false
                isVisible = getOSVersionCode in 30..34
            })
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
}
