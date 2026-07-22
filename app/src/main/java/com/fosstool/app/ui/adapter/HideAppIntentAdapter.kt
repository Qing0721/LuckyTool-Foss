package com.fosstool.app.ui.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.fosstool.app.R
import com.fosstool.app.databinding.LayoutIntentAppinfoSwitchItemBinding
import com.fosstool.app.utils.AppInfo
import com.fosstool.app.utils.AppIntentInfo
import com.fosstool.app.utils.IntentType

class HideAppIntentAdapter(
    private val context: Context,
    private val onChipClick: (AppInfo, List<IntentType>) -> Unit,
) : RecyclerView.Adapter<HideAppIntentAdapter.ViewHolder>() {

    private val allDatas: ArrayList<AppInfo> = ArrayList()
    private var filterDatas: ArrayList<AppInfo> = ArrayList()

    private var candidateMap: Map<String, List<AppIntentInfo>> = emptyMap()

    private var selectedMap: Map<String, List<AppIntentInfo>> = emptyMap()

    init {
        filterDatas = allDatas
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setAllData(apps: List<AppInfo>) {
        allDatas.clear()
        allDatas.addAll(apps)
        filterDatas = allDatas
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFilterData(apps: List<AppInfo>) {
        filterDatas = ArrayList(apps)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setConfig(
        candidates: Map<String, List<AppIntentInfo>>,
        selected: Map<String, List<AppIntentInfo>>,
    ) {
        candidateMap = candidates
        selectedMap = selected
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutIntentAppinfoSwitchItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appInfo = filterDatas[position]
        holder.appIcon.setImageDrawable(appInfo.appIcon)
        holder.appName.text = appInfo.appName
        holder.packName.text = appInfo.packName

        holder.chipContainer.removeAllViews()
        for ((group, iconRes) in GROUPS) {
            val total = countForGroup(candidateMap[appInfo.packName], group)
            val selected = countForGroup(selectedMap[appInfo.packName], group)
            val chip = com.google.android.material.chip.Chip(context).apply {
                isCheckable = false
                isClickable = true
                chipIcon = context.getDrawable(iconRes)
                textAlignment = android.view.View.TEXT_ALIGNMENT_TEXT_END
                text = context.getString(
                    R.string.hide_app_intent_chip_count_format,
                    selected,
                    total,
                )
                isCheckable = true
                isChecked = selected > 0
                isCheckable = false
                setOnClickListener {
                    if (total > 0) onChipClick(appInfo, group)
                }
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 2
                    marginEnd = 2
                }
            }
            holder.chipContainer.addView(chip)
        }
    }

    override fun getItemCount(): Int = filterDatas.size

    private fun countForGroup(list: List<AppIntentInfo>?, group: List<IntentType>): Int {
        if (list.isNullOrEmpty()) return 0
        return list.count { it.type in group }
    }

    class ViewHolder(binding: LayoutIntentAppinfoSwitchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val appIcon = binding.appIcon
        val appName = binding.appName
        val packName = binding.packName
        val chipContainer = binding.intentChipContainer
    }

    internal companion object {
        val GROUPS: List<Pair<List<IntentType>, Int>> = listOf(
            IntentType.SHARE_GROUP to R.drawable.ic_intent_share,
            IntentType.TEXT_GROUP to R.drawable.ic_intent_text,
            IntentType.OPEN_WITH_GROUP to R.drawable.ic_intent_open_with,
            IntentType.BROWSER_GROUP to R.drawable.ic_intent_browser,
        )

        const val QUERY_FLAGS = PackageManager.MATCH_ALL

        fun buildGlobalQueryIntent(type: IntentType): Intent = when (type) {
            IntentType.SINGLE_SHARE -> Intent(Intent.ACTION_SEND)
            IntentType.MULTI_SHARE -> Intent(Intent.ACTION_SEND_MULTIPLE)
            IntentType.PROCESS_TEXT -> Intent(Intent.ACTION_PROCESS_TEXT)
            IntentType.CONTENT -> Intent().setDataAndType(Uri.parse("content://"), "*/*")
            IntentType.FILE -> Intent().setDataAndType(Uri.parse("file://"), "*/*")
            IntentType.HTTP_LINK -> Intent().setDataAndType(Uri.parse("http://"), "*/*")
            IntentType.HTTPS_LINK -> Intent().setDataAndType(Uri.parse("https://"), "*/*")
            IntentType.UNKNOWN -> Intent()
        }.apply {
            if (getAction() == null) setAction(Intent.ACTION_VIEW)
            if (getData() == null) setType("*/*")
            putExtra(EXTRA_RESULT_ORIGIN_DATA, true)
        }

        private const val EXTRA_RESULT_ORIGIN_DATA = "result_origin_data"

        @Suppress("unused")
        private fun queryActivities(
            pm: PackageManager, pkg: String, type: IntentType,
        ): List<android.content.pm.ResolveInfo> = runCatching {
            pm.queryIntentActivities(buildGlobalQueryIntent(type).setPackage(pkg), QUERY_FLAGS)
        }.getOrDefault(emptyList())
    }
}
