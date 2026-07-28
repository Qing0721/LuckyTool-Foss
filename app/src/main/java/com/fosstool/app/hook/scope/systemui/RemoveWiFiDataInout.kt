package com.fosstool.app.hook.scope.systemui

import android.view.View
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.utils.getOSVersionCode
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

object RemoveWiFiDataInout : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode >= 34) hookNewPipeline() else hookLegacy()
    }

    private fun hookNewPipeline() {
        val viewModel = "com.oplus.systemui.statusbar.pipeline.wifi.ui.viewmodel.OplusWifiViewModel"
            .toClassOrNull(appClassLoader)
        if (viewModel == null) {
            YLog.error("RemoveWiFiDataInout: OplusWifiViewModel not found")
            return
        }

        viewModel.method { name = "getWifiActivityResId" }.ignored().hook {
            after {
                val flow = result ?: return@after
                val value = runCatching {
                    XposedHelpers.callMethod(flow, "getValue") as? Int
                }.getOrNull() ?: -1
                if (value <= 0) return@after
                val stateFlowKt = "kotlinx.coroutines.flow.StateFlowKt"
                    .toClassOrNull(appClassLoader) ?: return@after
                val shareKt = "kotlinx.coroutines.flow.FlowKt__ShareKt"
                    .toClassOrNull(appClassLoader) ?: return@after
                val replaced = runCatching {
                    val mutable = XposedHelpers.callStaticMethod(
                        stateFlowKt, "MutableStateFlow", -1
                    ) ?: return@runCatching null
                    XposedHelpers.callStaticMethod(shareKt, "asStateFlow", mutable)
                }.getOrNull() ?: return@after
                result = replaced
            }
        }
    }

    private fun hookLegacy() {
        val clazz = VariousClass(
            "com.oplusos.systemui.statusbar.OplusStatusBarWifiView",
            "com.oplus.systemui.statusbar.phone.signal.OplusStatusBarWifiViewExImpl"
        ).toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("RemoveWiFiDataInout: OplusStatusBarWifiView not found")
            return
        }
        clazz.method { name = "initViewState" }.ignored().hook {
            after {
                (clazz.findField("mWifiActivity")?.get(instance) as? View)?.isVisible = false
            }
        }
        clazz.method { name = "updateState" }.ignored().hook {
            after {
                (clazz.findField("mWifiActivity")?.get(instance) as? View)?.isVisible = false
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
