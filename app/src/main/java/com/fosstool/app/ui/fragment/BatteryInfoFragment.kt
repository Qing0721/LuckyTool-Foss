package com.fosstool.app.ui.fragment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.fosstool.app.R

class BatteryInfoFragment : Fragment() {
    private lateinit var batteryInfoText: TextView
    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent == null || context == null) return
            updateBatteryInfo(intent)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_battery_info, container, false)
        batteryInfoText = view.findViewById(R.id.battery_info_text)
        return view
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                addAction("android.intent.action.ADDITIONAL_BATTERY_CHANGED")
            }
        }
        ContextCompat.registerReceiver(
            requireContext(), batteryReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(batteryReceiver)
    }

    private fun updateBatteryInfo(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        val technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
        val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

        val batteryPct = if (level >= 0 && scale > 0) (level.toFloat() / scale.toFloat() * 100).toInt() else -1
        val tempC = if (temperature >= 0) temperature.toFloat() / 10f else -1f
        val voltMV = if (voltage >= 0) voltage else -1

        val sb = StringBuilder()
        sb.append("电量: ${batteryPct}%\n")
        sb.append("电压: ${voltMV}mV\n")
        sb.append("温度: ${tempC}°C\n")
        sb.append("状态: ${when(status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "充电中"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "放电中"
            BatteryManager.BATTERY_STATUS_FULL -> "已充满"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "未充电"
            else -> "未知"
        }}\n")
        sb.append("健康: ${when(health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "良好"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "过热"
            BatteryManager.BATTERY_HEALTH_DEAD -> "已损坏"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "过压"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "未知故障"
            else -> "未知"
        }}\n")
        sb.append("技术: ${technology ?: "未知"}\n")
        sb.append("充电方式: ${when(plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "AC充电器"
            BatteryManager.BATTERY_PLUGGED_USB -> "USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "无线充电"
            else -> "未知"
        }}\n")

        batteryInfoText.text = sb.toString()
    }
}
