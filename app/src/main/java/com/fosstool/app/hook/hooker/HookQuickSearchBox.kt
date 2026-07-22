package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.quicksearchbox.RemoveSearchBoxAppRecommendCard
import com.fosstool.app.hook.scope.quicksearchbox.RemoveSearchBoxUninstalledAppSuggestions
import com.fosstool.app.hook.scope.quicksearchbox.SearchboxDefaultSearchLocalTab
import com.fosstool.app.utils.ModulePrefs

object HookQuickSearchBox : YukiBaseHooker() {
    override fun onHook() {
        if (prefs(ModulePrefs).getBoolean("remove_searchbox_app_recommend_card", false)) {
            loadHooker(RemoveSearchBoxAppRecommendCard)
        }
        if (prefs(ModulePrefs).getBoolean("remove_searchbox_uninstalled_app_suggestions", false)) {
            loadHooker(RemoveSearchBoxUninstalledAppSuggestions)
        }
        if (prefs(ModulePrefs).getBoolean("searchbox_default_search_local_tab", false)) {
            loadHooker(SearchboxDefaultSearchLocalTab)
        }
    }
}
