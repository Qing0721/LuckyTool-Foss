package com.fosstool.app.hook.scope.themestore

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object UnlockThemeStoreVip : YukiBaseHooker() {

    private const val TAG = "UnlockThemeStoreVip"

    override fun onHook() {

        loadClass("com.oppo.cdo.card.theme.dto.vip.VipUserDto")?.let { c ->
            c.method { name = "getVipStatus" }.ignored().hook { replaceTo(1) }
            c.method { name = "getVipDays" }.ignored().hook { replaceTo(999) }
        }

        loadClass("com.oppo.cdo.card.theme.dto.page.WeatherPageResponseDto")?.let { c ->
            c.method { name = "getVipStatus" }.ignored().hook { replaceTo(1) }
        }

        loadClass("com.oppo.cdo.theme.domain.dto.response.ResourceItemDto")?.let { c ->
            c.method { name = "getIsVip" }.ignored().hook { replaceTo(1) }
            c.method { name = "getIsVipAvailable" }.ignored().hook { replaceTo(1) }
        }

        loadClass("com.oppo.cdo.theme.domain.dto.response.PublishProductItemDto")?.let { c ->
            c.method { name = "getPrice" }.ignored().hook { replaceTo(0.0) }
            c.method { name = "getIsVipAvailable" }.ignored().hook { replaceTo(1) }
        }

        loadClass("com.oppo.cdo.card.theme.dto.SplashDto")?.let { c ->
            c.method { name = "getAdData" }.ignored().hook { before { resultNull() } }
            c.method { name = "getShowTime" }.ignored().hook { replaceTo(1) }
            c.method { name = "getIsSkip" }.ignored().hook { replaceToTrue() }
        }

        loadClass("com.nearme.themespace.trial.ThemeTrialExpireReceiver")?.let { c ->
            c.method { name = "onReceive" }.ignored().hook { intercept() }
        }
    }

    private fun loadClass(name: String): Class<*>? =
        name.toClassOrNull(appClassLoader).also {
            if (it == null) YLog.debug("$TAG: class not found -> $name")
        }
}
