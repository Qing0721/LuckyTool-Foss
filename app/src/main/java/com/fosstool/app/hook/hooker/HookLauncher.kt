package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.launcher.AllowLockingUnLockingOfExcludedActivity
import com.fosstool.app.hook.scope.launcher.CustomDesktopDefaultHomePage
import com.fosstool.app.hook.scope.launcher.DisableAutoSwitchLastTask
import com.fosstool.app.hook.scope.launcher.DisableLongPressAppIconSecondaryMenu
import com.fosstool.app.hook.scope.launcher.DrawerLayoutRowColume
import com.fosstool.app.hook.scope.launcher.EnableAppUpdateDot
import com.fosstool.app.hook.scope.launcher.EnableAutoCloseFolder
import com.fosstool.app.hook.scope.launcher.EnableDockerBackground
import com.fosstool.app.hook.scope.launcher.EnableLauncherIndicatorEntry
import com.fosstool.app.hook.scope.launcher.FolderLayoutRowColume
import com.fosstool.app.hook.scope.launcher.ForceEnableDockerBackgroundBlur
import com.fosstool.app.hook.scope.launcher.ForceEnableRecentTaskMemoryDisplay
import com.fosstool.app.hook.scope.launcher.HookAppBadge
import com.fosstool.app.hook.scope.launcher.LauncherIconNameDisplay
import com.fosstool.app.hook.scope.launcher.LauncherLayoutRowColume
import com.fosstool.app.hook.scope.launcher.LongPressAppIconOpenAppDetails
import com.fosstool.app.hook.scope.launcher.PageIndicator
import com.fosstool.app.hook.scope.launcher.RecentTaskListClearButton
import com.fosstool.app.hook.scope.launcher.SetAppUpdateDotDisplayMode
import com.fosstool.app.hook.scope.launcher.RemoveBottomAppIconOfRecentTaskList
import com.fosstool.app.hook.scope.launcher.RemoveDockerMaxNumberLimit
import com.fosstool.app.hook.scope.launcher.RemoveFolderNameInputLimit
import com.fosstool.app.hook.scope.launcher.RemoveFolderPreviewBackground
import com.fosstool.app.hook.scope.launcher.RemoveLauncherCardName
import com.fosstool.app.hook.scope.launcher.RemoveWidgetsAddRequestWhitelist
import com.fosstool.app.hook.scope.launcher.UnlockTaskLocks
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

class HookLauncher : YukiBaseHooker() {
    override fun onHook() {
        loadHooker(LauncherIconNameDisplay)
        loadHooker(PageIndicator)
        if (SDK >= A13) loadHooker(HookAppBadge)
        if (prefs(ModulePrefs).getBoolean("enable_display_app_update_dot", false)) {
            loadHooker(EnableAppUpdateDot)
        }
        loadHooker(SetAppUpdateDotDisplayMode)
        if (prefs(ModulePrefs).getBoolean("launcher_layout_enable", false)) {
            loadHooker(LauncherLayoutRowColume)
        }
        if (prefs(ModulePrefs).getBoolean("enable_folder_layout_adjustment", false)) {
            loadHooker(FolderLayoutRowColume)
        }
        if (prefs(ModulePrefs).getBoolean("enable_drawer_layout_adjustment", false)) {
            loadHooker(DrawerLayoutRowColume)
        }
        if (prefs(ModulePrefs).getBoolean("remove_folder_preview_background", false)) {
            loadHooker(RemoveFolderPreviewBackground)
        }
        if (prefs(ModulePrefs).getBoolean("remove_recent_task_list_clear_button", false)) {
            loadHooker(RecentTaskListClearButton)
        }
        if (prefs(ModulePrefs).getBoolean("long_press_app_icon_open_app_details", false)) {
            loadHooker(LongPressAppIconOpenAppDetails)
        }
        if (prefs(ModulePrefs).getBoolean("remove_bottom_app_icon_of_recent_task_list", false)) {
            loadHooker(RemoveBottomAppIconOfRecentTaskList)
        }
        if (prefs(ModulePrefs).getBoolean("unlock_task_locks", false)) {
            loadHooker(UnlockTaskLocks)
        }
        if (prefs(ModulePrefs).getBoolean("allow_locking_unlocking_of_excluded_activity", false)) {
            loadHooker(AllowLockingUnLockingOfExcludedActivity)
        }

        if (prefs(ModulePrefs).getBoolean("enable_docker_background", false)) {
            loadHooker(EnableDockerBackground)
        }
        if (prefs(ModulePrefs).getBoolean("force_enable_docker_background_blur", false)) {
            loadHooker(ForceEnableDockerBackgroundBlur)
        }
        if (prefs(ModulePrefs).getBoolean("force_enable_recent_task_memory_display", false)) {
            loadHooker(ForceEnableRecentTaskMemoryDisplay)
        }
        if (prefs(ModulePrefs).getBoolean("enable_auto_close_folder", false)) {
            loadHooker(EnableAutoCloseFolder)
        }
        if (prefs(ModulePrefs).getBoolean("remove_widgets_add_request_whitelist", false)) {
            loadHooker(RemoveWidgetsAddRequestWhitelist)
        }
        if (prefs(ModulePrefs).getBoolean("remove_launcher_card_name", false)) {
            loadHooker(RemoveLauncherCardName)
        }
        if (prefs(ModulePrefs).getBoolean("disable_long_press_app_icon_secondary_menu", false)) {
            loadHooker(DisableLongPressAppIconSecondaryMenu)
        }
        if (prefs(ModulePrefs).getBoolean("enable_launcher_indicator_entry", false)) {
            loadHooker(EnableLauncherIndicatorEntry)
        }
        if (prefs(ModulePrefs).getBoolean("remove_docker_max_number_limit", false)) {
            loadHooker(RemoveDockerMaxNumberLimit)
        }
        if (prefs(ModulePrefs).getBoolean("remove_folder_name_input_limit", false)) {
            loadHooker(RemoveFolderNameInputLimit)
        }
        if (prefs(ModulePrefs).getBoolean("disable_auto_switch_last_task", false)) {
            loadHooker(DisableAutoSwitchLastTask)
        }
        loadHooker(CustomDesktopDefaultHomePage)

    }
}
