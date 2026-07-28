package com.fosstool.app.hook.scope.packageinstaller

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object AutoClickInstallButton : YukiBaseHooker() {

    private const val INSTALLER = "com.android.packageinstaller.oplus.OPlusPackageInstallerActivity"
    private const val PROGRESS = "com.android.packageinstaller.oplus.InstallAppProgress"

    override fun onHook() {
        val installer = INSTALLER.toClassOrNull(appClassLoader)
        if (installer == null) {
            YLog.error("AutoClickInstallButton -> $INSTALLER not found", tag = "LuckyTool")
        } else {
            installer.method { name = "startInstallConfirm" }.ignored().hook {
                after { (instance as? Activity)?.clickById("ok_button") }
            }
        }

        val progress = PROGRESS.toClassOrNull(appClassLoader)
        if (progress == null) {
            YLog.error("AutoClickInstallButton -> $PROGRESS not found", tag = "LuckyTool")
        } else {
            progress.method { name = "onPackageInstalled"; paramCount = 1 }.ignored().hook {
                after {
                    if ((args.getOrNull(0) as? Int) == 0) {
                        (instance as? Activity)?.clickById("done_button")
                    }
                }
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
