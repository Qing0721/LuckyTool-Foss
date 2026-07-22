package com.fosstool.app.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.drake.net.utils.scopeLife
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.highcapable.yukihookapi.YukiHookAPI
import com.highcapable.yukihookapi.hook.factory.prefs
import com.joom.paranoid.Obfuscate
import com.fosstool.app.BuildConfig
import com.fosstool.app.IGlobalFuncController
import com.fosstool.app.R
import com.fosstool.app.databinding.ActivityMainBinding
import com.fosstool.app.ui.fragment.HomeFragment
import com.fosstool.app.ui.service.GlobalFuncControllerService
import com.fosstool.app.utils.*
import kotlinx.coroutines.Dispatchers
import kotlin.system.exitProcess

@Obfuscate
@Suppress("PrivatePropertyName")
open class MainActivity : AppCompatActivity() {
    private var isStart = YukiHookAPI.Status.isXposedModuleActive
    private val KEY_PREFIX = MainActivity::class.java.name + '.'
    private val EXTRA_SAVED_INSTANCE_STATE = KEY_PREFIX + "SAVED_INSTANCE_STATE"

    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostFragment: NavHostFragment
    private lateinit var navController: NavController

    private fun newIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java)
    }

    private fun newIntent(savedInstanceState: Bundle, context: Context): Intent {
        return newIntent(context).putExtra(EXTRA_SAVED_INSTANCE_STATE, savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkTheme()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavigationFragment()
        initDynamicShortcuts()
        checkVerify()
        checkSuAndOS()
    }

    private fun checkSuAndOS() {
        val noModulePrefs = prefs(ModulePrefs).isPreferencesAvailable.not()
        val noSettingPrefs = prefs(SettingsPrefs).isPreferencesAvailable.not()
        val noOtherPrefs = prefs(OtherPrefs).isPreferencesAvailable.not()
        if (!isStart || noModulePrefs || noSettingPrefs || noOtherPrefs) {
            MaterialAlertDialogBuilder(this).apply {
                setCancelable(false)
                setMessage(getString(R.string.unsupported_xposed, "v1.9.2"))
                setPositiveButton(android.R.string.ok) { _, _ -> exitProcess(0) }
                setOnDismissListener { exitModule() }
                show()
            }
            return
        }
        val isSu = ShellUtils.checkRootPermission()
        putBoolean(SettingsPrefs, "is_su", isSu)
        if (!isSu) {
            MaterialAlertDialogBuilder(this, dialogCentered).apply {
                setCancelable(false)
                setTitle(getString(R.string.no_root))
                setMessage(getString(R.string.no_root_summary))
                setPositiveButton(android.R.string.ok) { _, _ -> exitProcess(0) }
                setOnDismissListener { exitModule() }
                show()
            }
            return
        }
        if (getOSVersionCode < 23) {
            val current = navController.currentDestination.toString()
            MaterialAlertDialogBuilder(this, dialogCentered).apply {
                setTitle(getString(R.string.unsupported_os))
                setMessage(getString(R.string.unsupported_os_summary))
                setNeutralButton(getString(R.string.common_words_ignore), null)
                setPositiveButton(android.R.string.ok) { _, _ -> exitProcess(0) }
                if (current.contains(HomeFragment::class.java.simpleName)) show()
            }
        }
        if (!BuildConfig.DEBUG) putBoolean(SettingsPrefs, "enable_module_print_logs", false)
        scopeLife(dispatcher = Dispatchers.Default) {
            putBoolean(SettingsPrefs, "boot_complete", true)
            putBoolean(SettingsPrefs, "boot_complete_final", true)
        }
        PermissionUtils(this).checkPermissions()
    }

    private fun initDynamicShortcuts() {
        if (!ShortcutUtils(this).getIconStatus()) return
        if (ShortcutUtils(this).getShortcutEnabledList().isEmpty()) return
        ShortcutUtils(this).setDynamicShortcuts()
    }

    private fun initNavigationFragment() {
        navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container)
                as NavHostFragment
        navController = navHostFragment.navController
        val appBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_other,
            R.id.nav_function,
            R.id.nav_home,
            R.id.nav_log,
            R.id.nav_setting,
        ).build()
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
        val bottomNavigationView = binding.navView
        bottomNavigationView.labelVisibilityMode = BottomNavigationView.LABEL_VISIBILITY_SELECTED
        bottomNavigationView.setupWithNavController(navController)
        bottomNavigationView.setOnItemSelectedListener {
            NavigationUI.onNavDestinationSelected(it, navController)
            true
        }
    }

    private fun checkTheme() {
        if (ThemeUtils.isDynamicColor(this)) {
            DynamicColors.applyToActivityIfAvailable(this)
        }
        val themeMode = getString(SettingsPrefs, "dark_theme", "MODE_NIGHT_FOLLOW_SYSTEM")
        ThemeUtils.initTheme(themeMode)
    }

    @Suppress("DEPRECATION")
    fun restart() {
        if (SDK >= A12 || !Process.isApplicationUid(Process.myUid())) {
            recreate()
        } else {
            try {
                val savedInstanceState = Bundle()
                onSaveInstanceState(savedInstanceState)
                finish()
                startActivity(newIntent(savedInstanceState, this))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            } catch (e: Throwable) {
                recreate()
            }
        }
    }

    fun initController(result: (IGlobalFuncController) -> Unit) {
        bindRootService(GlobalFuncControllerService::class.java, { _, iBinder ->
            val funcController = IGlobalFuncController.Stub.asInterface(iBinder)
            result(funcController)
        })
    }

    override fun onResume() {
        super.onResume()
        PermissionUtils(this).checkPermissions()
    }
}
