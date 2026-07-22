package com.fosstool.app.ui.service

import android.content.Intent
import android.os.Bundle
import com.fosstool.app.IRunInBackgroundController
import com.fosstool.app.utils.LogUtils
import com.topjohnwu.superuser.ipc.RootService

class RunInBackgroundControllerService : RootService() {
    private val tag = "RunInBackgroundController"

    override fun onBind(intent: Intent) = object : IRunInBackgroundController.Stub() {
        override fun startBackgroundStreamMode(): Int {
            return runCatching {
                val optionsClass = Class.forName("com.oplus.miragewindow.OplusMirageOptions")
                val makeBgMethod = optionsClass.getDeclaredMethod("makeBackgroundStreamModeOptions")
                val optionsInstance = makeBgMethod.invoke(null)
                val toBundleMethod = optionsClass.getDeclaredMethod("toBundle")
                val bundle = toBundleMethod.invoke(optionsInstance) as Bundle

                val managerClass = Class.forName("com.oplus.miragewindow.OplusMirageWindowManager")
                val getInstanceMethod = managerClass.getDeclaredMethod("getInstance")
                val managerInstance = getInstanceMethod.invoke(null)
                val startMethod = managerClass.getDeclaredMethod(
                    "startMirageWindowMode",
                    Intent::class.java,
                    Bundle::class.java
                )
                startMethod.invoke(managerInstance, null, bundle) as Int
            }.getOrElse {
                LogUtils.e(tag, "startBackgroundStreamMode", "$it")
                -1
            }
        }
    }
}
