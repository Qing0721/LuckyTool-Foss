package com.fosstool.app.hook.scope.android

import android.util.SparseArray
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import de.robv.android.xposed.XposedHelpers

object RemoveGmsUsageRestrictions : YukiBaseHooker() {

    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_gms_usage_restrictions", false)) return

        hookStartupStrategyAndDbConfig()

        if (getOSVersionCode > 30) hookBgSceneManager() else hookHansManager()
    }

    private fun hookStartupStrategyAndDbConfig() {
        val strategy = "com.android.server.am.OplusAppStartupManager\$OplusStartupStrategy"
            .toClassOrNull(appClassLoader)
        if (strategy == null) {
            YLog.error("RemoveGmsUsageRestrictions: OplusStartupStrategy not found")
        } else {
            strategy.method { name = "isGoogleRestricInfoOn" }.ignored().hook { replaceToFalse() }
        }

        val dbConfig = "com.android.server.hans.OplusHansDBConfig".toClassOrNull(appClassLoader)
        if (dbConfig == null) {
            YLog.error("RemoveGmsUsageRestrictions: OplusHansDBConfig not found")
            return
        }

        dbConfig.method { name = "updateManagedMap" }.ignored().hookAll {
            after { clearGmsList(instance) }
        }
        dbConfig.method { name = "updateTargetList" }.ignored().hook {
            after { clearGmsList(instance) }
        }
    }

    private fun hookBgSceneManager() {
        val cls = "com.android.server.hans.scene.OplusBgSceneManager".toClassOrNull(appClassLoader)
        if (cls == null) {
            YLog.error("RemoveGmsUsageRestrictions: OplusBgSceneManager not found")
            return
        }
        cls.method { name = "setGmsRestricted" }.ignored().hook {
            before { args(0).setFalse() }
        }
        cls.method { name = "isGmsRestricted" }.ignored().hook { replaceToFalse() }
        cls.method { name = "registerGmsRestrictObserver" }.ignored().hook { intercept() }
    }

    private fun hookHansManager() {
        val config = "com.android.server.am.OplusHansManager\$HansConfig"
            .toClassOrNull(appClassLoader)
        if (config == null) {
            YLog.error("RemoveGmsUsageRestrictions: OplusHansManager\$HansConfig not found")
        } else {
            config.method { name = "setGmsRestricted" }.ignored().hook {
                before { args(0).setFalse() }
            }
            config.method { name = "isGmsRestricted" }.ignored().hook { replaceToFalse() }
        }

        val trigger = "com.android.server.am.OplusHansManager\$HansTrigger"
            .toClassOrNull(appClassLoader)
        if (trigger == null) {
            YLog.error("RemoveGmsUsageRestrictions: OplusHansManager\$HansTrigger not found")
            return
        }
        trigger.method { name = "registerGmsRestrictObserver" }.ignored().hook { intercept() }
    }

    private fun clearGmsList(host: Any?) {
        if (host == null) return
        runCatching {
            (XposedHelpers.getObjectField(host, "mGMSList") as? SparseArray<*>)?.clear()
        }
    }
}
