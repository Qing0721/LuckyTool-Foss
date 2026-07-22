package com.fosstool.app.hook.scope.packageinstaller

import android.annotation.SuppressLint
import android.content.Context
import android.widget.LinearLayout
import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.safeOf

object ShowPackageNameAndVersionCode : YukiBaseHooker() {
    @SuppressLint("DiscouragedApi")
    override fun onHook() {
        "com.android.packageinstaller.oplus.view.ApkInfoView".toClass().apply {
            method { name = "loadApkInfo" }.hook {
                after {
                    val context = field { name = "mContext" }.get(instance).cast<Context>()
                        ?: return@after
                    val mAppVersion = field { name = "mAppVersion" }.get(instance).cast<TextView>()
                        ?: return@after
                    mAppVersion.apply {
                        (parent as LinearLayout).orientation = LinearLayout.VERTICAL
                        (layoutParams as LinearLayout.LayoutParams).width =
                            LinearLayout.LayoutParams.MATCH_PARENT
                        isSingleLine = false
                        setTextIsSelectable(true)
                    }
                    val apkInfo = args().first().any() ?: return@after
                    val sourceInfo = args().last().any() ?: return@after
                    val actionType = sourceInfo.current().field { name = "actionType" }.int()
                    val packName = apkInfo.current().field { name = "packageName" }.string()
                    val versionName = apkInfo.current().field { name = "versionName" }.string()
                    val versionCode = apkInfo.current().field { name = "versionCode" }.int()
                    val packageUtilCls = "com.android.packageinstaller.oplus.utils.OPlusPackageUtil"
                    val oplusPackageUtil = packageUtilCls.toClass()
                    val curVersionName = oplusPackageUtil.method {
                        name = "getPackageVersion"
                    }.get().invoke<String>(context, packName)
                    val curVersionCode = oplusPackageUtil.method {
                        name = "getTargetAppVersionCode"
                    }.get().invoke<Int>(context, packName)
                    val isInstalled = oplusPackageUtil.method {
                        name = "isPackageInstalled"
                    }.get().invoke<Boolean>(context, packName)
                    val isInstall = actionType == 0
                    val isUninstall = actionType == 1
                    val versionStr = safeOf("Version: ") {
                        context.resources.getString(
                            context.resources.getIdentifier(
                                "app_info_version", "string", context.packageName
                            )
                        )
                    }
                    mAppVersion.text = if (isInstall) {
                        if (isInstalled == true)
                            """
                            $packName
                            $versionStr$curVersionName($curVersionCode)
                            ↓↓↓
                            $versionStr$versionName($versionCode)
                        """
.trimIndent()
                        else
                            """
                            $packName
                            $versionStr$versionName($versionCode)
                        """
.trimIndent()
                    } else if (isUninstall) {
                        """
                            $packName
                            $versionStr$versionName($versionCode)
                        """
.trimIndent()
                    } else mAppVersion.text
                }
            }
        }
    }
}
