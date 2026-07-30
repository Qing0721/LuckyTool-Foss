package com.fosstool.app.hook.scope.settings

import android.util.ArrayMap
import android.util.ArraySet
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.MapClass
import org.json.JSONObject
import java.io.InputStream
import java.io.Reader
import java.util.concurrent.atomic.AtomicBoolean

object DarkModeList : YukiBaseHooker() {

    private data class DarkModeInfo(val packName: String, val curType: Int)

    private fun parseInfo(raw: String): DarkModeInfo? = runCatching {
        val text = raw.trim()
        when {
            text.isBlank() -> null
            text.startsWith("{") -> {
                val json = JSONObject(text)
                val pkg: String = json.optString("packName") ?: ""
                if (pkg.isBlank()) null else DarkModeInfo(pkg, json.optInt("curType", 0))
            }
            text.contains("|") -> {
                val arr = text.split("|")
                val pkg = arr[0]
                if (pkg.isBlank()) null
                else DarkModeInfo(pkg, arr.getOrNull(1)?.trim()?.toIntOrNull() ?: 0)
            }
            else -> DarkModeInfo(text, 0)
        }
    }.getOrNull()

    override fun onHook() {
        var isEnable = prefs(ModulePrefs).getBoolean("dark_mode_list_enable", false)
        dataChannel.wait<Boolean>("dark_mode_list_enable") { isEnable = it }

        val supportList = ArraySet<DarkModeInfo>()
        fun reload(source: Set<String>) {
            supportList.clear()
            source.forEach { raw -> parseInfo(raw)?.let { supportList.add(it) } }
        }
        reload(prefs(ModulePrefs).getStringSet("dark_mode_support_list", ArraySet()))
        dataChannel.wait<Set<String>>("dark_mode_support_list") { reload(it) }

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(Any::class.java.name)
                        addForType(AtomicBoolean::class.java.name)
                        addForType(Map::class.java.name)
                    }
                    methods {
                        add { paramTypes(Reader::class.java.name) }
                        add { paramTypes(InputStream::class.java.name) }
                    }
                    usingStrings("DarkModeFileUtils")
                }
            }.apply {
                checkDataList("DarkModeList")
                val outer = (firstOrNullSafe()?.name ?: return@apply).toClassOrNull(appClassLoader)
                    ?: return@apply
                val darkModeData = outer.classes.firstOrNull()?.name
                    ?.toClassOrNull(appClassLoader) ?: return@apply
                outer.method { param(ContextClass, MapClass) }.ignored().hook {
                    after {
                        if (!isEnable) return@after
                        val dataMap = ArrayMap<String, Any>()
                        supportList.forEach { info ->
                            val data = runCatching {
                                if (info.curType == 0) {
                                    darkModeData.getDeclaredConstructor().newInstance()
                                } else {
                                    darkModeData.declaredConstructors
                                        .firstOrNull { it.parameterCount == 4 }
                                        ?.also { it.isAccessible = true }
                                        ?.newInstance(0L, 0, info.curType, 0)
                                }
                            }.getOrNull() ?: return@forEach
                            dataMap[info.packName] = data
                        }

                        runCatching {
                            outer.field { type = MapClass }.ignored().get().set(dataMap.toMap())
                        }
                    }
                }
            }
        }
    }
}
