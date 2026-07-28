package com.fosstool.app.hook.scope.gallery

import com.fosstool.app.utils.A15
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.useFirst
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveAigcEliminationLimit : YukiBaseHooker() {
    override fun onHook() {
        if (SDK >= A15) return
        if (!prefs(ModulePrefs).getBoolean("remove_aigc_elimination_limit", false)) return

        DexkitUtils.create(appInfo.sourceDir) { bridge ->

            bridge.findClass {
                matcher {
                    fields {
                        addForType(BooleanType.name)
                    }
                    methods {
                        add { name = "equals" }
                        add { name = "hashCode" }
                        add { name = "toString" }
                    }
                    usingStrings("Info", "isContentSensitive")
                }
            }.useFirst("EliminateDetectInfo") { data ->
                hookDetectInfo(data.name)
            }

            bridge.findClass {
                matcher {
                    fields {
                        addForType(IntType.name)
                        addForType(StringClass.name)
                        addForType(BooleanType.name)
                    }
                    methods {
                        add { name = "equals" }
                        add { name = "hashCode" }
                        add { name = "toString" }
                    }
                    usingStrings("EliminateSaveEntry", "isContentSensitive")
                }
            }.useFirst("EliminateSaveEntry") { data ->
                hookSaveEntry(data.name)
            }
        }
    }

    private fun hookDetectInfo(className: String) {
        runCatching {
            className.toClassOrNull(appClassLoader)
                ?.constructor { param { types -> types.any { it == BooleanType } } }
                ?.ignored()
                ?.hook {
                    before {
                        args.forEachIndexed { index, value ->
                            when {
                                value is Boolean -> args(index).set(false)
                                value != null && value.javaClass.isEnum -> args(index).setNull()
                            }
                        }
                    }
                }
        }
    }

    private fun hookSaveEntry(className: String) {
        runCatching {
            className.toClassOrNull(appClassLoader)
                ?.constructor { param { types -> types.any { it == BooleanType } } }
                ?.ignored()
                ?.hook {
                    before {
                        args.forEachIndexed { index, value ->
                            when {
                                value is Boolean -> args(index).set(false)
                                value != null && value.javaClass.isEnum -> args(index).setNull()
                            }
                        }
                        if (args.lastOrNull() is Boolean) args(args.lastIndex).set(true)
                    }
                }
        }
    }
}
