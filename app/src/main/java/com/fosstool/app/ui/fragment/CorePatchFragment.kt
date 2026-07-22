package com.fosstool.app.ui.fragment

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.SwitchPreference
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.fosstool.app.R
import com.fosstool.app.ui.fragment.base.BaseScopePreferenceFeagment
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.dialogCentered

class CorePatchFragment : BaseScopePreferenceFeagment() {
    override val scopes = arrayOf("android")
    override fun onCreatePreferencesInModuleApp(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = ModulePrefs
        preferenceScreen = preferenceManager.createPreferenceScreen(requireActivity()).apply {
            addPreference(Preference(context).apply {
                title = getString(R.string.ColorOSCorePatchTip)
                key = "ColorOSCorePatchTip"
                isIconSpaceReserved = false
            })
            addPreference(PreferenceCategory(context).apply {
                setTitle(R.string.corepatch)
                setSummary(R.string.corepatch_summary)
                key = "CorePatch"
                isIconSpaceReserved = false
            })
            addPreference(SwitchPreference(context).apply {
                setTitle(R.string.downgr)
                setSummary(R.string.downgr_summary)
                key = "downgrade"
                setDefaultValue(true)
                isIconSpaceReserved = false
            })
            addPreference(SwitchPreference(context).apply {
                setTitle(R.string.authcreak)
                setSummary(R.string.authcreak_summary)
                key = "authcreak"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            addPreference(SwitchPreference(context).apply {
                setTitle(R.string.digestCreak)
                setSummary(R.string.digestCreak_summary)
                key = "digestCreak"
                setDefaultValue(true)
                isIconSpaceReserved = false
            })
            addPreference(SwitchPreference(context).apply {
                setTitle(R.string.exactSigCheck)
                setSummary(R.string.exactSigCheck_summary)
                key = "exactSigCheck"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            addPreference(SwitchPreference(context).apply {
                setTitle(R.string.UsePreSig)
                setSummary(R.string.UsePreSig_summary)
                key = "UsePreSig"
                setDefaultValue(false)
                isIconSpaceReserved = false
                setOnPreferenceChangeListener { _, newValue ->
                    if (newValue == true) {
                        MaterialAlertDialogBuilder(context, dialogCentered).apply {
                            setMessage(R.string.usepresig_warn)
                            setPositiveButton(android.R.string.ok, null)
                            show()
                        }
                    }
                    true
                }
            })
            addPreference(SwitchPreference(context).apply {
                setTitle(R.string.bypassBlock)
                setSummary(R.string.bypassBlock_summary)
                key = "bypassBlock"
                setDefaultValue(true)
                isIconSpaceReserved = false
            })
            addPreference(SwitchPreference(context).apply {
                setTitle(R.string.sharedUser)
                setSummary(R.string.sharedUser_summary)
                key = "sharedUser"
                setDefaultValue(false)
                isIconSpaceReserved = false
            })
            addPreference(SwitchPreference(context).apply {
                setTitle(R.string.disableVerificationAgent)
                setSummary(R.string.disableVerificationAgent_summary)
                key = "disableVerificationAgent"
                setDefaultValue(true)
                isIconSpaceReserved = false
            })
        }
    }
}
