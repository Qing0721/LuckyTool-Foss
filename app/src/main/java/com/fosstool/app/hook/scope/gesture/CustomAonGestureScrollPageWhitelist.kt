package com.fosstool.app.hook.scope.gesture

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.replaceSpace
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ArrayMapClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.ArrayListClass
import com.highcapable.yukihookapi.hook.type.java.FloatType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType

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
        val videoList =
            prefs(ModulePrefs).getString("custom_aon_gesture_video_whitelist", "")
                .ifBlank {
                    prefs(ModulePrefs).getString(
                        "custom_aon_gesture_video_whitelist_list",
                        "None"
                    )
                }

        val hasScroll = scrollList.isNotBlank() && scrollList != "None"
        val hasVideo = videoList.isNotBlank() && videoList != "None"
        if (!hasScroll && !hasVideo) return

        if (hasScroll) {
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
                    first().name.toClass().apply {
                        method { emptyParam(); returnType = ListClass }.hookAll {
                            after {
                                val field = result<List<String>>() ?: return@after
                                if (field.isEmpty()) return@after
                                result = field.toMutableList().apply {
                                    if (contains("com.ss.android.ugc.aweme") ||
                                        contains("com.smile.gifmaker")
                                    ) {
                                        mergePackages(scrollList)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        runCatching {
            "com.oplus.gesture.util.GestureUtil".toClass().apply {
                if (hasScroll) {
                    method { name = "getLocalAonAppListTurnPage" }.hook {
                        after {
                            val list = result<List<String>>() ?: return@after
                            result = list.toMutableList().apply { mergePackages(scrollList) }
                        }
                    }
                }
                if (hasVideo) {
                    method { name = "getLocalAonAppListPauseOrPlay" }.hook {
                        after {
                            val list = result<List<String>>() ?: return@after
                            result = list.toMutableList().apply { mergePackages(videoList) }
                        }
                    }
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
