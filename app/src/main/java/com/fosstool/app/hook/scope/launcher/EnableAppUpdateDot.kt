package com.fosstool.app.hook.scope.launcher

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object EnableAppUpdateDot : YukiBaseHooker() {
    override fun onHook() {
        val featureOption = "com.android.common.config.FeatureOption".toClassOrNull(appClassLoader)
        if (featureOption == null) {
            YLog.error("EnableAppUpdateDot: FeatureOption not found")
        } else {
            featureOption.method { name = "initFeature" }.ignored().hook {
                after {
                    runCatching {
                        featureOption.field { name = "isSupportAppUpdateDotSwitch"; superClass() }
                            .ignored().get().set(true)
                    }
                }
            }
        }

        val settingsUtils = "com.android.launcher.settings.LauncherSettingsUtils"
            .toClassOrNull(appClassLoader)
        if (settingsUtils == null) {
            YLog.error("EnableAppUpdateDot: LauncherSettingsUtils not found")
            return
        }
        settingsUtils.method { name = "isSupportAppUpdateDot"; superClass() }
            .ignored().hook { replaceToTrue() }
    }
}
