package com.fosstool.app.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Xml
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.fosstool.app.R
import com.fosstool.app.data.MemcConfigActivity
import com.fosstool.app.data.MemcConfigPackage
import com.fosstool.app.databinding.DialogAppSelectorBinding
import com.fosstool.app.databinding.DialogMemcConfigLayoutBinding
import com.fosstool.app.databinding.FragmentMemcConfigBinding
import com.fosstool.app.databinding.FragmentMemcListLayoutBinding
import com.fosstool.app.databinding.LayoutMemcConfigItemBinding
import com.fosstool.app.ui.adapter.AppSelectorAdapter
import com.fosstool.app.utils.AppInfo
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.PackageUtils
import com.fosstool.app.utils.dialogCentered
import com.fosstool.app.utils.getStringSet
import com.fosstool.app.utils.putStringSet
import com.fosstool.app.utils.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.xmlpull.v1.XmlPullParser

class MemcConfigFragment : Fragment() {

    companion object {
        private const val MENU_IMPORT = 1
        private const val MENU_RESET = 2
    }

    private var _binding: FragmentMemcConfigBinding? = null
    private val binding get() = _binding!!

    private lateinit var importLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        importLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) importXml(uri)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemcConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ensurePresetIfEmpty()
        binding.memcViewPager.adapter = MemcPagerAdapter(requireActivity())
        binding.memcViewPager.offscreenPageLimit = -1
        TabLayoutMediator(
            binding.memcTabLayout, binding.memcViewPager
        ) { tab, position ->
            tab.text = when (position) {
                0 -> getString(R.string.memc_tab_packages)
                else -> getString(R.string.memc_tab_activitys)
            }
        }.attach()
    }

    private fun ensurePresetIfEmpty() {
        val ctx = requireContext()
        val pkgs = ctx.getStringSet(ModulePrefs, "memc_config_package_list", emptySet())
        val acts = ctx.getStringSet(ModulePrefs, "memc_config_activity_list", emptySet())
        if (!pkgs.isNullOrEmpty() && !acts.isNullOrEmpty()) return
        runCatching {
            resources.openRawResource(R.raw.memc_preset).use { input ->
                val (packages, activities) = parseMemcXml(input)
                writeMemcPrefs(ctx, packages, activities)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.add(0, MENU_IMPORT, 0, getString(R.string.memc_menu_import_xml))
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        menu.add(0, MENU_RESET, 0, getString(R.string.memc_menu_reset))
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            MENU_IMPORT -> {
                importLauncher.launch(arrayOf("text/xml", "*/*"))
                true
            }
            MENU_RESET -> {
                showResetDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun importXml(uri: Uri) {
        val ctx = requireContext()
        try {
            ctx.contentResolver.openInputStream(uri)?.use { input ->
                val (packages, activities) = parseMemcXml(input)
                writeMemcPrefs(ctx, packages, activities)
                ctx.toast(getString(R.string.memc_import_success))
            }
        } catch (e: Throwable) {
            ctx.toast(getString(R.string.memc_import_failed, e.message ?: ""))
        }
    }

    private fun showResetDialog() {
        val ctx = requireContext()
        val presets = arrayOf("x7", "x7p")
        MaterialAlertDialogBuilder(ctx)
            .setTitle(getString(R.string.memc_reset_confirm))
            .setSingleChoiceItems(presets, -1) { dialog, which ->
                dialog.dismiss()
                resetToPreset(presets[which])
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun resetToPreset(presetName: String) {
        val ctx = requireContext()
        try {
            resources.openRawResource(R.raw.memc_preset).use { input ->
                val (packages, activities) = parseMemcXml(input)
                writeMemcPrefs(ctx, packages, activities)
                ctx.toast(getString(R.string.memc_reset_success))
            }
        } catch (e: Throwable) {
            ctx.toast(getString(R.string.memc_import_failed, e.message ?: ""))
        }
    }

    private fun parseMemcXml(input: java.io.InputStream): Pair<Set<String>, Set<String>> {
        val packages = mutableSetOf<String>()
        val activities = mutableSetOf<String>()
        val parser = Xml.newPullParser()
        parser.setInput(input, "UTF-8")
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (parser.name) {
                    "mConfigPackage" -> {
                        val rate = parser.getAttributeValue(null, "rate") ?: ""
                        val type = parser.getAttributeValue(null, "type") ?: ""
                        val pkg = parser.nextText()
                        if (pkg.isNotBlank()) {
                            val entry = MemcConfigPackage(pkg, rate, type)
                            runCatching {
                                packages.add(Json.encodeToString(MemcConfigPackage.serializer(), entry))
                            }
                        }
                    }
                    "mConfigActivity" -> {
                        val type = parser.getAttributeValue(null, "type") ?: ""
                        val text = parser.nextText()
                        if (text.isNotBlank()) {
                            val slashIdx = text.indexOf('/')
                            if (slashIdx > 0) {
                                val pkg = text.substring(0, slashIdx)
                                val act = text.substring(slashIdx + 1)
                                val entry = MemcConfigActivity(pkg, act, type)
                                runCatching {
                                    activities.add(Json.encodeToString(MemcConfigActivity.serializer(), entry))
                                }
                            }
                        }
                    }
                }
            }
            event = parser.next()
        }
        return packages.toSet() to activities.toSet()
    }

    private fun writeMemcPrefs(ctx: Context, packages: Set<String>, activities: Set<String>) {
        ctx.putStringSet(ModulePrefs, "memc_config_package_list", packages)
        ctx.putStringSet(ModulePrefs, "memc_config_activity_list", activities)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private class MemcPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> MemcPackageListFragment()
            else -> MemcActivityListFragment()
        }
    }
}

class MemcPackageListFragment : Fragment() {

    private var _binding: FragmentMemcListLayoutBinding? = null
    private val binding get() = _binding!!

    private val dataList: ArrayList<MemcConfigPackage> = ArrayList()
    private var filtered: List<MemcConfigPackage> = emptyList()
    private lateinit var adapter: PackageItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemcListLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()

        binding.searchViewLayout.apply {
            hint = ctx.getString(R.string.memc_search_hint_packages)
            isHintEnabled = true
            isHintAnimationEnabled = true
        }
        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.swipeRefreshLayout.setOnRefreshListener { reload() }

        binding.addButton.setOnClickListener { showEditDialog(null) }

        adapter = PackageItemAdapter(dataList) { item -> showEditDialog(item) }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(ctx)
            this.adapter = this@MemcPackageListFragment.adapter
        }

        reload()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun reload() {
        binding.swipeRefreshLayout.isRefreshing = true
        binding.searchView.text = null
        dataList.clear()
        val raw = requireContext().getStringSet(ModulePrefs, "memc_config_package_list", emptySet())
        for (json in raw.orEmpty()) {
            val v = runCatching { Json.decodeFromString<MemcConfigPackage>(json) }.getOrNull()
            if (v != null) dataList.add(v)
        }
        applyFilter("")
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun applyFilter(key: String) {
        filtered = if (key.isBlank()) dataList.toList()
        else {
            val k = key.lowercase()
            dataList.filter {
                it.packName.lowercase().contains(k) ||
                    it.rate.lowercase().contains(k) ||
                    it.type.lowercase().contains(k)
            }
        }
        adapter.updateData(filtered)
        binding.emptyView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showEditDialog(item: MemcConfigPackage?) {
        val ctx = requireContext()
        val db = DialogMemcConfigLayoutBinding.inflate(LayoutInflater.from(ctx))

        db.helperText.text = ctx.getString(R.string.memc_dialog_helper_sdr2hdr)
        db.packageNameLayout.hint = ctx.getString(R.string.memc_dialog_hint_package)
        db.rateLayout.hint = ctx.getString(R.string.memc_dialog_hint_rate)
        db.typeLayout.hint = ctx.getString(R.string.memc_dialog_hint_type)

        db.activityNameLayout.visibility = View.GONE

        if (item != null) {
            db.packageNameEdit.setText(item.packName)
            db.rateEdit.setText(item.rate)
            db.typeEdit.setText(item.type)
        }

        db.packageNameEdit.setOnClickListener {
            MemcSelectorHelper.pickPackage(this, ctx) { pkg ->
                db.packageNameEdit.setText(pkg)
            }
        }

        val builder = MaterialAlertDialogBuilder(ctx).setView(db.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val pkg = db.packageNameEdit.text?.toString().orEmpty()
                val rate = db.rateEdit.text?.toString().orEmpty()
                val type = db.typeEdit.text?.toString().orEmpty()
                if (pkg.isBlank() || rate.isBlank() || type.isBlank()) {
                    ctx.toast(ctx.getString(R.string.memc_data_incomplete))
                    return@setPositiveButton
                }
                val newEntry = MemcConfigPackage(pkg, rate, type)
                if (item == null || dataList.indexOf(item) == -1) {
                    dataList.add(newEntry)
                } else {
                    dataList[dataList.indexOf(item)] = newEntry
                }
                persist()
                applyFilter(binding.searchView.text?.toString().orEmpty())
            }
            .setNegativeButton(android.R.string.cancel, null)
        if (item != null) {
            builder.setNeutralButton(R.string.memc_remove) { _, _ ->
                dataList.remove(item)
                persist()
                applyFilter(binding.searchView.text?.toString().orEmpty())
            }
        }
        builder.show()
    }

    private fun persist() {
        val set = dataList.mapNotNull {
            runCatching { Json.encodeToString(MemcConfigPackage.serializer(), it) }.getOrNull()
        }.toSet()
        requireContext().putStringSet(ModulePrefs, "memc_config_package_list", set)
    }

    private inner class PackageItemAdapter(
        private val allData: List<MemcConfigPackage>,
        private val onClick: (MemcConfigPackage) -> Unit,
    ) : RecyclerView.Adapter<PackageItemAdapter.VH>() {
        private var data: List<MemcConfigPackage> = emptyList()

        init { data = allData }

        @SuppressLint("NotifyDataSetChanged")
        fun updateData(newData: List<MemcConfigPackage>) {
            data = newData
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = LayoutMemcConfigItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return VH(b)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = data[position]
            holder.line1.text = item.packName
            holder.line2.text = item.rate
            holder.line3.text = item.type
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount(): Int = data.size

        inner class VH(b: LayoutMemcConfigItemBinding) : RecyclerView.ViewHolder(b.root) {
            val line1 = b.line1
            val line2 = b.line2
            val line3 = b.line3
        }
    }
}

class MemcActivityListFragment : Fragment() {

    private var _binding: FragmentMemcListLayoutBinding? = null
    private val binding get() = _binding!!

    private val dataList: ArrayList<MemcConfigActivity> = ArrayList()
    private var filtered: List<MemcConfigActivity> = emptyList()
    private lateinit var adapter: ActivityItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMemcListLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = requireContext()

        binding.searchViewLayout.apply {
            hint = ctx.getString(R.string.memc_search_hint_activitys)
            isHintEnabled = true
            isHintAnimationEnabled = true
        }
        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilter(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.swipeRefreshLayout.setOnRefreshListener { reload() }

        binding.addButton.setOnClickListener { showEditDialog(null) }

        adapter = ActivityItemAdapter(dataList) { item -> showEditDialog(item) }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(ctx)
            this.adapter = this@MemcActivityListFragment.adapter
        }

        reload()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun reload() {
        binding.swipeRefreshLayout.isRefreshing = true
        binding.searchView.text = null
        dataList.clear()
        val raw = requireContext().getStringSet(ModulePrefs, "memc_config_activity_list", emptySet())
        for (json in raw.orEmpty()) {
            val v = runCatching { Json.decodeFromString<MemcConfigActivity>(json) }.getOrNull()
            if (v != null) dataList.add(v)
        }
        applyFilter("")
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun applyFilter(key: String) {
        filtered = if (key.isBlank()) dataList.toList()
        else {
            val k = key.lowercase()
            dataList.filter {
                it.packName.lowercase().contains(k) ||
                    it.activity.lowercase().contains(k) ||
                    it.type.lowercase().contains(k)
            }
        }
        adapter.updateData(filtered)
        binding.emptyView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showEditDialog(item: MemcConfigActivity?) {
        val ctx = requireContext()
        val db = DialogMemcConfigLayoutBinding.inflate(LayoutInflater.from(ctx))

        db.helperText.text = ctx.getString(R.string.memc_dialog_helper_memc)
        db.packageNameLayout.hint = ctx.getString(R.string.memc_dialog_hint_package)
        db.activityNameLayout.hint = ctx.getString(R.string.memc_dialog_hint_activity)
        db.typeLayout.hint = ctx.getString(R.string.memc_dialog_hint_type)

        db.rateLayout.visibility = View.GONE

        if (item != null) {
            db.packageNameEdit.setText(item.packName)
            db.activityNameEdit.setText(item.activity)
            db.typeEdit.setText(item.type)
        }

        db.packageNameEdit.setOnClickListener {
            MemcSelectorHelper.pickPackage(this, ctx) { pkg ->
                db.packageNameEdit.setText(pkg)
            }
        }
        db.activityNameEdit.setOnClickListener {
            val pkg = db.packageNameEdit.text?.toString().orEmpty()
            if (pkg.isBlank()) {
                ctx.toast("PackageName is null!")
                return@setOnClickListener
            }
            MemcSelectorHelper.pickActivity(this, ctx, pkg) { act ->
                db.activityNameEdit.setText(act)
            }
        }

        val builder = MaterialAlertDialogBuilder(ctx).setView(db.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val pkg = db.packageNameEdit.text?.toString().orEmpty()
                val activity = db.activityNameEdit.text?.toString().orEmpty()
                val type = db.typeEdit.text?.toString().orEmpty()
                if (pkg.isBlank() || activity.isBlank() || type.isBlank()) {
                    ctx.toast(ctx.getString(R.string.memc_data_incomplete))
                    return@setPositiveButton
                }
                val newEntry = MemcConfigActivity(pkg, activity, type)
                if (item == null || dataList.indexOf(item) == -1) {
                    dataList.add(newEntry)
                } else {
                    dataList[dataList.indexOf(item)] = newEntry
                }
                persist()
                applyFilter(binding.searchView.text?.toString().orEmpty())
            }
            .setNegativeButton(android.R.string.cancel, null)
        if (item != null) {
            builder.setNeutralButton(R.string.memc_remove) { _, _ ->
                dataList.remove(item)
                persist()
                applyFilter(binding.searchView.text?.toString().orEmpty())
            }
        }
        builder.show()
    }

    private fun persist() {
        val set = dataList.mapNotNull {
            runCatching { Json.encodeToString(MemcConfigActivity.serializer(), it) }.getOrNull()
        }.toSet()
        requireContext().putStringSet(ModulePrefs, "memc_config_activity_list", set)
    }

    private inner class ActivityItemAdapter(
        private val allData: List<MemcConfigActivity>,
        private val onClick: (MemcConfigActivity) -> Unit,
    ) : RecyclerView.Adapter<ActivityItemAdapter.VH>() {
        private var data: List<MemcConfigActivity> = emptyList()

        init { data = allData }

        @SuppressLint("NotifyDataSetChanged")
        fun updateData(newData: List<MemcConfigActivity>) {
            data = newData
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = LayoutMemcConfigItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return VH(b)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val item = data[position]
            holder.line1.text = item.packName
            holder.line2.text = item.activity
            holder.line3.text = item.type
            holder.itemView.setOnClickListener { onClick(item) }
        }

        override fun getItemCount(): Int = data.size

        inner class VH(b: LayoutMemcConfigItemBinding) : RecyclerView.ViewHolder(b.root) {
            val line1 = b.line1
            val line2 = b.line2
            val line3 = b.line3
        }
    }
}

internal object MemcSelectorHelper {

    fun pickPackage(fragment: Fragment, context: Context, onPicked: (String) -> Unit) {
        val dialogBinding = DialogAppSelectorBinding.inflate(LayoutInflater.from(context))
        val dialog = MaterialAlertDialogBuilder(context, dialogCentered)
            .setTitle(R.string.memc_pick_package)
            .setView(dialogBinding.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val adapter = dialogBinding.recyclerView.adapter as? AppSelectorAdapter
                val selected = adapter?.getSelected()?.firstOrNull()
                if (!selected.isNullOrBlank()) onPicked(selected)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialogBinding.swipeRefreshLayout.isRefreshing = true
        dialogBinding.searchViewLayout.isEnabled = false
        fragment.lifecycleScope.launch(Dispatchers.IO) {
            val pm = context.packageManager
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
            withContext(Dispatchers.Main) {
                val adapter = AppSelectorAdapter(context, appList) { selected ->
                    if (selected.size > 1) {
                        adapterKeepSingle(dialogBinding, selected.last())
                    }
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.isEnabled =
                        selected.isNotEmpty()
                }
                dialogBinding.recyclerView.apply {
                    this.adapter = adapter
                    layoutManager = LinearLayoutManager(context)
                }
                dialogBinding.searchView.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        adapter.getFilter.filter(s.toString())
                    }
                    override fun afterTextChanged(s: Editable?) {}
                })
                dialogBinding.swipeRefreshLayout.isRefreshing = false
                dialogBinding.searchViewLayout.isEnabled = true
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
            }
        }
    }

    private fun adapterKeepSingle(binding: DialogAppSelectorBinding, only: String) {
        val adapter = binding.recyclerView.adapter as? AppSelectorAdapter ?: return
        adapter.setSelected(setOf(only))
    }

    fun pickActivity(fragment: Fragment, context: Context, packageName: String, onPicked: (String) -> Unit) {
        fragment.lifecycleScope.launch(Dispatchers.IO) {
            val pm = context.packageManager
            val pkgInfo = runCatching {
                if (android.os.Build.VERSION.SDK_INT >= 33) {
                    pm.getPackageInfo(
                        packageName,
                        PackageManager.PackageInfoFlags.of(PackageManager.GET_ACTIVITIES.toLong())
                    )
                } else {
                    @Suppress("DEPRECATION")
                    pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
                }
            }.getOrNull()
            val activities = pkgInfo?.activities?.mapNotNull { it.name }?.sorted().orEmpty()
            withContext(Dispatchers.Main) {
                if (activities.isEmpty()) {
                    context.toast("App data is null!")
                    return@withContext
                }
                val labels = activities.toTypedArray()
                MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.memc_pick_activity)
                    .setItems(labels) { _, which ->
                        onPicked(labels[which])
                    }
                    .setNegativeButton(android.R.string.cancel, null)
                    .show()
            }
        }
    }
}
