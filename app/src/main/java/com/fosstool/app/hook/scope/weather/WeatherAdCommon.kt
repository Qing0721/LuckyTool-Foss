package com.fosstool.app.hook.scope.weather

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.highcapable.yukihookapi.hook.param.HookParam

internal fun sanitizeWeatherUrl(url: String): String {
    if (url.isBlank()) return url
    var result = url
    if (result.contains("fromWeatherApp=true")) {
        result = result.replace("fromWeatherApp=true", "fromWeatherApp=false")
    }
    if (result.contains("infoEnable=true")) {
        result = result.replace("infoEnable=true", "infoEnable=false")
    }
    if (result.contains("infoEnable")) return result
    if (result.lastOrNull() != '&') result += "&"
    return result + "infoEnable=false"
}

private fun buildBrowserDetailIntent(
    frontCode: Int,
    intentAction: String,
    url: String,
    statisticsTag: String,
): Intent = Intent("com.heytap.browser.action.DETAIL_PAGE").apply {
    if (intentAction.isNotBlank()) action = intentAction

    addFlags(0x20000000)
    data = Uri.parse(url)
    when (frontCode) {
        1 -> setPackage("com.android.browser")
        2 -> setPackage("com.heytap.browser")
        3 -> setPackage("com.coloros.browser")
    }
    putExtra("clickTime", System.currentTimeMillis())
    putExtra("clickType", "weather")
    putExtra("intent_params_url", url)
    putExtra("intent_params_isFirst", true)
    putExtra("intent_params_statistics", statisticsTag)
}

internal fun startBrowserDetailPage(
    frontCode: Int,
    context: Context,
    url: String,
    statisticsTag: String,
) {
    var code = frontCode
    while (code >= 0) {
        try {
            context.startActivity(buildBrowserDetailIntent(code, "", url, statisticsTag))
            return
        } catch (_: Throwable) {
            try {
                context.startActivity(
                    buildBrowserDetailIntent(code, Intent.ACTION_VIEW, url, statisticsTag),
                )
                return
            } catch (_: ActivityNotFoundException) {
                code--
            } catch (_: Throwable) {
                return
            }
        }
    }
}

internal fun HookParam.handleWeatherBrowserJump(
    removeAds: Boolean,
    disableJump: Boolean,
    openInternalPage: (frontCode: Int, context: Context, url: String, statisticsTag: String) -> Unit,
) {
    val current = args
    val context = current.firstOrNull { it is Context } as? Context ?: return
    val frontCode = current.firstOrNull { it is Int } as? Int ?: 0
    val urlIndex = current.indexOfFirst {
        it is String && (it.contains("http") || it.contains("://"))
    }
    val url = current.getOrNull(urlIndex) as? String ?: ""
    val tagIndex = current.indexOfFirst {
        it is String && !it.contains("http") && !it.contains("://")
    }
    val statisticsTag = current.getOrNull(tagIndex) as? String ?: ""
    if (url.startsWith("heytapbrowser://")) return
    if (removeAds && urlIndex >= 0) args(urlIndex).set(sanitizeWeatherUrl(url))
    if (disableJump) {
        val finalUrl = args.getOrNull(urlIndex) as? String ?: ""
        openInternalPage(frontCode, context, finalUrl, statisticsTag)
        resultNull()
    }
}
