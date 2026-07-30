package com.fosstool.app.hook.scope.launcher

import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object DrawerLayoutRowColume : YukiBaseHooker() {
    override fun onHook() {
        val columns = prefs(ModulePrefs).getInt("set_drawer_icon_columns", -1).let {
            if (it >= 0) it else prefs(ModulePrefs).getInt("set_icon_columns_in_drawer", 4)
        }

        val idp = "com.android.launcher3.InvariantDeviceProfile".toClassOrNull(appClassLoader)
        if (idp == null) {
            YLog.error("DrawerLayoutRowColume: InvariantDeviceProfile not found")
        } else {
            idp.method { name = "initGrid" }.ignored().hookAll {
                after { applyColumns(idp, instanceOrNull, columns) }
            }
        }

        val gridOption = "com.android.launcher3.InvariantDeviceProfile\$GridOption"
            .toClassOrNull(appClassLoader)
        if (gridOption == null) {
            YLog.error("DrawerLayoutRowColume: InvariantDeviceProfile\$GridOption not found")
        } else {
            gridOption.constructor { paramCount(2..3) }.ignored().hookAll {
                after { applyColumns(gridOption, instanceOrNull, columns) }
            }
        }

        val oidp = "com.android.launcher3.OplusInvariantDeviceProfile".toClassOrNull(appClassLoader)
        if (oidp == null) {
            YLog.error("DrawerLayoutRowColume: OplusInvariantDeviceProfile not found")
        } else {
            oidp.method { name { it.startsWith("injectInitGrid") } }.ignored().hookAll {
                after { applyColumns(oidp, instanceOrNull, columns) }
            }
        }

        val allAppsParam = "com.android.launcher.layoutparam.AllAppsParam"
            .toClassOrNull(appClassLoader) ?: return
        allAppsParam.method { name = "getNumAllAppsColumns" }.ignored().hook {
            before { result = columns }
        }
    }

    private fun applyColumns(clazz: Class<*>, host: Any?, columns: Int) {
        if (host == null) return
        runCatching {
            clazz.field { name = "numAllAppsColumns"; superClass() }.ignored().get(host).set(columns)
        }

        runCatching {
            clazz.field { name = "numColumnsAllApps"; superClass() }.ignored().get(host).set(columns)
        }
    }
}
