package com.fosstool.app.hook.scope.android

import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.java.IntType

object RemoveAlwaysAllowAppStartList : YukiBaseHooker() {

    private const val CHANNEL_KEY = "remove_always_allow_app_start_list"
    private const val CLASS_SECURITY =
        "com.android.server.am.OplusSecurityPermissionManager"
    private const val CLASS_CONTROLLER =
        "com.android.server.am.OplusActivityStartController"
    private const val METHOD_ON_USER_REMOVED = "onUserRemoved"

    @Volatile
    private var activityStartController: Any? = null

    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("enable_always_allow_app_start_dialog", false)) return

        dataChannel.wait<ArrayList<Int>>(CHANNEL_KEY) { userIds ->
            onRemoveRequest(userIds)
        }

        runCatching {
            CLASS_SECURITY.toClass().method { name = "init" }.hook {
                after {
                    activityStartController = runCatching {
                        instance.current().field {
                            type = CLASS_CONTROLLER
                        }.any()
                    }.getOrNull() ?: runCatching {
                        instance.current().field {
                            name = "OplusActivityStartController"
                        }.any()
                    }.getOrNull()
                }
            }
        }.onFailure {
            YLog.error("RemoveAlwaysAllowAppStartList: hook init failed", it, tag = "LuckyTool")
        }
    }

    private fun onRemoveRequest(userIds: ArrayList<Int>?) {
        if (userIds.isNullOrEmpty()) return
        val controller = activityStartController
        if (controller == null) {
            YLog.debug(
                "RemoveAlwaysAllowAppStartList: controller null, skip userIds=$userIds",
                tag = "LuckyTool"
            )
            return
        }
        userIds.forEach { userId ->
            runCatching {
                controller.current().method {
                    name = METHOD_ON_USER_REMOVED
                    param(IntType)
                }.call(userId)
            }.onFailure {
                YLog.error(
                    "RemoveAlwaysAllowAppStartList: onUserRemoved($userId) failed",
                    it,
                    tag = "LuckyTool"
                )
            }
        }
        YLog.debug(
            "cleaning $userIds always start app list",
            tag = "LuckyTool"
        )
    }
}
