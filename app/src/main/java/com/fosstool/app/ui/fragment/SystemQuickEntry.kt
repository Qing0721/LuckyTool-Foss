package com.fosstool.app.ui.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.fosstool.app.R
import com.fosstool.app.utils.ShellUtils
import com.fosstool.app.utils.jumpBatteryInfo
import com.fosstool.app.utils.jumpEngineermode
import com.fosstool.app.utils.jumpRunningApp

class SystemQuickEntry : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = requireContext()
        val screen: PreferenceScreen = preferenceManager.createPreferenceScreen(context)

        val debugCategory = PreferenceCategory(context).apply {
            title = getString(R.string.SystemDebuggingRelated)
            isIconSpaceReserved = false
        }
        screen.addPreference(debugCategory)

        debugCategory.addPreference(Preference(context).apply {
            title = getString(R.string.engineering_mode)
            isIconSpaceReserved = false
            setOnPreferenceClickListener { jumpEngineermode(context); true }
        })
        debugCategory.addPreference(Preference(context).apply {
            title = getString(R.string.charging_test)
            isIconSpaceReserved = false
            setOnPreferenceClickListener { jumpBatteryInfo(context); true }
        })
        debugCategory.addPreference(Preference(context).apply {
            title = getString(R.string.developer_option)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).setPackage("com.android.settings")
                )
                true
            }
        })
        debugCategory.addPreference(Preference(context).apply {
            title = getString(R.string.system_interface_adjustment)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent().setClassName("com.android.systemui", "com.android.systemui.DemoMode"),
                    "am start -n com.android.systemui/.DemoMode"
                )
                true
            }
        })
        debugCategory.addPreference(Preference(context).apply {
            title = getString(R.string.AOSPSettingsPage)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent().setClassName(
                        "com.android.settings",
                        "com.android.settings.homepage.DeepLinkHomepageActivityInternal"
                    ),
                    "am start -n com.android.settings/.homepage.DeepLinkHomepageActivityInternal"
                )
                true
            }
        })
        debugCategory.addPreference(Preference(context).apply {
            title = getString(R.string.process_manager)
            isIconSpaceReserved = false
            setOnPreferenceClickListener { jumpRunningApp(context); true }
        })
        debugCategory.addPreference(Preference(context).apply {
            title = getString(R.string.very_dark_mode)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent("android.settings.REDUCE_BRIGHT_COLORS_SETTINGS").setPackage("com.android.settings")
                )
                true
            }
        })
        debugCategory.addPreference(Preference(context).apply {
            title = getString(R.string.battery_health)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent().setClassName(
                        "com.oplus.battery", "com.oplus.powermanager.fuelgaue.BatteryHealthActivity"
                    ),
                    "am start -n com.oplus.battery/com.oplus.powermanager.fuelgaue.BatteryHealthActivity"
                )
                true
            }
        })
        debugCategory.addPreference(Preference(context).apply {
            title = getString(R.string.battery_optimization)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(Intent("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS"))
                true
            }
        })
        debugCategory.addPreference(Preference(context).apply {
            title = getString(R.string.camera_algo_page)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent().setClassName(
                        "com.oplus.camera", "com.oplus.camera.ui.menu.algoswitch.AlgoSwitchActivity"
                    ),
                    "am start -n com.oplus.camera/.ui.menu.algoswitch.AlgoSwitchActivity"
                )
                true
            }
        })

        val hideCategory = PreferenceCategory(context).apply {
            title = getString(R.string.HidePageRelated)
            isIconSpaceReserved = false
        }
        screen.addPreference(hideCategory)

        hideCategory.addPreference(Preference(context).apply {
            title = getString(R.string.about_device)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent().setClassName(
                        "com.android.settings", "com.android.settings.Settings\$AboutDeviceActivity"
                    ),
                    "am start -n com.android.settings/.Settings\$AboutDeviceActivity"
                )
                true
            }
        })
        hideCategory.addPreference(Preference(context).apply {
            title = getString(R.string.app_management)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS).setPackage("com.android.settings")
                )
                true
            }
        })
        hideCategory.addPreference(Preference(context).apply {
            title = getString(R.string.permission_privacy)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent().setClassName(
                        "com.android.settings", "com.android.settings.Settings\$PrivacySettingsActivity"
                    ),
                    "am start -n com.android.settings/.Settings\$PrivacySettingsActivity"
                )
                true
            }
        })
        hideCategory.addPreference(Preference(context).apply {
            title = getString(R.string.connection_sharing)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent().setClassName(
                        "com.android.settings", "com.android.settings.Settings\$ConnectionSharingActivity"
                    ),
                    "am start -n com.android.settings/.Settings\$ConnectionSharingActivity"
                )
                true
            }
        })
        hideCategory.addPreference(Preference(context).apply {
            title = getString(R.string.developer_option)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS).setPackage("com.android.settings")
                )
                true
            }
        })
        hideCategory.addPreference(Preference(context).apply {
            title = getString(R.string.display_settings)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent(Settings.ACTION_DISPLAY_SETTINGS).setPackage("com.android.settings")
                )
                true
            }
        })
        hideCategory.addPreference(Preference(context).apply {
            title = getString(R.string.lock_screen_settings)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent().setClassName(
                        "com.android.settings", "com.android.settings.Settings\$LockScreenSettingsActivity"
                    ),
                    "am start -n com.android.settings/.Settings\$LockScreenSettingsActivity"
                )
                true
            }
        })
        hideCategory.addPreference(Preference(context).apply {
            title = getString(R.string.other_settings_entry)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent().setClassName(
                        "com.android.settings", "com.android.settings.Settings\$OtherSettingsActivity"
                    ),
                    "am start -n com.android.settings/.Settings\$OtherSettingsActivity"
                )
                true
            }
        })
        hideCategory.addPreference(Preference(context).apply {
            title = getString(R.string.other_preferences)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent().setClassName(
                        "com.android.settings", "com.android.settings.Settings\$OtherPreferenceSettingsActivity"
                    ),
                    "am start -n com.android.settings/.Settings\$OtherPreferenceSettingsActivity"
                )
                true
            }
        })
        hideCategory.addPreference(Preference(context).apply {
            title = getString(R.string.password_security)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent().setClassName(
                        "com.android.settings", "com.android.settings.Settings\$PasswordSecuritySettingsActivity"
                    ),
                    "am start -n com.android.settings/.Settings\$PasswordSecuritySettingsActivity"
                )
                true
            }
        })
        hideCategory.addPreference(Preference(context).apply {
            title = getString(R.string.sound_settings)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent(Settings.ACTION_SOUND_SETTINGS).setPackage("com.android.settings")
                )
                true
            }
        })
        hideCategory.addPreference(Preference(context).apply {
            title = getString(R.string.status_bar_settings)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent().setClassName(
                        "com.android.settings", "com.android.settings.Settings\$StatusBarSettingsActivity"
                    ),
                    "am start -n com.android.settings/.Settings\$StatusBarSettingsActivity"
                )
                true
            }
        })
        hideCategory.addPreference(Preference(context).apply {
            title = getString(R.string.share_settings)
            isIconSpaceReserved = false
            setOnPreferenceClickListener {
                tryStart(
                    Intent().setClassName(
                        "com.android.settings", "com.android.settings.Settings\$ShareSettingsActivity"
                    ),
                    "am start -n com.android.settings/.Settings\$ShareSettingsActivity"
                )
                true
            }
        })

        setPreferenceScreen(screen)
    }

    private fun tryStart(intent: Intent, amStartFallback: String? = null) {
        val context = requireContext()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            amStartFallback?.let { ShellUtils.execCommand(it, true) }
        } catch (_: SecurityException) {
            amStartFallback?.let { ShellUtils.execCommand(it, true) }
        }
    }
}
