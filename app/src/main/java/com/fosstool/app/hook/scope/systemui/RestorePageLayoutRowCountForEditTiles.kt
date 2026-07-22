package com.fosstool.app.hook.scope.systemui

import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method

object RestorePageLayoutRowCountForEditTiles : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.qs.customize.OplusQSCustomizer",
            "com.oplus.systemui.qs.customize.OplusQSCustomizer"
        ).toClass().apply {
            constructor { paramCount = 2 }.hook {
                after {
                    field { name = "mMoreFunctionLabel" }.get(instance).cast<View>()
                        ?.isVisible = false
                }
            }
            method { name = "updateResources" }.hook {
                after {
                    field { name = "mRecyclerViewTop" }.get(instance).cast<View>()?.apply {
                        layoutParams = (layoutParams as LinearLayout.LayoutParams).apply {
                            height = (height / 3) * 4
                        }
                    }
                }
            }
        }
    }
}
