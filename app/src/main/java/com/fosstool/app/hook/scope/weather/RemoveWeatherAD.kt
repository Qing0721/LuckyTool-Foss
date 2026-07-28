package com.fosstool.app.hook.scope.weather

import android.content.Intent
import android.net.Uri
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getAppSet
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.IntentClass
import com.highcapable.yukihookapi.hook.type.android.PendingIntentClass
import com.highcapable.yukihookapi.hook.type.java.AnyClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.StringClass

object RemoveWeatherAD : YukiBaseHooker() {

    private const val TAG = "RemoveWeatherAD"
    private const val LOG_TAG = "LuckyTool"

    private const val LEGACY_VERSION_CODE = 13_000_000L

    private const val WEATHER_WRAPPER = "com.oplus.weather.main.model.WeatherWrapper"

    override fun onHook() {
        val removeAds = prefs(ModulePrefs).getBoolean("remove_weather_some_page_bottom_ads", false)
        val disableJump = prefs(ModulePrefs).getBoolean("disable_weather_jump_browser", false)

        if (!removeAds && !disableJump) return

        val versionCode = weatherVersionCode()
        if (versionCode in 1L until LEGACY_VERSION_CODE) {
            loadHooker(RemoveWeatherADLegacy)
            return
        }

        hookFeedAdManager(removeAds)
        hookAppFeatureUtils(removeAds)
        hookLocalUtils(removeAds, disableJump)
        hookNoticeItem(disableJump)
        hookSecondaryPageUtil(removeAds)
        hookReminders(disableJump)
    }

    private fun weatherVersionCode(): Long {
        getAppSet(ModulePrefs, packageName).getOrNull(1)
            ?.toLongOrNull()?.takeIf { it > 0 }?.let { return it }
        return runCatching {
            appContext?.packageManager?.getPackageInfo(packageName, 0)?.longVersionCode ?: 0L
        }.getOrDefault(0L)
    }

    private fun hookFeedAdManager(removeAds: Boolean) {
        val clazz = "com.oplus.weather.ad.OPPOFeedAdManager".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("$TAG -> OPPOFeedAdManager not found", tag = LOG_TAG)
            return
        }
        if (!removeAds) return
        clazz.method { name = "hasOpenPopularRecommended" }.ignored().hook { replaceToFalse() }
        clazz.method { name = "hasOpenAdSdkShowBannerFromNetwork" }.ignored().hook { replaceToFalse() }
    }

    private fun hookAppFeatureUtils(removeAds: Boolean) {
        val clazz = "com.oplus.weather.utils.AppFeatureUtils".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("$TAG -> AppFeatureUtils not found", tag = LOG_TAG)
            return
        }
        if (!removeAds) return
        clazz.method { name = "isSupportOplusAd" }.ignored().hook { replaceToFalse() }
    }

    private fun hookLocalUtils(removeAds: Boolean, disableJump: Boolean) {
        val clazz = "com.oplus.weather.utils.LocalUtils".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("$TAG -> LocalUtils not found", tag = LOG_TAG)
            return
        }

        clazz.method { name { it.contains("jumpToBrowser") } }.ignored().hookAll {
            before { handleWeatherBrowserJump(removeAds, disableJump, ::startBrowserDetailPage) }
        }

        clazz.method { name = "startBrowserForUrl" }.ignored().hookAll {
            before { handleWeatherBrowserJump(removeAds, disableJump, ::startBrowserDetailPage) }
        }

        clazz.method { name = "isBrowserSupportJump"; superClass() }
            .ignored().hook { replaceToFalse() }

        clazz.method { name = "getH5StringBuffer"; superClass() }.ignored().hook {
            after {
                val buffer = result as? StringBuffer ?: return@after
                result = StringBuffer(sanitizeWeatherUrl(buffer.toString()))
            }
        }
    }

    private fun hookNoticeItem(disableJump: Boolean) {
        val clazz = "com.oplus.weather.main.view.itemview.NoticeItem".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("$TAG -> NoticeItem not found", tag = LOG_TAG)
            return
        }
        val wrapperClass = WEATHER_WRAPPER.toClassOrNull(appClassLoader)
        if (wrapperClass == null) YLog.error("$TAG -> WeatherWrapper not found", tag = LOG_TAG)

        clazz.method { name = "showRainfallPanel" }.ignored().hook {
            before {
                if (!disableJump || wrapperClass == null) return@before
                val host = instanceOrNull ?: return@before
                val wrapper = clazz.field { type = wrapperClass; superClass() }
                    .ignored().get(host).any() ?: return@before
                wrapper.javaClass.method { name = "setRainFallAdLink"; superClass() }
                    .ignored().get(wrapper).call("")
            }
        }

        clazz.method { name = "showWarnWeatherPanel" }.ignored().hook {
            before {
                if (!disableJump) return@before
                val last = args.lastOrNull() ?: return@before
                last.javaClass.field { name = "addLink"; superClass() }
                    .ignored().get(last).set("")
            }
        }
    }

    private fun hookSecondaryPageUtil(removeAds: Boolean) {
        val clazz = "com.oplus.weather.utils.SecondaryPageUtil".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("$TAG -> SecondaryPageUtil not found", tag = LOG_TAG)
            return
        }

        clazz.method { name = "newLink" }.ignored().hook {
            after {
                if (!removeAds) return@after
                val link = result as? String ?: return@after
                result = sanitizeWeatherUrl(link)
            }
        }

        clazz.method {
            name { it.startsWith("jump") && it.contains("Browser") }
            param { types -> types.any { it == StringClass } && types.any { it == BooleanType } }
            returnType { it == IntentClass || it == AnyClass }
        }.ignored().hookAll {

            after {
                val intent = result as? Intent ?: return@after
                intent.data = Uri.parse(sanitizeWeatherUrl(intent.data.toString()))
            }
        }
    }

    private fun hookReminders(disableJump: Boolean) {

        arrayOf(
            "com.oplus.weather.service.service.RainReminder" to "createIntentOpenWeatherMainActivity",
            "com.oplus.weather.service.service.WarnReminder" to "getWarnWeatherIntent",
        ).forEach { (className, methodName) ->
            val clazz = className.toClassOrNull(appClassLoader)
            if (clazz == null) {
                YLog.error("$TAG -> $className not found", tag = LOG_TAG)
                return@forEach
            }
            clazz.method { name = methodName }.ignored().hook {
                before {
                    if (!disableJump) return@before
                    if (args.isNotEmpty()) args(args.lastIndex).set("")
                }
            }
        }

        val morning = "com.oplus.weather.morning.MorningReminder".toClassOrNull(appClassLoader)
        if (morning == null) {
            YLog.error("$TAG -> MorningReminder not found", tag = LOG_TAG)
            return
        }
        val wrapperClass = WEATHER_WRAPPER.toClassOrNull(appClassLoader) ?: return
        morning.method {
            param(wrapperClass, ContextClass)
            returnType = PendingIntentClass
        }.ignored().hook {
            before { if (disableJump) resultNull() }
        }
    }
}
