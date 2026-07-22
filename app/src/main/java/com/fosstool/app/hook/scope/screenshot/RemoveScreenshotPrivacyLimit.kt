package com.fosstool.app.hook.scope.screenshot

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveScreenshotPrivacyLimit : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.screenshot.screenshot.core.ScreenshotContext".toClass().apply {
            method { name = "setScreenshotReject" }.hook {
                before {
                    val rejectCls = args().first().any()?.javaClass ?: return@before
                    if (rejectCls.isEnum.not()) return@before
                    val enumConstants = rejectCls.enumConstants ?: return@before
                    enumConstants.forEach {
                        if (it.toString() == "ACCEPTED") args().first().set(it)
                    }
                }
            }
            method { name = "setLongshotReject" }.hook {
                before {
                    val rejectCls = args().first().any()?.javaClass ?: return@before
                    if (rejectCls.isEnum.not()) return@before
                    val enumConstants = rejectCls.enumConstants ?: return@before
                    enumConstants.forEach {
                        if (it.toString() == "ACCEPTED") args().first().set(it)
                    }
                }
            }
        }
    }
}
