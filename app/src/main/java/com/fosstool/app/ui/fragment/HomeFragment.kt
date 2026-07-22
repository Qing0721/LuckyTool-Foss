package com.fosstool.app.ui.fragment

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.drake.net.utils.scopeLife
import com.drake.net.utils.withDefault
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.highcapable.yukihookapi.YukiHookAPI
import com.fosstool.app.IGlobalFuncController
import com.fosstool.app.R
import com.fosstool.app.databinding.FragmentHomeBinding
import com.fosstool.app.ui.activity.MainActivity
import com.fosstool.app.utils.*

class HomeFragment : Fragment(), MenuProvider {

    private lateinit var binding: FragmentHomeBinding
    private var homeFuncController: IGlobalFuncController? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        setupMenuProvider(this)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        refreshModuleStatus()

        binding.systemInfo.apply {
            setOnLongClickListener {
                val isRealmeUI: Boolean
                val oplusOtaDialog = MaterialAlertDialogBuilder(context, dialogCentered).apply {
                    setTitle("OPLUS OTA")
                    setView(R.layout.layout_oplusota_dialog)
                }.show()
                val productModel =
                    oplusOtaDialog.findViewById<TextInputEditText>(R.id.oplusota_product_model)
                        ?.apply {
                            setText(getProp("ro.product.name"))
                            setOnLongClickListener {
                                context.copyStr(text as CharSequence)
                                true
                            }
                        }
                val otaVersion =
                    oplusOtaDialog.findViewById<TextInputEditText>(R.id.oplusota_ota_version)
                        ?.apply {
                            setText(getProp("ro.build.version.ota"))
                            setOnLongClickListener {
                                context.copyStr(text as CharSequence)
                                true
                            }
                        }
                val realmeuiVersionLayout =
                    oplusOtaDialog.findViewById<TextInputLayout>(R.id.oplusota_realmeui_version_layout)
                val realmeuiVersion =
                    oplusOtaDialog.findViewById<TextInputEditText>(R.id.oplusota_realmeui_version)
                        ?.apply {
                            setText(getProp("ro.build.version.realmeui"))
                            setOnLongClickListener {
                                context.copyStr(text as CharSequence)
                                true
                            }
                        }
                if (realmeuiVersion?.text.toString()
                        .isBlank() || realmeuiVersion?.text.toString() == "null"
                ) {
                    isRealmeUI = false
                    realmeuiVersionLayout?.isVisible = false
                } else isRealmeUI = true
                val nvIdentifier =
                    oplusOtaDialog.findViewById<TextInputEditText>(R.id.oplusota_nv_identifier)
                        ?.apply {
                            setText(getProp("ro.build.oplus_nv_id"))
                            setOnLongClickListener {
                                context.copyStr(text as CharSequence)
                                true
                            }
                        }
                val guid =
                    oplusOtaDialog.findViewById<TextInputEditText>(R.id.oplusota_guid)?.apply {
                        setText(getGuid)
                        setOnLongClickListener {
                            context.copyStr(text as CharSequence)
                            true
                        }
                    }
                oplusOtaDialog.findViewById<MaterialButton>(R.id.oplusota_copyall)?.apply {
                    setOnClickListener {
                        context.copyStr("ro.product.name -> ${productModel?.text}\nro.build.version.ota -> ${otaVersion?.text}\n${if (isRealmeUI) "ro.build.version.realmeui -> ${realmeuiVersion?.text}\n" else ""}ro.build.oplus_nv_id -> ${nvIdentifier?.text}\nguid -> ${guid?.text}\n")
                    }
                }
                true
            }
        }

        binding.tv.apply {
            isVisible = false
        }
    }

    private fun initSystemInfoText(funcController: IGlobalFuncController) {
        scopeLife {
            val deviceInfo = withDefault { requireActivity().getDeviceInfo(funcController) }
            binding.systemInfo.gravity = Gravity.START
            binding.systemInfo.text = deviceInfo
        }
    }

    private fun initController() {
        if (homeFuncController == null) {
            (activity as MainActivity).initController {
                homeFuncController = it
                initSystemInfoText(it)
            }
        } else {
            initSystemInfoText(homeFuncController!!)
        }
    }

    override fun onResume() {
        super.onResume()
        initController()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.add(0, 1, 0, getString(R.string.menu_reboot)).apply {
            setIcon(R.drawable.ic_baseline_refresh_24)
            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            if (ThemeUtils.isNightMode(resources.configuration)) {
                this.iconTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
        menu.add(0, 2, 0, getString(R.string.menu_settings)).apply {
            setIcon(R.drawable.ic_baseline_info_24)
            setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            if (ThemeUtils.isNightMode(resources.configuration)) {
                this.iconTintList = ColorStateList.valueOf(Color.WHITE)
            }
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == 1) requireActivity().restartMain()
        if (menuItem.itemId == 2) {
            MaterialAlertDialogBuilder(requireActivity()).apply {
                setTitle(getString(R.string.about_author))
                setView(MaterialTextView(context).apply {
                    var hideFunc = context.getBoolean(SettingsPrefs, "hidden_function", false)
                    setPadding(20.dp)
                    text = if (hideFunc) "忆清鸣、luckyzyx T" else "忆清鸣、luckyzyx"
                    setOnLongClickListener {
                        context.putBoolean(SettingsPrefs, "hidden_function", !hideFunc)
                        hideFunc = context.getBoolean(SettingsPrefs, "hidden_function", false)
                        text = if (hideFunc) "忆清鸣、luckyzyx T" else "忆清鸣、luckyzyx"
                        true
                    }
                })
                show()
            }
        }
        return true
    }

    @SuppressLint("SetTextI18n")
    fun refreshModuleStatus() {
        when {
            YukiHookAPI.Status.isXposedModuleActive -> {
                binding.statusIcon.setImageResource(R.drawable.ic_round_check_24)
            }

            else -> {
                binding.statusCard.setCardBackgroundColor(Color.GRAY)
                binding.statusIcon.setImageResource(R.drawable.ic_round_warning_24)
            }
        }
        binding.statusTitle.text = when {
            YukiHookAPI.Status.isXposedModuleActive -> getString(R.string.module_isactivated)
            else -> getString(R.string.module_notactive)
        }

        binding.statusSummary.apply {
            text = "${getString(R.string.module_version)}$getVersionName($getVersionCode)"
        }
    }
}
