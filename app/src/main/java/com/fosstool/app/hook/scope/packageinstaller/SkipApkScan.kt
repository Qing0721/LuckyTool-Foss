package com.fosstool.app.hook.scope.packageinstaller

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.hasMethod
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType

class SkipApkScan(private val commit: String) : YukiBaseHooker() {

    @Suppress("LocalVariableName")
    override fun onHook() {
        val OPIA = "com.android.packageinstaller.oplus.OPlusPackageInstallerActivity"
        val ADRU = "com.android.packageinstaller.oplus.utils.AppDetailRedirectionUtils"
        val opiaCls = OPIA.toClass()
        val adruCls = runCatching { ADRU.toClass() }.getOrNull()

        val isNew = adruCls?.hasMethod { name = "shouldStartAppDetail" } == true
        val hasPlainScan = opiaCls.hasMethod { name = "checkToScanRisk" }
        val hasPlainStart = opiaCls.hasMethod { name = "isStartAppDetail" }
        val hasPlainInit = opiaCls.hasMethod { name = "initiateInstall" }

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

        member[0].toClass().apply {
            method {
                name = member[1]
                if (member[0] == OPIA) returnType = BooleanType
                if (member[0] == ADRU) returnType = IntType
            }.hookAll {
                if (member[0] == OPIA) replaceToFalse()
                if (member[0] == ADRU) replaceTo(9)
            }
        }
        opiaCls.apply {
            method { name = member[2] }.hook {
                replaceUnit {
                    method { name = member[3] }.get(instance).call()
                }
            }
        }
    }
}
