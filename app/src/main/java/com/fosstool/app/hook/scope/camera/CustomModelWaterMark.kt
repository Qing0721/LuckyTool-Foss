package com.fosstool.app.hook.scope.camera

import android.os.Build
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.BitmapClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.PaintClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.FloatType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs

object CustomModelWaterMark : YukiBaseHooker() {
    override fun onHook() {
        val isRealme = Build.MODEL.startsWith("RM", true)
        if (isRealme) return
        loadHooker(HookCameraModelWaterMark)
    }

    private object HookCameraModelWaterMark : YukiBaseHooker() {
        override fun onHook() {
            val waterMark = prefs(ModulePrefs).getString("custom_model_watermark", "None")
            if (waterMark.isBlank() || waterMark == "None") return

            DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
                dexKitBridge.findClass {
                    matcher {
                        fields {
                            addForType(StringClass.name)
                            count(1)
                        }
                        methods {
                            add {
                                paramCount(0)
                                returnType(StringClass.name)
                            }
                            count(2..4)
                        }
                        usingStrings("ro.vendor.oplus.market.enname", "ro.vendor.oplus.market.name")
                    }
                }.apply {
                    checkDataList("HookCameraModelWaterMark MarketUtil")
                    val member = first()
                    member.name.toClass().apply {
                        method { emptyParam();returnType = StringClass }.hookAll {
                            after {
                                val res = result<String>() ?: return@after
                                if (res.contains("getVendorMarketName")) return@after
                                result = waterMark
                            }
                        }
                    }
                }

                dexKitBridge.findClass {
                    matcher {
                        methods {
                            add { returnType(PaintClass.name) }
                            add { returnType(BitmapClass.name) }
                            add { returnType(UnitType.name) }
                            add { returnType(IntType.name) }
                            add { returnType(FloatType.name) }
                            add { returnType(BooleanType.name) }
                            add { returnType(StringClass.name) }
                            add { paramCount(8) }
                            add { paramTypes(StringClass.name) }
                            add { paramTypes(ContextClass.name, FloatType.name) }
                            add { paramTypes(ContextClass.name, IntType.name, FloatType.name) }
                        }
                        usingStrings("WatermarkHelper", "removeChineseOfString")
                    }
                }.apply {
                    checkDataList("HookCameraModelWaterMark WatermarkHelper")
                    val member = first()
                    member.name.toClass().apply {
                        method { param(StringClass);returnType = StringClass }.hookAll {
                            after {
                                val res = result<String>() ?: return@after
                                if (res.contains("removeChineseOfString")) return@after
                                if (res.toIntOrNull() != null) return@after
                                result = waterMark
                            }
                        }
                    }
                }
            }
        }
    }
}
