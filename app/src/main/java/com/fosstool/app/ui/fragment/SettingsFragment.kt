package com.fosstool.app.ui.fragment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.ArraySet
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.DropDownPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.SwitchPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.highcapable.yukihookapi.hook.factory.dataChannel
import com.highcapable.yukihookapi.hook.xposed.prefs.ui.ModulePreferenceFragment
import com.fosstool.app.BuildConfig
import com.fosstool.app.R
import com.fosstool.app.ui.activity.MainActivity
import com.fosstool.app.utils.FileUtils
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.OtherPrefs
import com.fosstool.app.utils.SettingsPrefs
import com.fosstool.app.utils.arraySummaryLine
import com.fosstool.app.utils.backupAllPrefs
import com.fosstool.app.utils.base64Decode
import com.fosstool.app.utils.base64Encode
import com.fosstool.app.utils.clearAllPrefs
import com.fosstool.app.utils.formatDate
import com.fosstool.app.utils.navigatePage
import com.fosstool.app.utils.putBoolean
import com.fosstool.app.utils.putInt
import com.fosstool.app.utils.putString
import com.fosstool.app.utils.putStringSet
import com.fosstool.app.utils.setComponentDisabled
import com.fosstool.app.utils.toast
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.system.exitProcess

class SettingsFragment : ModulePreferenceFragment() {
    private val backupData = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) {
        if (it != null) {
            writeBackupData(requireActivity(), it)
        }
    }
    private val restoreData = registerForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) {
            writeRestoreData(requireActivity(), FileUtils.readFromUri(requireActivity(), it))
        }
    }

    private fun writeBackupData(context: Context, uri: Uri) {
        val json = JSONObject()
        val dataMapList = context.backupAllPrefs(ModulePrefs, SettingsPrefs, OtherPrefs)
        dataMapList?.keys?.forEach { prefs ->
            val jsons = JSONObject()
            val data = dataMapList[prefs]
            data?.keys?.forEach { key ->
                data[key].apply {
                    if (this?.javaClass?.simpleName == "HashSet") {
                        val arr = JSONArray()
                        val value = (this as HashSet<*>).toTypedArray()
                        for (i in value.indices) {
                            arr.put(value[i])
                        }
                        jsons.put(key, arr)
                    } else {
                        jsons.put(key, this)
                    }
                }
            }
            json.put(prefs, jsons)
        }
        val str = base64Encode(json.toString())
        try {
            context.contentResolver.openFileDescriptor(uri, "w")?.use { its ->
                FileOutputStream(its.fileDescriptor).use {
                    it.write(str.toByteArray())
                }
            }
            context.toast(getString(R.string.data_backup_complete))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            context.toast(getString(R.string.data_backup_error))
        } catch (e: IOException) {
            e.printStackTrace()
            context.toast(getString(R.string.data_backup_error))
        }
    }

    private fun writeRestoreData(context: Context, data: String) {
        val json = JSONObject(base64Decode(data))
        if (json.length() <= 0) return
        json.keys().forEach { prefs ->
            val prefsDatas = json.getJSONObject(prefs)
            if (prefsDatas.length() > 0) {
                prefsDatas.keys().forEach { key ->
                    val value = prefsDatas.get(key)
                    when (value.javaClass.simpleName) {
                        "Boolean" -> context.putBoolean(prefs, key, value as Boolean)
                        "Integer" -> context.putInt(prefs, key, value as Int)
                        "JSONArray" -> {
                            val set = ArraySet<String>()
                            val list = value as JSONArray
                            for (i in 0 until list.length()) {
                                set.add(list[i] as String)
                            }
                            context.putStringSet(prefs, key, set)
                        }

                        "String" -> context.putString(prefs, key, value as String)
                        else -> context.toast("Error: $key")
                    }
                }
            }
        }
        context.toast(getString(R.string.data_restore_complete))
        (activity as MainActivity).restart()
    }

    override fun onCreatePreferencesInModuleApp(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = SettingsPrefs
        preferenceScreen = preferenceManager.createPreferenceScreen(requireActivity()).apply {
            addPreference(PreferenceCategory(context).apply {
                setTitle(R.string.theme_title)
                setSummary(R.string.theme_title_summary)
                isIconSpaceReserved = false
            })
            addPreference(SwitchPreference(context).apply {
                key = "use_dynamic_color"
                setDefaultValue(false)
                setTitle(R.string.use_dynamic_color)
                setSummary(R.string.use_dynamic_color_summary)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            addPreference(DropDownPreference(context).apply {
                key = "dark_theme"
                title = getString(R.string.dark_theme)
                summary = "%s"
                entries = resources.getStringArray(R.array.dark_theme)
                entryValues = resources.getStringArray(R.array.dark_theme_value)
                setDefaultValue("MODE_NIGHT_FOLLOW_SYSTEM")
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })

            addPreference(PreferenceCategory(context).apply {
                title = getString(R.string.other_settings)
                isIconSpaceReserved = false
            })
            addPreference(SwitchPreference(context).apply {
                key = "enable_module_print_logs"
                title = getString(R.string.enable_module_print_logs)
                summary = getString(R.string.enable_module_print_logs_summary)
                setDefaultValue(BuildConfig.DEBUG)
                isChecked = BuildConfig.DEBUG
                isVisible = false
                isIconSpaceReserved = false
            })
            addPreference(SwitchPreference(context).apply {
                key = "tile_auto_start"
                title = getString(R.string.tile_auto_start)
                summary = getString(R.string.tile_auto_start_summary)
                setDefaultValue(true)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    context.dataChannel("com.android.systemui").put("tile_auto_start", newValue)
                    true
                }
            })
            addPreference(SwitchPreference(context).apply {
                key = "hide_function_page_icon"
                title = getString(R.string.hide_function_page_icon)
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, _ ->
                    (activity as MainActivity).restart()
                    true
                }
            })
            addPreference(SwitchPreference(context).apply {
                key = "hide_desktop_module_icon"
                setDefaultValue(false)
                title = getString(R.string.hide_desktop_module_icon)
                summary = getString(R.string.hide_desktop_module_icon_summary)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    context.setComponentDisabled(
                        ComponentName(
                            context.packageName, "${context.packageName}.Hide"
                        ), newValue as Boolean
                    )
                    true
                }
            })

            addPreference(PreferenceCategory(context).apply {
                title = getString(R.string.backup_restore_clear)
                key = "backup_restore_clear"
                isIconSpaceReserved = false
            })
            addPreference(Preference(context).apply {
                title = getString(R.string.backup_data)
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    val fileName = "LuckyTool_" + formatDate("yyyyMMdd_HHmmss") + "_backup.json"
                    backupData.launch(fileName)
                    true
                }
            })
            addPreference(Preference(context).apply {
                title = getString(R.string.restore_data)
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    restoreData.launch("application/json")
                    true
                }
            })
            addPreference(Preference(context).apply {
                title = getString(R.string.clear_all_data)
                summary = getString(R.string.clear_all_data_summary)
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    MaterialAlertDialogBuilder(context).apply {
                        setMessage(getString(R.string.clear_all_data_message))
                        setPositiveButton(android.R.string.ok) { _, _ ->
                            context.clearAllPrefs(ModulePrefs, SettingsPrefs, OtherPrefs)
                            exitProcess(0)
                        }
                        setNeutralButton(android.R.string.cancel, null)
                        show()
                    }
                    true
                }
            })

            addPreference(PreferenceCategory(context).apply {
                setTitle(R.string.about_title)
                isIconSpaceReserved = false
            })
            addPreference(Preference(context).apply {
                setTitle(R.string.open_source)
                setSummary(R.string.open_source_summary)
                isIconSpaceReserved = false
                setOnPreferenceClickListener {
                    navigatePage(R.id.action_nav_setting_to_sourceFragment, title)
                    true
                }
            })
        }
    }
}

class SourceFragment : ModulePreferenceFragment() {
    override fun onCreatePreferencesInModuleApp(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceScreen = preferenceManager.createPreferenceScreen(requireActivity()).apply {
            addPreference(PreferenceCategory(context).apply {
                setTitle(R.string.open_source)
                isIconSpaceReserved = false
            })
            addPreference(Preference(context).apply {
                title = "Xposed"
                summary = "rovo89 , Apache License 2.0"
                isIconSpaceReserved = false
                intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/rovo89/Xposed"))
            })
            addPreference(Preference(context).apply {
                title = "LSPosed"
                summary = "LSPosed , GPL-3.0 License"
                isIconSpaceReserved = false
                intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/LSPosed/LSPosed"))
            })
            addPreference(Preference(context).apply {
                title = "YukiHookAPI"
                summary = "fankes , MIT License"
                isIconSpaceReserved = false
                intent = Intent(
                    Intent.ACTION_VIEW, Uri.parse("https://github.com/fankes/YukiHookAPI")
                )
            })
            addPreference(Preference(context).apply {
                title = "ColorOSNotifyIcon"
                summary = "fankes , AGPL-3.0 License"
                isIconSpaceReserved = false
                intent = Intent(
                    Intent.ACTION_VIEW, Uri.parse("https://github.com/fankes/ColorOSNotifyIcon")
                )
            })
            addPreference(Preference(context).apply {
                title = "ColorOSTool"
                summary = "Oosl , GPL-3.0 License"
                isIconSpaceReserved = false
                intent =
                    Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Oosl/ColorOSTool"))
            })
            addPreference(Preference(context).apply {
                title = "WooBoxForColorOS"
                summary = "Simplicity-Team , GPL-3.0 License"
                isIconSpaceReserved = false
                intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/Simplicity-Team/WooBoxForColorOS")
                )
            })
            addPreference(Preference(context).apply {
                title = "CorePatch"
                summary = "LSPosed , GPL-2.0 license"
                isIconSpaceReserved = false
                intent = Intent(
                    Intent.ACTION_VIEW, Uri.parse("https://github.com/LSPosed/CorePatch")
                )
            })
            addPreference(Preference(context).apply {
                title = "DisableFlagSecure"
                summary = "LSPosed , GPL-3.0 license"
                isIconSpaceReserved = false
                intent = Intent(
                    Intent.ACTION_VIEW, Uri.parse("https://github.com/LSPosed/DisableFlagSecure")
                )
            })
            addPreference(Preference(context).apply {
                title = "FivegTile"
                summary = "libxzr , MIT license"
                isIconSpaceReserved = false
                intent = Intent(
                    Intent.ACTION_VIEW, Uri.parse("https://github.com/libxzr/FivegTile")
                )
            })
            addPreference(Preference(context).apply {
                title = "WooBoxForMIUI"
                summary = "LittleTurtle2333 , GPL-3.0 license"
                isIconSpaceReserved = false
                intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/Simplicity-Team/WooBoxForMIUI")
                )
            })
        }
    }
}
