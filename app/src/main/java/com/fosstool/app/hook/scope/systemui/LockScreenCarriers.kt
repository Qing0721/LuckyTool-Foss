package com.fosstool.app.hook.scope.systemui

import android.graphics.Typeface
import android.widget.TextView
import androidx.core.view.isVisible
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.A14
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK

object LockScreenCarriers : YukiBaseHooker() {
    override fun onHook() {
        if (SDK >= A14) loadHooker(LockScreenCarrierV14)
        else loadHooker(LockScreenCarrierV13)
    }

    private object LockScreenCarrierV14 : YukiBaseHooker() {
        override fun onHook() {
            val userFont =
                prefs(ModulePrefs).getBoolean("statusbar_carriers_use_user_typeface", false)
            val isRemove = prefs(ModulePrefs).getBoolean("remove_statusbar_carriers", false)

            "com.oplus.systemui.statusbar.widget.OplusStatCarrierTextController".toClass().apply {
                method { name = "onViewAttached" }.hook {
                    after {
                        if (isRemove) field { name = "mView";superClass() }.get(instance)
                            .cast<TextView>()?.isVisible = false
                    }
                }
                method { name = "setVisible" }.hook {
                    before { if (isRemove) args().first().setFalse() }
                }
                method { name = "updateCarrierInfo" }.hook {
                    after {
                        if (isRemove) field { name = "mView";superClass() }.get(instance)
                            .cast<TextView>()?.isVisible = false
                    }
                }
            }

            "com.oplus.systemui.statusbar.widget.OplusStatCarrierText".toClass().apply {
                constructor { paramCount = 2 }.hook {
                    after {
                        if (userFont) instance<TextView>().typeface = Typeface.DEFAULT_BOLD
                    }
                }
                method { name = "onConfigurationChanged" }.hook {
                    after {
                        if (userFont) instance<TextView>().typeface = Typeface.DEFAULT_BOLD
                    }
                }
            }
        }
    }

    private object LockScreenCarrierV13 : YukiBaseHooker() {
        override fun onHook() {
            val userFont =
                prefs(ModulePrefs).getBoolean("statusbar_carriers_use_user_typeface", false)
            val isRemove = prefs(ModulePrefs).getBoolean("remove_statusbar_carriers", false)

            "com.oplusos.systemui.statusbar.widget.StatOperatorNameView".toClass().apply {
                constructor { paramCount = 3 }.hook {
                    after {
                        if (userFont) instance<TextView>().typeface = Typeface.DEFAULT_BOLD
                    }
                }
                method { name = "onConfigurationChanged" }.hook {
                    after {
                        if (userFont) instance<TextView>().typeface = Typeface.DEFAULT_BOLD
                    }
                }
                method { name = "updateCarrierInfo";superClass() }.hook {
                    after { if (isRemove) instance<TextView>().isVisible = false }
                }
            }
        }
    }
}
