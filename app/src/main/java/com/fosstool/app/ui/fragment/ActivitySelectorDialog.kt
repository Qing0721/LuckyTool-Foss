package com.fosstool.app.ui.fragment

import android.content.Context
import android.content.pm.PackageManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.fosstool.app.R
import com.fosstool.app.databinding.DialogActivityInfoSelectorLayoutBinding
import com.fosstool.app.databinding.LayoutActivityinfoCheckboxItemBinding
import com.fosstool.app.utils.AppInfo
import com.fosstool.app.utils.AppIntentInfo
import com.fosstool.app.utils.IntentType
import com.fosstool.app.utils.dialogCentered
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ActivitySelectorDialog(
    private val context: Context,
    private val appInfo: AppInfo,
    private val group: List<IntentType>,
    private val candidates: List<CandidateEntry>,
    private val preSelected: Set<AppIntentInfo>,
    private val onConfirm: (List<AppIntentInfo>) -> Unit,
) {
    private val selected: MutableSet<AppIntentInfo> = LinkedHashSet(preSelected)
    private var filtered: List<CandidateEntry> = candidates
    private lateinit var binding: DialogActivityInfoSelectorLayoutBinding
    private lateinit var adapter: ActivityCheckboxAdapter
    private var dialog: AlertDialog? = null

    fun show() {
        binding = DialogActivityInfoSelectorLayoutBinding.inflate(LayoutInflater.from(context))
        adapter = ActivityCheckboxAdapter(candidates, selected) {
            dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = true
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            this.adapter = this@ActivitySelectorDialog.adapter
        }
        binding.swipeRefreshLayout.isEnabled = false
        binding.searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val key = s?.toString()?.lowercase() ?: ""
                filtered = if (key.isEmpty()) candidates
                else candidates.filter {
                    it.info.activity.lowercase().contains(key) ||
                        runCatching { it.resolveInfo.loadLabel(context.packageManager)?.toString() }
                            .getOrNull()?.lowercase()?.contains(key) == true
                }
                adapter.updateData(filtered)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.emptyView.visibility =
            if (candidates.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE

        val groupLabel = groupLabelRes(group)?.let { context.getString(it) } ?: ""

        dialog = MaterialAlertDialogBuilder(context, dialogCentered)
            .setTitle("${appInfo.appName} - $groupLabel")
            .setView(binding.root)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                onConfirm(selected.toList())
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        dialog?.show()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = true
    }

    private inner class ActivityCheckboxAdapter(
        initialData: List<CandidateEntry>,
        private val selected: MutableSet<AppIntentInfo>,
        private val onSelectionChanged: () -> Unit,
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<ActivityCheckboxAdapter.VH>() {
        private var data: List<CandidateEntry> = initialData
        private val pm: PackageManager = context.packageManager

        @android.annotation.SuppressLint("NotifyDataSetChanged")
        fun updateData(newData: List<CandidateEntry>) {
            data = newData
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = LayoutActivityinfoCheckboxItemBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            return VH(b)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val entry = data[position]
            val info = entry.info
            runCatching {
                holder.icon.setImageDrawable(entry.resolveInfo.loadIcon(pm))
                holder.icon.visibility = android.view.View.VISIBLE
            }.onFailure { holder.icon.visibility = android.view.View.GONE }

            val label = runCatching { entry.resolveInfo.loadLabel(pm)?.toString() }
                .getOrNull() ?: info.activity
            val suffix = typeSuffix(info.type)
            holder.appName.text = if (suffix.isNotEmpty()) "$label $suffix" else label
            holder.packName.text = info.activity

            holder.checkbox.setOnCheckedChangeListener(null)
            holder.checkbox.isChecked = selected.contains(info)
            holder.itemView.setOnClickListener {
                holder.checkbox.isChecked = !holder.checkbox.isChecked
            }
            holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selected.add(info) else selected.remove(info)
                onSelectionChanged()
            }
        }

        override fun getItemCount(): Int = data.size

        inner class VH(b: LayoutActivityinfoCheckboxItemBinding) :
            androidx.recyclerview.widget.RecyclerView.ViewHolder(b.root) {
            val icon = b.activityIcon
            val appName = b.appName
            val packName = b.packName
            val checkbox = b.checkbox
        }
    }

    private fun typeSuffix(type: IntentType): String = context.getString(when (type) {
        IntentType.SINGLE_SHARE -> R.string.intent_type_single_share
        IntentType.MULTI_SHARE -> R.string.intent_type_multi_share
        IntentType.PROCESS_TEXT -> R.string.intent_type_process_text
        IntentType.CONTENT -> R.string.intent_type_content
        IntentType.FILE -> R.string.intent_type_file
        IntentType.HTTP_LINK -> R.string.intent_type_http_link
        IntentType.HTTPS_LINK -> R.string.intent_type_https_link
        IntentType.UNKNOWN -> 0
    }).let { if (it == "0") "" else it }

    private fun groupLabelRes(group: List<IntentType>): Int? = when (group.firstOrNull()) {
        IntentType.SINGLE_SHARE, IntentType.MULTI_SHARE -> R.string.intent_group_share
        IntentType.PROCESS_TEXT -> R.string.intent_group_text
        IntentType.CONTENT, IntentType.FILE -> R.string.intent_group_open_with
        IntentType.HTTP_LINK, IntentType.HTTPS_LINK -> R.string.intent_group_browser
        else -> null
    }
}
