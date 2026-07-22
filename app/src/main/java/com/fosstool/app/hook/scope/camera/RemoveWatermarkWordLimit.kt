package com.fosstool.app.hook.scope.camera

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.BundleClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.CharSequenceClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getAppSet

object RemoveWatermarkWordLimit : YukiBaseHooker() {
    override fun onHook() {
        val appSet = getAppSet(ModulePrefs, packageName)
        val isNew = "com.oplus.camera.setting.CameraSettingActivity".hasClass()
        val clazz = if (isNew) "$5"
        else when (appSet[2]) {
            "8d5b992", "38e5b1a", "b696b47", "02aac8a" -> "$7"
            else -> "$1"
        }

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                searchPackages("com.oplus.camera.setting", "com.oplus.camera.ui.menu.setting")
                matcher {
                    fields {
                        addForType(IntType.name)
                        addForType(LongType.name)
                        addForType(BooleanType.name)
                    }
                    methods {
                        add { name = "onDestroy";returnType(UnitType.name) }
                        add { name = "onPause";returnType(UnitType.name) }
                        add { name = "onPreferenceChange";returnType(BooleanType.name) }
                        add { name = "onPreferenceClick";returnType(BooleanType.name) }
                        add { paramTypes(BundleClass.name);returnType(UnitType.name) }
                        add { paramTypes(BundleClass.name);returnType(BooleanType.name) }
                        add { paramTypes(StringClass.name);returnType(UnitType.name) }
                    }
                    if (isNew) usingStrings("CameraSubSettingFragment")
                    else usingStrings(
                        "CameraSloganSettingFragment",
                        "isSloganEnable",
                        "isVideoSloganEnable"
                    )
                }
            }.apply {
                checkDataList("RemoveWatermarkWordLimit")
                (first().name + clazz).toClass().apply {
                    method { name = "filter";returnType = CharSequenceClass }.hook {
                        intercept()
                    }
                }
            }
        }
    }
}
