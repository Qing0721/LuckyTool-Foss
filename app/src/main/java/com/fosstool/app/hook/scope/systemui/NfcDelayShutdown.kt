package com.fosstool.app.hook.scope.systemui

import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Handler
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.utils.ModulePrefs
import java.util.Locale
import java.util.regex.Pattern

object NfcDelayShutdown : YukiBaseHooker() {

    private val delayPattern = Pattern.compile("(\\d+)([WwDdHhMmSs])")

    @Volatile
    private var enabled = false

    @Volatile
    private var delaySpec = "10M"

    private var pendingDisable: Runnable? = null
    private var mainHandler: Handler? = null

    override fun onHook() {
        enabled = prefs(ModulePrefs).getBoolean("enable_nfc_delay_shutdown", false)
        if (!enabled) return
        delaySpec = prefs(ModulePrefs).getString("custom_nfc_delay_shutdown_time", "10M") ?: "10M"

        dataChannel.wait<Boolean>("enable_nfc_delay_shutdown") { enabled = it }
        dataChannel.wait<String>("custom_nfc_delay_shutdown_time") { delaySpec = it }

        onAppLifecycle {
            registerReceiver(Intent.ACTION_USER_PRESENT) { context, intent ->
                onNfcRelatedBroadcast(context, intent)
            }
            registerReceiver("LuckyTool_CloseCollapse") { context, intent ->
                onNfcRelatedBroadcast(context, intent)
            }
            registerReceiver(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED) { context, intent ->
                onNfcRelatedBroadcast(context, intent)
            }
        }
    }

    private fun onNfcRelatedBroadcast(context: Context, intent: Intent) {
        if (!enabled) return
        val delayMs = parseDelayToMs(delaySpec)
        if (delayMs < 0) {
            enabled = false
            return
        }
        val adapter = NfcAdapter.getDefaultAdapter(context) ?: return
        val handler = mainHandler ?: Handler(context.mainLooper).also { mainHandler = it }
        val task = pendingDisable ?: Runnable {
            runCatching {
                @android.annotation.SuppressLint("MissingPermission", "NewApi")
                if (adapter.isEnabled) adapter.disable()
            }
        }.also { pendingDisable = it }

        try {
            if (!adapter.isEnabled) {
                if (handler.hasCallbacks(task)) handler.removeCallbacks(task)
            } else {
                if (!handler.hasCallbacks(task)) {
                    handler.postDelayed(task, delayMs)
                }
            }
        } catch (_: Throwable) {
            handler.removeCallbacks(task)
            if (adapter.isEnabled) handler.postDelayed(task, delayMs)
        }
    }

    private fun parseDelayToMs(raw: String): Long {
        val s = raw.trim()
        if (s.isEmpty()) return 10L * 60_000L
        val m = delayPattern.matcher(s)
        if (m.find()) {
            val n = m.group(1)?.toLongOrNull() ?: return -1L
            val unit = m.group(2)?.uppercase(Locale.ROOT) ?: return -1L
            val mul = when (unit) {
                "W" -> 604_800_000L
                "D" -> 86_400_000L
                "H" -> 3_600_000L
                "M" -> 60_000L
                "S" -> 1_000L
                else -> return -1L
            }
            return n * mul
        }
        val digits = s.toLongOrNull() ?: return -1L
        return if (digits >= 1000L) digits else digits * 1000L
    }
}
