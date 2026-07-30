package com.fosstool.app.hook.scope.gesture

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.replaceSpace
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ArrayMapClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.ArrayListClass
import com.highcapable.yukihookapi.hook.type.java.FloatType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

object CustomAonGestureScrollPageWhitelist : YukiBaseHooker() {
    override fun onHook() {
        val scrollList =
            prefs(ModulePrefs).getString("custom_aon_gesture_scroll_page_whitelist", "")
                .ifBlank {
                    prefs(ModulePrefs).getString(
                        "custom_aon_gesture_scroll_page_whitelist_list",
                        "None"
                    )
                }

        val hasScroll = scrollList.isNotBlank() && scrollList != "None"
        if (!hasScroll) return

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(ContextClass.name)
                        addForType(ArrayListClass.name)
                        addForType(ArrayMapClass.name)
                        addForType(IntType.name)
                        addForType(FloatType.name)
                        addForType(ListClass.name)
                    }
                    methods {
                        add { paramTypes(StringClass.name); returnType(IntType.name) }
                        add { paramTypes(ListClass.name); returnType(UnitType.name) }
                    }
                    usingStrings("com.ss.android.ugc.aweme", "com.smile.gifmaker")
                }
            }.apply {
                checkDataList("CustomAonGestureScrollPageWhitelist")
                (firstOrNullSafe()?.name ?: return@apply).toClassOrNull(appClassLoader)?.declaredMethods
                    ?.filter {
                        it.parameterCount == 0 &&
                            List::class.java.isAssignableFrom(it.returnType)
                    }
                    ?.forEach { m ->
                        runCatching {
                            XposedBridge.hookMethod(m, object : XC_MethodHook() {
                                override fun afterHookedMethod(param: MethodHookParam) {
                                    val field = param.result as? List<*> ?: return
                                    if (field.isEmpty()) return
                                    val mutable = field.filterIsInstance<String>().toMutableList()
                                    if (mutable.contains("com.ss.android.ugc.aweme") ||
                                        mutable.contains("com.smile.gifmaker")
                                    ) {
                                        mutable.mergePackages(scrollList)
                                        param.result = mutable
                                    }
                                }
                            })
                        }
                    }
            }
        }

        "com.oplus.gesture.util.GestureUtil".toClassOrNull(appClassLoader)?.let { util ->
            util.method { name = "getLocalAonAppListTurnPage" }.ignored().hook {
                after {
                    val list = result as? List<*> ?: return@after
                    result = list.filterIsInstance<String>().toMutableList()
                        .apply { mergePackages(scrollList) }
                }
            }
        }
    }

    private fun MutableList<String>.mergePackages(raw: String) {
        val listString = raw.replaceSpace
        if (listString.contains("\n")) {
            listString.split("\n").forEach { if (it.isNotBlank()) add(it) }
        } else {
            add(raw)
        }
    }
}
