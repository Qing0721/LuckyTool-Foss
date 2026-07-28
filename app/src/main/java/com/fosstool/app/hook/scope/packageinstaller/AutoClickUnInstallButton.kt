package com.fosstool.app.hook.scope.packageinstaller

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.android.IntentClass

object AutoClickUnInstallButton : YukiBaseHooker() {

    private const val UNINSTALLER = "com.android.packageinstaller.UninstallerActivity"
    private const val UNINSTALL_PROGRESS = "com.android.packageinstaller.oplus.OPlusUninstallAppProgress"

    override fun onHook() {
        val uninstaller = UNINSTALLER.toClassOrNull(appClassLoader)
        if (uninstaller == null) {
            YLog.error("AutoClickUnInstallButton -> $UNINSTALLER not found", tag = "LuckyTool")
        } else {
            uninstaller.method {
                name = "showUninstallConfirmation"
                param(IntentClass)
            }.ignored().hook {
                after { (instance as? Activity)?.clickById("ok_button") }
            }
        }

        val progress = UNINSTALL_PROGRESS.toClassOrNull(appClassLoader)
        if (progress == null) {
            YLog.error("AutoClickUnInstallButton -> $UNINSTALL_PROGRESS not found", tag = "LuckyTool")
        } else {
            progress.method { name = "initView" }.ignored().hook {
                after { (instance as? Activity)?.clickById("complete_button") }
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun Activity.clickById(idName: String) {
        runCatching {
            val id = resources.getIdentifier(idName, "id", packageName)
            if (id == 0) return
            findViewById<View>(id)?.performClick()
        }
    }
}
