@file:Suppress("unused", "IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION")

package com.fosstool.app.utils

import android.content.*
import android.content.pm.PackageManager.*
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import android.provider.Settings
import android.service.quicksettings.TileService
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.ArrayMap
import android.util.ArraySet
import android.util.Base64
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import com.drake.net.utils.scope
import com.drake.net.utils.withDefault
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.dataChannel
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.fosstool.app.BuildConfig
import com.fosstool.app.IGlobalFuncController
import com.fosstool.app.R
import com.fosstool.app.hook.hooker.HookAndroid.prefs
import com.fosstool.app.ui.activity.MainActivity
import com.fosstool.app.utils.*
import com.topjohnwu.superuser.ipc.RootService
import kotlinx.coroutines.Dispatchers
import java.io.*
import java.util.*
import java.util.regex.Pattern
import kotlin.math.roundToLong
import kotlin.random.Random
import kotlin.system.exitProcess


@Suppress("DEPRECATION")
fun Context.getAppCommit(packName: String): String? {
    val commitInfo = PackageUtils(packageManager).getApplicationInfo(packName, 128)
    return safeOfNull { commitInfo.metaData.get("versionCommit").toString() }
}

@Suppress("DEPRECATION")
fun Context.getAppVersion(packName: String, save: Boolean = true): ArrayList<String> =
    safeOf(ArrayList()) {
        val arrayList = ArrayList<String>()
        val arraySet = ArraySet<String>()
        val packageInfo = PackageUtils(packageManager).getPackageInfo(packName, 0)
        val applicationInfo = PackageUtils(packageManager).getApplicationInfo(packName, 128)
        val commitData = applicationInfo.metaData
        val versionName = safeOf("null") { packageInfo.versionName.toString() }
        arrayList.add(versionName)
        arraySet.add("name|$versionName")
        val versionCode = safeOf("null") { packageInfo.longVersionCode.toString() }
        arrayList.add(versionCode)
        arraySet.add("code|$versionCode")
        val versionCommit = safeOf("null") { commitData.get("versionCommit").toString() }
        val versionDate = safeOf("null") { commitData.get("versionDate").toString() }
        val commit = versionCommit.ifBlank { versionDate.ifBlank { "null" } }
        arrayList.add(commit)
        arraySet.add("commit|$commit")
        if (save) putStringSet(ModulePrefs, packName, arraySet)
        return arrayList
    }

fun getAppSet(prefsName: String, packName: String): Array<String> {
    val newArray = arrayOf("null", "null", "null")
    prefs(prefsName).getStringSet(packName, ArraySet()).toTypedArray().apply {
        if (isEmpty()) return newArray
        forEach {
            if (it.isNullOrEmpty()) return@forEach
            if (it.contains("name|")) newArray[0] = it.substring(5)
            if (it.contains("code|")) newArray[1] = it.substring(5)
            if (it.contains("commit|")) newArray[2] = it.substring(7)
        }
        return newArray
    }
}

fun Context.getDeviceInfo(
    controller: IGlobalFuncController? = null, isLog: Boolean = false
): String {
    val empty = ""
    val marketName = getProp("ro.vendor.oplus.market.name").let { if (it == "null") empty else it }
    val fingerprintName = try {
        Build.FINGERPRINT.split("/").getOrNull(1) ?: Build.MODEL
    } catch (_: Throwable) { Build.MODEL }
    val osVersionName = try {
        val code = com.fosstool.app.hook.utils.OplusBuildUtlils().getOSVersionCode ?: 0
        val name = com.fosstool.app.hook.utils.OplusBuildUtlils().getOSVersionName
        if (code > 0 && name != null && name != "null") "$name($code)" else empty
    } catch (_: Throwable) { empty }
    return """
        ${getString(R.string.model)}: $fingerprintName $osVersionName $marketName
        ${getString(R.string.product)}: ${Build.PRODUCT} ${Build.DEVICE} ${controller?.marketName ?: empty} ${controller?.otaVersion ?: empty} ${controller?.flashInfo ?: empty}
        ${getString(R.string.system)}: Android ${Build.VERSION.RELEASE}(${Build.VERSION.SDK_INT}) OS $osVersionName
        ${getString(R.string.build_version)}: ${Build.DISPLAY} ${controller?.otaVersion ?: empty}
        ${getString(R.string.flash)}: ${controller?.flashInfo ?: empty}
        LCD: ${controller?.lcdInfo ?: empty}
        PAS: ${controller?.pcbInfo ?: empty} ${controller?.snInfo ?: empty}
    """
.trimIndent().let {
        if (isLog) "$it\n${getString(R.string.module_version)} $getVersionName($getVersionCode)\n\n" else it
    }
}

@Suppress("SENSELESS_COMPARISON")
fun Context.checkPackName(packName: String) = safeOfFalse {
    PackageUtils(packageManager).getPackageInfo(packName, 0) != null
}

fun Context.getAppIcon(packName: String): Drawable? = safeOfNull {
    return PackageUtils(packageManager).getApplicationInfo(packName, 0).loadIcon(packageManager)
}

fun Context.getAppVersionName(packName: String): String? = safeOfNull {
    return PackageUtils(packageManager).getPackageInfo(packName, 0).versionName
}

fun Context.getAppVersionCode(packName: String): Long? = safeOfNull {
    return PackageUtils(packageManager).getPackageInfo(packName, 0).longVersionCode
}

fun Context.getAppLabel(packName: String): CharSequence {
    return getAppLabelOrNull(packName) ?: packName
}

fun Context.getAppLabelOrNull(packName: String): CharSequence? = safeOfNull {
    return PackageUtils(packageManager).getApplicationInfo(packName, 0).loadLabel(packageManager)
}

fun Context.checkResolveActivity(intent: Intent): Boolean = safeOfFalse {
    return PackageUtils(packageManager).resolveActivity(intent, 0) != null
}

fun Context.toast(msg: String, long: Boolean? = false) = if (long == true) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
} else {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun getFpsMode1(): ArrayList<Any?> {
    return ArrayList<Any?>().apply {
        add(0, DisplayMode(0, null, null, null, null, 30.0F))
        add(1, DisplayMode(1, null, null, null, null, 60.0F))
        add(2, DisplayMode(2, null, null, null, null, 90.0F))
        add(3, DisplayMode(3, null, null, null, null, 120.0F))
        add(4, DisplayMode(4, null, null, null, null, 144.0F))
    }
}

fun getFpsMode2(): ArrayList<ArrayList<*>> = safeOf(ArrayList()) {
    val command =
        "dumpsys display | grep -A 24 'mSfDisplayModes=' | grep ' DisplayMode{id=' | cut -f2 -d '{' | while read row; do\n" + "  if [[ -n \$row ]]; then\n" + "    echo \$row | tr ',' '\\n' | while read col; do\n" + "      case \$col in\n" + "        'id='*)\n" + "          echo -n \$(echo \${col:3}'|')\n" + "        ;;\n" + "      'width='*)\n" + "        echo -n \$(echo \${col:6})\n" + "        ;;\n" + "      'height='*)\n" + "        echo -n x\$(echo \${col:7})\n" + "        ;;\n" + "      'refreshRate='*)\n" + "        echo ' '\$(echo \${col:12} | cut -f1 -d '.')Hz\n" + "        ;;\n" + "      esac\n" + "    done\n" + "    echo -e '\\\\n'\n" + "  fi\n" + "done"
    var dataArr: ArrayList<String>
    val idArr = ArrayList<Int>()
    val fpsArr = ArrayList<String>()
    ShellUtils.execCommand(command, true, true).apply {
        if (result == 1) return@safeOf ArrayList()
        else dataArr =
            successMsg.takeIf { e -> e.isNotBlank() }?.split("\\n")?.toMutableList()?.apply {
                removeIf { e -> e.isBlank() }
            } as ArrayList<String>
    }
    dataArr.forEach {
        val id = it.split("|").takeIf { e -> e.size >= 2 }?.get(0) ?: return@forEach
        val fps = it.split("|").takeIf { e -> e.size >= 2 }?.get(1) ?: return@forEach
        idArr.add(id.toInt())
        fpsArr.add(fps)
    }
    return ArrayList<ArrayList<*>>().apply {
        idArr.takeIf { e -> e.isNotEmpty() }?.let { add(it) }
        fpsArr.takeIf { e -> e.isNotEmpty() }?.let { add(it) }
    }
}

fun setRefresh(context: Context, name: String, refresh: String?) {
    setParameter(context, name, "min_refresh_rate", refresh)
    setParameter(context, name, "peak_refresh_rate", refresh)
}

fun setRefresh(context: Context, name: String, minRefresh: String?, peakRefresh: String?) {
    setParameter(context, name, "min_refresh_rate", minRefresh)
    setParameter(context, name, "peak_refresh_rate", peakRefresh)
}

fun setParameter(context: Context, name: String, key: String?, value: String?) {
    val contentResolver = context.contentResolver
    safeOf({
        context.toast("apply $name Hz failed!")
    }) {
        val contentValues = ContentValues(2)
        contentValues.put("name", key)
        contentValues.put("value", value)
        contentResolver.insert(Uri.parse("content://settings/system"), contentValues)
    }
}

fun getDeviceID(): String {
    ShellUtils.execCommand(
        "cat /sys/devices/soc0/serial_number", false, true
    ).apply {
        if (result == 0 && successMsg != null && successMsg.isNotBlank()) return successMsg
    }
    ShellUtils.execCommand(
        "cat /sys/firmware/devicetree/base/firmware/android/serialno", false, true
    ).apply {
        if (result == 0 && successMsg != null && successMsg.isNotBlank()) return successMsg
    }
    return "null"
}


val getGuid: String
    get() = ShellUtils.execCommand(
        "cat /data/system/openid_config.xml | sed  -n '3p'", true, true
    ).let {
        if ((it.result == 0 && it.successMsg.isNullOrBlank().not())) it.successMsg.split("\"")
            .getOrNull(3) ?: "null"
        else "null"
    }

fun getProp(key: String): String {
    return getProp(key, false)
}

fun getProp(key: String, root: Boolean): String = safeOf("null") {
    ShellUtils.execCommand("getprop $key", root, true).let {
        if (it.result == 1) "null" else formatSpace(it.successMsg)
    }
}

@Suppress("DEPRECATION")
fun TileService.closeCollapse() {
    Intent(Intent.ACTION_VIEW).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivityAndCollapse(this)
    }
}

fun jumpEngineermode(context: Context) {
    val activity = if (SDK >= A14) "aftersale.AfterSalePage" else "EngineerModeMain"
    if (context.checkPackName("com.oppo.engineermode")) {
        ShellUtils.execCommand("am start -n com.oppo.engineermode/.$activity", true)
    } else if (context.checkPackName("com.oplus.engineermode")) {
        ShellUtils.execCommand("am start -n com.oplus.engineermode/.$activity", true)
    }
}

fun jumpBatteryInfo(context: Context) {
    if (context.checkPackName("com.oppo.engineermode")) {
        ShellUtils.execCommand(
            "am start -n com.oppo.engineermode/.charge.modeltest.BatteryInfoShow", true
        )
    } else if (context.checkPackName("com.oplus.engineermode")) {
        ShellUtils.execCommand(
            "am start -n com.oplus.engineermode/.charge.modeltest.BatteryInfoShow", true
        )
    }
}

fun jumpMultiApp(context: Context) {
    if (context.checkPackName("com.oplus.multiapp")) {
        ShellUtils.execCommand(
            "am start com.oplus.multiapp/.ui.entry.ActivityMainActivity", true
        )
    }
}

fun jumpDarkMode(context: Context) {
    Intent("com.android.settings.DISPLAY_SETTINGS").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        context.startActivity(this)
    }
}

fun jumpOTA(context: Context) {
    if (context.checkPackName("com.oplus.ota")) {
        ShellUtils.execCommand(
            "am start com.oplus.ota/com.oplus.otaui.activity.EntryActivity", true
        )
    }
}

fun jumpPictorial(context: Context) {
    if (context.checkPackName("com.heytap.pictorial")) {
        ShellUtils.execCommand(
            "am start com.heytap.pictorial/.ui.SettingActivity", true
        )
    }
}

fun jumpGesture(context: Context) {
    if (context.checkPackName("com.oplus.gesture")) {
        ShellUtils.execCommand(
            "am start com.oplus.gesture/.guide.GestureMainActivity", true
        )
    }
}

fun jumpHighPerformance(context: Context) {
    if (context.checkPackName("com.oplus.battery")) {
        ShellUtils.execCommand(
            "am start com.oplus.battery/com.oplus.powermanager.fuelgaue.IntellPowerSaveScence", true
        )
    }
}

fun jumpBattery(context: Context) {
    if (context.checkPackName("com.oplus.battery")) {
        ShellUtils.execCommand(
            "am start com.oplus.battery/com.oplus.powermanager.fuelgaue.PowerConsumptionActivity",
            true
        )
    }
}

fun jumpRunningApp(context: Context) {
    val isoppoRunning = Intent().setClassName(
        "com.android.settings", "com.coloros.settings.feature.process.RunningApplicationActivity"
    )
    val isoplusRunning = Intent().setClassName(
        "com.android.settings", "com.oplus.settings.feature.process.RunningApplicationActivity"
    )
    if (context.checkResolveActivity(isoppoRunning)) {
        ShellUtils.execCommand(
            "am start -n com.android.settings/com.coloros.settings.feature.process.RunningApplicationActivity",
            true
        )
    } else if (context.checkResolveActivity(isoplusRunning)) {
        ShellUtils.execCommand(
            "am start -n com.android.settings/com.oplus.settings.feature.process.RunningApplicationActivity",
            true
        )
    }
}

fun Context.setComponentDisabled(component: ComponentName, value: Boolean) {
    packageManager.setComponentEnabledSetting(
        component,
        if (value) COMPONENT_ENABLED_STATE_DISABLED else COMPONENT_ENABLED_STATE_ENABLED,
        DONT_KILL_APP
    )
}

fun Context.getComponentEnabled(component: ComponentName): Int? {
    return when (packageManager.getComponentEnabledSetting(component)) {
        COMPONENT_ENABLED_STATE_DEFAULT -> COMPONENT_ENABLED_STATE_DEFAULT
        COMPONENT_ENABLED_STATE_ENABLED -> COMPONENT_ENABLED_STATE_ENABLED
        COMPONENT_ENABLED_STATE_DISABLED -> COMPONENT_ENABLED_STATE_DISABLED
        COMPONENT_ENABLED_STATE_DISABLED_USER -> COMPONENT_ENABLED_STATE_DISABLED_USER
        COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED -> COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED
        else -> null
    }
}

val getFlashInfo
    get(): String = ShellUtils.execCommand("cat /sys/class/block/sda/device/inquiry", true, true)
        .let {
            if ((it.result == 0 && it.successMsg.isNullOrBlank()
                    .not())
            ) formatSpace(it.successMsg.replaceSpace.uppercase())
            else "null"
        }

val getLcdInfo: String
    get() : String = ShellUtils.execCommand(
        "cat /proc/devinfo/lcd | sed 's/^.*\t//g; s/$/\n/g; s/\n/ /g;'", true, true
    ).let {
        if ((it.result == 0 && it.successMsg.isNullOrBlank()
                .not())
        ) it.successMsg.replaceSpace.uppercase()
        else "null"
    }

val getPcbInfo: String
    get() : String = ShellUtils.execCommand(
        "echo \$(getprop gsm.serial)\$(getprop vendor.gsm.serial)", true, true
    ).let {
        if ((it.result == 0 && it.successMsg.isNullOrBlank()
                .not())
        ) it.successMsg.replaceSpace.uppercase()
        else "null"
    }

val getSnInfo: String
    get() : String = ShellUtils.execCommand(
        "getprop ro.serialno", true, true
    ).let {
        if ((it.result == 0 && it.successMsg.isNullOrBlank()
                .not())
        ) it.successMsg.replaceSpace.uppercase()
        else "null"
    }

val Float.dp: Float
    get() = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
    )

val Int.dp: Int
    get() = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

val Float.sp: Float
    get() = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics
    )

val Int.sp: Int
    get() = android.util.TypedValue.applyDimension(
        android.util.TypedValue.COMPLEX_UNIT_SP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    ).toInt()

fun Context.copyStr(string: CharSequence) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText(null, string)
    clipboard.setPrimaryClip(clipData)
}

fun base64ToBitmap(code: String): Bitmap? {
    val decode: ByteArray = Base64.decode(code.split(",")[1], Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decode, 0, decode.size)
}

val dialogCentered get() = com.google.android.material.R.style.ThemeOverlay_Material3_MaterialAlertDialog_Centered

fun Preference.setPrefsIconRes(resource: Any?, result: (Drawable?, Boolean) -> Unit) {
    if (context.getBoolean(SettingsPrefs, "hide_function_page_icon", false)) {
        result(null, false)
        return
    }
    val image: Drawable? = when (resource) {
        is Int -> ResourcesCompat.getDrawable(context.resources, resource, null)
        is Drawable -> resource
        is String -> context.getAppIcon(resource)
        else -> null
    }
    if (image == null || image.intrinsicWidth <= 0 || image.intrinsicHeight <= 0) {
        val icon =
            ResourcesCompat.getDrawable(context.resources, android.R.mipmap.sym_def_app_icon, null)
        result(icon, true)
        return
    }
    val bitmap = image.toBitmapOrNull()
    if (bitmap == null) {
        result(null, false)
        return
    }
    val drawable = RoundedBitmapDrawableFactory.create(context.resources, bitmap)
    drawable.setAntiAlias(true)
    drawable.cornerRadius = 30F
    result(drawable, true)
}

fun Preference.fixIconSize(icon: Drawable?): Drawable? {
    return if (icon != null && ((icon.intrinsicWidth < 48.dp) || (icon.intrinsicHeight < 48.dp))) {
        context.zoomDrawable(icon, 48.dp, 48.dp)
    } else icon
}

fun arraySummaryDot(vararg string: String?): String {
    var res = ""
    string.forEachIndexed { index, s ->
        if (s.isNullOrBlank()) return@forEachIndexed
        res += s
        if (index != string.lastIndex) res += ","
    }
    return res
}

fun arraySummaryLine(vararg string: String?): String {
    var res = ""
    string.forEachIndexed { index, s ->
        if (s.isNullOrBlank()) return@forEachIndexed
        res += s
        if (index != string.lastIndex) res += "\n"
    }
    return res
}

fun getRandomString(length: Int): String {
    val random = Random
    val sb = StringBuffer()
    for (i in 0 until length) {
        val number: Int = random.nextInt(3)
        var result: Long
        when (number) {
            0 -> {
                result = (Math.random() * 25 + 65).roundToLong()
                sb.append(Char(result.toUShort()).toString())
            }

            1 -> {
                result = (Math.random() * 25 + 97).roundToLong()
                sb.append(Char(result.toUShort()).toString())
            }

            2 -> sb.append(java.lang.String.valueOf(Random.nextInt(10)))
        }
    }
    return sb.toString()
}

fun hexToByte(inHex: String): Byte {
    return inHex.toInt(16).toByte()
}

fun base64Encode(string: String): String {
    return Base64.encodeToString(string.toByteArray(), Base64.DEFAULT)
}

fun base64Decode(string: String): String {
    return String(Base64.decode(string, Base64.DEFAULT))
}

fun isZh(context: Context): Boolean {
    val locale = context.resources.configuration.locales
    val language = locale[0].language
    return language.endsWith("zh")
}

fun Context.openApp(packNames: Array<String>) {
    packNames.forEach {
        packageManager.getLaunchIntentForPackage(it)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            startActivity(this)
        }
    }
}

fun Context.openApp(packName: String) {
    packageManager.getLaunchIntentForPackage(packName)?.apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        startActivity(this)
    }
}

fun Context.openAppDetailIntent(packName: String, userId: Int?) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packName, null)
    )
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    userId?.let { intent.putExtra("userId", it) }
    startActivity(intent)
}

fun Context.openMarketIntent(packName: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packName"))
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    startActivity(intent)
}

fun Context.restartMain() {
    val list = arrayOf(
        getString(R.string.restart_scope),
        getString(R.string.reboot),
        getString(R.string.fast_reboot)
    )
    MaterialAlertDialogBuilder(this, dialogCentered).apply {
        setCancelable(true)
        setItems(list) { _: DialogInterface?, i: Int ->
            when (i) {
                0 -> restartAllScope()
                1 -> ShellUtils.execCommand("reboot", true)
                2 -> ShellUtils.execCommand("killall zygote", true)
            }
        }
        show()
    }
}

fun Context.restartScopes(scopes: Array<String>) {
    val list = arrayOf(
        getString(R.string.restart_scope), getString(R.string.restart_only_this_page_scope)
    )
    MaterialAlertDialogBuilder(this, dialogCentered).apply {
        setItems(list) { _, which ->
            when (which) {
                0 -> restartAllScope()
                1 -> restartAllScope(scopes)
            }
        }
        show()
    }
}

fun getPackageAbsolutePath(packName: String): ArrayMap<String, String> {
    ShellUtils.execCommand("pm list packages -f | grep $packName", true, true).apply {
        return if (result == 0 && successMsg != null && successMsg.isNotBlank()) {
            val map = ArrayMap<String, String>()
            successMsg?.split("package:")?.toMutableList()?.apply {
                removeIf { it.isBlank() }
            }?.forEach {
                val key = it.substringAfterLast("=")
                val value = it.substringBeforeLast("=")
                map[key] = value
            }
            map
        } else ArrayMap()
    }
}

fun uninstallApp(packName: String, userId: String? = "0") {
    ShellUtils.execCommand("pm uninstall --user $userId $packName", true)
}

fun forceUninstallApp(packName: String) {
    getPackageAbsolutePath(packName).forEach { (k, v) ->
        if (k == packName) ShellUtils.execCommand("rm -rf $v", true)
    }
}

fun Context.removeModule() {
    getUsers().forEach { uninstallApp(BuildConfig.APPLICATION_ID, it) }
    getUsers().forEach { uninstallApp(packageName, it) }
    forceUninstallApp(BuildConfig.APPLICATION_ID)
    forceUninstallApp(packageName)
}

fun Context.exitModule() {
    (this as MainActivity).finishAndRemoveTask()
    exitProcess(0)
}

fun Context.restartAllScope() {
    val xposedScope = resources.getStringArray(R.array.xposed_scope)
    val commands = ArrayList<String>()
    for (scope in xposedScope) {
        if (scope == "android") continue
        if (scope.contains("systemui")) {
            commands.add("kill -9 `pgrep systemui`")
            continue
        }
        commands.add("pkill -9 $scope")
        commands.add("am force-stop $scope")
        getAppVersion(scope)
    }
    MaterialAlertDialogBuilder(this).apply {
        setMessage(getString(R.string.restart_scope_message))
        setPositiveButton(getString(android.R.string.ok)) { _: DialogInterface?, _: Int ->
            scope(Dispatchers.Default) { ShellUtils.execCommand(commands, true) }
        }
        setNeutralButton(getString(android.R.string.cancel), null)
        show()
    }
}

fun Context.restartAllScope(scopes: Array<String>) {
    val commands = ArrayList<String>()
    for (scope in scopes) {
        if (scope == "android") continue
        if (scope.contains("systemui")) {
            commands.add("kill -9 `pgrep systemui`")
            continue
        }
        commands.add("killall $scope")
        commands.add("am force-stop $scope")
        getAppVersion(scope)
    }
    scope(Dispatchers.Default) { ShellUtils.execCommand(commands, true) }
}

fun Context.bindRootService(
    clazz: Class<*>,
    onConnected: (ComponentName?, IBinder?) -> Unit,
    onDisconnected: (ComponentName?) -> Unit = {}
) {
    val intent = Intent(this, clazz)
    RootService.bind(intent, object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) =
            onConnected(name, service)

        override fun onServiceDisconnected(name: ComponentName?) = onDisconnected(name)
    })
}

fun callFunc(bundle: Bundle?) {
    scope {
        withDefault {
            bundle?.apply {
                val command = ArrayList<String>()
                val tileAutoStart = getBoolean("tileAutoStart", false)
                if (getBoolean("fps_auto", false)) {
                    val fpsMode = getInt("fps_mode", 1)
                    val fpsCur = getInt("fps_cur", -1)
                    if ((fpsMode == 2) && (fpsCur != -1)) {
                        command.add("service call SurfaceFlinger 1035 i32 $fpsCur")
                    }
                }
                if (tileAutoStart && getBoolean("touchSamplingRate", false)) {
                    command.add("echo > /proc/touchpanel/game_switch_enable 1")
                }
                if (tileAutoStart && getBoolean("highBrightness", false)) {
                    command.add("echo > /sys/kernel/oplus_display/hbm 1")
                }
                if (tileAutoStart && getBoolean("globalDC", false)) {
                    command.add("echo > /sys/kernel/oppo_display/dimlayer_hbm 1")
                    command.add("echo > /sys/kernel/oplus_display/dimlayer_hbm 1")
                }
                if (command.isNotEmpty()) ShellUtils.execCommand(command, true)
            }
        }
    }
}

fun getRefreshRateStatus(): Boolean = safeOfFalse {
    val result = ShellUtils.execCommand("service call SurfaceFlinger 1034 i32 2", true, true).let {
        if (it.result == 0 && it.successMsg != null && it.successMsg.isNotBlank()) it.successMsg
        else return@safeOfFalse false
    }
    return when (result.filterNumber.toIntOrNull()) {
        0 -> false
        1 -> true
        else -> false
    }
}

fun showRefreshRate(status: Boolean) {
    ShellUtils.execCommand(
        "service call SurfaceFlinger 1034 i32 ${if (status) 1 else 0}", true
    )
}

fun Fragment.navigatePage(action: Int, title: CharSequence? = "Title") = try {
    findNavController().navigate(action, Bundle().apply {
        putCharSequence("title_label", title)
    })
} catch (_: IllegalArgumentException) {

}


fun Fragment.navigatePage(action: Int, bundle: Bundle?) = try {
    findNavController().navigate(action, bundle)
} catch (_: IllegalArgumentException) {

}



fun getScreenOrientation(view: View, result: (Boolean) -> Unit) {
    getScreenOrientation(view.resources) { result(it) }
}

fun getScreenOrientation(context: Context, result: (Boolean) -> Unit) {
    getScreenOrientation(context.resources) { result(it) }
}

fun getScreenOrientation(resource: Resources, result: (Boolean) -> Unit) {
    val mConfiguration: Configuration = resource.configuration
    if (mConfiguration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        result(true)
    }
    if (mConfiguration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        result(false)
    }
}

fun getUsers(): Array<String> {
    ShellUtils.execCommand("ls /data/user/ -mF", true, true).apply {
        return if (result == 0 && successMsg != null && successMsg.isNotBlank()) {
            successMsg?.replace(" ", "")?.replace("/", "")?.split(",")?.toTypedArray() ?: arrayOf()
        } else arrayOf()
    }
}

fun Context.getQSlist(): ArrayList<String> {
    val list = ArrayList<String>()
    getUsers().forEach { u ->
        val dir1 = getString(R.string.tencent_files, u)
        val command1 = "if [[ -d $dir1 ]]; then\n  ls $dir1 -mF\nfi"
        val list1 = ShellUtils.execCommand(command1, true, true).let { its ->
            if (its.result == 0 && its.successMsg != null && its.successMsg.isNotBlank()) {
                its.successMsg?.replace(" ", "")?.split(",")?.toMutableList()?.apply {
                    removeIf { it.contains("/").not() }
                    removeIf { Pattern.matches(".*[a-zA-Z]+.*", it) }
                } ?: arrayListOf()
            } else arrayListOf()
        }
        list.addAll(list1)
        val dir2 = getString(R.string.tencent_configs, u)
        val command2 = "if [[ -d $dir2 ]]; then\n  ls $dir2 -mF\nfi"
        val list2 = ShellUtils.execCommand(command2, true, true).let { its ->
            if (its.result == 0 && its.successMsg != null && its.successMsg.isNotBlank()) {
                its.successMsg?.replace(" ", "")?.split(",")?.toMutableList()?.apply {
                    removeIf { it.contains("/").not() }
                    removeIf { Pattern.matches(".*[a-zA-Z]+.*", it) }
                } ?: arrayListOf()
            } else arrayListOf()
        }
        list.addAll(list2)
    }
    return list
}

fun Context.getQStatus(id: String): Boolean {
    if (getQSlist().contains("$id/")) return true
    return false
}

fun Context.getCSid(): String? {
    getUsers().forEach { u ->
        val dir = getString(R.string.cool_black, u)
        val command =
            "if [[ -f $dir ]]; then\n  cat $dir | grep 'name=\"uid\"' | cut -d \">\" -f2 | cut -d \"<\" -f1\nfi"
        val uid = ShellUtils.execCommand(command, true, true).let { its ->
            if (its.result == 0 && its.successMsg != null && its.successMsg.isNotBlank()) {
                its.successMsg?.replace(" ", "")
            } else null
        }
        if (!uid.isNullOrBlank()) return uid
    }
    return null
}

fun Context.getCStatus(id: String): Boolean {
    if (getCSid() == id) return true
    return false
}

fun getCharColor(char: CharSequence): Int? {
    val sp = SpannableString(char)
    val colorSpan = sp.getSpans(0, sp.length, ForegroundColorSpan::class.java)
    return if (colorSpan != null && colorSpan.isNotEmpty()) colorSpan[0].foregroundColor else null
}

fun getCharSpans(char: CharSequence): Array<out ForegroundColorSpan>? {
    val colorSpans = SpannableString(char).getSpans(0, char.length, ForegroundColorSpan::class.java)
    return if (colorSpans == null || colorSpans.isEmpty()) null else colorSpans
}

fun Context.zoomDrawable(drawable: Drawable, width: Int, height: Int): Drawable {
    val oldBmp = drawable.toBitmap()
    val newBmp = Bitmap.createScaledBitmap(oldBmp, width, height, true)
    return BitmapDrawable(resources, newBmp)
}

fun Context.checkVerify() = safeOf({ exitModule() }) {
    val packInfo = PackageUtils(packageManager).getPackageInfo(BuildConfig.APPLICATION_ID, 0)
    if (packInfo.packageName != packageName || packInfo.longVersionCode != getVersionCode.toLong() || packInfo.versionName != getVersionName) {
        exitModule()
    }
}

val isMTK get() = Pattern.compile("mt[0-9]*").matcher(Build.HARDWARE).find()

val Context.is24
    get() = Settings.System.getString(
        contentResolver, Settings.System.TIME_12_24
    ) == "24"

fun closeScreen(context: Context) {
    val service = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    service.current().method { name = "goToSleep";param(LongType) }.call(SystemClock.uptimeMillis())
}

fun Fragment.setupMenuProvider(@MenuRes menuId: Int, onMenuSelected: (MenuItem) -> Boolean) =
    (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) =
            menuInflater.inflate(menuId, menu)

        override fun onMenuItemSelected(menuItem: MenuItem) = onMenuSelected(menuItem)
    }, viewLifecycleOwner, Lifecycle.State.RESUMED)

fun Fragment.setupMenuProvider(menuProvider: MenuProvider) =
    (requireActivity() as MenuHost).addMenuProvider(
        menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED
    )

fun Context.checkModuleValied(isValied: (Boolean) -> Unit) {
    dataChannel("com.android.systemui").checkingVersionEquals(result = isValied)
}

val redOneTextColor = Color.parseColor("#c41442")
