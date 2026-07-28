package com.fosstool.app.hook.scope.oplusgames

import android.app.Activity
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object EnableDeveloperPage : YukiBaseHooker() {
    override fun onHook() {
        "business.compact.activity.GameDevelopOptionsActivity".toClassOrNull(appClassLoader)
            ?.method { name = "onCreate"; paramCount = 1 }
            ?.ignored()
            ?.hook {
                before {
                    val activity = instance as? Activity ?: return@before
                    activity.intent.apply {

                        putExtra("gameDevelopOptions", "GameDevelopOptionsActivity")
                        putExtra("openAutomation", -1)
                    }
                    if (args.isNotEmpty()) args[0] = null
                }
            }
    }
}
