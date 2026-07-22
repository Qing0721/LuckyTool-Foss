package com.fosstool.app.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.ArrayMap
import android.util.ArraySet
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drake.net.utils.scopeLife
import com.drake.net.utils.withIO
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.slider.Slider
import com.highcapable.yukihookapi.hook.factory.dataChannel
import com.fosstool.app.R
import com.fosstool.app.databinding.FragmentApplistFunctionLayoutBinding
import com.fosstool.app.databinding.LayoutAppinfoSwitchItemDarkmodeBinding
import com.fosstool.app.utils.*

class DarkModeFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentApplistFunctionLayoutBinding
    private var appListAllDatas = ArrayList<AppInfo>()
    private var darkModeAdapter: DarkModeAdapter? = null
    private val scopes = arrayOf("com.android.settings")
    private var isShowSystemApp = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        setupMenuProvider(this)
        isShowSystemApp =
            requireActivity().getBoolean(ModulePrefs, "show_system_app_dark_mode", false)
        binding = FragmentApplistFunctionLayoutBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.enableSwitch.apply {
            text = context.getString(R.string.enable_zoom_window)
            isChecked = context.getBoolean(ModulePrefs, "dark_mode_list_enable", false)
            setOnCheckedChangeListener { buttonView, isChecked ->
                if (buttonView.isPressed) {
                    context.putBoolean(ModulePrefs, "dark_mode_list_enable", isChecked)
                    requireActivity().dataChannel("android").put("dark_mode_list_enable", isChecked)
                }
            }
        }

        binding.searchViewLayout.apply {
            hint = "Name / PackageName"
            isHintEnabled = true
            isHintAnimationEnabled = true
        }
        binding.searchView.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    darkModeAdapter?.getFilter?.filter(s.toString())
                }

                override fun afterTextChanged(s: Editable?) {}
            })
        }
        binding.swipeRefreshLayout.apply {
            setOnRefreshListener { loadData() }
        }

        if (appListAllDatas.isEmpty()) loadData()
    }

    private fun loadData() {
        scopeLife {
            binding.swipeRefreshLayout.isRefreshing = true
            binding.searchViewLayout.isEnabled = false
            binding.searchView.text = null
            val enableData =
                requireActivity().getStringSet(ModulePrefs, "dark_mode_support_list", ArraySet())
            val formatEnableData = ArrayMap<String, Int>()
            withIO {
                enableData?.forEach {
                    if (it.contains("|")) {
                        val arr = it.split("|").toMutableList()
                        if (arr.size < 2 || arr[1].isBlank()) arr[1] = (0).toString()
                        formatEnableData[arr[0]] = arr[1].toInt()
                    } else formatEnableData[it] = 0
                }
                appListAllDatas.clear()
                val packageManager = requireActivity().packageManager
                val appinfos = PackageUtils(packageManager).getInstalledApplications(0)
                for (i in appinfos) {
                    if (i.flags and ApplicationInfo.FLAG_SYSTEM == 1 && !isShowSystemApp) continue
                    appListAllDatas.add(
                        AppInfo(
                            i.loadIcon(packageManager),
                            i.loadLabel(packageManager),
                            i.packageName,
                        )
                    )
                }
            }
            darkModeAdapter = DarkModeAdapter(requireActivity(), appListAllDatas, formatEnableData)
            binding.recyclerView.apply {
                adapter = darkModeAdapter
                layoutManager = LinearLayoutManager(context)
            }
            binding.swipeRefreshLayout.isRefreshing = false
            binding.searchViewLayout.isEnabled = true
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.app_list_menu, menu)
        menu.findItem(R.id.show_system_app).isChecked = isShowSystemApp
        menu.add(0, 1, 0, getString(R.string.menu_reboot)).apply {
            setIcon(R.drawable.ic_baseline_refresh_24)
            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            if (ThemeUtils.isNightMode(resources.configuration)) {
                iconTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
        menu.add(0, 2, 0, getString(R.string.common_words_open)).apply {
            setIcon(R.drawable.baseline_open_in_new_24)
            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            if (ThemeUtils.isNightMode(resources.configuration)) {
                iconTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == 1) requireActivity().restartScopes(scopes)
        if (menuItem.itemId == 2) jumpDarkMode(requireActivity())
        if (menuItem.itemId == R.id.show_system_app) {
            menuItem.isChecked = !menuItem.isChecked
            isShowSystemApp = menuItem.isChecked
            requireActivity().putBoolean(
                ModulePrefs, "show_system_app_dark_mode", isShowSystemApp
            )
            loadData()
        }
        return true
    }
}

class DarkModeAdapter(
    val context: Context, datas: ArrayList<AppInfo>, enableData: ArrayMap<String, Int>
) : RecyclerView.Adapter<DarkModeAdapter.ViewHolder>() {

    private var allDatas = ArrayList<AppInfo>()
    private var filterDatas = ArrayList<AppInfo>()
    private var enabledMulti = ArrayMap<String, Int>()
    private var tempData = ArrayMap<String, Int>()
    private var hasPermissions = true

    init {
        if (datas.size <= 1) hasPermissions = false
        allDatas = datas
        enabledMulti = enableData
        sortDatas()
        filterDatas = datas
    }

    private fun sortDatas() {
        allDatas.forEach { its ->
            enabledMulti.forEach {
                if (it.key == its.packName) tempData[it.key] = it.value
            }
        }
        enabledMulti.clear()
        enabledMulti = tempData
        saveEnableList()
        val tempInfo = ArrayList<AppInfo>()
        tempData.forEach { map ->
            allDatas.forEach {
                if (it.packName == map.key) tempInfo.add(it)
            }
        }
        tempInfo.forEach {
            allDatas.remove(it)
            allDatas.add(0, it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutAppinfoSwitchItemDarkmodeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.appIcon.setImageDrawable(filterDatas[position].appIcon)
        holder.appName.text = filterDatas[position].appName
        holder.packName.text = filterDatas[position].packName
        holder.appInfoView.setOnClickListener(null)
        holder.switchview.setOnCheckedChangeListener(null)
        holder.sliderview.clearOnChangeListeners()

        holder.switchview.isChecked = enabledMulti.contains(filterDatas[position].packName)
        holder.sliderLayout.isVisible = holder.switchview.isChecked
        enabledMulti.forEach {
            if (it.key == filterDatas[position].packName) {
                holder.sliderview.value = it.value * 1F
            }
        }
        holder.appInfoView.setOnClickListener {
            holder.switchview.isChecked = !holder.switchview.isChecked
        }
        holder.switchview.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enabledMulti[filterDatas[position].packName] = 0
                holder.sliderLayout.isVisible = true
                holder.sliderview.value = 0F
            } else {
                enabledMulti.remove(filterDatas[position].packName)
                holder.sliderLayout.isVisible = false
            }
            saveEnableList()
        }
        holder.sliderview.addOnChangeListener { _, value, fromUser ->
            if (!fromUser) return@addOnChangeListener
            enabledMulti[filterDatas[position].packName] = value.toInt()
            saveEnableList()
        }
    }

    override fun getItemCount(): Int = filterDatas.size

    val getFilter
        get() = object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                filterDatas = if (constraint.isBlank()) {
                    allDatas
                } else {
                    val filterlist = ArrayList<AppInfo>()
                    allDatas.forEach {
                        if (it.appName.toString().lowercase().contains(
                                constraint.toString().lowercase()
                            ) || it.packName.lowercase().contains(constraint.toString().lowercase())
                        ) {
                            filterlist.add(it)
                        }
                    }
                    filterlist
                }
                val filterResults = FilterResults()
                filterResults.values = filterDatas
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence, results: FilterResults?) {
                filterDatas = results?.values as ArrayList<AppInfo>
                refreshDatas()
            }
        }

    private fun saveEnableList() {
        if (!hasPermissions) return
        val data = ArraySet<String>()
        enabledMulti.forEach {
            data.add("${it.key}|${it.value}")
        }
        context.putStringSet(ModulePrefs, "dark_mode_support_list", data.toSet())
        context.dataChannel("android").put("dark_mode_support_list", data.toSet())
        context.dataChannel("com.android.settings").put("dark_mode_support_list", data.toSet())
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshDatas() {
        notifyDataSetChanged()
    }

    class ViewHolder(binding: LayoutAppinfoSwitchItemDarkmodeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val appInfoView: ConstraintLayout = binding.appinfoView
        val appIcon: ImageView = binding.appIcon
        val appName: TextView = binding.appName
        val packName: TextView = binding.packName
        val switchview: MaterialSwitch = binding.switchview
        val sliderLayout: LinearLayout = binding.sliderLayout
        val sliderview: Slider = binding.slider
    }
}
