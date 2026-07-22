package com.fosstool.app.ui.fragment

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.fosstool.app.R
import com.fosstool.app.databinding.FragmentHideIntentApplistLayoutBinding
import com.fosstool.app.ui.adapter.HideAppIntentAdapter
import com.fosstool.app.utils.AppInfo
import com.fosstool.app.utils.AppIntentInfo
import com.fosstool.app.utils.IntentAppUpdate
import com.fosstool.app.utils.IntentPrefs
import com.fosstool.app.utils.IntentType
import com.fosstool.app.utils.getBoolean
import com.fosstool.app.utils.getStringSet
import com.fosstool.app.utils.putBoolean
import com.fosstool.app.utils.putStringSet
import com.fosstool.app.utils.removeKey
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.fosstool.app.databinding.DialogSortFilterSelectorLayoutBinding
import com.highcapable.yukihookapi.hook.factory.dataChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HideAppIntentFragment : Fragment() {

    private var _binding: FragmentHideIntentApplistLayoutBinding? = null
    private val binding get() = _binding!!

    private var showSystemApps = true
    private val typeFilter: MutableSet<IntentType> = mutableSetOf()

    private var sortMode = 0
    private var reverseSort = false

    private val allApps: ArrayList<AppInfo> = ArrayList()
    private val sortMetaMap: MutableMap<String, SortMeta> = mutableMapOf()
    private val candidateMap: MutableMap<String, MutableList<AppIntentInfo>> = mutableMapOf()
    private val candidateResolveMap: MutableMap<AppIntentInfo, ResolveInfo> = mutableMapOf()
    private val selectedMap: MutableMap<String, MutableList<AppIntentInfo>> = mutableMapOf()
    private val enabledSet: MutableSet<String> = mutableSetOf()

    private lateinit var adapter: HideAppIntentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHideIntentApplistLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()

        binding.intentEnableSwitch.isChecked =
            ctx.getBoolean(IntentPrefs, KEY_CONFIG_LIST, false)
        binding.intentEnableSwitch.setOnCheckedChangeListener { button, checked ->
            if (!button.isPressed) return@setOnCheckedChangeListener
            ctx.putBoolean(IntentPrefs, KEY_CONFIG_LIST, checked)
            runCatching {
                ctx.dataChannel("android").put(KEY_CONFIG_LIST, checked)
            }
        }

        binding.searchViewLayout.setEndIconOnClickListener { showFilterDialog() }
        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.swipeRefreshLayout.setOnRefreshListener { reload() }

        adapter = HideAppIntentAdapter(ctx) { appInfo, group ->
            showActivitySelector(appInfo, group)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(ctx)
            this.adapter = this@HideAppIntentFragment.adapter
        }

        typeFilter.addAll(IntentType.SHARE_GROUP)
        typeFilter.addAll(IntentType.TEXT_GROUP)
        typeFilter.addAll(IntentType.OPEN_WITH_GROUP)
        typeFilter.addAll(IntentType.BROWSER_GROUP)

        reload()
    }

    @Suppress("DEPRECATION")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.add(0, MENU_SELECT_ALL_SHARE, 0, getString(R.string.select_all_share_intent))
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.add(0, MENU_SELECT_ALL_TEXT, 0, getString(R.string.select_all_text_intent))
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.add(0, MENU_SELECT_ALL_OPEN_WITH, 0, getString(R.string.select_all_open_with_intent))
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.add(0, MENU_SELECT_ALL_BROWSER, 0, getString(R.string.select_all_browser_intent))
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.add(0, MENU_CLEAR_ALL, 0, getString(R.string.clear_intent_config_data))
            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
    }

    @Suppress("DEPRECATION")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            MENU_SELECT_ALL_SHARE -> { batchSelect(IntentType.SHARE_GROUP); true }
            MENU_SELECT_ALL_TEXT -> { batchSelect(IntentType.TEXT_GROUP); true }
            MENU_SELECT_ALL_OPEN_WITH -> { batchSelect(IntentType.OPEN_WITH_GROUP); true }
            MENU_SELECT_ALL_BROWSER -> { batchSelect(IntentType.BROWSER_GROUP); true }
            MENU_CLEAR_ALL -> { confirmClearAll(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showFilterDialog() {
        val ctx = requireContext()
        val dialogBinding = DialogSortFilterSelectorLayoutBinding.inflate(
            LayoutInflater.from(ctx), null, false
        )

        val sortOptions = listOf(
            R.string.intent_sort_app_name to 0,
            R.string.intent_sort_package_name to 1,
            R.string.intent_sort_app_size to 2,
            R.string.intent_sort_install_time to 3,
            R.string.intent_sort_last_update_time to 4,
            R.string.intent_sort_target_sdk to 5,
        )
        dialogBinding.sortChipGroup.isSingleSelection = true
        for ((labelRes, mode) in sortOptions) {
            val chip = Chip(ctx).apply {
                text = getString(labelRes)
                isCheckable = true
                isClickable = true
                isChecked = mode == sortMode
                setOnClickListener {
                    sortMode = mode
                    for (i in 0 until dialogBinding.sortChipGroup.childCount) {
                        (dialogBinding.sortChipGroup.getChildAt(i) as? Chip)?.isChecked = false
                    }
                    isChecked = true
                    applySort()
                }
            }
            dialogBinding.sortChipGroup.addView(chip)
        }

        dialogBinding.reverseCheckbox.isChecked = reverseSort
        dialogBinding.reverseCheckbox.setOnCheckedChangeListener { button, checked ->
            if (!button.isPressed) return@setOnCheckedChangeListener
            reverseSort = checked
            applySort()
        }

        dialogBinding.filterChipGroup.isSingleSelection = false
        val systemChip = Chip(ctx).apply {
            text = getString(R.string.intent_filter_system_apps)
            isCheckable = true
            isClickable = true
            isChecked = showSystemApps
            setOnCheckedChangeListener { button, checked ->
                if (!button.isPressed) return@setOnCheckedChangeListener
                showSystemApps = checked
                applyFilter()
            }
        }
        dialogBinding.filterChipGroup.addView(systemChip)
        val groupLabels = listOf(
            IntentType.SHARE_GROUP to R.string.intent_group_share,
            IntentType.TEXT_GROUP to R.string.intent_group_text,
            IntentType.OPEN_WITH_GROUP to R.string.intent_group_open_with,
            IntentType.BROWSER_GROUP to R.string.intent_group_browser,
        )
        for ((group, labelRes) in groupLabels) {
            val chip = Chip(ctx).apply {
                text = getString(labelRes)
                isCheckable = true
                isClickable = true
                isChecked = group.any { it in typeFilter }
                setOnCheckedChangeListener { button, checked ->
                    if (!button.isPressed) return@setOnCheckedChangeListener
                    if (checked) typeFilter.addAll(group) else typeFilter.removeAll(group.toSet())
                    applyFilter()
                }
            }
            dialogBinding.filterChipGroup.addView(chip)
        }

        BottomSheetDialog(ctx).apply {
            setContentView(dialogBinding.root)
            show()
        }
    }

    private fun reload() {
        val ctx = requireContext()
        binding.swipeRefreshLayout.isRefreshing = true
        binding.searchViewLayout.isEnabled = false
        lifecycleScope.launch(Dispatchers.IO) {
            val pm = ctx.packageManager
            val queryTypes = IntentType.values().filter { it != IntentType.UNKNOWN }

            val candidatesByPkg: MutableMap<String, MutableList<AppIntentInfo>> = mutableMapOf()
            val resolveMap: MutableMap<AppIntentInfo, ResolveInfo> = mutableMapOf()
            val pkgSet = LinkedHashSet<String>()
            for (type in queryTypes) {
                val intent = HideAppIntentAdapter.buildGlobalQueryIntent(type)
                val infos = runCatching {
                    if (android.os.Build.VERSION.SDK_INT >= 33) {
                        pm.queryIntentActivities(
                            intent, PackageManager.ResolveInfoFlags.of(HideAppIntentAdapter.QUERY_FLAGS.toLong())
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        pm.queryIntentActivities(intent, HideAppIntentAdapter.QUERY_FLAGS)
                    }
                }.getOrDefault(emptyList())
                for (ri in infos) {
                    val pkg = ri.activityInfo.packageName
                    val name = runCatching { ri.loadLabel(pm)?.toString() }.getOrNull()
                        ?: ri.activityInfo.name
                    val info = AppIntentInfo(
                        name = name,
                        packName = pkg,
                        action = intent.action ?: "",
                        activity = ri.activityInfo.name,
                        type = type,
                    )
                    candidatesByPkg.getOrPut(pkg) { mutableListOf() }.add(info)
                    resolveMap[info] = ri
                    pkgSet.add(pkg)
                }
            }

            val apps = ArrayList<AppInfo>()
            val sortMetas = mutableMapOf<String, SortMeta>()
            for (pkg in pkgSet) {
                val info = runCatching {
                    if (android.os.Build.VERSION.SDK_INT >= 33) {
                        pm.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0))
                    } else {
                        @Suppress("DEPRECATION")
                        pm.getPackageInfo(pkg, 0)
                    }
                }.getOrNull() ?: continue
                val app = runCatching { info.applicationInfo }.getOrNull() ?: continue
                val label = app.loadLabel(pm)
                apps.add(AppInfo(app.loadIcon(pm), label, app.packageName))
                val size = runCatching {
                    java.io.File(app.sourceDir).length()
                }.getOrDefault(0L)
                sortMetas[pkg] = SortMeta(
                    packName = pkg,
                    appName = label.toString().lowercase(),
                    size = size,
                    installTime = info.firstInstallTime,
                    updateTime = info.lastUpdateTime,
                    targetSdk = app.targetSdkVersion,
                )
            }

            val enabled = ctx.getStringSet(IntentPrefs, KEY_ENABLED_LIST, emptySet()) ?: emptySet()
            val selectedByPkg: MutableMap<String, MutableList<AppIntentInfo>> = mutableMapOf()
            for (pkg in pkgSet) {
                val raw = ctx.getStringSet(IntentPrefs, pkg, emptySet()) ?: emptySet()
                if (raw.isEmpty()) continue
                val candidates = candidatesByPkg[pkg].orEmpty()
                for (json in raw) {
                    val cfg = AppIntentInfo.fromJson(json) ?: continue
                    val matched = candidates.firstOrNull { c ->
                        c.packName == cfg.packName && c.activity == cfg.activity &&
                            c.type == cfg.type && c.action == cfg.action
                    } ?: cfg
                    selectedByPkg.getOrPut(pkg) { mutableListOf() }.add(matched)
                }
            }

            withContext(Dispatchers.Main) {
                allApps.clear()
                allApps.addAll(apps)
                sortMetaMap.clear()
                sortMetaMap.putAll(sortMetas)
                candidateMap.clear()
                candidateMap.putAll(candidatesByPkg)
                candidateResolveMap.clear()
                candidateResolveMap.putAll(resolveMap)
                selectedMap.clear()
                selectedMap.putAll(selectedByPkg)
                enabledSet.clear()
                enabledSet.addAll(enabled)
                adapter.setConfig(candidateMap, selectedMap)
                applySort()
                binding.swipeRefreshLayout.isRefreshing = false
                binding.searchViewLayout.isEnabled = true
            }
        }
    }

    private fun applySort() {
        val sorted = when (sortMode) {
            1 -> allApps.sortedBy { it.packName.lowercase() }
            2 -> allApps.sortedByDescending { sortMetaMap[it.packName]?.size ?: 0L }
            3 -> allApps.sortedByDescending { sortMetaMap[it.packName]?.installTime ?: 0L }
            4 -> allApps.sortedByDescending { sortMetaMap[it.packName]?.updateTime ?: 0L }
            5 -> allApps.sortedByDescending { sortMetaMap[it.packName]?.targetSdk ?: 0 }
            else -> allApps.sortedBy { sortMetaMap[it.packName]?.appName ?: it.appName.toString().lowercase() }
        }
        allApps.clear()
        if (reverseSort) allApps.addAll(sorted.reversed()) else allApps.addAll(sorted)
        adapter.setAllData(allApps)
        applyFilter()
    }

    private fun applyFilter() {
        val pm = requireContext().packageManager
        val key = binding.searchView.text?.toString()?.lowercase() ?: ""
        val allTypeFilter = IntentType.values().filter { it != IntentType.UNKNOWN }.toSet()
        val typeFilterEnabled = typeFilter.isNotEmpty() && typeFilter.size < allTypeFilter.size
        val filtered = ArrayList<AppInfo>()
        for (app in allApps) {
            if (key.isNotEmpty() &&
                !app.appName.toString().lowercase().contains(key) &&
                !app.packName.lowercase().contains(key)
            ) continue
            val isSystem = runCatching {
                pm.getApplicationInfo(app.packName, 0).flags and
                    android.content.pm.ApplicationInfo.FLAG_SYSTEM != 0
            }.getOrDefault(false)
            if (!showSystemApps && isSystem) continue
            if (typeFilterEnabled) {
                val hasMatchedType = candidateMap[app.packName]?.any { it.type in typeFilter } ?: false
                if (!hasMatchedType) continue
            }
            filtered.add(app)
        }
        adapter.setFilterData(filtered)
        binding.emptyView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun batchSelect(group: List<IntentType>) {
        val ctx = requireContext()
        val toUpdate = mutableSetOf<String>()
        for (pkg in candidateMap.keys) {
            val groupCandidates = candidateMap[pkg].orEmpty().filter { it.type in group }
            if (groupCandidates.isEmpty()) continue
            val others = selectedMap[pkg].orEmpty().filter { it.type !in group }
            val merged = (others + groupCandidates).distinct()
            selectedMap[pkg] = merged.toMutableList()
            persistPackageConfig(pkg, merged)
            toUpdate.add(pkg)
        }
        adapter.setConfig(candidateMap, selectedMap)
        for (pkg in toUpdate) sendBroadcastUpdate(pkg, enabledSet.contains(pkg))
    }

    private fun confirmClearAll() {
        val ctx = requireContext()
        MaterialAlertDialogBuilder(ctx).apply {
            setTitle(R.string.clear_intent_config_data)
            setMessage(R.string.clear_intent_config_data_confirm)
            setPositiveButton(android.R.string.ok) { _, _ ->
                for (pkg in candidateMap.keys) {
                    ctx.removeKey(IntentPrefs, pkg)
                }
                ctx.putStringSet(IntentPrefs, KEY_ENABLED_LIST, emptySet())
                selectedMap.clear()
                enabledSet.clear()
                adapter.setConfig(candidateMap, selectedMap)
                for (pkg in candidateMap.keys) sendBroadcastUpdate(pkg, false)
            }
            setNegativeButton(android.R.string.cancel, null)
            show()
        }
    }

    private fun showActivitySelector(appInfo: AppInfo, group: List<IntentType>) {
        val ctx = requireContext()
        val candidates = candidateMap[appInfo.packName].orEmpty()
            .filter { it.type in group }
            .mapNotNull { info ->
                candidateResolveMap[info]?.let { ri -> CandidateEntry(info, ri) }
            }
        val preSelected = selectedMap[appInfo.packName].orEmpty()
            .filter { it.type in group }
            .toSet()
        ActivitySelectorDialog(
            ctx, appInfo, group, candidates, preSelected,
        ) { selected ->
            persistSelection(appInfo, group, selected)
        }.show()
    }

    private fun persistSelection(
        appInfo: AppInfo,
        group: List<IntentType>,
        selected: List<AppIntentInfo>,
    ) {
        val pkg = appInfo.packName
        val others = selectedMap[pkg].orEmpty().filter { it.type !in group }
        val merged = (others + selected).distinct()
        selectedMap[pkg] = merged.toMutableList()
        persistPackageConfig(pkg, merged)
        adapter.setConfig(candidateMap, selectedMap)
    }

    private fun persistPackageConfig(pkg: String, configs: List<AppIntentInfo>) {
        val ctx = requireContext()
        if (configs.isEmpty()) {
            ctx.removeKey(IntentPrefs, pkg)
        } else {
            ctx.putStringSet(IntentPrefs, pkg, configs.map { it.toJson() }.toSet())
        }
        val hasConfig = configs.isNotEmpty()
        val current = enabledSet.toMutableSet()
        if (hasConfig) current.add(pkg) else current.remove(pkg)
        enabledSet.clear()
        enabledSet.addAll(current)
        ctx.putStringSet(IntentPrefs, KEY_ENABLED_LIST, current)
        sendBroadcastUpdate(pkg, hasConfig)
    }

    private fun sendBroadcastUpdate(packName: String, enabled: Boolean) {
        val ctx = requireContext()
        runCatching {
            ctx.dataChannel("android").put(ACTION_UPDATE_APP_CONFIG, packName)
        }
        runCatching {
            ctx.dataChannel("android").put(ACTION_UPDATE_APPS, IntentAppUpdate(packName, enabled))
        }
    }

    companion object {
        private const val KEY_CONFIG_LIST = "custom_config_app_intent_list"
        private const val KEY_ENABLED_LIST = "enable_app_hide_list"

        const val ACTION_UPDATE_APP_CONFIG =
            "custom_config_app_intent_list_update_app_config"
        const val ACTION_UPDATE_APPS =
            "custom_config_app_intent_list_update_apps"
        const val EXTRA_PACKAGE_NAME = "packageName"
        const val EXTRA_ENABLED = "enabled"

        private const val MENU_SELECT_ALL_SHARE = 1
        private const val MENU_SELECT_ALL_TEXT = 2
        private const val MENU_SELECT_ALL_OPEN_WITH = 3
        private const val MENU_SELECT_ALL_BROWSER = 4
        private const val MENU_CLEAR_ALL = 10
    }
}
