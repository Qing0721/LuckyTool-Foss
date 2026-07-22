package com.fosstool.app.hook.scope.gallery

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.BooleanClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.LongClass
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs

object HookConfigAbility : YukiBaseHooker() {

    override fun onHook() {
        val aiMaster = prefs(ModulePrefs).getBoolean("enable_ai_master_watermark", false)
        val hassel = prefs(ModulePrefs).getBoolean("enable_hassel_watermark", false)
        val privacy = prefs(ModulePrefs).getBoolean("enable_privacy_watermark", false)
        val waterMarkLegacy = prefs(ModulePrefs).getBoolean("enable_watermark_editing", false)
        val waterMark = waterMarkLegacy || aiMaster || hassel || privacy
        val notOplus = prefs(ModulePrefs).getBoolean("replace_oneplus_model_watermark", false)
        val lnsCutPhoto = prefs(ModulePrefs).getBoolean("enable_lns_cut_photo", false)
        val seniorPicked = prefs(ModulePrefs).getBoolean("enable_photo_listview_senior_picked", false)
        val photoThumbLine = prefs(ModulePrefs).getString("set_photo_view_thumb_line_display_mode", "0") ?: "0"
        val gifSynthesis = prefs(ModulePrefs).getBoolean("enable_photo_editor_gif_synthesis", false)
        val springFestival = prefs(ModulePrefs).getBoolean("enable_spring_festival_watermark", false)
        val nationalDay = prefs(ModulePrefs).getBoolean("enable_national_day_watermark", false)

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(ContextClass.name)
                    }
                    methods {
                        add { name = "close";paramCount = 0 }
                        add { name = "contains";paramTypes = listOf(StringClass.name) }
                        add { returnType = AutoCloseable::class.java.name }
                        add {
                            paramTypes = listOf(StringClass.name, IntType.name)
                            returnType = IntClass.name
                        }
                        add {
                            paramTypes = listOf(StringClass.name, LongType.name)
                            returnType = LongClass.name
                        }
                        add {
                            paramTypes = listOf(StringClass.name, StringClass.name)
                            returnType = StringClass.name
                        }
                        add {
                            paramTypes = listOf(StringClass.name, BooleanType.name)
                            returnType = BooleanClass.name
                        }
                    }
                }
            }.apply {
                checkDataList("HookConfigAbility")
                val member = first()
                member.name.toClass().apply {
                    method {
                        param(StringClass, BooleanType)
                        returnType = BooleanClass
                    }.hook {
                        after {
                            when (args().first().string()) {
                                "is_oneplus_brand" -> if (notOplus) resultFalse()
                                "feature_is_support_watermark" -> if (waterMark) resultTrue()
                                "feature_is_support_hassel_watermark" -> if (hassel || waterMarkLegacy) resultTrue()
                                "feature_is_support_photo_editor_watermark" -> if (waterMark) resultTrue()
                                "feature_is_support_privacy_watermark" -> if (privacy || waterMarkLegacy) resultTrue()
                                "feature_is_support_ai_master_watermark" -> if (aiMaster || waterMarkLegacy) resultTrue()
                                "feature_is_support_lns" -> if (lnsCutPhoto) resultTrue()
                                "feature_is_support_senior_picked" -> if (seniorPicked) resultTrue()
                                "feature_is_support_photo_thumb_line" -> when (photoThumbLine) {
                                    "1" -> resultTrue()
                                    "2" -> resultFalse()
                                }
                                "feature_is_support_gif_synthesis" -> if (gifSynthesis) resultTrue()
                                "feature_is_support_spring_festival_watermark" -> if (springFestival) resultTrue()
                                "feature_is_support_national_day_watermark" -> if (nationalDay) resultTrue()
                            }
                        }
                    }
                }
            }
        }
    }
}
