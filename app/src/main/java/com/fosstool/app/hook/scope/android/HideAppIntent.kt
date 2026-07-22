package com.fosstool.app.hook.scope.android

import android.content.Intent
import android.content.pm.ResolveInfo
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.AppIntentInfo
import com.fosstool.app.utils.IntentAppUpdate
import com.fosstool.app.utils.IntentPrefs
import com.fosstool.app.utils.IntentType
import com.fosstool.app.utils.getOSVersionCode
import com.fosstool.app.utils.safeOf

object HideAppIntent : YukiBaseHooker() {

    private const val KEY_CONFIG_LIST = "custom_config_app_intent_list"
    private const val KEY_ENABLED_LIST = "enable_app_hide_list"
    private const val KEY_UPDATE_APP_CONFIG = "custom_config_app_intent_list_update_app_config"
    private const val KEY_UPDATE_APPS = "custom_config_app_intent_list_update_apps"
    private const val EXTRA_RESULT_ORIGIN_DATA = "result_origin_data"

    @Volatile
    private var masterEnabled: Boolean = false

    @Volatile
    private var configMap: Map<String, List<AppIntentInfo>> = emptyMap()

    @Volatile
    private var enabledSet: Set<String> = emptySet()

    override fun onHook() {
        refreshCache()

        dataChannel.wait<Boolean>(KEY_CONFIG_LIST) { masterEnabled = it }
        dataChannel.wait<String>(KEY_UPDATE_APP_CONFIG) { pkg -> refreshPackageConfig(pkg) }
        dataChannel.wait<IntentAppUpdate>(KEY_UPDATE_APPS) { update ->
            val current = enabledSet.toMutableSet()
            if (update.enabled) current.add(update.packageName) else current.remove(update.packageName)
            enabledSet = current
        }

        val useIPackageManagerBase = getOSVersionCode >= 26
        val targetClass = if (useIPackageManagerBase) {
            "com.android.server.pm.IPackageManagerBase"
        } else {
            "com.android.server.pm.PackageManagerService"
        }
        targetClass.toClass().apply {
            if (useIPackageManagerBase) {
                method {
                    name = "queryIntentActivities"
                    param(Intent::class.java, StringClass, LongType, IntType)
                }.hook {
                    after {
                        val intent = args().first().cast<Intent>() ?: return@after
                        val slice = result ?: return@after
                        filterResult(intent, slice)
                    }
                }
            } else {
                method {
                    name = "queryIntentActivities"
                    param(Intent::class.java, StringClass, IntType, IntType)
                }.hook {
                    after {
                        val intent = args().first().cast<Intent>() ?: return@after
                        val slice = result ?: return@after
                        filterResult(intent, slice)
                    }
                }
            }
        }
    }

    private fun filterResult(intent: Intent, slice: Any?) {
        if (!masterEnabled) return
        val action = intent.action ?: return
        if (intent.getBooleanExtra(EXTRA_RESULT_ORIGIN_DATA, false)) return
        if (slice == null) return
        val list = runCatching {
            slice.javaClass.getMethod("getList").invoke(slice) as? java.util.List<*>
        }.getOrNull() ?: return
        for (type in IntentType.values()) {
            if (type == IntentType.UNKNOWN) continue
            val matchedConfigs = configMap.values.flatten().filter { it.type == type }
            if (matchedConfigs.isEmpty()) continue
            val toRemove = mutableListOf<Any?>()
            for (ri in list) {
                val info = (ri as? ResolveInfo)?.activityInfo ?: continue
                if (matchedConfigs.any { cfg ->
                    cfg.action == action &&
                        cfg.packName == info.packageName &&
                        cfg.activity == info.name
                }) {
                    toRemove.add(ri)
                }
            }
            if (toRemove.isNotEmpty()) {
                runCatching {
                    @Suppress("UNCHECKED_CAST")
                    (list as java.util.List<Any?>).removeAll(toRemove)
                }
            }
        }
    }

    private fun refreshCache() {
        masterEnabled = safeOf(false) {
            prefs(IntentPrefs).getBoolean(KEY_CONFIG_LIST, false)
        }
        enabledSet = safeOf(emptySet()) {
            prefs(IntentPrefs).getStringSet(KEY_ENABLED_LIST, emptySet()) ?: emptySet()
        }
        val byPkg: MutableMap<String, MutableList<AppIntentInfo>> = mutableMapOf()
        for (pkg in enabledSet) {
            val rawSet = safeOf<Set<String>>(emptySet()) {
                prefs(IntentPrefs).getStringSet(pkg, emptySet()) ?: emptySet()
            }
            val configs = rawSet.mapNotNull { AppIntentInfo.fromJson(it) }
            if (configs.isNotEmpty()) byPkg[pkg] = configs.toMutableList()
        }
        configMap = byPkg
        YLog.debug("[HideAppIntent] cache refreshed: master=$masterEnabled, " +
                "enabled=${enabledSet.size}, configs=${byPkg.values.sumOf { it.size }}")
    }

    private fun refreshPackageConfig(pkg: String) {
        val rawSet = safeOf<Set<String>>(emptySet()) {
            prefs(IntentPrefs).getStringSet(pkg, emptySet()) ?: emptySet()
        }
        val configs = rawSet.mapNotNull { AppIntentInfo.fromJson(it) }
        val newMap = configMap.toMutableMap()
        if (configs.isEmpty()) newMap.remove(pkg) else newMap[pkg] = configs
        configMap = newMap
    }
}
