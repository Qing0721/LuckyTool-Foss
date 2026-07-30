package com.fosstool.app.hook.scope.phonemanager

import android.os.CountDownTimer
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveCountdownAddVirusAppWhitelist : YukiBaseHooker() {
    private const val TAG = "DialogCrossActivity"

    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_countdown_add_virus_app_whitelist", false)) return

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            val classes = dexKitBridge.findClass {
                matcher { className = "com.oplus.phonemanager.common.DialogCrossActivity" }
            }.checkDataList(TAG)
            val clazz = classes.firstOrNullSafe()?.name?.toClassOrNull(appClassLoader) ?: return@create

            if (clazz.declaredFields.none { it.type == CountDownTimer::class.java }) return@create

            dexKitBridge.findMethod {
                searchInClass(classes)
                matcher {
                    paramCount(2..3)
                    addUsingField { type(CountDownTimer::class.java.name) }
                }
            }.apply {
                checkDataList("CountDownTimer", onlyOne = false)
                forEach { data ->
                    clazz.method {
                        name = data.methodName
                        paramCount(2..3)
                    }.ignored().hook { intercept() }
                }
            }
        }
    }
}
