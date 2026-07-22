package com.fosstool.app.ui.service

import android.content.Intent
import com.fosstool.app.ITouchPanelController
import com.fosstool.app.utils.ShellUtils
import com.fosstool.app.utils.replaceSpace
import com.topjohnwu.superuser.ipc.RootService
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileOutputStream

class TouchPanelControllerService : RootService() {
    companion object {
        private val gameSwitchFile = File("/proc/touchpanel/game_switch_enable")
        private val touchHidlTestFile = File("/odm/bin/touchHidlTest")
        private val touchHidlTestReadCmd = "/odm/bin/touchHidlTest -c ro 0 26"
        private val touchHidlTestWriteCmd = "/odm/bin/touchHidlTest -c wo 0 26 "

        private val mode: Int
            get() = if (gameSwitchFile.exists()) 1 else if (touchHidlTestFile.exists()) 2 else 0

        private fun toHexString(value: Int): String = Integer.toHexString(value)
    }

    override fun onBind(intent: Intent) = object : ITouchPanelController.Stub() {

        override fun checkTouchMode(): Boolean {
            return runCatching { gameSwitchFile.exists() }.getOrDefault(false)
        }

        override fun getTouchMode(): Boolean {
            return runCatching { readSamplingRateValue() != null }.getOrDefault(false)
        }

        override fun setTouchMode(status: Boolean) {
            runCatching {
                val hex = toHexString(if (status) 1 else 0)
                writeHexValue(hex)
            }
        }

        override fun checkSamplingRateLevel(): Boolean {
            return runCatching { mode != 0 }.getOrDefault(false)
        }

        override fun getSamplingRateLevel(): Int {
            return runCatching { readSamplingRateValue() ?: 0 }.getOrDefault(0)
        }

        override fun setSamplingRateLevel(level: Int) {
            runCatching { writeHexValue(toHexString(level)) }
        }

        override fun resetSamplingRateLevel() {
            runCatching { writeHexValue(toHexString(0)) }
        }


        private fun readSamplingRateValue(): Int? {
            val raw: String? = when (mode) {
                1 -> runCatching { BufferedReader(FileReader(gameSwitchFile)).readLine() }.getOrNull()
                2 -> ShellUtils.execCommand(touchHidlTestReadCmd, true, true).successMsg
                else -> null
            } ?: return null
            val firstSegment = raw?.replaceSpace?.substringBefore(",")?.trim() ?: return null
            return firstSegment.toIntOrNull()
        }

        private fun writeHexValue(hex: String) {
            when (mode) {
                1 -> FileOutputStream(gameSwitchFile).use { it.write(hex.toByteArray()) }
                2 -> ShellUtils.execCommand(touchHidlTestWriteCmd + hex, true)
            }
        }
    }
}
