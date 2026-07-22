package com.fosstool.app.hook.scope.packageinstaller

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object HookPackageInstallerFeature : YukiBaseHooker() {
    override fun onHook() {
        val isAOSP = prefs(ModulePrefs).getBoolean("replase_aosp_installer", false)
        val isAds = prefs(ModulePrefs).getBoolean("remove_install_ads", false)
        "com.android.packageinstaller.oplus.common.FeatureOption".toClass().apply {
            method { name = "init";paramCount = 1 }.hook {
                after {
                    if (isAds) field { name = "sIsBusinessCustomProduct" }.get().setFalse()
                }
            }
            method { name = "setIsClosedSuperFirewall";paramCount = 1 }.hook {
                after {
                    if (isAOSP) field { name = "sIsClosedSuperFirewall" }.get().setTrue()
                }
            }
        }

    }
}
