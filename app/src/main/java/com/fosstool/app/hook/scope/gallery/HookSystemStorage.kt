package com.fosstool.app.hook.scope.gallery

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.defined.VagueType
import com.highcapable.yukihookapi.hook.type.java.BooleanClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntClass
import com.highcapable.yukihookapi.hook.type.java.LongClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs

object HookSystemStorage : YukiBaseHooker() {

    override fun onHook() {
        val aiMaster = prefs(ModulePrefs).getBoolean("enable_ai_master_watermark", false)
        val hassel = prefs(ModulePrefs).getBoolean("enable_hassel_watermark", false)
        val privacy = prefs(ModulePrefs).getBoolean("enable_privacy_watermark", false)
        val waterMarkLegacy = prefs(ModulePrefs).getBoolean("enable_watermark_editing", false)
        val waterMark = waterMarkLegacy || aiMaster || hassel || privacy
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
                        add { paramCount(2);returnType(IntClass.name) }
                        add { paramCount(2);returnType(LongClass.name) }
                        add { paramCount(2);returnType(BooleanClass.name) }
                        add { paramCount(2);returnType(StringClass.name) }
                        add { paramCount(2);returnType(UnitType.name) }
                        add { paramCount(0);returnType(BooleanType.name) }
                        add { paramCount(4);returnType(BooleanType.name) }
                    }
                    usingStrings("configNode")
                }
            }.apply {
                checkDataList("HookSystemStorage")
                first().name.toClass().apply {
                    method {
                        param(VagueType, BooleanType)
                        returnType = BooleanClass
                    }.hook {
                        after {
                            val configNode = args().first().any()?.toString() ?: return@after
                            when {
                                configNode.contains("feature_is_support_hassel_watermark") -> if (hassel || waterMarkLegacy) resultTrue()
                                configNode.contains("feature_is_support_privacy_watermark") -> if (privacy || waterMarkLegacy) resultTrue()
                                configNode.contains("feature_is_support_ai_master_watermark") -> if (aiMaster || waterMarkLegacy) resultTrue()
                                configNode.contains("feature_is_support_photo_editor_watermark") -> if (waterMark) resultTrue()
                                configNode.contains("feature_is_support_watermark") -> if (waterMark) resultTrue()
                                configNode.contains("feature_is_support_lns") -> if (lnsCutPhoto) resultTrue()
                                configNode.contains("feature_is_support_senior_picked") -> if (seniorPicked) resultTrue()
                                configNode.contains("feature_is_support_photo_thumb_line") -> when (photoThumbLine) {
                                    "1" -> resultTrue()
                                    "2" -> resultFalse()
                                }
                                configNode.contains("feature_is_support_gif_synthesis") -> if (gifSynthesis) resultTrue()
                                configNode.contains("feature_is_support_spring_festival_watermark") -> if (springFestival) resultTrue()
                                configNode.contains("feature_is_support_national_day_watermark") -> if (nationalDay) resultTrue()
                            }
                        }
                    }
                }
            }
        }
    }
}
