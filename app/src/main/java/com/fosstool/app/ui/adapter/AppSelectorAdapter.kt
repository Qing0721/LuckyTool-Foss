package com.fosstool.app.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.recyclerview.widget.RecyclerView
import com.fosstool.app.databinding.LayoutAppinfoCheckboxItemBinding
import com.fosstool.app.utils.AppInfo

class AppSelectorAdapter(
    private val context: Context,
    private val allDatas: ArrayList<AppInfo>,
    private val onSelectionChanged: (List<String>) -> Unit
) : RecyclerView.Adapter<AppSelectorAdapter.ViewHolder>() {

    private val selectedPackages = LinkedHashSet<String>()
    private var filterDatas = ArrayList<AppInfo>()

    init {
        filterDatas = allDatas
    }

    fun setSelected(packages: Set<String>) {
        selectedPackages.clear()
        selectedPackages.addAll(packages)
        notifyDataSetChanged()
    }

    fun getSelected(): List<String> = selectedPackages.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutAppinfoCheckboxItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appInfo = filterDatas[position]
        holder.appIcon.setImageDrawable(appInfo.appIcon)
        holder.appName.text = appInfo.appName
        holder.packName.text = appInfo.packName
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = selectedPackages.contains(appInfo.packName)
        holder.itemView.setOnClickListener {
            holder.checkbox.isChecked = !holder.checkbox.isChecked
        }
        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedPackages.add(appInfo.packName)
            } else {
                selectedPackages.remove(appInfo.packName)
            }
            onSelectionChanged(selectedPackages.toList())
        }
    }

    override fun getItemCount(): Int = filterDatas.size

    val getFilter: Filter
        get() = object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                filterDatas = if (constraint.isBlank()) {
                    allDatas
                } else {
                    val list = ArrayList<AppInfo>()
                    val key = constraint.toString().lowercase()
                    allDatas.forEach {
                        if (it.appName.toString().lowercase().contains(key) ||
                            it.packName.lowercase().contains(key)
                        ) {
                            list.add(it)
                        }
                    }
                    list
                }
                val results = FilterResults()
                results.values = filterDatas
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence, results: FilterResults?) {
                filterDatas = results?.values as ArrayList<AppInfo> ?: ArrayList()
                notifyDataSetChanged()
            }
        }

    class ViewHolder(binding: LayoutAppinfoCheckboxItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val appIcon = binding.appIcon
        val appName = binding.appName
        val packName = binding.packName
        val checkbox = binding.checkbox
    }
}
