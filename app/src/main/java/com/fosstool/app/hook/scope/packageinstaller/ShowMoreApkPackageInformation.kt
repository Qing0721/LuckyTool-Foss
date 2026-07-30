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
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.safeOf
import com.fosstool.app.utils.safeOfNull
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import java.io.File
import java.lang.reflect.Field
import java.util.WeakHashMap

object ShowMoreApkPackageInformation : YukiBaseHooker() {

    private const val APK_INFO_VIEW = "com.android.packageinstaller.oplus.view.ApkInfoView"
    private const val APK_INFO = "com.android.packageinstaller.oplus.common.ApkInfo"
    private const val SOURCE_INFO = "com.android.packageinstaller.oplus.common.SourceInfo"

    private val apkInfoCache = WeakHashMap<Any, Map<String, Any?>>()
    private val sourceInfoCache = WeakHashMap<Any, Map<String, Any?>>()

    @SuppressLint("DiscouragedApi", "SetTextIsSelectable")
    @Suppress("DEPRECATION")
    override fun onHook() {
        if (!prefs(ModulePrefs).getBoolean("show_more_apk_package_information", false)) return

        cacheApkInfo()
        cacheSourceInfo()

        val clazz = APK_INFO_VIEW.toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("ShowMoreApkPackageInformation -> $APK_INFO_VIEW not found", tag = "LuckyTool")
            return
        }
        clazz.method { name = "loadApkInfo" }.ignored().hook {
            after {
                val container = runCatching { instance }.getOrNull() as? LinearLayout ?: run {
                    YLog.error(
                        "ShowMoreApkPackageInformation -> loadApkInfo instance is not LinearLayout",
                        tag = "LuckyTool",
                    )
                    return@after
                }
                val context = container.context ?: return@after

                val apkInfo = args.pickByClassName(APK_INFO) ?: return@after
                val sourceInfo = args.pickByClassName(SOURCE_INFO) ?: return@after

                val apk = apkInfoCache[apkInfo] ?: apkInfo.readApkInfoFields()
                val source = sourceInfoCache[sourceInfo] ?: sourceInfo.readSourceInfoFields()

                val packageName = apk["packageName"] as? String ?: ""
                val versionName = apk["versionName"] as? String ?: ""
                val versionCode = apk["versionCode"] as? Int ?: 0
                val apkPath = apk["apkPath"] as? String ?: ""
                val label = apk["label"] as? String ?: ""
                val apkSize = apk["size"] as? Long ?: 0L
                val sourceName = source["sourceName"] as? String ?: ""
                val sourcePackage = source["sourcePackage"] as? String ?: ""
                val actionType = source["actionType"] as? Int ?: -1

                val isInstall = actionType == 0
                val isUninstall = actionType == 1

                val pm = context.packageManager
                val archivePkgInfo: PackageInfo? =
                    if (apkPath.isEmpty()) null else safeOfNull {
                        if (Build.VERSION.SDK_INT >= 33)
                            pm.getPackageArchiveInfo(apkPath, PackageManager.PackageInfoFlags.of(1L))
                        else pm.getPackageArchiveInfo(apkPath, 1)
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
                } ?: return@after

                container.removeAllViews()
                container.addView(root)
                apkInfoCache.remove(apkInfo)
                sourceInfoCache.remove(sourceInfo)
            }
        }
    }

    private fun cacheApkInfo() {
        val cls = APK_INFO.toClassOrNull(appClassLoader)
        if (cls == null) {
            YLog.error("ShowMoreApkPackageInformation -> $APK_INFO not found", tag = "LuckyTool")
            return
        }
        cls.constructor { paramCount = 7 }.ignored().hook {
            after {
                val target: Any = runCatching { instance }.getOrNull() ?: return@after
                apkInfoCache[target] = mapOf(
                    "icon" to (args.getOrNull(0) as? Int ?: 0),
                    "apkPath" to (args.getOrNull(1) as? String ?: ""),
                    "label" to (args.getOrNull(2) as? String ?: ""),
                    "versionName" to (args.getOrNull(3) as? String ?: ""),
                    "versionCode" to (args.getOrNull(4) as? Int ?: 0),
                    "packageName" to (args.getOrNull(5) as? String ?: ""),
                    "size" to (args.getOrNull(6) as? Long ?: 0L),
                )
            }
        }
    }

    private fun cacheSourceInfo() {
        val cls = SOURCE_INFO.toClassOrNull(appClassLoader)
        if (cls == null) {
            YLog.error("ShowMoreApkPackageInformation -> $SOURCE_INFO not found", tag = "LuckyTool")
            return
        }
        cls.constructor { paramCount = 4 }.ignored().hook {
            after {
                val target: Any = runCatching { instance }.getOrNull() ?: return@after
                sourceInfoCache[target] = mapOf(
                    "sourcePackage" to (args.getOrNull(0) as? String ?: ""),
                    "sourceName" to (args.getOrNull(1) as? String ?: ""),
                    "bUnknownSource" to (args.getOrNull(2) as? Boolean ?: false),
                    "actionType" to (args.getOrNull(3) as? Int ?: 0),
                )
            }
        }
    }

    private fun Array<Any?>.pickByClassName(className: String): Any? {
        firstOrNull { it != null && it.javaClass.name == className }?.let { return it }
        val cls = className.toClassOrNull(appClassLoader) ?: return null
        return firstOrNull { it != null && cls.isInstance(it) }
    }

    private fun Any.readApkInfoFields(): Map<String, Any?> = mapOf(
        "apkPath" to fieldValue("apkPath"),
        "label" to fieldValue("label"),
        "versionName" to fieldValue("versionName"),
        "versionCode" to fieldValue("versionCode"),
        "packageName" to fieldValue("packageName"),
        "size" to fieldValue("size"),
    )

    private fun Any.readSourceInfoFields(): Map<String, Any?> = mapOf(
        "sourcePackage" to fieldValue("sourcePackage"),
        "sourceName" to fieldValue("sourceName"),
        "actionType" to fieldValue("actionType"),
    )

    private fun Any.fieldValue(name: String): Any? =
        safeOfNull { javaClass.findField(name)?.get(this) }

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

    @SuppressLint("SetTextIsSelectable")
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

    private fun Class<*>.findField(name: String): Field? {
        var c: Class<*>? = this
        while (c != null && c != Any::class.java) {
            c.declaredFields.firstOrNull { it.name == name }?.let { return it.apply { isAccessible = true } }
            c = c.superclass
        }
        return null
    }
}
