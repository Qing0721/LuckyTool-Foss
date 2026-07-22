package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.ModulePrefs

object HookSystemProperties : YukiBaseHooker() {
    override fun onHook() {
        val ltpoMode = prefs(ModulePrefs).getString("set_ltpo_refresh_rate_mode", "0")
        val disableDmVerity = prefs(ModulePrefs).getBoolean("disable_dm_verity_verification", false)
        val force32Bit = prefs(ModulePrefs).getBoolean("force_enable_32_bit_support", false)
        val gaussianLevel = prefs(ModulePrefs).getString("customized_gaussian_blur_effect_level", "-1")
        val memcFrameInsertion = prefs(ModulePrefs).getBoolean("enable_video_memc_frame_insertion", false)

        "android.os.SystemProperties".toClass().apply {
            method {
                name = "get"
                param(StringClass, StringClass)
                returnType = StringClass
            }.hook {
                after {
                    when (args().first().string()) {
                        "persist.oplus.display.vrr.adfr" -> {
                            if (ltpoMode == "1") {
                                YLog.debug("adfr -> " + result.toString())
                                result = "2"
                            } else if (ltpoMode == "2") {
                                YLog.debug("adfr -> " + result.toString())
                                result = "0"
                            }
                        }

                        "persist.oplus.display.vrr" -> {
                            if (ltpoMode == "1") {
                                YLog.debug("vrr -> " + result.toString())
                                result = "1"
                            } else if (ltpoMode == "2") {
                                YLog.debug("vrr -> " + result.toString())
                                result = "0"
                            }
                        }

                        "ro.boot.veritymode" -> {
                            if (disableDmVerity) result = "enforcing"
                        }

                        "ro.boot.vbmeta.device_state" -> {
                            if (disableDmVerity) result = "locked"
                        }

                        "persist.sys.oplus_support_app32_status" -> {
                            if (force32Bit) result = "1"
                        }

                        "ro.vendor.oplus.app32_boost_support" -> {
                            if (force32Bit) result = "1"
                        }

                        "ro.oplus.gaussianlevel" -> {
                            if (gaussianLevel in listOf("0", "1", "2", "3")) result = gaussianLevel
                        }

                        "ro.oplus.display.memc_video_refreshrate" -> {
                            if (memcFrameInsertion) result = "true"
                        }
                        "vendor.display.show_memc_tomast" -> {
                            if (memcFrameInsertion) result = "true"
                        }
                    }
                }
            }
            method {
                name = "getBoolean"
                param(StringClass, BooleanType)
                returnType = BooleanType
            }.hook {
                after {
                    when (args().first().string()) {
                        "ro.oplus.display.memc_video_refreshrate" -> {
                            if (memcFrameInsertion) result = true
                        }
                        "vendor.display.show_memc_tomast" -> {
                            if (memcFrameInsertion) result = true
                        }
                    }
                }
            }
        }
    }
}
