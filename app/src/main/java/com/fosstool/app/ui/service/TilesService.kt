package com.fosstool.app.ui.service

import android.content.ComponentName
import android.os.IBinder
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.telephony.SubscriptionManager
import com.highcapable.yukihookapi.hook.factory.dataChannel
import com.fosstool.app.IDarkModeController
import com.fosstool.app.IFiveGController
import com.fosstool.app.IGlobalDCController
import com.fosstool.app.IGoogleServiceController
import com.fosstool.app.IHighBrightnessController
import com.fosstool.app.IRefreshRateController
import com.fosstool.app.ITouchPanelController
import com.fosstool.app.utils.SettingsPrefs
import com.fosstool.app.utils.OtherPrefs
import com.fosstool.app.utils.bindRootService
import com.fosstool.app.utils.closeCollapse
import com.fosstool.app.utils.jumpBatteryInfo
import com.fosstool.app.utils.jumpHighPerformance
import com.fosstool.app.utils.jumpRunningApp
import com.fosstool.app.utils.putBoolean

class ChargingTest : TileService() {
    override fun onClick() {
        closeCollapse()
        jumpBatteryInfo(this)
    }
}

class ProcessManager : TileService() {
    override fun onClick() {
        closeCollapse()
        jumpRunningApp(this)
    }
}

class HighPerformanceMode : TileService() {

    override fun onClick() {
        closeCollapse()
        jumpHighPerformance(this)
    }
}

class ShowFPS : TileService() {
    private var controller: IRefreshRateController? = null

    override fun onStartListening() = startController()

    override fun onClick() {
        if (qsTile.state == Tile.STATE_INACTIVE) controller?.refreshRateDisplay = true
        else if (qsTile.state == Tile.STATE_ACTIVE) controller?.refreshRateDisplay = false
        refreshData()
    }

    private fun startController() {
        if (controller == null) bindRootService(RefreshRateControllerService::class.java,
            { _: ComponentName?, iBinder: IBinder? ->
                controller = IRefreshRateController.Stub.asInterface(iBinder)
                refreshData()
            })
    }

    private fun refreshData() {
        qsTile.state = if (controller == null) Tile.STATE_UNAVAILABLE
        else if (controller!!.refreshRateDisplay) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }
}

class HighBrightness : TileService() {
    private var controller: IHighBrightnessController? = null
    val key = "high_brightness_mode"

    override fun onStartListening() = startController()

    override fun onClick() {
        when (qsTile.state) {
            Tile.STATE_INACTIVE -> {
                controller?.highBrightnessMode = true
                putBoolean(SettingsPrefs, "high_brightness_mode", true)
                dataChannel("com.android.systemui").put(key, true)
            }

            Tile.STATE_ACTIVE -> {
                controller?.highBrightnessMode = false
                putBoolean(SettingsPrefs, "high_brightness_mode", false)
                dataChannel("com.android.systemui").put(key, false)
            }

            Tile.STATE_UNAVAILABLE -> {}
        }
        refreshData()
    }

    private fun startController() {
        if (controller == null) bindRootService(HighBrightnessControllerService::class.java,
            { _: ComponentName?, iBinder: IBinder? ->
                controller = IHighBrightnessController.Stub.asInterface(iBinder)
                refreshData()
            })
    }

    private fun refreshData() {
        qsTile.state = if (controller == null) Tile.STATE_UNAVAILABLE
        else if (!controller!!.checkHighBrightnessMode()) Tile.STATE_UNAVAILABLE
        else if (controller!!.highBrightnessMode) Tile.STATE_ACTIVE
        else Tile.STATE_INACTIVE
        qsTile.updateTile()
        if (qsTile.state == Tile.STATE_UNAVAILABLE) putBoolean(
            SettingsPrefs, key, false
        )
    }
}

class GlobalDC : TileService() {
    private var controller: IGlobalDCController? = null
    val key = "global_dc_mode"

    override fun onStartListening() = startController()

    override fun onClick() {
        when (qsTile.state) {
            Tile.STATE_INACTIVE -> {
                controller?.globalDCMode = true
                putBoolean(SettingsPrefs, "global_dc_mode", true)
                dataChannel("com.android.systemui").put(key, true)
            }

            Tile.STATE_ACTIVE -> {
                controller?.globalDCMode = false
                putBoolean(SettingsPrefs, "global_dc_mode", false)
                dataChannel("com.android.systemui").put(key, false)
            }

            Tile.STATE_UNAVAILABLE -> {}
        }
        refreshData()
    }

    private fun startController() {
        if (controller == null) bindRootService(GlobalDCControllerService::class.java,
            { _: ComponentName?, iBinder: IBinder? ->
                controller = IGlobalDCController.Stub.asInterface(iBinder)
                refreshData()
            })
    }

    private fun refreshData() {
        qsTile.state = if (controller == null) Tile.STATE_UNAVAILABLE
        else if (!controller!!.checkGlobalDCMode()) Tile.STATE_UNAVAILABLE
        else if (controller!!.globalDCMode) Tile.STATE_ACTIVE
        else Tile.STATE_INACTIVE
        qsTile.updateTile()
        if (qsTile.state == Tile.STATE_UNAVAILABLE) putBoolean(
            SettingsPrefs, key, false
        )
    }
}

class TouchSamplingRate : TileService() {
    private var controller: ITouchPanelController? = null
    val key = "touch_sampling_rate"

    override fun onStartListening() = startController()

    override fun onClick() {
        when (qsTile.state) {
            Tile.STATE_INACTIVE -> {
                controller?.touchMode = true
                putBoolean(SettingsPrefs, "touch_sampling_rate", true)
                dataChannel("com.android.systemui").put(key, true)
            }

            Tile.STATE_ACTIVE -> {
                controller?.touchMode = false
                putBoolean(SettingsPrefs, "touch_sampling_rate", false)
                dataChannel("com.android.systemui").put(key, false)
            }

            Tile.STATE_UNAVAILABLE -> {}
        }
        refreshData()
    }

    private fun startController() {
        if (controller == null) bindRootService(TouchPanelControllerService::class.java,
            { _: ComponentName?, iBinder: IBinder? ->
                controller = ITouchPanelController.Stub.asInterface(iBinder)
                refreshData()
            })
    }

    private fun refreshData() {
        qsTile.state = if (controller == null) Tile.STATE_UNAVAILABLE
        else if (!controller!!.checkTouchMode()) Tile.STATE_UNAVAILABLE
        else if (controller!!.touchMode) Tile.STATE_ACTIVE
        else Tile.STATE_INACTIVE
        qsTile.updateTile()
        if (qsTile.state == Tile.STATE_UNAVAILABLE) putBoolean(
            SettingsPrefs, key, false
        )
    }
}

class FiveG : TileService() {
    private var controller: IFiveGController? = null
    override fun onStartListening() = startController()

    override fun onClick() {
        val subId = SubscriptionManager.getDefaultDataSubscriptionId()
        if (qsTile.state == Tile.STATE_INACTIVE) controller?.setFiveGStatus(subId, true)
        else if (qsTile.state == Tile.STATE_ACTIVE) controller?.setFiveGStatus(subId, false)
        refreshData()
    }

    private fun startController() {
        if (controller == null) bindRootService(FiveGControllerService::class.java,
            { _: ComponentName?, iBinder: IBinder? ->
                controller = IFiveGController.Stub.asInterface(iBinder)
                refreshData()
            })
    }

    private fun refreshData() {
        val subId = SubscriptionManager.getDefaultDataSubscriptionId()
        qsTile.state = if (controller == null) Tile.STATE_UNAVAILABLE
        else if (!controller!!.checkCompatibility(subId)) Tile.STATE_UNAVAILABLE
        else if (controller!!.getFiveGStatus(subId)) Tile.STATE_ACTIVE
        else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }
}

class VeryDarkMode : TileService() {
    private var controller: IDarkModeController? = null

    override fun onStartListening() = startController()

    override fun onClick() {
        when (qsTile.state) {
            Tile.STATE_INACTIVE -> controller?.darkMode = true
            Tile.STATE_ACTIVE -> controller?.darkMode = false
            Tile.STATE_UNAVAILABLE -> {}
        }
        refreshData()
    }

    private fun startController() {
        if (controller == null) bindRootService(DarkModeControllerService::class.java,
            { _: ComponentName?, iBinder: IBinder? ->
                controller = IDarkModeController.Stub.asInterface(iBinder)
                refreshData()
            })
    }

    private fun refreshData() {
        qsTile.state = if (controller == null) Tile.STATE_UNAVAILABLE
        else if (!controller!!.checkDarkMode()) Tile.STATE_UNAVAILABLE
        else if (controller!!.darkMode) Tile.STATE_ACTIVE
        else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }
}

class GoogleService : TileService() {
    private var controller: IGoogleServiceController? = null

    override fun onStartListening() = startController()

    override fun onClick() {
        when (qsTile.state) {
            Tile.STATE_INACTIVE -> controller?.googleStatus = true
            Tile.STATE_ACTIVE -> controller?.googleStatus = false
            Tile.STATE_UNAVAILABLE -> {}
        }
        refreshData()
    }

    private fun startController() {
        if (controller == null) bindRootService(GoogleServiceControllerService::class.java,
            { _: ComponentName?, iBinder: IBinder? ->
                controller = IGoogleServiceController.Stub.asInterface(iBinder)
                refreshData()
            })
    }

    private fun refreshData() {
        qsTile.state = if (controller == null) Tile.STATE_UNAVAILABLE
        else if (controller!!.googleStatus) Tile.STATE_ACTIVE
        else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }
}

class BypassPowerMode : TileService() {
    val key = "bypass_power_mode"

    private val sysfsPath = "/sys/devices/virtual/oplus_chg/battery/mmi_charging_enable"

    private val powerReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: android.content.Context?, intent: android.content.Intent?) {
            if (context == null || intent == null) return
            val action = intent.action ?: return
            if (action == android.content.Intent.ACTION_POWER_CONNECTED ||
                action == android.content.Intent.ACTION_POWER_DISCONNECTED
            ) {
                if (!isBatteryChargingOrFull()) {
                    com.fosstool.app.utils.ShellUtils.execCommand("echo 1 > $sysfsPath", true)
                    putBoolean(SettingsPrefs, key, false)
                    dataChannel("com.android.systemui").put(key, false)
                }
                refreshData()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val filter = android.content.IntentFilter().apply {
            addAction(android.content.Intent.ACTION_POWER_CONNECTED)
            addAction(android.content.Intent.ACTION_POWER_DISCONNECTED)
        }
        runCatching { registerReceiver(powerReceiver, filter) }
    }

    override fun onDestroy() {
        super.onDestroy()
        runCatching { unregisterReceiver(powerReceiver) }
    }

    override fun onStartListening() = refreshData()

    override fun onClick() {
        when (qsTile.state) {
            Tile.STATE_INACTIVE -> {
                if (!isBatteryChargingOrFull()) {
                    refreshData()
                    return
                }
                com.fosstool.app.utils.ShellUtils.execCommand("echo 0 > $sysfsPath", true)
                putBoolean(SettingsPrefs, key, true)
                dataChannel("com.android.systemui").put(key, true)
            }
            Tile.STATE_ACTIVE -> {
                com.fosstool.app.utils.ShellUtils.execCommand("echo 1 > $sysfsPath", true)
                putBoolean(SettingsPrefs, key, false)
                dataChannel("com.android.systemui").put(key, false)
            }
            Tile.STATE_UNAVAILABLE -> {}
        }
        refreshData()
    }

    private fun isBatteryChargingOrFull(): Boolean {
        val intent = registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(android.os.BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
            status == android.os.BatteryManager.BATTERY_STATUS_FULL
    }

    private fun refreshData() {
        val result = com.fosstool.app.utils.ShellUtils.execCommand("cat $sysfsPath", true, true)
        val firstChar = result.successMsg?.trim()?.firstOrNull()
        val available = result.result == 0 && firstChar != null
        val enabled = firstChar == '0'
        qsTile.state = if (!available) Tile.STATE_UNAVAILABLE
        else if (enabled) Tile.STATE_ACTIVE
        else Tile.STATE_INACTIVE
        qsTile.updateTile()
        if (qsTile.state == Tile.STATE_UNAVAILABLE) putBoolean(SettingsPrefs, key, false)
    }
}

class RemoteAdb : TileService() {
    private var controller: com.fosstool.app.IAdbDebugController? = null
    val key = "remote_adb"

    override fun onStartListening() = startController()

    override fun onClick() {
        when (qsTile.state) {
            Tile.STATE_INACTIVE -> {
                val port = getSharedPreferences(OtherPrefs, 0).getString("adb_port", "6666") ?: "6666"
                controller?.adbPort = port.toInt()
                controller?.restartAdb()
                putBoolean(SettingsPrefs, key, true)
            }
            Tile.STATE_ACTIVE -> {
                controller?.adbPort = -1
                controller?.restartAdb()
                controller?.adbPort = 0
                putBoolean(SettingsPrefs, key, false)
            }
            Tile.STATE_UNAVAILABLE -> {}
        }
        refreshData()
    }

    private fun startController() {
        if (controller == null) bindRootService(AdbDebugControllerService::class.java,
            { _: ComponentName?, iBinder: IBinder? ->
                controller = com.fosstool.app.IAdbDebugController.Stub.asInterface(iBinder)
                refreshData()
            })
    }

    private fun refreshData() {
        val port = controller?.adbPort ?: 0
        qsTile.state = if (port == 0 || port == -1) Tile.STATE_INACTIVE
        else Tile.STATE_ACTIVE
        qsTile.updateTile()
    }
}

class RunInBackground : TileService() {
    val key = "run_in_background_tile"
    private var controller: com.fosstool.app.IRunInBackgroundController? = null

    override fun onStartListening() {
        startController()
        refreshData()
    }

    override fun onClick() {
        if (qsTile.state == Tile.STATE_INACTIVE) {
            runCatching { sendBroadcast(android.content.Intent("LuckyTool_CloseCollapse")) }
            controller?.startBackgroundStreamMode()
        }
        refreshData()
    }

    private fun startController() {
        if (controller == null) bindRootService(
            RunInBackgroundControllerService::class.java,
            { _: ComponentName?, iBinder: IBinder? ->
                controller = com.fosstool.app.IRunInBackgroundController.Stub.asInterface(iBinder)
                refreshData()
            }
        )
    }

    private fun refreshData() {
        qsTile.state = if (controller == null) Tile.STATE_UNAVAILABLE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }
}
