package com.fosstool.app.hook.scope.systemui

import android.annotation.SuppressLint
import android.content.res.Resources
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.hasMethod
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.hook.utils.sysui.DependencyUtils
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.safeOfNull
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

object MediaPlayerPanel : YukiBaseHooker() {
    override fun onHook() {
        val container = VariousClass(
            "com.oplusos.systemui.qs.OplusQSTileMediaContainer",
            "com.oplus.systemui.qs.OplusQSTileMediaContainer",
        ).toClassOrNull(appClassLoader)
        val isPermanent = container?.hasMethod { name = "setMediaMode" }?.not() ?: false

        if (isPermanent) loadHooker(MediaPlayerDisplayMode)
        else {
            loadHooker(MediaPlayerDisplayModeC13)
            loadHooker(MediaPlayerDisplayModeShown)
        }
        if (prefs(ModulePrefs).getBoolean("force_enable_media_toggle_button", false)) {
            if (SDK == A13) loadHooker(ForceEnableMediaToggleButton)
        }
    }

    object MediaPlayerDisplayMode : YukiBaseHooker() {
        @SuppressLint("DiscouragedApi")
        override fun onHook() {
            var mode = prefs(ModulePrefs).getString("set_media_player_display_mode", "0")
            dataChannel.wait<String>("set_media_player_display_mode") {
                mode = it
                ControlCenterTiles.callback?.invoke("set_media_player_display_mode", it)
            }

            val clazz = VariousClass(
                "com.oplusos.systemui.qs.OplusQSTileMediaContainer",
                "com.oplus.systemui.qs.OplusQSTileMediaContainer",
            ).toClassOrNull(appClassLoader) ?: return

            clazz.method { name = "setListening" }.ignored().hook {
                after {
                    runCatching {
                        clazz.getDeclaredMethod("updateResources").apply { isAccessible = true }
                            .invoke(instance)
                    }
                }
            }
            clazz.method { name = "updateQsMediaPanelView" }.ignored().hook {
                before {
                    val status = when (mode) {
                        "1" -> 0
                        "2" -> 8
                        "3" -> if (getMediaData() == null) 8 else 0
                        else -> return@before
                    }
                    val res = args.getOrNull(0) as? Resources ?: return@before
                    val bool = args.lastOrNull() as? Boolean ?: return@before
                    val linear = clazz.findField("mQsMediaPanelContainer")
                        ?.get(instance) as? LinearLayout ?: return@before
                    val mTmpConstraintSet = clazz.findField("mTmpConstraintSet")
                        ?.get(instance) ?: return@before
                    val smallHeight = res.getIdentifier(
                        "oplus_qs_media_panel_height_smallspace",
                        "dimen", packageName,
                    ).takeIf { it != 0 } ?: return@before
                    val height = res.getIdentifier(
                        "oplus_qs_media_panel_height",
                        "dimen", packageName,
                    ).takeIf { it != 0 } ?: return@before
                    val heightSize = safeOfNull {
                        res.getDimensionPixelSize(if (bool) smallHeight else height)
                    } ?: return@before
                    mTmpConstraintSet.setVisibilitySet(linear.id, status)
                    if (status == 0) mTmpConstraintSet.constrainHeightSet(linear.id, heightSize)
                    result = null
                }
            }
            clazz.method { name = "updateQsSecondTileContainer" }.ignored().hook {
                before {
                    val isShow = when (mode) {
                        "1" -> true
                        "2" -> false
                        "3" -> getMediaData() != null
                        else -> return@before
                    }
                    val res = args.getOrNull(0) as? Resources ?: return@before
                    val bool = args.lastOrNull() as? Boolean ?: return@before
                    val linear = clazz.findField("mSecondTileContainer")
                        ?.get(instance) as? LinearLayout ?: return@before
                    val mTmpConstraintSet = clazz.findField("mTmpConstraintSet")
                        ?.get(instance) ?: return@before
                    val smallSideMargin = res.getIdentifier(
                        "qs_footer_hl_tile_side_margin_smallspace",
                        "dimen", packageName,
                    ).takeIf { it != 0 } ?: return@before
                    val sideMargin = res.getIdentifier(
                        "qs_footer_hl_tile_side_margin",
                        "dimen", packageName,
                    ).takeIf { it != 0 } ?: return@before
                    val sideSize = safeOfNull {
                        res.getDimensionPixelSize(if (bool) smallSideMargin else sideMargin)
                    } ?: return@before
                    val guideLine = res.getIdentifier(
                        "guide_line", "id", packageName,
                    ).takeIf { it != 0 } ?: return@before
                    if (isShow) {
                        val firstTile = clazz.findField("mFirstTileContainer")
                            ?.get(instance) as? LinearLayout ?: return@before
                        val smallContainerMargin = res.getIdentifier(
                            "qs_footer_hl_tile_two_container_margin_top_smallspace",
                            "dimen", packageName,
                        ).takeIf { it != 0 } ?: return@before
                        val containerMargin = res.getIdentifier(
                            "qs_footer_hl_tile_two_container_margin_top",
                            "dimen", packageName,
                        ).takeIf { it != 0 } ?: return@before
                        val containerSize = safeOfNull {
                            res.getDimensionPixelSize(
                                if (bool) smallContainerMargin else containerMargin,
                            )
                        } ?: return@before
                        mTmpConstraintSet.connectSet(linear.id, 6, 0, 6, 0)
                        mTmpConstraintSet.connectSet(linear.id, 7, guideLine, 6, sideSize)
                        mTmpConstraintSet.connectSet(
                            linear.id, 3, firstTile.id, 4, containerSize,
                        )
                    } else {
                        mTmpConstraintSet.connectSet(linear.id, 6, guideLine, 7, sideSize)
                        mTmpConstraintSet.connectSet(linear.id, 7, 0, 7, 0)
                        mTmpConstraintSet.connectSet(linear.id, 3, 0, 3, 0)
                    }
                    result = null
                }
            }
        }
    }

    object MediaPlayerDisplayModeC13 : YukiBaseHooker() {
        override fun onHook() {
            var mode = prefs(ModulePrefs).getString("set_media_player_display_mode", "0")
            dataChannel.wait<String>("set_media_player_display_mode") { mode = it }

            val clazz = "com.oplus.systemui.qs.media.OplusQsMediaCarouselController"
                .toClassOrNull(appClassLoader) ?: return

            fun applyStatus(listener: Any?) {
                if (listener == null) return
                val status = when (mode) {
                    "1" -> true
                    "2" -> false
                    else -> return
                }
                runCatching {
                    listener.current().method { name = "onChanged" }.call(status)
                }.onFailure {
                    runCatching { XposedHelpers.callMethod(listener, "onChanged", status) }
                }
            }

            clazz.method { name = "setCurrentMediaData" }.ignored().hook {
                after {
                    val listener = clazz.findField("mediaModeChangeListener")?.get(instance)
                    applyStatus(listener)
                }
            }
            clazz.method { name = "setMediaModeChangeListener" }.ignored().hook {
                after {
                    applyStatus(args.getOrNull(0))
                }
            }
        }
    }

    object MediaPlayerDisplayModeShown : YukiBaseHooker() {
        override fun onHook() {
            var mode = prefs(ModulePrefs).getString("set_media_player_display_mode", "0")
            dataChannel.wait<String>("set_media_player_display_mode") { mode = it }

            listOf(
                "com.oplus.systemui.qs.OplusQSContainerImpl",
                "com.oplus.systemui.qs.OplusQSTileMediaContainerController",
                "com.oplusos.systemui.qs.OplusQSTileMediaContainerController",
            ).forEach { className ->
                val clazz = className.toClassOrNull(appClassLoader) ?: return@forEach
                clazz.method { name = "setQsMediaPanelShown" }.ignored().hook {
                    before {
                        if (args.isEmpty()) return@before
                        when (mode) {
                            "1" -> args[0] = true
                            "2" -> args[0] = false
                            "3" -> args[0] = getMediaData() != null
                            else -> return@before
                        }
                    }
                }
            }

            val container = VariousClass(
                "com.oplusos.systemui.qs.OplusQSTileMediaContainer",
                "com.oplus.systemui.qs.OplusQSTileMediaContainer",
            ).toClassOrNull(appClassLoader) ?: return
            container.method { name = "setMediaMode" }.ignored().hook {
                before {
                    if (args.isEmpty()) return@before
                    when (mode) {
                        "1" -> when (args[0]) {
                            is Boolean -> args[0] = true
                            is Int -> args[0] = 1
                        }
                        "2" -> when (args[0]) {
                            is Boolean -> args[0] = false
                            is Int -> args[0] = 0
                        }
                        else -> return@before
                    }
                }
            }
        }
    }

    fun getMediaData(): Any? {
        val clazz = VariousClass(
            "com.oplus.systemui.qs.media.OplusQsMediaCarouselController\$MediaPlayerData",
            "com.oplus.systemui.media.OplusMediaControllerImpl\$MediaPlayerData",
            "com.oplusos.systemui.media.OplusMediaControllerImpl\$MediaPlayerData",
        ).toClassOrNull(appClassLoader) ?: return null
        val mediaPlayerData = clazz.findField("INSTANCE")?.get(null) ?: return null
        val firstActive = runCatching {
            mediaPlayerData.current().method {
                name = if (SDK >= A14) "getFirstActiveMediaSortKey" else "firstActiveMedia"
            }.call()
        }.getOrNull() ?: return null
        if (SDK >= A14) {
            runCatching {
                mediaPlayerData.current().method {
                    name = "getMediaDataKey"
                    paramCount = 1
                }.call(firstActive)
            }.getOrNull() ?: return null
        }
        return runCatching {
            firstActive.current().method {
                name = "getData"
                emptyParam()
            }.call()
        }.getOrNull()
    }

    fun Any.connectSet(startId: Int, startSide: Int, endId: Int, endSide: Int, margin: Int) {
        runCatching {
            this.current().method {
                name = "connect"
                paramCount = 5
            }.call(startId, startSide, endId, endSide, margin)
        }
    }

    fun Any.constrainHeightSet(viewId: Int, height: Int) {
        runCatching {
            this.current().method {
                name = "constrainHeight"
                paramCount = 2
            }.call(viewId, height)
        }
    }

    fun Any.setVisibilitySet(viewId: Int, visibility: Int) {
        runCatching {
            this.current().method {
                name = "setVisibility"
                paramCount = 2
            }.call(viewId, visibility)
        }
    }

    object ForceEnableMediaToggleButton : YukiBaseHooker() {
        override fun onHook() {
            val panel = "com.oplus.systemui.qs.media.OplusQsMediaPanelView"
                .toClassOrNull(appClassLoader)
            panel?.method { name = "bindMediaData" }?.ignored()?.hook {
                after {
                    if (args.getOrNull(0) != null) return@after
                    (panel.findField("mMediaOutputBtn")?.get(instance) as? ImageButton)
                        ?.setMediaOutputBtn()
                }
            }
            val dialog = "com.oplus.systemui.qs.media.OplusQsMediaOutputDialog"
                .toClassOrNull(appClassLoader)
            dialog?.method { name = "bindMediaView" }?.ignored()?.hook {
                after {
                    if (args.getOrNull(0) != null) return@after
                    (dialog.findField("mMediaOutputBtn")?.get(instance) as? ImageButton)
                        ?.setMediaOutputBtn()
                }
            }
        }
    }

    private fun ImageButton.setMediaOutputBtn() {
        isVisible = true
        isEnabled = true
        setOnClickListener {
            val clazz = "com.android.systemui.media.dialog.MediaOutputDialogFactory"
                .toClassOrNull(appClassLoader) ?: return@setOnClickListener
            val factory = DependencyUtils(appClassLoader).get(clazz)
            runCatching {
                factory?.current()?.method { name = "create"; paramCount = 3 }
                    ?.call("", true, null)
            }
        }
    }

    private fun Class<*>.findField(name: String): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            runCatching { return cls.getDeclaredField(name).also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }
}
