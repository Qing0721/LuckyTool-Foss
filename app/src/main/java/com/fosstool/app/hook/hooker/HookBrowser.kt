package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.browser.RemoveAdsAtDownloadPageBottom
import com.fosstool.app.hook.scope.browser.RemoveAdsFromDownloadDialog
import com.fosstool.app.hook.scope.browser.RemoveBrowserSearchBarAppPromotion
import com.fosstool.app.hook.scope.browser.RemoveBrowserWindowLimitNumber
import com.fosstool.app.utils.ModulePrefs

object HookBrowser : YukiBaseHooker() {
    override fun onHook() {
        if (prefs(ModulePrefs).getBoolean("remove_ads_from_download_dialog", false)) {
            loadHooker(RemoveAdsFromDownloadDialog)
        }
        if (prefs(ModulePrefs).getBoolean("remove_ads_at_download_page_bottom", false)) {
            loadHooker(RemoveAdsAtDownloadPageBottom)
        }
        if (prefs(ModulePrefs).getBoolean("remove_browser_window_limit_number", false)) {
            loadHooker(RemoveBrowserWindowLimitNumber)
        }
        if (prefs(ModulePrefs).getBoolean("remove_browser_search_bar_app_promotion", false)) {
            loadHooker(RemoveBrowserSearchBarAppPromotion)
        }
    }
}
