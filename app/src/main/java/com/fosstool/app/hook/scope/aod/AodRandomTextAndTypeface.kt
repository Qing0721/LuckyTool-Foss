package com.fosstool.app.hook.scope.aod

import android.graphics.Typeface
import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.concurrent.thread
import kotlin.random.Random

object AodRandomTextAndTypeface : YukiBaseHooker() {

    private val lines = CopyOnWriteArrayList<String>()
    private var mode = "0"

    override fun onHook() {
        mode = prefs(ModulePrefs).getString("set_random_text_display_mode", "0") ?: "0"
        val randomFile = prefs(ModulePrefs).getString("custom_random_text_file", "") ?: ""
        val randomApi = prefs(ModulePrefs).getString("custom_random_text_api", "") ?: ""
        val typefaceMode = prefs(ModulePrefs).getString("set_aod_typeface_mode", "0") ?: "0"
        val applyToClock =
            prefs(ModulePrefs).getBoolean("apply_typeface_to_aod_clock", false) ||
                prefs(ModulePrefs).getBoolean("apply_aod_clock_typeface", false)

        if (mode != "0") {
            preloadLines(mode, randomFile, randomApi)
            runCatching {
                "com.oplus.aodimpl.AodRootLayout".toClass().apply {
                    constructor { paramCount = 2 }.hook {
                        after { preloadLines(mode, randomFile, randomApi) }
                    }
                    method {
                        name = "getCustomView"
                    }.hookAll {
                        after {
                            val view = result as? android.view.View ?: return@after
                            val text = pickLine() ?: return@after
                            applyTextToViewTree(view, text)
                        }
                    }
                }
            }
        }

        if (typefaceMode != "0") {
            val typeface = when (typefaceMode) {
                "1" -> Typeface.DEFAULT
                "2" -> Typeface.DEFAULT_BOLD
                else -> null
            }
            if (typeface != null) {
                listOf(
                    "com.oplus.egview.widget.AodTextView",
                    "com.oplus.aod.widget.AodTextView",
                    "com.oplus.aodimpl.AodTextView",
                    "com.oplusos.systemui.aod.AodTextView",
                ).forEach { cls ->
                    runCatching {
                        cls.toClass().apply {
                            constructor { paramCount = 3 }.hookAll {
                                after { instance<TextView>()?.typeface = typeface }
                            }
                        }
                    }
                }
                if (applyToClock) {
                    listOf(
                        "com.oplus.egview.widget.TimeView",
                        "com.oplus.aod.widget.TimeView",
                        "com.oplus.aodimpl.TimeView",
                        "com.oplusos.systemui.aod.TimeView",
                    ).forEach { cls ->
                        runCatching {
                            cls.toClass().apply {
                                method { name = "setTextWidget" }.hookAll {
                                    after {
                                        runCatching {
                                            instance<TextView>()?.typeface = typeface
                                            field { name = "mTextPaint"; superClass() }
                                                .get(instance).cast<android.text.TextPaint>()
                                                ?.setTypeface(typeface)
                                            instance<android.view.View>()?.requestLayout()
                                        }
                                        val host = instance as? android.view.ViewGroup
                                        host?.let { applyTypefaceTree(it, typeface) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun preloadLines(mode: String, filePath: String, apiUrl: String) {
        thread(name = "AodRandomTextLoad", isDaemon = true) {
            runCatching {
                val loaded = when (mode) {
                    "1" -> {
                        if (filePath.isBlank()) emptyList()
                        else {
                            val f = File(filePath)
                            if (!f.exists()) emptyList()
                            else BufferedReader(
                                InputStreamReader(FileInputStream(f), StandardCharsets.UTF_8)
                            ).use { br -> br.readLines().filter { it.isNotBlank() } }
                        }
                    }
                    "2" -> {
                        if (apiUrl.isBlank()) emptyList()
                        else {
                            val conn = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
                                connectTimeout = 5000
                                readTimeout = 5000
                                requestMethod = "GET"
                            }
                            conn.inputStream.bufferedReader(StandardCharsets.UTF_8).use { br ->
                                br.readLines().filter { it.isNotBlank() }
                            }.also { conn.disconnect() }
                        }
                    }
                    else -> emptyList()
                }
                if (loaded.isNotEmpty()) {
                    lines.clear()
                    lines.addAll(loaded)
                }
            }
        }
    }

    private fun pickLine(): String? {
        if (lines.isEmpty()) return null
        return lines[Random.nextInt(lines.size)]
    }

    private fun applyTextToViewTree(view: android.view.View, text: String) {
        if (view is TextView) {
            val cur = view.text?.toString().orEmpty()
            if (cur.any { it.isLetter() } || cur.isEmpty() || cur.length > 8) {
                view.text = text
            }
            return
        }
        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                applyTextToViewTree(view.getChildAt(i), text)
            }
        }
    }

    private fun applyTypefaceTree(group: android.view.ViewGroup, tf: Typeface) {
        for (i in 0 until group.childCount) {
            when (val c = group.getChildAt(i)) {
                is TextView -> c.typeface = tf
                is android.view.ViewGroup -> applyTypefaceTree(c, tf)
            }
        }
    }
}
