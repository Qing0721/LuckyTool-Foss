package com.fosstool.app.hook.scope.gallery

import android.content.Context
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.defined.VagueType
import com.highcapable.yukihookapi.hook.type.java.BooleanClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.LongClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.DexkitUtils.useEach
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object HookSystemStorage : YukiBaseHooker() {

    override fun onHook() {
        val aiMaster = prefs(ModulePrefs).getBoolean("enable_ai_master_watermark", false)
        val hassel = prefs(ModulePrefs).getBoolean("enable_hassel_watermark", false)
        val privacy = prefs(ModulePrefs).getBoolean("enable_privacy_watermark", false)
        val lnsCutPhoto = prefs(ModulePrefs).getBoolean("enable_lns_cut_photo", false)
        val seniorPicked = prefs(ModulePrefs).getBoolean("enable_photo_listview_senior_picked", false)
        val photoThumbLine = prefs(ModulePrefs).getString("set_photo_view_thumb_line_display_mode", "0") ?: "0"
        val gifSynthesis = prefs(ModulePrefs).getBoolean("enable_photo_editor_gif_synthesis", false)
        val springFestival = prefs(ModulePrefs).getBoolean("enable_spring_festival_watermark", false)
        val nationalDay = prefs(ModulePrefs).getBoolean("enable_national_day_watermark", false)

        val osVer = getOSVersionCode

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
                    }
                    usingStrings("configNode")
                }
            }.apply {
                checkDataList("HookSystemStorage")
                val configClass = (firstOrNullSafe()?.name ?: return@apply)
                    .toClassOrNull(appClassLoader) ?: return@apply
                configClass.method {
                    param(VagueType, BooleanType)
                    returnType = BooleanClass
                }.hook {
                    after {

                        val context = configClass.field { type = ContextClass }
                            .ignored().get(instance).cast<Context>() ?: return@after
                        val configNode = args().first().any()?.toString() ?: return@after
                        when {
                            configNode.contains("feature_is_support_watermark") -> {
                                if (osVer < 30 && hassel) resultTrue()
                                if (osVer >= 34 && aiMaster) resultTrue()
                            }

                            configNode.contains("feature_is_support_hassel_watermark") ->
                                if (hassel) resultTrue()

                            configNode.contains("feature_is_support_privacy_watermark") ->
                                if (privacy) resultTrue()

                            configNode.contains("feature_is_support_senior_picked") ->
                                if (seniorPicked) resultTrue()

                            configNode.contains("feature_is_support_photo_thumb_line") -> when (photoThumbLine) {
                                "1" -> resultTrue()
                                "2" -> resultFalse()
                            }

                            configNode.contains("feature_is_support_gif_synthesis") ->
                                if (gifSynthesis) resultTrue()

                            configNode.contains("feature_is_support_lns") ->
                                if (lnsCutPhoto) resultTrue()

                            configNode.contains("feature_is_support_spring_festival_watermark") ->
                                if (springFestival && osVer in 27..33 && isDomestic(context)) resultTrue()

                            configNode.contains("feature_is_support_national_day_watermark") ->
                                if (nationalDay && osVer in 27..33 && isDomestic(context)) resultTrue()
                        }
                    }
                }
            }

            if (hassel) {
                dexKitBridge.findClass {
                    matcher {
                        fields {
                            addForType(IntType.name)
                            addForType(BooleanType.name)
                            addForType(StringClass.name)
                        }
                        usingStrings("WatermarkDevice", "isHasselDevice")
                    }
                }.useEach("WatermarkDevice HasselDevice") { classData ->
                    val deviceClass = classData.name.toClassOrNull(appClassLoader) ?: run {
                        YLog.error(
                            "WatermarkDevice HasselDevice hook error! -> ${classData.name}",
                            tag = DexkitUtils.tag,
                        )
                        return@useEach
                    }
                    val hasselField =
                        deviceClass.declaredFields.firstOrNull { it.type == BooleanType } ?: run {
                            YLog.error(
                                "WatermarkDevice HasselDevice hook error! -> ${classData.name}",
                                tag = DexkitUtils.tag,
                            )
                            return@useEach
                        }
                    hasselField.isAccessible = true
                    deviceClass.constructor().ignored().hookAll {
                        after {
                            runCatching { hasselField.setBoolean(instance, true) }
                        }
                    }
                }
            }
        }
    }

    private fun isDomestic(context: Context): Boolean = runCatching {
        context.resources.getBoolean(
            context.resources.getIdentifier("property_domestic", "bool", packageName)
        )
    }.getOrElse {
        runCatching {
            context.resources.configuration.locales[0].language.startsWith("zh")
        }.getOrDefault(false)
    }
}
