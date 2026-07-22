package com.fosstool.app.hook.scope.packageinstaller

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.format.Formatter
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.safeOf
import com.fosstool.app.utils.safeOfNull
import java.io.File

object ShowMoreApkPackageInformation : YukiBaseHooker() {
    @SuppressLint("DiscouragedApi", "SetTextIsSelectable")
    @Suppress("DEPRECATION")
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("show_more_apk_package_information", false)) return

        "com.android.packageinstaller.oplus.view.ApkInfoView".toClass().apply {
            method { name = "loadApkInfo" }.hook {
                after {
                    val context = field { name = "mContext" }.get(instance).cast<Context>()
                        ?: return@after
                    val mAppVersion = field { name = "mAppVersion" }.get(instance).cast<TextView>()
                        ?: return@after

                    val apkInfo = args().first().any() ?: return@after
                    val sourceInfo = args().last().any() ?: return@after
                    val apkCur = apkInfo.current()
                    val srcCur = sourceInfo.current()

                    val packageName = safeOf("") { apkCur.field { name = "packageName" }.string() } ?: ""
                    val versionName = safeOf("") { apkCur.field { name = "versionName" }.string() } ?: ""
                    val versionCode = safeOf(0) { apkCur.field { name = "versionCode" }.int() } ?: 0
                    val apkPath = safeOf("") { apkCur.field { name = "apkPath" }.string() } ?: ""
                    val label = safeOf("") { apkCur.field { name = "label" }.string() } ?: ""
                    val apkSize = safeOf(-1L) { apkCur.field { name = "size" }.long() } ?: -1L
                    val sourceName = safeOf("") { srcCur.field { name = "sourceName" }.string() } ?: ""
                    val sourcePackage =
                        safeOf("") { srcCur.field { name = "sourcePackage" }.string() } ?: ""
                    val actionType = safeOf(-1) { srcCur.field { name = "actionType" }.int() } ?: -1

                    val isInstall = actionType == 0
                    val isUninstall = actionType == 1

                    val pm = context.packageManager
                    val archivePkgInfo: PackageInfo? =
                        if (apkPath.isEmpty()) null else safeOfNull {
                            if (Build.VERSION.SDK_INT >= 33)
                                pm.getPackageArchiveInfo(apkPath, PackageManager.PackageInfoFlags.of(0L))
                            else pm.getPackageArchiveInfo(apkPath, 0)
                        }
                    val installedPkgInfo: PackageInfo? =
                        if (packageName.isEmpty()) null else safeOfNull {
                            if (Build.VERSION.SDK_INT >= 33)
                                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L))
                            else pm.getPackageInfo(packageName, 0)
                        }
                    val isInstalled = installedPkgInfo != null

                    val archiveIcon: Drawable? = archivePkgInfo?.applicationInfo
                        ?.let { safeOfNull { it.loadIcon(pm) } }
                    val archiveMinSdk = archivePkgInfo?.applicationInfo?.minSdkVersion
                    val archiveTargetSdk = archivePkgInfo?.applicationInfo?.targetSdkVersion
                    val installedIcon: Drawable? = installedPkgInfo?.applicationInfo
                        ?.let { safeOfNull { it.loadIcon(pm) } }
                    val installedVersionName = installedPkgInfo?.versionName
                    val installedVersionCode: Long? = installedPkgInfo?.let {
                        safeOfNull {
                            if (Build.VERSION.SDK_INT >= 28) it.longVersionCode
                            else it.versionCode.toLong()
                        }
                    }
                    val installedMinSdk = installedPkgInfo?.applicationInfo?.minSdkVersion
                    val installedTargetSdk = installedPkgInfo?.applicationInfo?.targetSdkVersion
                    val installedSourceDir = installedPkgInfo?.applicationInfo?.sourceDir

                    val sourceDisplay = sourceName.ifEmpty { sourcePackage }

                    val sizeBytes = if (isUninstall && !installedSourceDir.isNullOrEmpty()) {
                        safeOf(apkSize) { File(installedSourceDir).length() } ?: apkSize
                    } else {
                        apkSize
                    }

                    val container = mAppVersion.parent as? LinearLayout
                    if (container == null) {
                        fallbackAppend(mAppVersion, context, packageName, versionName, versionCode,
                            apkPath, label, apkSize, sourceName, sourcePackage, isUninstall)
                        return@after
                    }

                    val root = safeOfNull {
                        buildRows(
                            context, isInstall, isUninstall, isInstalled,
                            archiveIcon, installedIcon,
                            label, packageName, sourceDisplay,
                            sizeBytes, versionName, versionCode,
                            installedVersionName, installedVersionCode,
                            archiveMinSdk, archiveTargetSdk,
                            installedMinSdk, installedTargetSdk
                        )
                    }
                    if (root == null) {
                        fallbackAppend(mAppVersion, context, packageName, versionName, versionCode,
                            apkPath, label, apkSize, sourceName, sourcePackage, isUninstall)
                        return@after
                    }
                    container.removeAllViews()
                    container.addView(root)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun buildRows(
        context: Context,
        isInstall: Boolean,
        isUninstall: Boolean,
        isInstalled: Boolean,
        archiveIcon: Drawable?,
        installedIcon: Drawable?,
        label: String,
        packageName: String,
        sourceDisplay: String,
        sizeBytes: Long,
        versionName: String,
        versionCode: Int,
        installedVersionName: String?,
        installedVersionCode: Long?,
        archiveMinSdk: Int?,
        archiveTargetSdk: Int?,
        installedMinSdk: Int?,
        installedTargetSdk: Int?
    ): LinearLayout {
        val dp = context.resources.displayMetrics.density
        val root = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        ImageView(context).apply {
            setImageDrawable(if (isInstall) archiveIcon else installedIcon)
            scaleType = ImageView.ScaleType.FIT_CENTER
            layoutParams = LinearLayout.LayoutParams((80 * dp).toInt(), (80 * dp).toInt()).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                bottomMargin = (8 * dp).toInt()
            }
        }.also { root.addView(it) }
        root.addView(textRow(context, label, 22f, Gravity.CENTER_HORIZONTAL))
        root.addView(textRow(context, packageName, null, Gravity.CENTER_HORIZONTAL))
        val sizeLabel = safeOf("Size: ") {
            val id = context.resources.getIdentifier("app_info_size", "string", context.packageName)
            if (id != 0) context.resources.getString(id) else "Size: "
        } ?: "Size: "
        val sizeStr = if (sizeBytes > 0) Formatter.formatFileSize(context, sizeBytes) else "0 B"
        root.addView(textRow(context, "$sizeLabel $sizeStr", null, Gravity.START))
        val versionText = if (isInstalled && !isUninstall) {
            "${installedVersionName ?: ""}(${installedVersionCode ?: 0})$versionName($versionCode)"
        } else {
            "$versionName($versionCode)"
        }
        root.addView(textRow(context, versionText, null, Gravity.START))
        if (isInstall) {
            val sdkText = if (isInstalled) {
                "Min SDK: ${installedMinSdk.str()} → ${archiveMinSdk.str()}" +
                    "  |  Target SDK: ${installedTargetSdk.str()} → ${archiveTargetSdk.str()}"
            } else {
                "Min SDK: ${archiveMinSdk.str()}  |  Target SDK: ${archiveTargetSdk.str()}"
            }
            root.addView(textRow(context, sdkText, null, Gravity.START))
        }
        if (isInstall) {
            val fromText = safeOf("From: $sourceDisplay") {
                val id = context.resources.getIdentifier("from_source", "string", context.packageName)
                if (id != 0) context.resources.getString(id, sourceDisplay) else "From: $sourceDisplay"
            } ?: "From: $sourceDisplay"
            root.addView(textRow(context, fromText, null, Gravity.START))
        }
        return root
    }

    private fun textRow(context: Context, text: String, sizeSp: Float?, gravity: Int): TextView =
        TextView(context).apply {
            setText(text)
            if (sizeSp != null) setTextSize(TypedValue.COMPLEX_UNIT_SP, sizeSp)
            setTextIsSelectable(true)
            this.gravity = gravity
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

    private fun Int?.str(): String = this?.toString() ?: "null"

    @Suppress("DEPRECATION")
    private fun fallbackAppend(
        mAppVersion: TextView,
        context: Context,
        packName: String,
        versionName: String,
        versionCode: Int,
        apkPath: String,
        label: String,
        size: Long,
        sourceName: String,
        sourcePackage: String,
        isUninstall: Boolean
    ) {
        mAppVersion.apply {
            (parent as? LinearLayout)?.orientation = LinearLayout.VERTICAL
            (layoutParams as? LinearLayout.LayoutParams)?.width =
                LinearLayout.LayoutParams.MATCH_PARENT
            isSingleLine = false
            setTextIsSelectable(true)
        }
        val versionStr = safeOf("Version: ") {
            context.resources.getString(
                context.resources.getIdentifier("app_info_version", "string", context.packageName)
            )
        } ?: "Version: "
        mAppVersion.text = buildString {
            append(packName)
            append("\n").append(versionStr).append(versionName).append("(").append(versionCode).append(")")
            if (label.isNotEmpty() && label != packName) append("\n").append("Label: ").append(label)
            if (apkPath.isNotEmpty()) append("\n").append("Path: ").append(apkPath)
            if (size > 0L) append("\n").append("Size: ").append(size).append(" B")
            if (!isUninstall) {
                if (sourceName.isNotEmpty()) append("\n").append("Source: ").append(sourceName)
                if (sourcePackage.isNotEmpty()) append("\n").append("SourcePkg: ").append(sourcePackage)
            }
        }.trimEnd()
    }
}
