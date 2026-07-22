package com.fosstool.app.ui.fragment

import android.content.Context
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.fosstool.app.R
import com.fosstool.app.ui.fragment.base.BaseScopePreferenceFeagment
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.openApp

class AlphaBackupPro : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_xposed_to_alphaBackupPro
    override val scopes = arrayOf("com.ruet_cse_1503050.ragib.appbackup.pro")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(
                SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.remove_pro_license)
                    key = "remove_check_license"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                }
            )
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class KsWeb : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_ksWeb
    override val scopes = arrayOf("ru.kslabs.ksweb")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(
                SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.remove_pro_license)
                    key = "ksweb_remove_check_license"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                }
            )
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}

class ADM : BaseScopePreferenceFeagment() {
    override val navAction = R.id.action_nav_function_to_ADM
    override val scopes = arrayOf("com.dv.adm")
    override fun h0(ctx: Context): ArrayList<Preference> {
        return ArrayList<Preference>().apply {
            add(
                SwitchPreference(ctx).apply {
                    title = ctx.getString(R.string.adm_unlock_pro)
                    key = "adm_unlock_pro"
                    setDefaultValue(false)
                    isIconSpaceReserved = false
                }
            )
            add(
                androidx.preference.EditTextPreference(ctx).apply {
                    title = ctx.getString(R.string.adm_unlock_more_threads)
                    summary = ctx.getString(R.string.adm_unlock_more_threads_summary)
                    key = "adm_unlock_more_threads"
                    setDefaultValue("0")
                    isIconSpaceReserved = false
                }
            )
        }
    }

    override fun isEnableRestartMenu(): Boolean = true
    override fun isEnableOpenMenu(): Boolean = true
    override fun callOpenMenu() = requireActivity().openApp(scopes)
}
