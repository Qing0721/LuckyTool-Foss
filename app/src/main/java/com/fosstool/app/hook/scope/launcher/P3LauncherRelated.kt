package com.fosstool.app.hook.scope.launcher

import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.hasMethod
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import java.lang.reflect.Field

object EnableDockerBackground : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 26) return
        val clazz = "com.android.common.util.ScreenUtils".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("EnableDockerBackground: ScreenUtils not found")
            return
        }
        clazz.method { name = "isSupportDockerExpandScreen" }.ignored().hook { replaceToTrue() }
    }
}

object ForceEnableDockerBackgroundBlur : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 37) return
        val clazz = "com.android.launcher3.OplusHotseat".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("ForceEnableDockerBackgroundBlur: OplusHotseat not found")
            return
        }
        clazz.method { name = "setDockerBackground" }.ignored().hook {
            after {
                val host = instanceOrNull ?: return@after
                val group = runCatching {
                    clazz.field { name = "mShortcutsAndWidgets"; superClass() }
                        .ignored().get(host).cast<ViewGroup>()
                }.getOrNull() ?: return@after
                if (group.childCount == 0) {
                    group.setBackgroundResource(0)
                    return@after
                }
                val drawable = runCatching {
                    clazz.method { name = "createBlurDrawable" }.ignored()
                        .get(host).invoke<Drawable>()
                }.getOrNull() ?: return@after
                group.background = drawable
            }
        }
    }
}

object EnableAutoCloseFolder : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 34) return
        val afvClass = "com.android.launcher3.AbstractFloatingView".toClassOrNull(appClassLoader)
        if (afvClass == null) {
            YLog.error("EnableAutoCloseFolder: AbstractFloatingView not found")
            return
        }
        val typeFolder = afvClass.findFieldCompat("TYPE_FOLDER")?.get(null) as? Int ?: return
        afvClass.method { name = "closeOpenViews"; paramCount = 4 }.ignored().hook {
            before {
                val launcher = args.getOrNull(0) ?: return@before
                val animate = args.getOrNull(1) as? Boolean ?: false
                val type = args.getOrNull(2) as? Int ?: 0
                if (type and typeFolder == 0) return@before
                val dragLayer = runCatching {
                    launcher.javaClass.methods.firstOrNull {
                        it.name == "getDragLayer" && it.parameterCount == 0
                    }?.invoke(launcher) as? ViewGroup
                }.getOrNull() ?: return@before
                val childCount = dragLayer.childCount
                var i = 0
                while (i < childCount) {
                    val child = dragLayer.getChildAt(i) ?: break
                    i++
                    if (!afvClass.isAssignableFrom(child.javaClass)) continue
                    val isFolder = runCatching {
                        child.javaClass.methods.firstOrNull {
                            it.name == "isOfType" && it.parameterCount == 1
                        }?.invoke(child, typeFolder) as? Boolean
                    }.getOrNull() == true
                    if (!isFolder) continue
                    runCatching {
                        child.javaClass.methods.firstOrNull {
                            it.name == "close" && it.parameterCount == 1
                        }?.invoke(child, animate)
                    }
                }
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
            val clazz = cls.toClassOrNull(appClassLoader)
            if (clazz == null) {
                YLog.error("RemoveWidgetsAddRequestWhitelist: $cls not found")
                return@forEach
            }
            clazz.method { name = "isAllowedAddWidget" }.ignored().hook { replaceToTrue() }
        }
    }
}

object RemoveLauncherCardName : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 26) return
        if (getOSVersionCode >= 30) hookCardNameHelper() else hookLegacyCardViews()
    }

    private fun hookCardNameHelper() {
        val clazz = "com.android.launcher3.card.utils.CardNameHelper".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("RemoveLauncherCardName: CardNameHelper not found")
            return
        }
        clazz.method { name = "initCardName" }.ignored().hook {
            after {
                (args.getOrNull(0) as? View)?.let { view ->
                    view.visibility = View.GONE
                    runCatching {
                        view.current(true).method {
                            name = "setTextVisibility"; param(BooleanType)
                        }.call(false)
                    }
                }
                val host = instanceOrNull ?: return@after
                val card = runCatching {
                    clazz.field { name = "card"; superClass() }.ignored().get(host).cast<View>()
                }.getOrNull() ?: return@after
                runCatching {
                    card.current(true).method { name = "setTextVisible" }.call(false)
                }
            }
        }
    }

    private fun hookLegacyCardViews() {
        listOf(
            "com.android.launcher3.card.TitleCardView",
            "com.android.launcher3.card.uscard.USCardContainerView",
        ).forEach { cls ->
            val clazz = cls.toClassOrNull(appClassLoader)
            if (clazz == null) {
                YLog.error("RemoveLauncherCardName: $cls not found")
                return@forEach
            }
            clazz.method { name = "initCardName" }.ignored().hook {
                after {
                    val host = instanceOrNull ?: return@after
                    val view = runCatching {
                        clazz.field { name = "cardName"; superClass() }
                            .ignored().get(host).cast<View>()
                    }.getOrNull() ?: return@after
                    view.visibility = View.GONE
                    runCatching {
                        view.current(true).method {
                            name = "setTextVisibility"; param(BooleanType)
                        }.call(false)
                    }
                }
            }
        }
    }
}

object DisableLongPressAppIconSecondaryMenu : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 37) return
        val clazz = "com.android.launcher3.popup.PopupDataProvider".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("DisableLongPressAppIconSecondaryMenu: PopupDataProvider not found")
            return
        }
        clazz.method { name = "getNotificationKeysForItem"; returnType = ListClass }.ignored().hook {
            before {
                val item = args.getOrNull(0) ?: return@before

                runCatching {
                    item.javaClass.field { name = "mAddShortcutCount"; superClass() }
                        .ignored().get().set(0)
                }
                runCatching {
                    item.javaClass.field { name = "mAddShortcutCount"; superClass() }
                        .ignored().get(item).set(0)
                }
            }
        }
    }
}

object EnableLauncherIndicatorEntry : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 37) return
        var hit = false
        listOf(
            "com.android.launcher3.search.IndicatorEntry\$Companion",
            "com.android.launcher3.search.IndicatorEntry",
        ).forEach { cls ->
            cls.toClassOrNull(appClassLoader)?.let { clazz ->
                hit = true
                clazz.method { name = "isSupportIndicatorEntryMenu" }.ignored().hook { replaceToTrue() }
            }
        }
        if (!hit) YLog.error("EnableLauncherIndicatorEntry: IndicatorEntry not found")
    }
}

object RemoveDockerMaxNumberLimit : YukiBaseHooker() {
    override fun onHook() {
        val featureOption = "com.android.common.config.FeatureOption".toClassOrNull(appClassLoader)
        if (featureOption == null) {
            YLog.error("RemoveDockerMaxNumberLimit: FeatureOption not found")
        } else {
            featureOption.method { name = "isDockerMax5"; superClass() }
                .ignored().hook { replaceToFalse() }
        }

        if (getOSVersionCode < 38) return
        val expandConfig = "com.android.launcher3.hotseat.expand.ExpandConfig".toClassOrNull(appClassLoader)
        if (expandConfig == null) {
            YLog.error("RemoveDockerMaxNumberLimit: ExpandConfig not found")
            return
        }
        val appStateClass = "com.android.launcher3.LauncherAppState".toClassOrNull(appClassLoader)
        expandConfig.method {
            name = "getHotseatNormalItemsMaxCountBy"
            param(IntType, IntType)
            returnType = IntType
        }.ignored().hook {
            after {
                val old = result as? Int ?: return@after
                if (appStateClass == null) return@after
                val columns = runCatching {
                    val state = appStateClass.method { name = "getInstanceNoCreate" }
                        .ignored().get().call() ?: return@runCatching null
                    val idp = state.current(true)
                        .method { name = "getInvariantDeviceProfile" }.call() ?: return@runCatching null
                    idp.current(true).method { name = "getNumColumns" }.call() as? Int
                }.getOrNull() ?: return@after
                if (columns > old) result = columns
            }
        }
    }
}

object RemoveFolderNameInputLimit : YukiBaseHooker() {
    override fun onHook() {
        val clazz = "com.android.launcher3.folder.OplusFolder".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("RemoveFolderNameInputLimit: OplusFolder not found")
            return
        }
        clazz.constructor().ignored().hookAll {
            after {
                val host = instanceOrNull ?: return@after
                runCatching {
                    clazz.field { type = TextWatcher::class.java }
                        .ignored().get(host).set(EmptyTextWatcher)
                }
            }
        }
    }

    private object EmptyTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: Editable?) = Unit
    }
}

object ForceEnableRecentTaskMemoryDisplay : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 30) return
        val clazz = "com.oplus.quickstep.memory.MemoryInfoManager".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("ForceEnableRecentTaskMemoryDisplay: MemoryInfoManager not found")
            return
        }
        clazz.method { name = "isAllowMemoryInfoDisplay" }.ignored().hook { replaceToTrue() }
        clazz.method { name = "needMemoryDetail" }.ignored().hook { replaceToTrue() }
    }
}

object DisableAutoSwitchLastTask : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 34) return
        val clazz = "com.android.common.util.AppFeatureUtils".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("DisableAutoSwitchLastTask: AppFeatureUtils not found")
            return
        }
        val hasBoolOverload = clazz.hasMethod {
            name = "isSupportAutoFocusToNextPageInOverviewState"
            param(BooleanType)
            superClass()
        }
        clazz.method {
            name = "isSupportAutoFocusToNextPageInOverviewState"
            if (hasBoolOverload) param(BooleanType)
            superClass()
        }.ignored().hook { replaceToFalse() }
    }
}

object CustomDesktopDefaultHomePage : YukiBaseHooker() {
    override fun onHook() {
        val raw = prefs(ModulePrefs).getString("custom_desktop_default_home_page", "0") ?: "0"
        if (raw.isBlank()) return
        val page = raw.toIntOrNull() ?: return
        val clazz = "com.android.launcher3.Workspace".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("CustomDesktopDefaultHomePage: Workspace not found")
            return
        }
        clazz.method { name = "initWorkspace" }.ignored().hook {
            before {
                runCatching {
                    clazz.field { name = "DEFAULT_PAGE"; superClass() }.ignored().get().set(page)
                }
            }
            after {
                val host = instanceOrNull ?: return@after
                runCatching {
                    clazz.field { name = "mCurrentPage"; superClass() }.ignored().get(host).set(page)
                }
            }
        }
        clazz.method { name = "moveToDefaultScreen" }.ignored().hook {
            before {
                runCatching {
                    clazz.field { name = "DEFAULT_PAGE"; superClass() }.ignored().get().set(page)
                }
            }
        }
    }
}

private fun Class<*>.findFieldCompat(name: String): Field? {
    var c: Class<*>? = this
    while (c != null && c != Any::class.java) {
        c.declaredFields.firstOrNull { it.name == name }?.let { return it.apply { isAccessible = true } }
        c = c.superclass
    }
    return null
}
