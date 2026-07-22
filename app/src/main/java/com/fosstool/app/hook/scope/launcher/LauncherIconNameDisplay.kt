package com.fosstool.app.hook.scope.launcher

import android.util.TypedValue
import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode

object LauncherIconNameDisplay : YukiBaseHooker() {
    override fun onHook() {
        val allowMultiLine = prefs(ModulePrefs).getBoolean("allow_app_names_display_multiple_lines", false)
        val iconLineHeight = prefs(ModulePrefs).getInt("custom_app_icon_name_line_height", -1)
        val iconSize = prefs(ModulePrefs).getInt("custom_launcher_app_icon_size", 0)
        if (!allowMultiLine && iconLineHeight <= -1 && iconSize <= 0) return

        val osVer = getOSVersionCode
        if (allowMultiLine && osVer < 26) {
            runCatching {
                "com.android.launcher3.OplusBubbleTextView".toClass().apply {
                    method {
                        name = "setMaxLines"
                        param(IntType)
                    }.hookAll {
                        before {
                            val tv = instance<TextView>() ?: return@before
                            tv.maxLines = 2
                            result = null
                        }
                    }
                }
            }
        }
        if (allowMultiLine && iconLineHeight > -1) {
            runCatching {
                "com.android.launcher3.OplusBubbleTextView".toClass().apply {
                    method {
                        paramCount = 3
                    }.hookAll {
                        after {
                            val tv = instance<TextView>() ?: return@after
                            val px = TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_DIP,
                                iconLineHeight.toFloat(),
                                android.content.res.Resources.getSystem().displayMetrics
                            ).toInt()
                            tv.lineHeight = px
                        }
                    }
                }
            }
        }
        if (iconSize > 0) {
            runCatching {
                "com.android.launcher.layoutparam.IconParam".toClass().apply {
                    method {
                        name = "getIconSizePx"
                        returnType = IntType
                    }.hookAll {
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
    }
}
