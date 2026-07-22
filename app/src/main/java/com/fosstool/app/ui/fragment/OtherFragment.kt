package com.fosstool.app.ui.fragment

import android.annotation.SuppressLint
import android.app.StatusBarManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.fosstool.app.IAdbDebugController
import com.fosstool.app.ITouchPanelController
import com.fosstool.app.R
import com.fosstool.app.databinding.DialogAppSelectorBinding
import com.fosstool.app.databinding.LayoutAdbDialogBinding
import com.fosstool.app.databinding.FragmentOtherBinding
import com.fosstool.app.ui.adapter.AppSelectorAdapter
import com.fosstool.app.ui.service.AdbDebugControllerService
import com.fosstool.app.ui.service.TouchPanelControllerService
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.AppInfo
import com.fosstool.app.utils.OtherPrefs
import com.fosstool.app.utils.PackageUtils
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.SettingsPrefs
import com.fosstool.app.utils.ShellUtils
import com.fosstool.app.utils.ShortcutUtils
import com.fosstool.app.utils.ThemeUtils
import com.fosstool.app.utils.bindRootService
import com.fosstool.app.utils.dialogCentered
import com.fosstool.app.utils.getBoolean
import com.fosstool.app.utils.getString
import com.fosstool.app.utils.navigatePage
import com.fosstool.app.utils.putBoolean
import com.fosstool.app.utils.putString
import com.fosstool.app.utils.restartAllScope
import com.fosstool.app.utils.setupMenuProvider
import com.fosstool.app.utils.toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OtherFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentOtherBinding
    private var touchPanelController: ITouchPanelController? = null
    private var adbController: IAdbDebugController? = null

    companion object {
        private val SAMPLING_RATE_LEVELS = arrayOf("120", "180", "240", "360", "480", "600", "720")
        private const val ADB_PORT_DEFAULT = "6666"
        private const val ADB_PORT_KEY = "adb_port"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentOtherBinding.inflate(inflater)
        setupMenuProvider(this)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.quickEntry.setOnClickListener {
            navigatePage(R.id.action_nav_other_to_systemQuickEntry, getString(R.string.quick_entry))
        }

        binding.moduleTiles.setOnClickListener {
            showModuleTilesDialog()
        }

        binding.forceFps.setOnClickListener {
            navigatePage(R.id.action_nav_other_to_forceFps, getString(R.string.fps_title))
        }

        binding.touchSamplingRate.setOnClickListener {
            showSamplingRateLevelDialog()
        }

        binding.moduleShortcuts.setOnClickListener { showModuleShortcutsDialog() }

        binding.remoteAdbDebug.setOnClickListener { showRemoteAdbDialog() }

        bindTouchPanelController()

        bindAdbDebugController()
    }

    private fun bindTouchPanelController() {
        if (touchPanelController == null) {
            requireActivity().bindRootService(
                TouchPanelControllerService::class.java,
                { _: ComponentName?, iBinder: IBinder? ->
                    touchPanelController = ITouchPanelController.Stub.asInterface(iBinder)
                    checkSamplingRateSupport()
                }
            )
        } else {
            checkSamplingRateSupport()
        }
    }

    private fun bindAdbDebugController() {
        if (adbController == null) {
            requireActivity().bindRootService(
                AdbDebugControllerService::class.java,
                { _: ComponentName?, iBinder: IBinder? ->
                    adbController = IAdbDebugController.Stub.asInterface(iBinder)
                    binding.remoteAdbDebug.isVisible = adbController != null
                }
            )
        } else {
            binding.remoteAdbDebug.isVisible = true
        }
    }

    private fun checkSamplingRateSupport() {
        val controller = touchPanelController ?: return
        lifecycleScope.launch(Dispatchers.IO) {
            val supported = try {
                controller.checkSamplingRateLevel()
            } catch (_: Throwable) {
                false
            }
            withContext(Dispatchers.Main) {
                binding.touchSamplingRate.isVisible = supported
            }
        }
    }

    private fun showSamplingRateLevelDialog() {
        val context = requireContext()
        val controller = touchPanelController
        val currentLevel = context.getString(SettingsPrefs, "touch_sampling_rate_level", "240")
        val checkedIndex = SAMPLING_RATE_LEVELS.indexOf(currentLevel).coerceAtLeast(0)
        MaterialAlertDialogBuilder(context, dialogCentered).apply {
            setTitle(R.string.touch_sampling_rate_level)
            setSingleChoiceItems(SAMPLING_RATE_LEVELS, checkedIndex) { dialog, which ->
                val level = SAMPLING_RATE_LEVELS[which]
                context.putString(SettingsPrefs, "touch_sampling_rate_level", level)
                val levelValue = level.toIntOrNull() ?: 240
                try {
                    controller?.setSamplingRateLevel(levelValue)
                } catch (_: Throwable) {
                }
                dialog.dismiss()
            }
            setNegativeButton(android.R.string.cancel, null)
            show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showRemoteAdbDialog() {
        val context = requireContext()
        val controller = adbController ?: return

        lifecycleScope.launch(Dispatchers.IO) {
            val currentPort = try {
                controller.adbPort
            } catch (_: Throwable) {
                0
            }
            val ip = try {
                controller.wifiIP
            } catch (_: Throwable) {
                null
            } ?: "IP"
            withContext(Dispatchers.Main) {
                buildAdbDialog(context, controller, currentPort, ip)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun buildAdbDialog(
        context: Context,
        controller: IAdbDebugController,
        currentPort: Int,
        ip: String,
    ) {
        val dialogBinding = LayoutAdbDialogBinding.inflate(LayoutInflater.from(context))

        val savedPort = context.getString(OtherPrefs, ADB_PORT_KEY, ADB_PORT_DEFAULT)
            ?: ADB_PORT_DEFAULT
        val portText = if (currentPort <= 0) savedPort else currentPort.toString()
        dialogBinding.adbPort.setText(portText)

        val addressFormat = getString(R.string.adb_connect_address_format)
        if (currentPort > 0) {
            dialogBinding.adbTv.text = String.format(addressFormat, ip, currentPort)
        } else {
            dialogBinding.adbTv.text = ""
        }
        dialogBinding.adbTv.setOnLongClickListener { tv ->
            val text = (tv as? android.widget.TextView)?.text?.toString()
            if (!text.isNullOrBlank()) {
                val cm = context.getSystemService(ClipboardManager::class.java)
                cm?.setPrimaryClip(ClipData.newPlainText("adb", text))
                true
            } else false
        }
        dialogBinding.adbTvTip.isVisible = dialogBinding.adbTv.text.isNotBlank()
        dialogBinding.adbTvTip.setOnLongClickListener {
            val text = dialogBinding.adbTv.text?.toString()
            if (!text.isNullOrBlank()) {
                val cm = context.getSystemService(ClipboardManager::class.java)
                cm?.setPrimaryClip(ClipData.newPlainText("adb", text))
                true
            } else false
        }

        val enabled = currentPort > 0
        dialogBinding.adbSwitch.isChecked = enabled
        dialogBinding.adbPortLayout.isEnabled = !enabled

        dialogBinding.adbSwitch.setOnCheckedChangeListener { switch, isChecked ->
            if (!switch.isPressed) return@setOnCheckedChangeListener
            if (isChecked) {
                val portStr = dialogBinding.adbPort.text?.toString()
                if (portStr.isNullOrBlank()) {
                    switch.isChecked = false
                    dialogBinding.adbTv.text = context.getString(R.string.adb_debug_port_cannot_null)
                    return@setOnCheckedChangeListener
                }
                val port = portStr.toIntOrNull()
                if (port == null || port <= 0) {
                    switch.isChecked = false
                    dialogBinding.adbTv.text = context.getString(R.string.adb_debug_port_cannot_null)
                    return@setOnCheckedChangeListener
                }
                context.putString(OtherPrefs, ADB_PORT_KEY, portStr)
                switch.isEnabled = false
                dialogBinding.adbPortLayout.isEnabled = false
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        controller.adbPort = port
                        controller.restartAdb()
                    } catch (_: Throwable) {
                    }
                    val newIp = try {
                        controller.wifiIP
                    } catch (_: Throwable) {
                        null
                    } ?: "IP"
                    withContext(Dispatchers.Main) {
                        dialogBinding.adbTv.text = String.format(addressFormat, newIp, port)
                        dialogBinding.adbTvTip.isVisible = true
                        switch.isEnabled = true
                    }
                }
            } else {
                switch.isEnabled = false
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        controller.adbPort = -1
                        controller.restartAdb()
                        controller.adbPort = 0
                    } catch (_: Throwable) {
                    }
                    withContext(Dispatchers.Main) {
                        dialogBinding.adbTv.text = ""
                        dialogBinding.adbTvTip.isVisible = false
                        dialogBinding.adbPortLayout.isEnabled = true
                        switch.isEnabled = true
                    }
                }
            }
        }

        MaterialAlertDialogBuilder(context, dialogCentered).apply {
            setView(dialogBinding.root)
            show()
        }
    }

    private fun showModuleTilesDialog() {
        val context = requireContext()
        if (SDK < A14) {
            context.toast(getString(R.string.module_tiles_unsupported))
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val packageManager = context.packageManager
            val packageName = context.packageName
            val packageInfo = try {
                @android.annotation.SuppressLint("NewApi")
                if (SDK < 33) packageManager.getPackageInfo(packageName, PackageManager.GET_SERVICES)
                else packageManager.getPackageInfo(
                    packageName, PackageManager.PackageInfoFlags.of(PackageManager.GET_SERVICES.toLong())
                )
            } catch (_: Throwable) {
                null
            } ?: return@launch
            val tileServices = packageInfo.services?.filter {
                it.permission == "android.permission.BIND_QUICK_SETTINGS_TILE"
            } ?: emptyList()
            if (tileServices.isEmpty()) return@launch
            val labels = tileServices.map { serviceInfo ->
                serviceInfo.loadLabel(packageManager).toString()
            }.toTypedArray<CharSequence>()
            withContext(Dispatchers.Main) {
                MaterialAlertDialogBuilder(context, dialogCentered).apply {
                    setTitle(R.string.module_tiles_title)
                    setItems(labels) { _, which ->
                        val serviceInfo = tileServices[which]
                        requestAddTileService(context, serviceInfo)
                    }
                    setNegativeButton(android.R.string.cancel, null)
                    show()
                }
            }
        }
    }

    @android.annotation.SuppressLint("WrongConstant")
    private fun requestAddTileService(context: Context, serviceInfo: ServiceInfo) {
        val statusBarManager = context.getSystemService(StatusBarManager::class.java) ?: return
        val componentName = ComponentName(context.packageName, serviceInfo.name)
        val label = serviceInfo.loadLabel(context.packageManager)
        val icon = serviceInfo.loadIcon(context.packageManager)
        val iconBitmap = try {
            android.graphics.Bitmap.createBitmap(
                icon.intrinsicWidth.coerceAtLeast(1),
                icon.intrinsicHeight.coerceAtLeast(1),
                android.graphics.Bitmap.Config.ARGB_8888
            ).also { bmp ->
                val canvas = android.graphics.Canvas(bmp)
                icon.setBounds(0, 0, canvas.width, canvas.height)
                icon.draw(canvas)
            }
        } catch (_: Throwable) {
            null
        }
        val tileIcon = iconBitmap?.let { Icon.createWithBitmap(it) } ?: Icon.createWithResource(context.packageName, android.R.mipmap.sym_def_app_icon)
        @android.annotation.SuppressLint("NewApi")
        statusBarManager.requestAddTileService(
            componentName,
            label,
            tileIcon,
            context.mainExecutor
        ) { resultCode: Int? ->
            val msg = when (resultCode) {
                0 -> "$label ${getString(R.string.tile_add_failed)}"
                1 -> "$label ${getString(R.string.tile_add_already)}"
                2 -> "$label ${getString(R.string.tile_add_success)}"
                else -> null
            }
            if (msg != null) {
                requireActivity().runOnUiThread { context.toast(msg) }
            }
        }
    }

    private fun showModuleShortcutsDialog() {
        val context = requireContext()
        val shortcutUtils = ShortcutUtils(context)
        val list = shortcutUtils.getShortcutList()
        if (list.isEmpty()) return
        val ids = list.keys.toTypedArray()
        val labels = list.values.toTypedArray<CharSequence>()
        val checked = BooleanArray(ids.size).apply {
            ids.forEachIndexed { index, id ->
                this[index] = context.getBoolean(SettingsPrefs, id, false)
            }
        }
        MaterialAlertDialogBuilder(context, dialogCentered).apply {
            setTitle(context.getString(R.string.set_module_shortcuts))
            setMultiChoiceItems(labels, checked) { _, which, isChecked ->
                checked[which] = isChecked
            }
            setPositiveButton(android.R.string.ok) { _, _ ->
                ids.forEachIndexed { index, id ->
                    shortcutUtils.setShortcutStatus(id, checked[index])
                }
                shortcutUtils.setDynamicShortcuts()
            }
            setNegativeButton(android.R.string.cancel, null)
            show()
        }
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.add(0, 0, 0, getString(R.string.optimize_app)).apply {
            setIcon(R.drawable.ic_baseline_extension_24)
            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            if (ThemeUtils.isNightMode(resources.configuration)) {
                iconTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == 0) {
            showOptimizeAppDialog()
        }
        return true
    }

    private fun showOptimizeAppDialog() {
        val context = requireContext()
        val dialogBinding = DialogAppSelectorBinding.inflate(LayoutInflater.from(context))
        val dialog = MaterialAlertDialogBuilder(context, dialogCentered).apply {
            setTitle(R.string.select_apps_to_optimize)
            setView(dialogBinding.root)
            setPositiveButton(android.R.string.ok) { _, _ ->
                val adapter = dialogBinding.recyclerView.adapter as? AppSelectorAdapter
                val selected = adapter?.getSelected() ?: emptyList()
                if (selected.isNotEmpty()) {
                    optimizeDexForPackages(selected)
                }
            }
            setNegativeButton(android.R.string.cancel, null)
            create()
        }.show()
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        loadAllApps(context, dialogBinding, dialog)
    }

    private fun loadAllApps(
        context: Context,
        binding: DialogAppSelectorBinding,
        dialog: androidx.appcompat.app.AlertDialog
    ) {
        binding.swipeRefreshLayout.isRefreshing = true
        binding.searchViewLayout.isEnabled = false
        lifecycleScope.launch(Dispatchers.IO) {
            val packageManager = context.packageManager
            val appInfos = PackageUtils(packageManager).getInstalledApplications(0)
            val appList = ArrayList<AppInfo>()
            for (info in appInfos) {
                appList.add(
                    AppInfo(
                        info.loadIcon(packageManager),
                        info.loadLabel(packageManager),
                        info.packageName,
                    )
                )
            }
            appList.sortBy { it.appName.toString().lowercase() }
            withContext(Dispatchers.Main) {
                val adapter = AppSelectorAdapter(context, appList) { selected ->
                    dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.isEnabled =
                        selected.isNotEmpty()
                }
                binding.recyclerView.apply {
                    this.adapter = adapter
                    layoutManager = LinearLayoutManager(context)
                }
                binding.searchView.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?, start: Int, count: Int, after: Int
                    ) {
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        adapter.getFilter.filter(s.toString())
                    }

                    override fun afterTextChanged(s: Editable?) {}
                })
                binding.swipeRefreshLayout.setOnRefreshListener {
                    loadAllApps(context, binding, dialog)
                }
                binding.swipeRefreshLayout.isRefreshing = false
                binding.searchViewLayout.isEnabled = true
                dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
            }
        }
    }

    private fun optimizeDexForPackages(packages: List<String>) {
        val context = requireContext()
        val dialog = MaterialAlertDialogBuilder(context, dialogCentered).apply {
            setTitle(R.string.optimizing_dex)
            setMessage(R.string.optimizing_dex_message)
            setCancelable(false)
        }.show()
        lifecycleScope.launch(Dispatchers.IO) {
            val failed = ArrayList<String>()
            for (pkg in packages) {
                val result = ShellUtils.execCommand(
                    "cmd package compile -m speed -f $pkg", true, true
                )
                if (result.result != 0) {
                    failed.add(pkg)
                }
            }
            withContext(Dispatchers.Main) {
                dialog.dismiss()
                if (failed.isEmpty()) {
                    MaterialAlertDialogBuilder(context, dialogCentered).apply {
                        setTitle(R.string.optimize_complete)
                        setMessage(R.string.optimize_complete_message)
                        setPositiveButton(R.string.restart_scope) { _, _ ->
                            context.restartAllScope()
                        }
                        setNeutralButton(R.string.fast_reboot) { _, _ ->
                            ShellUtils.execCommand("killall zygote", true)
                        }
                        setNegativeButton(android.R.string.cancel, null)
                        show()
                    }
                } else {
                    MaterialAlertDialogBuilder(context, dialogCentered).apply {
                        setTitle(R.string.optimize_failed)
                        setMessage("${context.getString(R.string.optimize_failed_apps)}\n${failed.joinToString("\n")}")
                        setPositiveButton(android.R.string.ok, null)
                        show()
                    }
                }
            }
        }
    }
}
