package com.fosstool.app.hook.scope.packageinstaller

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.HookParam
import org.luckypray.dexkit.query.enums.StringMatchType
import java.lang.ref.WeakReference

object RemoveInstallAds : YukiBaseHooker() {

    private const val PROGRESS = "com.android.packageinstaller.oplus.InstallAppProgress"

    private var cached: WeakReference<Activity>? = null

    override fun onHook() {
        var initViewHooked = false

        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            val classes = dexKitBridge.findClass {
                matcher { className(PROGRESS, StringMatchType.StartsWith) }
            }.checkDataList("RemoveInstallAds find InstallAppProgress", onlyOne = false)
            if (classes.isEmpty()) return@create

            dexKitBridge.findMethod {
                searchInClass(classes)
                matcher {
                    paramCount(0)
                    returnType("void")
                    addCaller { name("onCreate") }
                    usingStrings("source_info", "type_channel_title", "type_channel_tips")
                }
            }.apply {
                checkDataList("RemoveInstallAds find initView")
                val target = firstOrNullSafe() ?: return@apply
                val owner = target.className.toClassOrNull(appClassLoader) ?: return@apply
                owner.method { name = target.methodName; emptyParam() }
                    .ignored()
                    .hook { after { cacheAndHide() } }
                initViewHooked = true
            }

            dexKitBridge.findMethod {
                searchInClass(classes)
                matcher {
                    name("handleMessage")
                    usingStrings(
                        "oplus.intent.action.VIRUS_APK_INSTALLED",
                        "oplus.permission.OPLUS_COMPONENT_SAFE",
                    )
                }
            }.apply {
                checkDataList("RemoveInstallAds find handleMessage")
                val target = firstOrNullSafe() ?: return@apply
                target.className.toClassOrNull(appClassLoader)
                    ?.method { name = target.methodName; paramCount = target.paramTypeNames.size }
                    ?.ignored()
                    ?.hook {
                        after { cached?.get()?.hideAds() }
                    }
            }
        }

        if (!initViewHooked) {
            val progress = PROGRESS.toClassOrNull(appClassLoader)
            if (progress == null) {
                YLog.error("RemoveInstallAds -> $PROGRESS not found", tag = "LuckyTool")
            } else {
                progress.method { name = "initView"; emptyParam() }.ignored().hook {
                    after { cacheAndHide() }
                }
            }
        }
    }

    private fun HookParam.cacheAndHide() {
        (instance as? Activity)?.let {
            cached = WeakReference(it)
            it.hideAds()
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun Activity.hideAds() {
        runCatching {
            listOf("suggest_A_scroll_layout", "install_done_suggest_B").forEach { idName ->
                val id = resources.getIdentifier(idName, "id", packageName)
                if (id != 0) findViewById<View>(id)?.visibility = View.GONE
            }
        }
    }
}
