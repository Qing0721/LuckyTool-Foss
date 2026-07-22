package com.fosstool.app.hook.scope.oplusmms

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import org.json.JSONArray
import org.json.JSONObject

object RemoveMmsCardMarketingButton : YukiBaseHooker() {

    private val marketingActions = setOf(3, 4, 6, 12, 23)

    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findMethod {
                matcher {
                    returnType = JSONObject::class.java.name
                    usingStrings("buttonText", "entities", "actions")
                }
            }.apply {
                checkDataList("RemoveMmsCardMarketingButton", onlyOne = false)
                forEach { data ->
                    runCatching {
                        data.className.toClass().method {
                            name = data.methodName
                            returnType = JSONObject::class.java
                        }.hook {
                            after {
                                val json = result<JSONObject>() ?: return@after
                                filterMarketing(json)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun filterMarketing(jsonObject: JSONObject) {
        if (!jsonObject.has("entities") || !jsonObject.has("msgId") || !jsonObject.has("date")) return
        var entities = jsonObject.optJSONArray("entities") ?: return
        if (entities.length() == 0) return
        val len = entities.length()
        for (i in 0 until len) {
            val entity = entities.optJSONObject(i) ?: continue
            if (!entity.has("actions")) continue
            var actions = entity.optJSONArray("actions") ?: JSONArray()
            if (actions.length() > 0) {
                val filtered = JSONArray()
                val n = actions.length()
                for (j in 0 until n) {
                    val action = actions.optJSONObject(j) ?: continue
                    val type = action.optInt("action", -1)
                    if (!marketingActions.contains(type)) filtered.put(action)
                }
                entity.put("actions", filtered)
            }
        }
    }
}
