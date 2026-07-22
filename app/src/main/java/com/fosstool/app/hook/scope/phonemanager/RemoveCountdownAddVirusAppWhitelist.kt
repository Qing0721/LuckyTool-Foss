package com.fosstool.app.hook.scope.phonemanager

import android.os.CountDownTimer
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveCountdownAddVirusAppWhitelist : YukiBaseHooker() {
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("remove_countdown_add_virus_app_whitelist", false) &&
            !prefs(ModulePrefs).getBoolean("remove_virus_whitelist_countdown", false)
        ) return

        DexkitUtils.create(appInfo.sourceDir) { bridge ->
            runCatching {
                bridge.findMethod {
                    matcher {
                        declaredClass { usingStrings("DialogCrossActivity") }
                        usingFields {
                            add { type = CountDownTimer::class.java.name }
                        }
                    }
                }.apply {
                    checkDataList("RemoveCountdownAddVirusAppWhitelist", onlyOne = false)
                    forEach { data ->
                        runCatching {
                            data.className.toClass().method {
                                name = data.methodName
                            }.hook { intercept() }
                        }
                    }
                }
            }
            runCatching {
                bridge.findClass {
                    matcher { usingStrings("DialogCrossActivity") }
                }.forEach { clsData ->
                    runCatching {
                        val clazz = clsData.name.toClass()
                        val hasTimerField = clazz.declaredFields.any {
                            CountDownTimer::class.java.isAssignableFrom(it.type)
                        }
                        if (!hasTimerField) return@runCatching
                        clazz.method {
                            paramCount(0..4)
                        }.hookAll {
                            intercept()
                        }
                    }
                }
            }
        }
    }
}
