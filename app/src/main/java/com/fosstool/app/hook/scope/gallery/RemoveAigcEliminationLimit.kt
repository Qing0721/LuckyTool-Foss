package com.fosstool.app.hook.scope.gallery

import com.fosstool.app.utils.A15
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType

object RemoveAigcEliminationLimit : YukiBaseHooker() {
    override fun onHook() {
        if (SDK >= A15) return
        if (!prefs(ModulePrefs).getBoolean("remove_aigc_elimination_limit", false)) return

        DexkitUtils.create(appInfo.sourceDir) { bridge ->
            runCatching {
                bridge.findClass {
                    matcher {
                        usingStrings("Info", "isContentSensitive")
                    }
                }.forEach { data ->
                    if (data.name.contains("EliminateDetect", ignoreCase = true) ||
                        data.name.endsWith("EliminateDetectInfo")
                    ) {
                        hookDetectInfo(data.name)
                    } else if (data.name.contains("EliminateSave", ignoreCase = true) ||
                        data.name.endsWith("EliminateSaveEntry")
                    ) {
                        hookSaveEntry(data.name)
                    } else if (data.name.contains("Eliminate", ignoreCase = true)) {
                        when {
                            data.name.contains("Detect", ignoreCase = true) ->
                                hookDetectInfo(data.name)
                            data.name.contains("Save", ignoreCase = true) ->
                                hookSaveEntry(data.name)
                        }
                    }
                }
            }
            listOf("EliminateDetectInfo", "EliminateSaveEntry").forEach { key ->
                runCatching {
                    bridge.findClass {
                        matcher { usingStrings(key) }
                    }.forEach { data ->
                        when {
                            data.name.contains("Detect", ignoreCase = true) ->
                                hookDetectInfo(data.name)
                            data.name.contains("Save", ignoreCase = true) ->
                                hookSaveEntry(data.name)
                        }
                    }
                }
            }
        }
    }

    private fun hookDetectInfo(className: String) {
        runCatching {
            className.toClass().method {
                returnType = BooleanType
            }.hookAll {
                before {
                    for (i in 0 until 8) {
                        when (val v = runCatching { args(i).any() }.getOrNull()) {
                            is Boolean -> args(i).set(false)
                            is Enum<*> -> args(i).setNull()
                        }
                    }
                }
            }
        }
    }

    private fun hookSaveEntry(className: String) {
        runCatching {
            className.toClass().method {
                returnType = BooleanType
            }.hookAll {
                before {
                    var lastBool = -1
                    for (i in 0 until 8) {
                        when (val v = runCatching { args(i).any() }.getOrNull()) {
                            is Boolean -> {
                                args(i).set(false)
                                lastBool = i
                            }
                            is Enum<*> -> args(i).setNull()
                        }
                    }
                    if (lastBool >= 0) args(lastBool).set(true)
                }
            }
        }
    }
}
