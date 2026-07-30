package com.fosstool.app.hook.scope.systemui

import android.content.Context
import android.text.TextUtils
import android.util.LayoutDirection
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.hook.utils.sysui.LunarHelperUtils
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.SDK
import com.fosstool.app.utils.getScreenOrientation
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@Suppress("LocalVariableName", "DiscouragedApi")
object ControlCenterDateStyle : YukiBaseHooker() {
    override fun onHook() {
        var removeComma = prefs(ModulePrefs).getBoolean("remove_control_center_date_comma", false)
        dataChannel.wait<Boolean>("remove_control_center_date_comma") { removeComma = it }
        var showLunar =
            prefs(ModulePrefs).getBoolean("statusbar_control_center_date_show_lunar", false)
        dataChannel.wait<Boolean>("statusbar_control_center_date_show_lunar") { showLunar = it }
        var fixWidth =
            prefs(ModulePrefs).getBoolean("statusbar_control_center_date_fix_width", false)
        dataChannel.wait<Boolean>("statusbar_control_center_date_fix_width") { fixWidth = it }
        var disableTextScroll =
            prefs(ModulePrefs).getBoolean("statusbar_control_center_date_disable_text_scroll", false)
        dataChannel.wait<Boolean>("statusbar_control_center_date_disable_text_scroll") {
            disableTextScroll = it
        }

        var fixLunar =
            prefs(ModulePrefs).getString("statusbar_control_center_date_fix_lunar_horizontal", "0")
        dataChannel.wait<String>("statusbar_control_center_date_fix_lunar_horizontal") {
            fixLunar = it
        }
        var setDisplayModeHorizontal =
            prefs(ModulePrefs).getString("statusbar_control_center_date_set_display_mode_horizontal", "0")
        dataChannel.wait<String>("statusbar_control_center_date_set_display_mode_horizontal") {
            setDisplayModeHorizontal = it
        }

        VariousClass(
            "com.oplusos.systemui.qs.widget.OplusQSDateView",
            "com.oplus.systemui.qs.widget.OplusQSDateView"
        ).toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "updateClock"; emptyParam() }.ignored().hook {
                before {
                    if (!removeComma && !showLunar) return@before
                    val textView = instance as? TextView ?: return@before
                    val lastText = c.findField("mLastText")?.get(instance) as? String

                    val timeInfo = getLocalTimeInfo(textView.context)
                    if (timeInfo != null) {
                        val dateInfo = runCatching {
                            XposedHelpers.callMethod(timeInfo, "getDateInfo") as? String
                        }.getOrNull().orEmpty()
                        if (dateInfo != lastText && dateInfo.isNotBlank()) textView.text = dateInfo
                    } else {
                        val formatterField = c.findFieldOfType(DateTimeFormatter::class.java)
                        var formatter = formatterField?.get(instance) as? DateTimeFormatter
                        if (formatter == null) {
                            val pattern = c.findField("mDatePattern")?.get(instance) as? String
                            formatter = runCatching {
                                DateTimeFormatter.ofPattern(pattern, Locale.getDefault())
                            }.getOrNull()
                            runCatching { formatterField?.set(instance, formatter) }
                        }
                        val now = formatter?.let {
                            runCatching { LocalDateTime.now().format(it) }.getOrNull()
                        }
                        if (now != null && now != lastText) textView.text = now
                    }

                    val current = textView.text?.toString().orEmpty()
                    if (current.isNotBlank()) {
                        var text = current
                        if (removeComma) text = text.replace("，", " ")
                        if (showLunar) {
                            val lunar = getLunarSuffix(textView.context)
                            if (lunar.isNotBlank()) text = "$text $lunar"
                        }
                        textView.text = text
                        runCatching { c.findField("mLastText")?.set(instance, text) }
                    }
                    resultNull()
                }
            }
        }

        VariousClass(
            "com.oplusos.systemui.keyguard.clock.WeatherInfoParseHelper",
            "com.oplus.systemui.keyguard.clock.WeatherInfoParseHelper"
        ).toClassOrNull(appClassLoader)
            ?.method { name = "getChineseDateInfo"; paramCount = 2 }?.ignored()?.hook {
                after {
                    if (removeComma) result = (result as? String)?.replace("，", " ")
                    if (showLunar) {
                        val context = args.getOrNull(1) as? Context ?: return@after
                        val lunarInstance = LunarHelperUtils(appClassLoader).buildInstance(context)
                        val lunarDate = LunarHelperUtils(appClassLoader).getDateToString(
                            lunarInstance, System.currentTimeMillis()
                        ).let {
                            if ((it.isNullOrBlank()) || (it.length < 8)) ""
                            else " " + it.substring(4, it.length)
                        }
                        result = (result as? String).orEmpty() + lunarDate
                    }
                }
            }

        if (SDK < A13) return
        var translationX = 0
        VariousClass(
            "com.oplusos.systemui.qs.OplusQSFooterImpl",
            "com.oplus.systemui.qs.OplusQSFooterImpl"
        ).toClassOrNull(appClassLoader)?.let { c ->
            if (runCatching { c.getDeclaredMethod("updateQsDateView") }.isFailure) return@let
            c.method { name = "updateQsDateView" }.ignored().hook {
                after {
                    val mTmpConstraintSet =
                        c.findField("mTmpConstraintSet")?.get(instance) ?: return@after
                    val mClockView =
                        c.findField("mClockView")?.get(instance) as? TextView
                            ?: return@after
                    val mQsDateView =
                        c.findField("mQsDateView")?.get(instance) as? TextView
                            ?: return@after

                    if (fixWidth || disableTextScroll) {
                        runCatching {
                            XposedHelpers.callMethod(
                                mTmpConstraintSet,
                                "constrainWidth",
                                mQsDateView.id,
                                ConstraintLayout.LayoutParams.WRAP_CONTENT
                            )
                        }
                    }

                    val horizontalMode =
                        if (fixLunar != "0") fixLunar else setDisplayModeHorizontal
                    if (showLunar && (horizontalMode != "0")) {
                        val res = (instance as? ViewGroup)?.resources ?: return@after
                        val qs_footer_date_width = res.getDimensionPixelSize(
                            res.getIdentifier(
                                "qs_footer_date_width", "dimen",
                                ControlCenterDateStyle.packageName
                            )
                        )
                        val qs_footer_date_margin_start = res.getDimensionPixelSize(
                            res.getIdentifier(
                                "qs_footer_date_margin_start", "dimen",
                                ControlCenterDateStyle.packageName
                            )
                        )
                        val qs_footer_date_expand_translation_y = res.getDimensionPixelSize(
                            res.getIdentifier(
                                "qs_footer_date_expand_translation_y", "dimen",
                                ControlCenterDateStyle.packageName
                            )
                        )
                        val isRtl =
                            TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == LayoutDirection.RTL
                        val width = mClockView.width + qs_footer_date_margin_start
                        if (abs(translationX) < abs(width)) translationX =
                            if (!isRtl) (-width) else width
                        val translationY = qs_footer_date_expand_translation_y / 2

                        getScreenOrientation(res) {
                            if (it) return@getScreenOrientation
                            if (translationX == 0 || translationY == 0) return@getScreenOrientation

                            when (horizontalMode) {
                                "1" -> runCatching {
                                    XposedHelpers.callMethod(
                                        mTmpConstraintSet,
                                        "constrainWidth",
                                        mQsDateView.id,
                                        qs_footer_date_width * 2
                                    )
                                }

                                "2" -> {
                                    runCatching {
                                        XposedHelpers.callMethod(
                                            mTmpConstraintSet,
                                            "setTranslationX",
                                            mQsDateView.id,
                                            translationX.toFloat()
                                        )
                                    }
                                    runCatching {
                                        XposedHelpers.callMethod(
                                            mTmpConstraintSet,
                                            "setTranslationY",
                                            mQsDateView.id,
                                            translationY.toFloat()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getLocalTimeInfo(context: Context): Any? = runCatching {
        val helper = VariousClass(
            "com.oplusos.systemui.keyguard.clock.WeatherInfoParseHelper",
            "com.oplus.systemui.keyguard.clock.WeatherInfoParseHelper"
        ).toClassOrNull(appClassLoader) ?: return null
        val holder = "${helper.name}\$HolderInnerClass".toClassOrNull(appClassLoader) ?: return null
        val single = holder.declaredFields.firstOrNull { it.type == helper }
            ?.also { it.isAccessible = true }?.get(null) ?: return null
        helper.getDeclaredMethod("getLocalTimeInfo", Context::class.java)
            .also { it.isAccessible = true }.invoke(single, context)
    }.getOrNull()

    private var lunarInstance: Any? = null

    private fun getLunarSuffix(context: Context): String = runCatching {
        val helper = LunarHelperUtils(appClassLoader)
        if (lunarInstance == null) lunarInstance = helper.buildInstance(context)
        helper.getDateToString(lunarInstance, System.currentTimeMillis()).let {
            if (it.isNullOrBlank() || it.length < 8) "" else it.substring(4)
        }
    }.getOrDefault("")

    private fun Class<*>.findField(name: String): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            runCatching { return cls.getDeclaredField(name).also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }

    private fun Class<*>.findFieldOfType(type: Class<*>): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            cls.declaredFields.firstOrNull { it.type == type }
                ?.let { return it.also { f -> f.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }
}
