package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.otherapp.AdmUnlockMoreThreads
import com.fosstool.app.hook.scope.otherapp.HookADM
import com.fosstool.app.hook.scope.otherapp.HookAlphaBackupPro
import com.fosstool.app.hook.scope.otherapp.HookKsWeb

object HookOtherApp : YukiBaseHooker() {
    override fun onHook() {
        if (packageName == "com.ruet_cse_1503050.ragib.appbackup.pro") loadHooker(HookAlphaBackupPro)

        if (packageName == "ru.kslabs.ksweb") loadHooker(HookKsWeb)

        if (packageName == "com.dv.adm") {
            loadHooker(HookADM)
            loadHooker(AdmUnlockMoreThreads)
        }
    }
}
