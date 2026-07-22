package com.fosstool.app.hook.scope.systemui

import android.annotation.SuppressLint
import android.content.res.Resources
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.hasMethod
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.hook.utils.sysui.DependencyUtils
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.safeOfNull

object MediaPlayerPanel : YukiBaseHooker() {
    override fun onHook() {
        val isPermanent = VariousClass(
            "com.oplusos.systemui.qs.OplusQSTileMediaContainer",
            "com.oplus.systemui.qs.OplusQSTileMediaContainer"
        ).toClassOrNull()?.hasMethod { name = "setMediaMode" }?.not() ?: false

        if (isPermanent) loadHooker(MediaPlayerDisplayMode)
        else loadHooker(MediaPlayerDisplayModeC13)
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

            VariousClass(
                "com.oplusos.systemui.qs.OplusQSTileMediaContainer",
                "com.oplus.systemui.qs.OplusQSTileMediaContainer"
            ).toClass().apply {
                method { name = "setListening" }.hook {
                    after { method { name = "updateResources" }.get(instance).call() }
                }
                method { name = "updateQsMediaPanelView" }.hook {
                    before {
                        val status = when (mode) {
                            "1" -> 0
                            "2" -> 8
                            "3" -> if (getMediaData() == null) 8 else 0
                            else -> return@before
                        }
                        val res = args().first().cast<Resources>() ?: return@before
                        val bool = args().last().cast<Boolean>() ?: return@before
                        val linear = field { name = "mQsMediaPanelContainer" }.get(instance)
                            .cast<LinearLayout>() ?: return@before
                        val mTmpConstraintSet = field { name = "mTmpConstraintSet" }
                            .get(instance).any() ?: return@before
                        val smallHeight = res.getIdentifier(
                            "oplus_qs_media_panel_height_smallspace",
                            "dimen", MediaPlayerDisplayMode.packageName
                        ).takeIf { it != 0 } ?: return@before
                        val height = res.getIdentifier(
                            "oplus_qs_media_panel_height",
                            "dimen", MediaPlayerDisplayMode.packageName
                        ).takeIf { it != 0 } ?: return@before
                        val heightSize = safeOfNull {
                            res.getDimensionPixelSize(if (bool) smallHeight else height)
                        } ?: return@before
                        mTmpConstraintSet.setVisibilitySet(linear.id, status)
                        if (status == 0) mTmpConstraintSet.constrainHeightSet(
                            linear.id, heightSize
                        )
                        resultNull()
                    }
                }
                method { name = "updateQsSecondTileContainer" }.hook {
                    before {
                        val isShow = when (mode) {
                            "1" -> true
                            "2" -> false
                            "3" -> getMediaData() != null
                            else -> return@before
                        }
                        val res = args().first().cast<Resources>() ?: return@before
                        val bool = args().last().cast<Boolean>() ?: return@before
                        val linear = field { name = "mSecondTileContainer" }.get(instance)
                            .cast<LinearLayout>() ?: return@before
                        val mTmpConstraintSet = field { name = "mTmpConstraintSet" }
                            .get(instance).any() ?: return@before
                        val smallSideMargin = res.getIdentifier(
                            "qs_footer_hl_tile_side_margin_smallspace",
                            "dimen", MediaPlayerDisplayMode.packageName
                        ).takeIf { it != 0 } ?: return@before
                        val sideMargin = res.getIdentifier(
                            "qs_footer_hl_tile_side_margin",
                            "dimen", MediaPlayerDisplayMode.packageName
                        ).takeIf { it != 0 } ?: return@before
                        val sideSize = safeOfNull {
                            res.getDimensionPixelSize(if (bool) smallSideMargin else sideMargin)
                        } ?: return@before
                        val guideLine = res.getIdentifier(
                            "guide_line", "id", MediaPlayerDisplayMode.packageName
                        ).takeIf { it != 0 } ?: return@before
                        if (isShow) {
                            val firstTile = field { name = "mFirstTileContainer" }.get(instance)
                                .cast<LinearLayout>() ?: return@before
                            val smallContainerMargin = res.getIdentifier(
                                "qs_footer_hl_tile_two_container_margin_top_smallspace",
                                "dimen", MediaPlayerDisplayMode.packageName
                            ).takeIf { it != 0 } ?: return@before
                            val containerMargin = res.getIdentifier(
                                "qs_footer_hl_tile_two_container_margin_top",
                                "dimen", MediaPlayerDisplayMode.packageName
                            ).takeIf { it != 0 } ?: return@before
                            val containerSize = safeOfNull {
                                res.getDimensionPixelSize(if (bool) smallContainerMargin else containerMargin)
                            } ?: return@before
                            mTmpConstraintSet.connectSet(linear.id, 6, 0, 6, 0)
                            mTmpConstraintSet.connectSet(linear.id, 7, guideLine, 6, sideSize)
                            mTmpConstraintSet.connectSet(
                                linear.id, 3, firstTile.id, 4, containerSize
                            )
                        } else {
                            mTmpConstraintSet.connectSet(linear.id, 6, guideLine, 7, sideSize)
                            mTmpConstraintSet.connectSet(linear.id, 7, 0, 7, 0)
                            mTmpConstraintSet.connectSet(linear.id, 3, 0, 3, 0)
                        }
                        resultNull()
                    }
                }
            }
        }
    }

    object MediaPlayerDisplayModeC13 : YukiBaseHooker() {
        override fun onHook() {
            var mode = prefs(ModulePrefs).getString("set_media_player_display_mode", "0")
            dataChannel.wait<String>("set_media_player_display_mode") { mode = it }

            "com.oplus.systemui.qs.media.OplusQsMediaCarouselController".toClass().apply {
                method { name = "setCurrentMediaData" }.hook {
                    after {
                        val status = when (mode) {
                            "1" -> true
                            "2" -> false
                            else -> return@after
                        }
                        val mediaModeChangeListener = field { name = "mediaModeChangeListener" }
                            .get(instance).any() ?: return@after
                        mediaModeChangeListener.current().method { name = "onChanged" }.call(status)
                    }
                }
                method { name = "setMediaModeChangeListener" }.hook {
                    after {
                        val status = when (mode) {
                            "1" -> true
                            "2" -> false
                            else -> return@after
                        }
                        val mediaModeChangeListener = args().first().any() ?: return@after
                        mediaModeChangeListener.current().method { name = "onChanged" }.call(status)
                    }
                }
            }
        }
    }

    fun getMediaData(): Any? {
        val clazz = VariousClass(
            "com.oplus.systemui.qs.media.OplusQsMediaCarouselController\$MediaPlayerData",
            "com.oplus.systemui.media.OplusMediaControllerImpl\$MediaPlayerData"
        ).toClass()
        val mediaPlayerData = clazz.field { name = "INSTANCE" }.get().any() ?: return null
        val firstActiveMediaOrSortKey = mediaPlayerData.current().method {
            name = if (SDK >= A14) "getFirstActiveMediaSortKey" else "firstActiveMedia"
        }.call() ?: return null
        if (SDK >= A14) mediaPlayerData.current().method {
            name = "getMediaDataKey";paramCount = 1
        }.call(firstActiveMediaOrSortKey) ?: return null
        val getData = firstActiveMediaOrSortKey.current().method {
            name = "getData";emptyParam()
        }.call()
        return getData
    }

    fun Any.connectSet(startId: Int, startSide: Int, endId: Int, endSide: Int, margin: Int) {
        this.current().method {
            name = "connect"
            paramCount = 5
        }.call(startId, startSide, endId, endSide, margin)
    }

    fun Any.constrainHeightSet(viewId: Int, height: Int) {
        this.current().method {
            name = "constrainHeight"
            paramCount = 2
        }.call(viewId, height)
    }

    fun Any.setVisibilitySet(viewId: Int, visibility: Int) {
        this.current().method {
            name = "setVisibility"
            paramCount = 2
        }.call(viewId, visibility)
    }

    object ForceEnableMediaToggleButton : YukiBaseHooker() {
        override fun onHook() {
            "com.oplus.systemui.qs.media.OplusQsMediaPanelView".toClass().apply {
                method { name = "bindMediaData" }.hook {
                    after {
                        args().first().any() ?: field { name = "mMediaOutputBtn" }.get(instance)
                            .cast<ImageButton>()?.setMediaOutputBtn()
                    }
                }
            }
            "com.oplus.systemui.qs.media.OplusQsMediaOutputDialog".toClass().apply {
                method { name = "bindMediaView" }.hook {
                    after {
                        args().first().any() ?: field { name = "mMediaOutputBtn" }.get(instance)
                            .cast<ImageButton>()?.setMediaOutputBtn()
                    }
                }
            }
        }
    }

    private fun ImageButton.setMediaOutputBtn() {
        isVisible = true
        isEnabled = true
        setOnClickListener {
            val clazz = "com.android.systemui.media.dialog.MediaOutputDialogFactory".toClass()
            val mMediaOutputDialogFactory = DependencyUtils(appClassLoader).get(clazz)
            mMediaOutputDialogFactory?.current()?.method { name = "create";paramCount = 3 }
                ?.call("", true, null)
        }
    }
}
