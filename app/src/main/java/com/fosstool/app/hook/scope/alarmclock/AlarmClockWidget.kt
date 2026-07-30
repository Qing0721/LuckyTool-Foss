package com.fosstool.app.hook.scope.alarmclock

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.widget.RemoteViews
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.HandlerClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

object AlarmClockWidget : YukiBaseHooker() {

    private lateinit var redMode: String

    override fun onHook() {
        redMode = prefs(ModulePrefs).getString("alarmclock_widget_redone_mode", "0")
        dataChannel.wait<String>("alarmclock_widget_redone_mode") { redMode = it }

        val clazz = "com.coloros.widget.smallweather.OnePlusWidget".toClassOrNull(appClassLoader)
            ?: return
        val has12 = clazz.declaredMethods.any {
            it.parameterCount == 2 &&
                it.parameterTypes[0] == String::class.java &&
                it.parameterTypes[1] == String::class.java &&
                CharSequence::class.java.isAssignableFrom(it.returnType)
        }
        val has13 = clazz.declaredMethods.any { it.returnType == RemoteViews::class.java }
        val base = "com.coloros.widget.smallweather.BaseClockWidget".toClassOrNull(appClassLoader)

        when {
            has12 -> loadHooker(AlarmClock12)
            has13 -> loadHooker(AlarmClock13)
            base != null -> loadHooker(AlarmClock131)
            else -> loadHooker(AlarmClock145)
        }
    }

    private object AlarmClock131 : YukiBaseHooker() {
        override fun onHook() {
            "com.coloros.widget.smallweather.BaseClockWidget".toClassOrNull(appClassLoader)
                ?.let { hookRemoteViewsMethods(it) }
        }
    }

    private object AlarmClock145 : YukiBaseHooker() {
        override fun onHook() {
            DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
                dexKitBridge.findClass {
                    matcher {

                        fields {
                            addForType("java.lang.Class")
                            addForType(ContextClass.name)
                            addForType("android.graphics.Bitmap")
                            addForType(BooleanType.name)
                            addForType(IntType.name)
                        }

                        methods {
                            add {
                                paramCount(0)
                                returnType(IntType.name)
                            }
                            add {
                                paramTypes(
                                    RemoteViews::class.java.name, IntType.name, StringClass.name,
                                )
                                usingStrings("setTimeZone")
                            }
                            add {
                                paramTypes(
                                    RemoteViews::class.java.name, BooleanType.name, BooleanType.name,
                                )
                            }
                            add {
                                paramTypes(
                                    RemoteViews::class.java.name, IntType.name, "java.lang.CharSequence",
                                )
                                usingStrings("setFormat24Hour", "setFormat12Hour")
                            }
                            add {
                                paramTypes(RemoteViews::class.java.name)
                                usingStrings("com.oplus.widget.smallweather.REFRESH_CLICK")
                            }
                        }
                    }
                }.apply {
                    checkDataList("AlarmClock145")
                    val cls = (firstOrNullSafe()?.name ?: return@apply)
                        .toClassOrNull(appClassLoader) ?: return@apply
                    hookRemoteViewsMethods(cls)
                }
            }
        }
    }

    private fun hookRemoteViewsMethods(clazz: Class<*>) {
        clazz.declaredMethods
            .filter { it.parameterCount == 0 && it.returnType == RemoteViews::class.java }
            .forEach { m ->
                runCatching {
                    XposedBridge.hookMethod(m, object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            if (redMode == "0") return
                            val remoteViews = param.result as? RemoteViews ?: return
                            val context = resolveContext(param.thisObject) ?: return
                            val layoutName = runCatching {
                                context.resources.getResourceEntryName(remoteViews.layoutId)
                            }.getOrNull() ?: return
                            val resId = getReplaceLayout(context, layoutName, redMode) ?: return
                            param.result = RemoteViews(context.packageName, resId)
                        }
                    })
                }
            }
    }

    private fun resolveContext(host: Any?): Context? {
        if (host is Context) return host
        runCatching {
            var cursor: Class<*>? = host?.javaClass
            while (cursor != null && cursor != Any::class.java) {
                val owner: Class<*> = cursor
                val field = owner.declaredFields.firstOrNull {
                    Context::class.java.isAssignableFrom(it.type)
                }
                if (field != null) {
                    field.isAccessible = true
                    return field.get(host) as? Context
                }
                cursor = owner.superclass
            }
        }
        return runCatching {
            XposedHelpers.callStaticMethod(
                Class.forName("android.app.AndroidAppHelper"),
                "currentApplication",
            ) as? Context
        }.getOrNull()
    }

    @SuppressLint("DiscouragedApi")
    private fun getReplaceLayout(context: Context, layoutName: String, redMode: String): Int? {
        val curRedMode = layoutName.contains("red")
        val entryName = when (redMode) {
            "1" -> if (curRedMode) null else getRedLayoutRes(layoutName)
            "2" -> if (curRedMode) getNonRedLayoutRes(layoutName) else null
            else -> null
        } ?: return null
        val resId = context.resources.getIdentifier(entryName, "layout", context.packageName)
        return resId.takeIf { it != 0 }
    }

    private fun getNonRedLayoutRes(layoutName: String?): String? {
        return when (layoutName) {
            "op_double_clock_red_widget_land_view" -> "op_double_clock_widget_land_view"
            "op_double_clock_red_widget_view" -> "op_double_clock_widget_view"
            "one_plus_red_widget_land_view" -> "one_plus_widget_land_view"
            "one_plus_red_widget_view" -> "one_plus_widget_view"
            "table_op_double_clock_red_widget_land_view" -> "table_op_double_clock_widget_land_view"
            "table_op_double_clock_red_widget_view" -> "table_op_double_clock_widget_view"
            "table_one_plus_red_widget_land_view" -> "table_one_plus_widget_land_view"
            "table_one_plus_red_widget_view" -> "table_one_plus_widget_view"
            "hor_double_clock_red_widget_land_view_t" -> "hor_double_clock_widget_land_view_t"
            "hor_double_clock_red_widget_view_t" -> "hor_double_clock_widget_view_t"
            "hor_single_clock_red_widget_land_view_t" -> "hor_single_clock_widget_land_view_t"
            "hor_single_clock_red_widget_view_t" -> "hor_single_clock_widget_view_t"
            "table_hor_double_clock_red_widget_land_view_t" -> "table_hor_double_clock_widget_land_view_t"
            "table_hor_double_clock_red_widget_view_t" -> "table_hor_double_clock_widget_view_t"
            "table_hor_single_clock_red_widget_land_view_t" -> "table_hor_single_clock_widget_land_view_t"
            "table_hor_single_clock_red_widget_view_t" -> "table_hor_single_clock_widget_view_t"
            "one_line_double_clock_red_widget_land_view_t" -> "one_line_double_clock_widget_land_view_t"
            "one_line_double_clock_red_widget_view_t" -> "one_line_double_clock_widget_view_t"
            "one_line_hor_single_clock_red_widget_land_view_t" -> "one_line_hor_single_clock_widget_land_view_t"
            "one_line_hor_single_clock_red_widget_view_t" -> "one_line_hor_single_clock_widget_view_t"
            "table_one_line_double_clock_red_widget_land_view_t" -> "table_one_line_double_clock_widget_land_view_t"
            "table_one_line_double_clock_red_widget_view_t" -> "table_one_line_double_clock_widget_view_t"
            "table_one_line_hor_single_clock_red_widget_land_view_t" -> "table_one_line_hor_single_clock_widget_land_view_t"
            "table_one_line_hor_single_clock_red_widget_view_t" -> "table_one_line_hor_single_clock_widget_view_t"
            "vertical_double_clock_red_widget_land_view_t" -> "vertical_double_clock_widget_land_view_t"
            "vertical_double_clock_red_widget_view_t" -> "vertical_double_clock_widget_view_t"
            "vertical_single_clock_red_widget_land_view_t" -> "vertical_single_clock_widget_land_view_t"
            "vertical_single_clock_red_widget_view_t" -> "vertical_single_clock_widget_view_t"
            "table_vertical_double_clock_red_widget_land_view_t" -> "table_vertical_double_clock_widget_land_view_t"
            "table_vertical_double_clock_red_widget_view_t" -> "table_vertical_double_clock_widget_view_t"
            "table_vertical_single_clock_red_widget_land_view_t" -> "table_vertical_single_clock_widget_land_view_t"
            "vertical_multi_clock_red_widget_view_t" -> "vertical_multi_clock_widget_view_t"
            "table_vertical_multi_clock_red_widget_view_t" -> "table_vertical_multi_clock_widget_view_t"
            else -> null
        }
    }

    private fun getRedLayoutRes(layoutName: String?): String? {
        return when (layoutName) {
            "op_double_clock_widget_land_view" -> "op_double_clock_red_widget_land_view"
            "op_double_clock_widget_view" -> "op_double_clock_red_widget_view"
            "one_plus_widget_land_view" -> "one_plus_red_widget_land_view"
            "one_plus_widget_view" -> "one_plus_red_widget_view"
            "table_op_double_clock_widget_land_view" -> "table_op_double_clock_red_widget_land_view"
            "table_op_double_clock_widget_view" -> "table_op_double_clock_red_widget_view"
            "table_one_plus_widget_land_view" -> "table_one_plus_red_widget_land_view"
            "table_one_plus_widget_view" -> "table_one_plus_red_widget_view"
            "hor_double_clock_widget_land_view_t" -> "hor_double_clock_red_widget_land_view_t"
            "hor_double_clock_widget_view_t" -> "hor_double_clock_red_widget_view_t"
            "hor_single_clock_widget_land_view_t" -> "hor_single_clock_red_widget_land_view_t"
            "hor_single_clock_widget_view_t" -> "hor_single_clock_red_widget_view_t"
            "table_hor_double_clock_widget_land_view_t" -> "table_hor_double_clock_red_widget_land_view_t"
            "table_hor_double_clock_widget_view_t" -> "table_hor_double_clock_red_widget_view_t"
            "table_hor_single_clock_widget_land_view_t" -> "table_hor_single_clock_red_widget_land_view_t"
            "table_hor_single_clock_widget_view_t" -> "table_hor_single_clock_red_widget_view_t"
            "one_line_double_clock_widget_land_view_t" -> "one_line_double_clock_red_widget_land_view_t"
            "one_line_double_clock_widget_view_t" -> "one_line_double_clock_red_widget_view_t"
            "one_line_hor_single_clock_widget_land_view_t" -> "one_line_hor_single_clock_red_widget_land_view_t"
            "one_line_hor_single_clock_widget_view_t" -> "one_line_hor_single_clock_red_widget_view_t"
            "table_one_line_double_clock_widget_land_view_t" -> "table_one_line_double_clock_red_widget_land_view_t"
            "table_one_line_double_clock_widget_view_t" -> "table_one_line_double_clock_red_widget_view_t"
            "table_one_line_hor_single_clock_widget_land_view_t" -> "table_one_line_hor_single_clock_red_widget_land_view_t"
            "table_one_line_hor_single_clock_widget_view_t" -> "table_one_line_hor_single_clock_red_widget_view_t"
            "vertical_double_clock_widget_land_view_t" -> "vertical_double_clock_red_widget_land_view_t"
            "vertical_double_clock_widget_view_t" -> "vertical_double_clock_red_widget_view_t"
            "vertical_single_clock_widget_land_view_t" -> "vertical_single_clock_red_widget_land_view_t"
            "vertical_single_clock_widget_view_t" -> "vertical_single_clock_red_widget_view_t"
            "table_vertical_double_clock_widget_land_view_t" -> "table_vertical_double_clock_red_widget_land_view_t"
            "table_vertical_double_clock_widget_view_t" -> "table_vertical_double_clock_red_widget_view_t"
            "table_vertical_single_clock_widget_land_view_t" -> "table_vertical_single_clock_red_widget_land_view_t"
            "vertical_multi_clock_widget_view_t" -> "vertical_multi_clock_red_widget_view_t"
            "table_vertical_multi_clock_widget_view_t" -> "table_vertical_multi_clock_red_widget_view_t"
            else -> null
        }
    }

    private object AlarmClock13 : YukiBaseHooker() {
        override fun onHook() {
            DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
                dexKitBridge.findClass {

                    matcher {
                        fields {
                            addForType(BooleanType.name)
                            addForType(HandlerClass.name)
                        }
                        methods {
                            add { returnType(BooleanType.name) }
                            add { returnType(HandlerClass.name) }
                            add { paramTypes(ContextClass.name) }
                            add { paramTypes(ContextClass.name, StringClass.name) }
                            add {
                                paramTypes(
                                    ContextClass.name, StringClass.name, StringClass.name
                                )
                            }
                        }
                    }
                }.apply {
                    checkDataList("AlarmClock13")
                    val clazz = (firstOrNullSafe()?.name ?: return@apply)
                        .toClassOrNull(appClassLoader) ?: return@apply
                    hookRedOnClass(clazz)
                }
            }
        }
    }

    private fun hookRedOnClass(clazz: Class<*>) {
        clazz.declaredMethods
            .filter {
                it.parameterCount in 2..3 &&
                    Context::class.java.isAssignableFrom(it.parameterTypes[0]) &&
                    it.parameterTypes.getOrNull(1) == String::class.java &&
                    (CharSequence::class.java.isAssignableFrom(it.returnType) ||
                        it.returnType == String::class.java ||
                        it.returnType.name == "java.lang.CharSequence")
            }
            .forEach { m ->
                runCatching {
                    XposedBridge.hookMethod(m, object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            if (redMode == "0") return
                            val result = param.result as? CharSequence ?: return
                            param.result = when (redMode) {
                                "1" -> setCharRedOne(result)
                                "2" -> result.toString()
                                else -> result
                            }
                        }
                    })
                }
            }
    }

    private object AlarmClock12 : YukiBaseHooker() {
        override fun onHook() {

            val clazz = "com.coloros.widget.smallweather.OnePlusWidget".toClassOrNull(appClassLoader) ?: return
            val method = clazz.declaredMethods.firstOrNull {
                it.parameterCount == 2 &&
                    it.parameterTypes[0] == String::class.java &&
                    it.parameterTypes[1] == String::class.java &&
                    CharSequence::class.java.isAssignableFrom(it.returnType)
            } ?: return
            XposedBridge.hookMethod(method, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (redMode == "0") return
                    val result = param.result as? CharSequence ?: return
                    param.result = when (redMode) {
                        "1" -> setCharRedOne(result)
                        "2" -> result.toString()
                        else -> result
                    }
                }
            })
        }
    }

    private fun setCharRedOne(format: CharSequence): CharSequence {
        val sp = SpannableStringBuilder(format)
        val length = if (format.contains(":")) format.toString().substringBefore(":").length
        else if (format.contains("\u2236")) format.toString().substringBefore("\u2236").length
        else format.length
        for (i in 0 until length) {
            if (format[i].toString() == "1") {
                val colorRes = Color.parseColor("#c41442")
                sp.setSpan(ForegroundColorSpan(colorRes), i, i + 1, 34)
            }
        }
        return sp
    }
}
