package com.fosstool.app.hook.scope.android

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.utils.ModulePrefs
import de.robv.android.xposed.XposedHelpers

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
            val cls = CLASS_SECURITY.toClassOrNull(appClassLoader) ?: return@runCatching
            cls.method { name = "init" }.ignored().hook {
                after {
                    activityStartController = runCatching {
                        cls.field { name = CLASS_CONTROLLER }.ignored().get(instance).any()
                    }.getOrNull()
                        ?: runCatching {
                            cls.declaredFields.firstOrNull {
                                it.type.name == CLASS_CONTROLLER
                            }?.let {
                                it.isAccessible = true
                                it.get(instance)
                            }
                        }.getOrNull()
                        ?: runCatching {
                            XposedHelpers.getObjectField(instance, "OplusActivityStartController")
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
                XposedHelpers.callMethod(controller, METHOD_ON_USER_REMOVED, userId)
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
