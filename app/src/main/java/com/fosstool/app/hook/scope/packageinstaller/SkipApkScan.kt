package com.fosstool.app.hook.scope.packageinstaller

import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Method

class SkipApkScan(private val commit: String) : YukiBaseHooker() {

    @Suppress("LocalVariableName")
    override fun onHook() {
        val OPIA = "com.android.packageinstaller.oplus.OPlusPackageInstallerActivity"
        val ADRU = "com.android.packageinstaller.oplus.utils.AppDetailRedirectionUtils"
        val opiaCls = OPIA.toClassOrNull(appClassLoader) ?: return
        val adruCls = ADRU.toClassOrNull(appClassLoader)

        val isNew = adruCls?.findMethod("shouldStartAppDetail") != null
        val hasPlainScan = opiaCls.findMethod("checkToScanRisk") != null
        val hasPlainStart = opiaCls.findMethod("isStartAppDetail") != null
        val hasPlainInit = opiaCls.findMethod("initiateInstall") != null

        val member: Array<String> = when {
            isNew -> arrayOf(ADRU, "shouldStartAppDetail", "checkToScanRisk", "initiateInstall")
            hasPlainScan && hasPlainInit && hasPlainStart ->
                arrayOf(OPIA, "isStartAppDetail", "checkToScanRisk", "initiateInstall")
            hasPlainScan && hasPlainInit ->
                arrayOf(OPIA, "isStartAppDetail", "checkToScanRisk", "initiateInstall")
            else -> when (commit) {
                "7bc7db7", "e1a2c58" -> arrayOf(OPIA, "L", "C", "K")
                "75fe984", "532ffef" -> arrayOf(OPIA, "L", "D", "i")
                "38477f0" -> arrayOf(OPIA, "M", "D", "k")
                "a222497" -> arrayOf(OPIA, "M", "E", "j")
                else -> arrayOf(OPIA, "isStartAppDetail", "checkToScanRisk", "initiateInstall")
            }
        }

        if (prefs(ModulePrefs).getBoolean("disable_start_app_detail", false)) {
            member[0].toClassOrNull(appClassLoader)?.findMethod(member[1])?.let { m ->
                if (member[0] == OPIA) runCatching { XposedBridge.hookMethod(m, XC_MethodReplacement.returnConstant(false)) }
                if (member[0] == ADRU) runCatching { XposedBridge.hookMethod(m, XC_MethodReplacement.returnConstant(9)) }
            }
        }
        opiaCls.findMethod(member[2])?.let { m ->
            runCatching {
                XposedBridge.hookMethod(m, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        runCatching {
                            XposedHelpers.callMethod(param.thisObject, member[3])
                        }
                        param.result = null
                    }
                })
            }
        }
    }

    private fun Class<*>.findMethod(name: String): Method? {
        var c: Class<*>? = this
        while (c != null && c != Any::class.java) {
            c.declaredMethods.firstOrNull { it.name == name }?.let { return it.apply { isAccessible = true } }
            c = c.superclass
        }
        return null
    }
}
