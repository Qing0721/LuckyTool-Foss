package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.screenshot.CustomizeLongScreenshotMaxCapturedPages
import com.fosstool.app.hook.scope.screenshot.DisableScreenshotPackageNameMd5Encrypt
import com.fosstool.app.hook.scope.screenshot.EnablePNGSaveFormat
import com.fosstool.app.hook.scope.screenshot.RemoveScreenshotPrivacyLimit
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getAppSet

object HookScreenshot : YukiBaseHooker() {
    override fun onHook() {
        val appSet = getAppSet(ModulePrefs, packageName)
        if (prefs(ModulePrefs).getBoolean("remove_screenshot_privacy_limit", false)) {
            loadHooker(RemoveScreenshotPrivacyLimit)
        }
        if (prefs(ModulePrefs).getBoolean("remove_page_limit_for_long_screenshots", false)) {
            val exist = appSet[1].toIntOrNull()?.let { it > 130005000 } ?: false
            if (exist) loadHooker(CustomizeLongScreenshotMaxCapturedPages)
        }
        if (prefs(ModulePrefs).getBoolean("enable_png_save_format", false)) {
            loadHooker(EnablePNGSaveFormat)
        }
        if (prefs(ModulePrefs).getBoolean("disable_screenshot_packagename_md5_encrypt", false)) {
            loadHooker(DisableScreenshotPackageNameMd5Encrypt)
        }
    }
}
