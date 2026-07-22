package com.fosstool.app.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.database.getStringOrNull
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.drake.net.utils.scopeLife
import com.drake.net.utils.withDefault
import com.fosstool.app.R
import com.fosstool.app.databinding.FragmentExtractOtaBinding
import com.fosstool.app.utils.SQLiteUtils
import com.fosstool.app.utils.SQLiteUtils.getTableData
import com.fosstool.app.utils.SQLiteUtils.readOnly
import com.fosstool.app.utils.ShellUtils
import com.fosstool.app.utils.copyStr
import com.fosstool.app.utils.formatDataSize
import java.io.File

class ExtractOTAFragment : Fragment() {

    private lateinit var binding: FragmentExtractOtaBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExtractOtaBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("SdCardPath")
    fun init(context: Context) {
        scopeLife {
            binding.swipeRefreshLayout.apply {
                setOnRefreshListener { init(context) }
                isRefreshing = true
            }
            binding.otaData.isVisible = false
            binding.noOtaData.isVisible = true
            binding.copyOtaData.isEnabled = false
            val data = withDefault {
                val command =
                    "cp /data/user/0/com.oplus.ota/databases/ota.db ${context.cacheDir.path}"
                ShellUtils.execCommand(command, true)
                val dbFile = File(context.cacheDir.path + "/ota.db")
                val table = if (dbFile.exists()) {
                    val db = SQLiteUtils.openDataBase(dbFile.path, readOnly)
                    db?.getTableData("pkgList")
                } else null
                val dataList = ArrayList<String>()
                if (table != null) {
                    for (data in 0 until table.count) {
                        table.moveToPosition(data)
                        val packNameColumn = table.getColumnIndex("package_name")
                        val packName = table.getStringOrNull(packNameColumn) ?: "Null"
                        val sizeColumn = table.getColumnIndex("size")
                        val size = table.getStringOrNull(sizeColumn) ?: "Null"
                        val md5Column = table.getColumnIndex("md5")
                        val md5 = table.getStringOrNull(md5Column) ?: "Null"
                        val activeUrlColumn = table.getColumnIndex("active_url")
                        val activeUrl = table.getStringOrNull(activeUrlColumn) ?: "Null"
                        val urlColumn = table.getColumnIndex("url")
                        val url = table.getStringOrNull(urlColumn) ?: "Null"
                        dataList.add(
                            """
                            PackName -> $packName
                            
                            Size -> ${formatDataSize(size)}
                            
                            ActiveUrl -> $activeUrl
                            
                            Url -> $url
                            
                            MD5 -> $md5
                            
                            ${getString(R.string.extract_ota_source)} -> @LuckyTool
                            """
.trimIndent()
                        )
                    }
                }
                dataList
            }
            if (data.isNotEmpty()) {
                var str = ""
                data.forEach { str += it + "\n\n" }
                binding.otaData.apply {
                    text = str
                    isVisible = text.isNotBlank()
                }
                binding.noOtaData.isVisible = !binding.otaData.isVisible
                binding.copyOtaData.apply {
                    isEnabled = binding.otaData.isVisible
                    setOnClickListener { context.copyStr(str) }
                }
            }
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init(requireActivity())
    }
}
