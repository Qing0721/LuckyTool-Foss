package com.fosstool.app.hook.scope.launcher

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object DrawerLayoutRowColume : YukiBaseHooker() {
    override fun onHook() {
        val columns = prefs(ModulePrefs).getInt("set_drawer_icon_columns", -1).let {
            if (it >= 0) it else prefs(ModulePrefs).getInt("set_icon_columns_in_drawer", 4)
        }
        "com.android.launcher3.InvariantDeviceProfile".toClass().apply {
            method {
                name = "initGrid"
                paramCount(3..4)
            }.hook {
                after {
                    runCatching {
                        field { name = "numAllAppsColumns" }.get(instance).set(columns)
                    }
                    runCatching {
                        field { name = "numColumnsAllApps" }.get(instance).set(columns)
                    }
                }
            }
        }
    }
}
