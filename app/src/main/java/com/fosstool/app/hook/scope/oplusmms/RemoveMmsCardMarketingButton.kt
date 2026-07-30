package com.fosstool.app.hook.scope.oplusmms

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.type.java.StringClass
import org.json.JSONArray
import org.json.JSONObject

object RemoveMmsCardMarketingButton : YukiBaseHooker() {

    private val marketingActions = setOf(3, 4, 6, 12, 23)

    override fun onHook() {

        JSONObject::class.java.constructor { param(StringClass) }.hook {
            after {
                val json = instance as? JSONObject ?: return@after
                runCatching { filterMarketing(json) }
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
