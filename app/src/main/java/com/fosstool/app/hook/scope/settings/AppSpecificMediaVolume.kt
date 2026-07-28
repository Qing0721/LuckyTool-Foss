package com.fosstool.app.hook.scope.settings

import android.content.Context
import com.fosstool.app.BuildConfig
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.useFirst
import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.param.HookParam
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import org.luckypray.dexkit.query.enums.StringMatchType
import java.io.InputStream

object AppSpecificMediaVolume : YukiBaseHooker() {
    override fun onHook() {

        "com.android.settings.SettingsPreferenceFragment".toClassOrNull(appClassLoader)
            ?.method { name = "removePreference"; paramCount = 1 }
            ?.ignored()
            ?.hook {
                before {

                    if (args.firstOrNull() == "voice_mode_category") {
                        resultTrue()
                    }
                }
            }

        if (getOSVersionCode >= 27) hookLottieAssetFallback()
    }

    private fun hookLottieAssetFallback() {
        DexkitUtils.create(appInfo.sourceDir) { bridge ->
            val classes = bridge.findClass {
                matcher { className("com.oplus.anim", StringMatchType.StartsWith) }
            }.checkDataList("FixAppSpecificMediaVolumePage Clazz", onlyOne = false)
            if (classes.isEmpty()) return@create

            bridge.findMethod {
                searchInClass(classes)
                matcher {
                    paramTypes(ContextClass.name, StringClass.name, StringClass.name)
                    usingStrings(".zip", ".lottie")
                }
            }.useFirst("FixAppSpecificMediaVolumePage") { md ->
                val clazz = md.className.toClassOrNull(appClassLoader) ?: return@useFirst
                val fromAsset = clazz.declaredMethods.firstOrNull {
                    it.name == md.methodName && it.parameterCount == 3 &&
                        it.parameterTypes[0] == Context::class.java &&
                        it.parameterTypes[1] == String::class.java &&
                        it.parameterTypes[2] == String::class.java
                } ?: return@useFirst
                val returnType = fromAsset.returnType

                clazz.method {
                    name = md.methodName
                    param(ContextClass, StringClass, StringClass)
                }.ignored().hook {
                    before { replaceFromModuleRaw(clazz, returnType, this) }
                }
            }
        }
    }

    private fun replaceFromModuleRaw(clazz: Class<*>, returnType: Class<*>, param: HookParam) {
        val context = param.args.getOrNull(0) as? Context ?: return
        val assetName = param.args.getOrNull(1) as? String ?: ""
        val cacheKey = param.args.lastOrNull() as? String ?: ""
        if (!assetName.contains("multi_app_volume")) return

        val hasAsset = runCatching {
            context.assets.open(assetName).close()
            true
        }.getOrDefault(false)
        if (hasAsset) return

        runCatching { context.injectModuleAppResources() }
        if (assetName.endsWith(".zip") || assetName.endsWith(".lottie")) return

        val resName = assetName.substringAfterLast('/').substringBeforeLast(".json")
        @Suppress("DiscouragedApi")
        val resId = runCatching {
            context.resources.getIdentifier(resName, "raw", BuildConfig.APPLICATION_ID)
        }.getOrDefault(0)
        if (resId == 0) return
        val stream = runCatching { context.resources.openRawResource(resId) }.getOrNull() ?: return

        val fromJson = clazz.declaredMethods.firstOrNull {
            it.parameterCount == 2 &&
                it.parameterTypes[0] == InputStream::class.java &&
                it.parameterTypes[1] == String::class.java &&
                it.returnType == returnType
        } ?: return
        runCatching {
            fromJson.isAccessible = true
            fromJson.invoke(null, stream, cacheKey)
        }.getOrNull()?.let { param.result = it }
    }
}
