package com.fosstool.app.hook.scope.launcher

import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A12
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.SDK

object RemoveAppUpdateDot : YukiBaseHooker() {
    override fun onHook() {
        val clazz = if (SDK >= A13) "com.android.launcher3.BubbleTextView"
        else if (SDK >= A12) "com.android.launcher3.OplusBubbleTextView"
        else "com.android.launcher3.OplusBubbleTextView"

        clazz.toClass().apply {
            method {
                name = "applyLabel"
                paramCount = when (simpleName) {
                    "BubbleTextView" -> 1
                    "OplusBubbleTextView" -> 3
                    else -> 1
                }
            }.hook {
                before {
                    val itemInfoWithIcon = args().first().any() ?: return@before
                    val title = itemInfoWithIcon.current().field {
                        name = "title";superClass()
                    }.cast<CharSequence>()
                    instance<TextView>().text = title
                    resultNull()
                }
            }
        }
    }
}
