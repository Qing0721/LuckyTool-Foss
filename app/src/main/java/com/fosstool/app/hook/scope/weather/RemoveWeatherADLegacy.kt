package com.fosstool.app.hook.scope.weather

import android.content.Context
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import org.luckypray.dexkit.query.enums.StringMatchType

internal object RemoveWeatherADLegacy : YukiBaseHooker() {

    private const val TAG = "RemoveWeatherADLegacy"
    private const val LOG_TAG = "LuckyTool"

    @Volatile
    private var browserHelperClass: Class<*>? = null

    override fun onHook() {
        val removeAds = prefs(ModulePrefs).getBoolean("remove_weather_some_page_bottom_ads", false)
        val disableJump = prefs(ModulePrefs).getBoolean("disable_weather_jump_browser", false)
        if (!removeAds && !disableJump) return

        DexkitUtils.create(appInfo.sourceDir) { bridge ->

            val webViewClasses = bridge.findClass {
                matcher {
                    className("com.coloros.weather.plugin.webview", StringMatchType.StartsWith)
                }
            }.checkDataList("$TAG webview package", onlyOne = false)
            if (webViewClasses.isNotEmpty()) {
                bridge.findMethod {
                    searchInClass(webViewClasses)
                    matcher {
                        paramCount = 5
                        returnType(UnitType.name)
                        usingNumbers(0x20000000)
                        usingStrings(
                            "context",
                            "url",
                            "statisticsTag",
                            "intent_params_url",
                            "intent_params_isFirst",
                            "intent_params_statistics",
                        )
                    }
                }.apply {
                    checkDataList("$TAG BrowserCommonUtils", onlyOne = false)
                    browserHelperClass =
                        firstOrNullSafe()?.className?.toClassOrNull(appClassLoader)
                }
            }

            bridge.findClass {
                matcher {
                    fields {
                        addForType(BooleanType.name)
                        addForType("java.util.regex.Pattern")
                    }
                    methods {
                        add {
                            paramTypes(
                                IntType.name,
                                ContextClass.name,
                                StringClass.name,
                                StringClass.name,
                                BooleanType.name,
                                BooleanType.name,
                            )
                            returnType(UnitType.name)
                            usingStrings("OppoUtils", "frontCode", "infoEnable", "fromWeatherApp")
                        }
                        add {
                            paramTypes(
                                ContextClass.name,
                                IntType.name,
                                StringClass.name,
                                StringClass.name,
                                BooleanType.name,
                            )
                            returnType(UnitType.name)
                            usingStrings(
                                "com.heytap.browser",
                                "com.android.browser",
                                "com.coloros.browser",
                            )
                        }
                    }
                }
            }.apply {
                checkDataList("$TAG OppoUtils")
                val clazz = firstOrNullSafe()?.name?.toClassOrNull(appClassLoader)
                if (clazz == null) {
                    YLog.error("$TAG -> OppoUtils class not resolved", tag = LOG_TAG)
                    return@apply
                }
                clazz.method {
                    param(IntType, ContextClass, StringClass, StringClass, BooleanType, BooleanType)
                    returnType = UnitType
                }.ignored().hookAll {
                    before {
                        handleWeatherBrowserJump(removeAds, disableJump) { code, ctx, url, tag ->
                            openLegacyBrowserPage(code, ctx, url, tag)
                        }
                    }
                }
                clazz.method {
                    param(ContextClass, IntType, StringClass, StringClass, BooleanType)
                    returnType = UnitType
                }.ignored().hookAll {
                    before {
                        handleWeatherBrowserJump(removeAds, disableJump) { code, ctx, url, tag ->
                            openLegacyBrowserPage(code, ctx, url, tag)
                        }
                    }
                }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun openLegacyBrowserPage(
        frontCode: Int,
        context: Context,
        url: String,
        statisticsTag: String,
    ) {
        val clazz = browserHelperClass ?: return
        runCatching {
            clazz.method { paramCount = 5 }.ignored().get()
                .call(context, url, true, statisticsTag, true)
        }
    }
}
