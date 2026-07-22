package com.fosstool.app.ui.service

import android.content.Intent
import com.fosstool.app.IDarkModeController
import com.fosstool.app.hook.utils.IColorDisplayUtils
import com.topjohnwu.superuser.ipc.RootService

class DarkModeControllerService : RootService() {
    companion object {

        private val iColorDisplayManager by lazy {
            IColorDisplayUtils(null).getInstance()
        }
    }

    override fun onBind(intent: Intent) = object : IDarkModeController.Stub() {

        override fun checkDarkMode(): Boolean {
            return iColorDisplayManager != null
        }

        override fun getDarkMode(): Boolean {
            return try {
                IColorDisplayUtils(null).isReduceBrightColorsActivated(iColorDisplayManager) == true
            } catch (_: Throwable) {
                false
            }
        }

        override fun setDarkMode(status: Boolean) {
            try {
                IColorDisplayUtils(null)
                    .setReduceBrightColorsActivated(iColorDisplayManager, status)
            } catch (_: Throwable) {

            }
        }
    }
}
