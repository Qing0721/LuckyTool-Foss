package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.market.RemoveMarketMinePageAppRecommend
import com.fosstool.app.hook.scope.market.RemoveMarketSplashPageAppRecommend
import com.fosstool.app.hook.scope.market.RemoveMarketUpdatePageAppRecommend
import com.fosstool.app.utils.ModulePrefs

object HookMarket : YukiBaseHooker() {
    override fun onHook() {
        if (prefs(ModulePrefs).getBoolean("remove_market_splash_page_app_recommend", false)) {
            loadHooker(RemoveMarketSplashPageAppRecommend)
        }

        if (prefs(ModulePrefs).getBoolean("remove_market_update_download_page_app_recommend", false)) {
            loadHooker(RemoveMarketUpdatePageAppRecommend)
        }
        if (prefs(ModulePrefs).getBoolean("remove_market_mine_page_app_recommend", false)) {
            loadHooker(RemoveMarketMinePageAppRecommend)
        }
    }
}
