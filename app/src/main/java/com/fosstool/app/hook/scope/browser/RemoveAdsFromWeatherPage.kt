package com.fosstool.app.hook.scope.browser

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.method

object RemoveAdsFromWeatherPage : YukiBaseHooker() {

    private const val REMOVE_ADS_JS_COMMAND = """
        (function () {
          window.heyTapNewsRequest = undefined;
          const elements = document.getElementsByClassName('news-con');
          for (let i = 0; i < elements.length; i++) if (elements[i] && !elements[i].hidden) elements[i].hidden = true;
          const element = document.getElementById('news-con');
          if (element && !element.hidden) element.hidden = true;
        })();
    """

    override fun onHook() {
        "com.heytap.browser.webview.WrappedMCWebViewClient".toClass().apply {
            method { name { it.startsWith("onPage") } }.hook {
                after {
                    val currentWebView = args().first().any()?.current()
                    val currentUrl = currentWebView?.method {
                        name = "getUrl";superClass()
                    }?.string() ?: ""
                    if (currentUrl.let { it.startsWith("http://m.weathercn.com") || it.startsWith("https://m.weathercn.com") }) {
                        currentWebView?.method {
                            name = "evaluateJavascript"
                            paramCount = 2
                            superClass()
                        }?.call(REMOVE_ADS_JS_COMMAND.trimIndent(), null)
                    }
                }
            }
        }
    }
}
