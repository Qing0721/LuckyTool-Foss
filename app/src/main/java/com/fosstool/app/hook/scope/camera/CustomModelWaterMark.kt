package com.fosstool.app.hook.scope.camera

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.defined.VagueType
import com.highcapable.yukihookapi.hook.type.java.ArrayListClass
import com.highcapable.yukihookapi.hook.type.java.FloatType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object CustomModelWaterMark : YukiBaseHooker() {
    override fun onHook() {

        loadHooker(HookCameraModelWaterMark)

    }

    private object HookCameraModelWaterMark : YukiBaseHooker() {
        override fun onHook() {
            val waterMark = prefs(ModulePrefs).getString("custom_model_watermark", "None")
            if (waterMark.isBlank() || waterMark == "None") return

            DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

                dexKitBridge.findMethod {
                    matcher {
                        paramTypes(ContextClass.name, FloatType.name, null, null)
                        usingStrings("key_watermark_part_a_line", "key_watermark_part_b_line")
                        usingNumbers(0.03f, 0.007f)
                    }
                }.apply {
                    checkDataList("CustomModelWaterMark Shot")
                    val member = firstOrNullSafe() ?: return@apply
                    val clazz = member.className.toClassOrNull(appClassLoader) ?: return@apply
                    clazz.method {
                        name = member.methodName
                        param(ContextClass, FloatType, VagueType, VagueType)
                        returnType = member.returnTypeName
                    }.ignored().hook {
                        after {
                            @Suppress("UNCHECKED_CAST")
                            val map = result as? HashMap<String, Any> ?: return@after
                            arrayOf(
                                "key_watermark_part_a_line",
                                "key_watermark_part_b_line"
                            ).forEach { key ->
                                val holder = map[key] ?: return@forEach
                                @Suppress("UNCHECKED_CAST")
                                val lines = clazz.field { type = ArrayListClass }
                                    .ignored().get(holder).any() as? ArrayList<String>
                                    ?: return@forEach
                                for (index in lines.indices) {
                                    if (lines[index].contains("Shot on OnePlus")) {
                                        lines[index] = waterMark
                                    }
                                }
                            }
                        }
                    }
                }

                dexKitBridge.findMethod {
                    matcher {
                        paramCount(0)
                        returnType(StringClass.name)
                        usingStrings(
                            "",
                            "ro.vendor.oplus.market.enname",
                            "ro.vendor.oplus.market.name"
                        )
                    }
                }.apply {
                    checkDataList("CustomModelWaterMark MarketName", onlyOne = false)
                    forEach { member ->
                        member.className.toClassOrNull(appClassLoader)
                            ?.method {
                                name = member.methodName
                                emptyParam()
                                returnType = StringClass
                            }
                            ?.ignored()
                            ?.hook { replaceTo(waterMark) }
                    }
                }

                dexKitBridge.findMethod {
                    matcher {
                        returnType(StringClass.name)
                        usingStrings("[一-龥]", "")
                    }
                }.apply {
                    checkDataList("CustomModelWaterMark RemoveChineseOfString")
                    val member = firstOrNullSafe() ?: return@apply
                    member.className.toClassOrNull(appClassLoader)
                        ?.method {
                            name = member.methodName
                            returnType = StringClass
                        }
                        ?.ignored()
                        ?.hookAll { replaceTo(waterMark) }
                }
            }
        }
    }
}
