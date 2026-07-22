package com.fosstool.app.hook.scope.launcher

import android.view.ViewGroup
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode

object EnableDockerBackground : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 26) return
        runCatching {
            "com.android.common.util.ScreenUtils".toClass().apply {
                method {
                    name = "isSupportDockerExpandScreen"
                    returnType = BooleanType
                }.hook { replaceToTrue() }
            }
        }
    }
}

object ForceEnableDockerBackgroundBlur : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 37) return
        runCatching {
            "com.android.launcher3.OplusHotseat".toClass().apply {
                method {
                    name = "isSupportDockerBackground"
                    returnType = BooleanType
                }.hook { replaceToTrue() }
                method {
                    name = "isDockerBackgroundBlurEnabled"
                    returnType = BooleanType
                }.hook { replaceToTrue() }
            }
        }
        runCatching {
            "com.coloros.edgepanel.utils.EdgePanelUtils".toClass().apply {
                method {
                    name = "isSupportBlur"
                    returnType = BooleanType
                }.hook { replaceToTrue() }
            }
        }
    }
}

object EnableAutoCloseFolder : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 34) return
        runCatching {
            val afvClass = "com.android.launcher3.AbstractFloatingView".toClass()
            val typeFolder = afvClass.field {
                name = "TYPE_FOLDER"
                type = IntType
            }.get().int()
            afvClass.method { name = "closeOpenViews" }.hookAll {
                before {
                    val launcher = runCatching { args(0).any() }.getOrNull() ?: return@before
                    val animate = runCatching { args(1).boolean() }.getOrNull() ?: false
                    val type = runCatching { args(2).int() }.getOrNull() ?: 0
                    if (type and typeFolder == 0) return@before
                    val dragLayer = runCatching {
                        launcher.current().method {
                            name = "getDragLayer"
                            emptyParam()
                        }.invoke<ViewGroup>()
                    }.getOrNull() ?: return@before
                    val childCount = dragLayer.childCount
                    var i = 0
                    while (i < childCount) {
                        val child = dragLayer.getChildAt(i) ?: break
                        i++
                        if (!afvClass.isAssignableFrom(child.javaClass)) continue
                        val isFolder = runCatching {
                            child.current().method {
                                name = "isOfType"
                                param(IntType)
                            }.invoke<Boolean>(typeFolder)
                        }.getOrNull() == true
                        if (!isFolder) continue
                        runCatching {
                            child.current().method {
                                name = "close"
                                param(BooleanType)
                            }.call(animate)
                        }
                    }
                }
            }
        }
        runCatching {
            "com.android.launcher3.folder.Folder".toClass().apply {
                method {
                    name = "shouldAutoClose"
                    returnType = BooleanType
                }.hook { replaceToTrue() }
            }
        }
    }
}

object RemoveWidgetsAddRequestWhitelist : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 30) return
        listOf(
            "com.android.launcher3.widget.WidgetControlHelper",
            "com.android.launcher3.dragndrop.AddItemActivity",
        ).forEach { cls ->
            runCatching {
                cls.toClass().apply {
                    method {
                        name = "isAllowedAddWidget"
                        returnType = BooleanType
                    }.hook { replaceToTrue() }
                }
            }
        }
    }
}

object RemoveLauncherCardName : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 30) return
        runCatching {
            "com.android.launcher3.card.TitleCardView".toClass().apply {
                method { name = "setTitle" }.hookAll {
                    before { args().first().set("") }
                }
                method { name = "setText" }.hookAll {
                    before {
                        runCatching { args().first().set("") }
                    }
                }
            }
        }
    }
}

object DisableLongPressAppIconSecondaryMenu : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 37) return
        runCatching {
            "com.android.launcher3.popup.PopupDataProvider".toClass().apply {
                method { name = "getNotificationKeysForItem" }.hookAll {
                    after { result = emptyList<Any>() }
                }
            }
        }
    }
}

object EnableLauncherIndicatorEntry : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 37) return
        runCatching {
            "com.android.launcher3.search.IndicatorEntry\$Companion".toClass().apply {
                method {
                    name = "isSupportIndicatorEntryMenu"
                    returnType = BooleanType
                }.hook { replaceToTrue() }
            }
        }
        runCatching {
            "com.android.launcher3.search.IndicatorEntry".toClass().apply {
                method {
                    name = "isSupportIndicatorEntryMenu"
                    returnType = BooleanType
                }.hook { replaceToTrue() }
            }
        }
    }
}

object RemoveDockerMaxNumberLimit : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 38) return
        runCatching {
            "com.android.launcher3.hotseat.expand.ExpandConfig".toClass().apply {
                method {
                    name = "getHotseatNormalItemsMaxCountBy"
                    returnType = IntType
                }.hook { replaceTo(999) }
            }
        }
    }
}

object RemoveFolderNameInputLimit : YukiBaseHooker() {
    override fun onHook() {
        runCatching {
            "com.android.launcher3.folder.OplusFolder".toClass().apply {
                method { name = "getMaxNameLength"; returnType = IntType }.hook {
                    replaceTo(Int.MAX_VALUE)
                }
                method { name = "getFolderNameMaxLength"; returnType = IntType }.hook {
                    replaceTo(Int.MAX_VALUE)
                }
                method {
                    name = "isNameLengthValid"
                    returnType = BooleanType
                }.hook { replaceToTrue() }
            }
        }
        runCatching {
            "com.android.launcher3.folder.FolderNameEditText".toClass().apply {
                method { name = "setFilters" }.hookAll {
                    before { args().first().set(emptyArray<android.text.InputFilter>()) }
                }
            }
        }
    }
}

object ForceEnableRecentTaskMemoryDisplay : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 30) return
        runCatching {
            "com.oplus.quickstep.memory.MemoryInfoManager".toClass().apply {
                method {
                    name = "isAllowMemoryInfoDisplay"
                    returnType = BooleanType
                }.hook { replaceToTrue() }
            }
        }
    }
}

object DisableAutoSwitchLastTask : YukiBaseHooker() {
    override fun onHook() {
        runCatching {
            "com.android.common.util.AppFeatureUtils".toClass().apply {
                method {
                    name = "isSupportAutoFocusToNextPageInOverviewState"
                    returnType = BooleanType
                }.hook { replaceToFalse() }
            }
        }
    }
}

object CustomDesktopDefaultHomePage : YukiBaseHooker() {
    override fun onHook() {
        val page = prefs(ModulePrefs).getString("custom_desktop_default_home_page", "0") ?: "0"
        val pageIndex = page.toIntOrNull() ?: 0
        if (pageIndex <= 0) return
        val target = pageIndex - 1
        runCatching {
            "com.android.launcher3.Workspace".toClass().apply {
                method { name = "moveToDefaultScreen" }.hookAll {
                    before {
                        runCatching {
                            instance.javaClass.getMethod("snapToPage", Int::class.javaPrimitiveType)
                                .invoke(instance, target)
                            result = null
                        }
                    }
                }
                method { name = "initWorkspace" }.hookAll {
                    after {
                        runCatching {
                            instance.javaClass.getMethod("snapToPage", Int::class.javaPrimitiveType)
                                .invoke(instance, target)
                        }
                    }
                }
            }
        }
    }
}
