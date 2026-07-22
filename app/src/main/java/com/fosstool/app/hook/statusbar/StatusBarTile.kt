package com.fosstool.app.hook.statusbar

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.systemui.ControlCenterTiles
import com.fosstool.app.hook.scope.systemui.FixTileAlignBothSides
import com.fosstool.app.hook.scope.systemui.ForceDisplayDeviceControlsTile
import com.fosstool.app.hook.scope.systemui.LongPressTileOpenThePage
import com.fosstool.app.hook.scope.systemui.MediaPlayerPanel
import com.fosstool.app.hook.scope.systemui.RestorePageLayoutRowCountForEditTiles
import com.fosstool.app.hook.scope.systemui.TileBackgroundTransparency
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object StatusBarTile : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(LongPressTileOpenThePage)

        if (SDK >= A13) loadHooker(MediaPlayerPanel)

        loadHooker(ControlCenterTiles)

        if (prefs(ModulePrefs).getBoolean("fix_tile_align_both_sides", false)) {
            if (SDK >= A13) loadHooker(FixTileAlignBothSides)
        }
        if (prefs(ModulePrefs).getBoolean("restore_page_layout_row_count_for_edit_tiles", false)) {
            if (SDK >= A13) loadHooker(RestorePageLayoutRowCountForEditTiles)
        }
        if (prefs(ModulePrefs).getInt("custom_tile_background_transparency", -1) >= 0) {
            loadHooker(TileBackgroundTransparency)
        }
        if (prefs(ModulePrefs).getBoolean("force_display_of_device_controls_tiles", false)) {
            loadHooker(ForceDisplayDeviceControlsTile)
        }
    }
}
