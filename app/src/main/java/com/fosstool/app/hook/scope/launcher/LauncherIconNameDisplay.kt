package com.fosstool.app.hook.scope.launcher

import android.content.res.Resources
import android.util.TypedValue
import android.widget.TextView
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object LauncherIconNameDisplay : YukiBaseHooker() {
    override fun onHook() {
        val allowMultiLine = prefs(ModulePrefs).getBoolean("allow_app_names_display_multiple_lines", false)
        val iconLineHeight = prefs(ModulePrefs).getInt("custom_app_icon_name_line_height", -1)
        val iconSize = prefs(ModulePrefs).getInt("custom_launcher_app_icon_size", 0)
        if (!allowMultiLine && iconLineHeight <= -1 && iconSize <= 0) return

        val osVer = getOSVersionCode
        if (allowMultiLine && osVer < 26) {
            "com.android.launcher3.OplusBubbleTextView".toClassOrNull(appClassLoader)
                ?.method { name = "setMaxLines"; paramCount = 1 }
                ?.ignored()
                ?.hook {
                    before {
                        val tv = instance as? TextView ?: return@before
                        tv.maxLines = 2
                        result = null
                    }
                }
        }
        if (allowMultiLine && iconLineHeight > -1) {

            val bubbleText = "com.android.launcher3.OplusBubbleTextView".toClassOrNull(appClassLoader)
            if (bubbleText == null) {
                YLog.error("LauncherIconNameDisplay: OplusBubbleTextView not found")
            } else {
                bubbleText.constructor { paramCount = 3 }.ignored().hookAll {
                    after {
                        val tv = instance as? TextView ?: return@after
                        val px = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            iconLineHeight.toFloat(),
                            Resources.getSystem().displayMetrics
                        ).toInt()
                        tv.lineHeight = px
                    }
                }
            }
        }
        if (iconSize > 0) {
            "com.android.launcher.layoutparam.IconParam".toClassOrNull(appClassLoader)
                ?.method { name = "getIconSizePx" }
                ?.ignored()
                ?.hook {
                    before {
                        val px = TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            iconSize.toFloat(),
                            android.content.res.Resources.getSystem().displayMetrics
                        ).toInt()
                        result = px
                    }
                }
        }
    }
}
