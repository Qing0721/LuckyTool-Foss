package com.fosstool.app.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drake.net.scope.AndroidScope
import com.drake.net.utils.scopeLife
import com.drake.net.utils.withDefault
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.highcapable.yukihookapi.hook.factory.dataChannel
import com.highcapable.yukihookapi.hook.log.data.YLogData
import com.highcapable.yukihookapi.hook.xposed.channel.annotation.SendTooLargeChannelData
import com.fosstool.app.IGlobalFuncController
import com.fosstool.app.R
import com.fosstool.app.databinding.FragmentLogsBinding
import com.fosstool.app.databinding.LayoutLoginfoItemBinding
import com.fosstool.app.ui.activity.MainActivity
import com.fosstool.app.utils.FileUtils
import com.fosstool.app.utils.ThemeUtils
import com.fosstool.app.utils.copyStr
import com.fosstool.app.utils.dialogCentered
import com.fosstool.app.utils.dp
import com.fosstool.app.utils.formatDate
import com.fosstool.app.utils.getAppIcon
import com.fosstool.app.utils.getAppLabel
import com.fosstool.app.utils.getDeviceInfo
import com.fosstool.app.utils.setupMenuProvider
import com.fosstool.app.utils.toast
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class LoggerFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentLogsBinding
    private var logFuncController: IGlobalFuncController? = null

    private var listData = ArrayList<YLogData>()
    private var logInfoViewAdapter: LogInfoViewAdapter? = null
    private var fileName: String = ""
    private var filterString = ""
    private var scope: AndroidScope? = null
    private lateinit var logsDir: File

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        setupMenuProvider(this)
        binding = FragmentLogsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        checkDirs()
        logInfoViewAdapter = LogInfoViewAdapter(requireActivity(), listData)
        binding.loglistView.apply {
            adapter = logInfoViewAdapter
            layoutManager = LinearLayoutManager(context)
        }
        binding.swipeRefreshLayout.apply {
            setOnRefreshListener { loadLogger() }
        }
    }

    @OptIn(SendTooLargeChannelData::class)
    private fun loadLogger() {
        scope = scopeLife {
            listData.clear()
            binding.swipeRefreshLayout.isRefreshing = true
            binding.logNodataView.isVisible = false
            requireActivity().resources.getStringArray(R.array.xposed_scope).forEach { scope ->
                withDefault {
                    requireActivity().dataChannel(scope).allowSendTooLargeData()
                        .obtainLoggerInMemoryData { its ->
                            its.takeIf { e -> e.isNotEmpty() }?.run { listData.addAll(its) }
                            logInfoViewAdapter?.refreshDatas()
                        }
                }
            }
            binding.loglistView.isVisible = listData.isNotEmpty()
            binding.logNodataView.isVisible = listData.isEmpty()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onResume() {
        super.onResume()
        initController()
        loadLogger()
    }

    override fun onPause() {
        super.onPause()
        scope?.cancel()
        scope?.close()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.add(0, 1, 0, getString(R.string.common_words_refresh)).apply {
            setIcon(R.drawable.ic_baseline_refresh_24)
            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            if (ThemeUtils.isNightMode(resources.configuration)) {
                iconTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
        menu.add(0, 2, 0, getString(R.string.common_words_filter)).apply {
            setIcon(R.drawable.baseline_filter_list_24)
            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            if (ThemeUtils.isNightMode(resources.configuration)) {
                iconTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
        menu.add(0, 3, 0, getString(R.string.common_words_save)).apply {
            setIcon(R.drawable.ic_baseline_save_24)
            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            if (ThemeUtils.isNightMode(resources.configuration)) {
                iconTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
        menu.add(0, 4, 0, getString(R.string.common_words_share)).apply {
            setIcon(R.drawable.baseline_share_24)
            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
            if (ThemeUtils.isNightMode(resources.configuration)) {
                iconTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == 1) loadLogger()
        if (menuItem.itemId == 2) {
            val dialog = MaterialAlertDialogBuilder(requireActivity(), dialogCentered).apply {
                setTitle(getString(R.string.log_filter_title))
                setView(R.layout.layout_log_filter_dialog)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    logInfoViewAdapter?.getFilter?.filter(filterString)
                }
                setNeutralButton(android.R.string.cancel, null)
            }.show()
            dialog.findViewById<TextInputLayout>(R.id.log_filter_layout)?.apply {
                hint = getString(R.string.log_filter_layout_hint)
                isCounterEnabled = true
            }
            dialog.findViewById<TextInputEditText>(R.id.log_filter)?.apply {
                setText(filterString)
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(
                        s: CharSequence?, start: Int, before: Int, count: Int
                    ) {
                        filterString = s.toString()
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })
            }
        }
        if (menuItem.itemId == 3) {
            fileName = "LuckyTool_" + formatDate("yyyyMMdd_HHmmss") + ".log"
            saveFile(fileName)
        }
        if (menuItem.itemId == 4) {
            fileName = "LuckyTool_" + formatDate("yyyyMMdd_HHmmss") + ".log"
            shareFile(fileName)
        }
        return true
    }

    private val createDocument = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/log")
    ) {
        if (it != null) {
            alterDocument(requireActivity(), it)
        }
    }

    private fun saveFile(fileName: String) {
        checkDirs()
        if (listData.isEmpty()) requireActivity().toast(getString(R.string.log_data_is_empty))
        else createDocument.launch(fileName)
    }

    private fun shareFile(fileName: String) {
        checkDirs()
        if (listData.isEmpty()) requireActivity().toast(getString(R.string.log_data_is_empty))
        else {
            logsDir.mkdirs()
            val logFile = File(logsDir, fileName)
            if (!logFile.exists()) logFile.createNewFile()
            logFile.writeText(getLogsString(requireActivity()))
            FileUtils.shareFile(requireActivity(), "Share", logFile)
        }
    }

    private fun checkDirs() {
        val dir: File = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS + "/LuckyTool"
        )
        if (!dir.exists()) dir.mkdirs()
        logsDir = File(requireActivity().cacheDir, "logs")
        logsDir.listFiles()?.forEach { if (it.exists()) it.delete() }
        if (logsDir.exists()) logsDir.delete()
    }

    private fun alterDocument(context: Context, uri: Uri) {
        val str = getLogsString(context)
        try {
            context.contentResolver.openFileDescriptor(uri, "w")?.use { its ->
                FileOutputStream(its.fileDescriptor).use {
                    it.write(str.toByteArray())
                }
            }
            context.toast(getString(R.string.log_save_success))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            context.toast(getString(R.string.log_save_failed))
        } catch (e: IOException) {
            e.printStackTrace()
            context.toast(getString(R.string.log_save_failed))
        }
    }

    private fun getLogsString(context: Context): String {
        var str = ""
        str += context.getDeviceInfo(logFuncController, true)
        listData.forEach {
            val time = formatDate("yyyy/MM/dd-HH:mm:ss", it.timestamp)
            val messageFinal = if (it.msg != "null") "\nMessage -> ${it.msg}" else ""
            val throwableFinal =
                if (it.throwable.toString() != "null") "\nThrowable -> ${it.throwable}\n\n" else "\n\n"
            str += "[${time}][${it.tag}][${it.priority}][${it.packageName}][${it.userId}]$messageFinal$throwableFinal"
        }
        return str
    }

    private fun initController() {
        if (logFuncController == null) {
            (activity as MainActivity).initController {
                logFuncController = it
            }
        }
    }

}

class LogInfoViewAdapter(val context: Context, data: ArrayList<YLogData>) :
    RecyclerView.Adapter<LogInfoViewAdapter.ViewHolder>() {

    private var allData = ArrayList<YLogData>()
    private var filterData = ArrayList<YLogData>()

    init {
        allData = data
        filterData = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LayoutLoginfoItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val time = formatDate("yyyy/MM/dd-HH:mm:ss", filterData[position].timestamp)
        val tag = filterData[position].tag
        val priority = filterData[position].priority
        val packageName = filterData[position].packageName
        val userId = filterData[position].userId
        val msg = filterData[position].msg
        val throwable = filterData[position].throwable.toString()
        holder.logIcon.setImageDrawable(context.getAppIcon(packageName))
        holder.logTime.text = time
        holder.logMsg.text = msg
        holder.logRoot.setOnClickListener(null)
        holder.logRoot.setOnClickListener {
            MaterialAlertDialogBuilder(context, dialogCentered).apply {
                setTitle(context.getAppLabel(packageName))
                setView(NestedScrollView(context).apply {
                    addView(MaterialTextView(context).apply {
                        setPadding(20.dp, 0, 20.dp, 20.dp)
                        text = msg + if (throwable != "null") "\n\n$throwable" else ""
                    })
                })
                setPositiveButton(android.R.string.copy) { _, _ ->
                    val messageFinal = if (msg != "null") "\nMessage -> $msg" else ""
                    val throwableFinal =
                        if (throwable != "null") "\nThrowable -> ${throwable}\n\n" else "\n\n"
                    context.copyStr("[${time}][${tag}][${priority}][${packageName}][${userId}]$messageFinal$throwableFinal")
                }
            }.show()
        }
    }

    override fun getItemCount(): Int {
        return filterData.size
    }

    val getFilter
        get() = object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                filterData = if (constraint.isBlank()) {
                    allData
                } else {
                    val filterlist = ArrayList<YLogData>()
                    allData.forEach {
                        if (it.toString().lowercase().contains(constraint.toString().lowercase())) {
                            filterlist.add(it)
                        }
                    }
                    filterlist
                }
                val filterResults = FilterResults()
                filterResults.values = filterData
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence, results: FilterResults?) {
                filterData = results?.values as ArrayList<YLogData>
                refreshDatas()
            }
        }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshDatas() {
        notifyDataSetChanged()
    }

    class ViewHolder(binding: LayoutLoginfoItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val logRoot = binding.root
        val logIcon: ImageView = binding.logIcon
        val logTime: TextView = binding.logTime
        val logMsg: TextView = binding.logMsg
    }
}
