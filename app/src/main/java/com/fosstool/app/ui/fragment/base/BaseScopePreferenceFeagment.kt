package com.fosstool.app.ui.fragment.base

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.MenuProvider
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import com.highcapable.yukihookapi.hook.xposed.prefs.ui.ModulePreferenceFragment
import com.fosstool.app.R
import com.fosstool.app.utils.ThemeUtils
import com.fosstool.app.utils.restartScopes
import com.fosstool.app.utils.setupMenuProvider

@Suppress("unused")
abstract class BaseScopePreferenceFeagment : ModulePreferenceFragment(), MenuProvider {

    open val scopes = arrayOf<String>()

    open val navAction: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        setupMenuProvider(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleSearchHighlight()
    }

    private fun handleSearchHighlight() {
        val args = arguments ?: return
        val targetTitle = args.getString("search_pref_title") ?: return
        val targetKey = args.getString("search_pref_key")
        val target = findPreferenceByKeyOrTitle(targetKey, targetTitle) ?: return
        try {
            scrollToPreference(target)
        } catch (_: Throwable) {
        }
        listView.post { highlightPreferenceView(target, 0) }
    }

    private fun findPreferenceByKeyOrTitle(key: String?, title: String): Preference? {
        if (!key.isNullOrBlank()) {
            preferenceScreen?.findPreference<Preference>(key)?.let { return it }
        }
        var result: Preference? = null
        fun traverse(group: PreferenceGroup) {
            for (i in 0 until group.preferenceCount) {
                if (result != null) return
                val p = group.getPreference(i)
                if (p is PreferenceGroup) {
                    traverse(p)
                } else if (p.title?.toString() == title) {
                    result = p
                    return
                }
            }
        }
        preferenceScreen?.let { traverse(it) }
        return result
    }

    private fun highlightPreferenceView(target: Preference, retry: Int) {
        val targetTitle = target.title?.toString() ?: return
        val listView = listView
        var found: View? = null
        for (i in 0 until listView.childCount) {
            val child = listView.getChildAt(i)
            val titleView = child.findViewById<TextView>(android.R.id.title)
            if (titleView?.text?.toString() == targetTitle) {
                found = child
                break
            }
        }
        if (found == null) {
            if (retry < 10) {
                listView.postDelayed({ highlightPreferenceView(target, retry + 1) }, 60)
            }
            return
        }
        val child = found
        val background: Drawable? = child.background
        if (background is RippleDrawable) {
            val ripple = background
            ripple.setState(intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled))
            Handler(Looper.getMainLooper()).postDelayed({
                ripple.setState(intArrayOf())
            }, 300)
        }
    }

    override fun onCreatePreferencesInModuleApp(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = "ModulePrefs"
        val screen = preferenceManager.createPreferenceScreen(requireActivity())
        for (pref in h0(requireContext())) {
            screen.addPreference(pref)
        }
        preferenceScreen = screen
    }

    open fun isEnableRestartMenu(): Boolean = false

    open fun isEnableOpenMenu(): Boolean = false

    open fun callOpenMenu() {}

    open fun h0(ctx: android.content.Context): ArrayList<androidx.preference.Preference> = arrayListOf()

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.add(0, 1, 0, getString(R.string.menu_reboot)).apply {
            setIcon(R.drawable.ic_baseline_refresh_24)
            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            isVisible = isEnableRestartMenu()
            if (ThemeUtils.isNightMode(resources.configuration)) {
                iconTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
        menu.add(0, 2, 0, getString(R.string.common_words_open)).apply {
            setIcon(R.drawable.baseline_open_in_new_24)
            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            isVisible = isEnableOpenMenu()
            if (ThemeUtils.isNightMode(resources.configuration)) {
                iconTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == 1) requireActivity().restartScopes(scopes)
        if (menuItem.itemId == 2) callOpenMenu()
        return true
    }
}
