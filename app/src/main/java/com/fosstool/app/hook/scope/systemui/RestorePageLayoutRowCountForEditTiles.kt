package com.fosstool.app.hook.scope.systemui

import android.view.View
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Field

object RestorePageLayoutRowCountForEditTiles : YukiBaseHooker() {
    override fun onHook() {
        VariousClass(
            "com.oplusos.systemui.qs.customize.OplusQSCustomizer",
            "com.oplus.systemui.qs.customize.OplusQSCustomizer"
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.declaredConstructors.filter { it.parameterCount == 2 }.forEach { ctor ->
                runCatching {
                    XposedBridge.hookMethod(ctor, object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            (c.findField("mMoreFunctionLabel")?.get(param.thisObject) as? View)
                                ?.isVisible = false
                        }
                    })
                }
            }
            c.method { name = "updateResources" }.ignored().hook {
                after {
                    val top = c.findField("mRecyclerViewTop")?.get(instance) as? View
                        ?: return@after
                    top.layoutParams = (top.layoutParams as LinearLayout.LayoutParams).apply {
                        height = (height / 3) * 4
                    }
                }
            }
        }
    }

    private fun Class<*>.findField(name: String): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            runCatching { return cls.getDeclaredField(name).also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }
}
