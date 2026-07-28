package com.fosstool.app.hook.scope.settings

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageItemInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.UserHandle
import com.fosstool.app.hook.utils.OplusBuildUtlils
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.param.HookParam
import com.highcapable.yukihookapi.hook.type.java.ListClass
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

object ForceDisplayGoogleAutoFill : YukiBaseHooker() {
    private const val CLS_PICKER =
        "com.oplus.settings.feature.othersettings.input.OplusDefaultAutofillPicker"
    private const val CLS_DEFAULT_APP_INFO =
        "com.android.settingslib.applications.DefaultAppInfo"

    override fun onHook() {
        val osVersionCode = try {
            OplusBuildUtlils().getOSVersionCode ?: 0
        } catch (_: Throwable) {
            0
        }
        if (osVersionCode >= 30) {

            CLS_PICKER.toClassOrNull(appClassLoader)
                ?.method { name = "getCandidates" }
                ?.ignored()
                ?.hook { before { rebuildCandidates(this) } }
        } else {
            DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

                val infoClassName = dexKitBridge.findClass {
                    matcher {
                        fields {
                            addForType("int")
                            addForType("java.lang.String")
                            addForType("android.content.Context")
                            addForType("android.content.ComponentName")
                            addForType("android.content.pm.PackageManager")
                            addForType("android.content.pm.PackageItemInfo")
                        }
                        methods {
                            add { paramCount = 0; returnType = "java.lang.String" }
                            add { paramCount = 0; returnType = "android.graphics.drawable.Drawable" }
                            add { paramCount = 0; returnType = "java.lang.CharSequence" }
                            add { paramCount = 0; returnType = "android.content.pm.ComponentInfo" }
                        }
                    }

                }.checkDataList("GoogleAutoFillV13").firstOrNullSafe()?.name ?: return@create

                CLS_PICKER.toClassOrNull(appClassLoader)
                    ?.method { emptyParam(); returnType = ListClass }
                    ?.ignored()
                    ?.hook { before { buildAutoFillCandidates(this, infoClassName) } }
            }
        }
    }

    private fun buildAutoFillCandidates(param: HookParam, infoClassName: String) {
        val host = param.instance ?: return
        val hostClass = host.javaClass
        val context = runCatching {
            hostClass.findMethodByName("getContext")?.invoke(host) as? Context
        }.getOrNull() ?: return
        val pm = findPackageManager(host) ?: return
        val intent = hostClass.findFieldOfType(Intent::class.java)
            ?.let { runCatching { it.get(host) as? Intent }.getOrNull() } ?: return
        val userId = currentUserId

        val infoClass = infoClassName.toClassOrNull(appClassLoader) ?: return
        val ctor = findComponentNameCtor(infoClass) ?: return
        val query = findQueryIntentServicesAsUser(pm) ?: return

        val resolves = runCatching {
            query.invoke(pm, intent, 128, userId) as? List<*>
        }.getOrNull() ?: return

        val candidates = ArrayList<Any>(resolves.size)
        for (item in resolves) {
            val serviceInfo = (item as? ResolveInfo)?.serviceInfo ?: continue
            val permission = serviceInfo.permission
            if (permission != "android.permission.BIND_AUTOFILL_SERVICE" &&
                permission != "android.permission.BIND_AUTOFILL"
            ) continue
            val instance = runCatching {
                ctor.newInstance(
                    context,
                    pm,
                    userId,
                    ComponentName(serviceInfo.packageName, serviceInfo.name),
                )
            }.getOrNull() ?: continue
            candidates.add(instance)
        }
        if (candidates.isNotEmpty()) {
            param.result = candidates
        }
    }

    private fun rebuildCandidates(param: HookParam) {
        val host = param.instance ?: return
        val hostClass = host.javaClass
        val context = runCatching {
            hostClass.findMethodByName("getContext")?.invoke(host) as? Context
        }.getOrNull() ?: return
        val pm = findPackageManager(host) ?: return
        val userId = runCatching {
            (hostClass.findMethodByName("getUser")?.invoke(host) as? Number)?.toInt()
        }.getOrNull() ?: 0
        val providers = runCatching {
            hostClass.findMethodByName("getAllProviders")?.invoke(host) as? List<*>
        }.getOrNull() ?: return
        if (providers.isEmpty()) return

        val infoClass = CLS_DEFAULT_APP_INFO.toClassOrNull(appClassLoader) ?: return
        val ctor = findDefaultAppInfoCtor(infoClass) ?: return

        val rebuilt = ArrayList<Any>(providers.size)
        for (provider in providers) {
            if (provider == null) continue
            val pClass = provider.javaClass
            val packageItem = runCatching {
                pClass.findMethodByName("getBrandingService")?.invoke(provider)
            }.getOrNull() as? PackageItemInfo
                ?: runCatching {
                    pClass.findMethodByName("getServiceInfo")?.invoke(provider) as? PackageItemInfo
                }.getOrNull()
                ?: continue
            val summary = runCatching {
                pClass.findMethodByName("getSettingsSubtitle")?.invoke(provider) as? String
            }.getOrNull()
                ?: runCatching {
                    pClass.findMethodByName("getSettingsActivity")?.invoke(provider) as? String
                }.getOrNull()
            val instance = runCatching {
                ctor.newInstance(context, pm, userId, packageItem, summary, true)
            }.getOrNull() ?: continue
            rebuilt.add(instance)
        }
        if (rebuilt.isNotEmpty()) {
            param.result = rebuilt
        }
    }

    private val currentUserId: Int
        get() = runCatching {
            (UserHandle::class.java.getDeclaredMethod("myUserId")
                .also { it.isAccessible = true }
                .invoke(null) as? Number)?.toInt()
        }.getOrNull() ?: 0

    private fun findQueryIntentServicesAsUser(pm: PackageManager): Method? {
        var c: Class<*>? = pm.javaClass
        while (c != null && c != Any::class.java) {
            c.declaredMethods.firstOrNull { m ->
                m.name == "queryIntentServicesAsUser" &&
                    m.parameterTypes.size == 3 &&
                    Intent::class.java.isAssignableFrom(m.parameterTypes[0]) &&
                    m.parameterTypes[1] == Int::class.javaPrimitiveType &&
                    m.parameterTypes[2] == Int::class.javaPrimitiveType
            }?.let { return it.apply { isAccessible = true } }
            c = c.superclass
        }
        return null
    }

    private fun findPackageManager(host: Any): PackageManager? {
        val hostClass = host.javaClass
        hostClass.findFieldByName("mPm")?.let { f ->
            f.isAccessible = true
            runCatching { f.get(host) as? PackageManager }.getOrNull()?.let { return it }
        }
        hostClass.declaredFields.forEach { f ->
            if (PackageManager::class.java.isAssignableFrom(f.type)) {
                f.isAccessible = true
                runCatching { f.get(host) as? PackageManager }.getOrNull()?.let { return it }
            }
        }
        val ctx = runCatching {
            hostClass.findMethodByName("getContext")?.invoke(host) as? Context
        }.getOrNull()
        return ctx?.packageManager
    }

    private fun findComponentNameCtor(clazz: Class<*>): Constructor<*>? {
        return clazz.declaredConstructors.firstOrNull { c ->
            val p = c.parameterTypes
            p.size == 4 &&
                Context::class.java.isAssignableFrom(p[0]) &&
                PackageManager::class.java.isAssignableFrom(p[1]) &&
                (p[2] == Int::class.javaPrimitiveType || p[2] == Integer::class.java) &&
                ComponentName::class.java.isAssignableFrom(p[3])
        }?.also { it.isAccessible = true }
    }

    private fun findDefaultAppInfoCtor(clazz: Class<*>): Constructor<*>? {
        return clazz.declaredConstructors.firstOrNull { c ->
            val p = c.parameterTypes
            p.size == 6 &&
                Context::class.java.isAssignableFrom(p[0]) &&
                PackageManager::class.java.isAssignableFrom(p[1]) &&
                (p[2] == Int::class.javaPrimitiveType || p[2] == Integer::class.java) &&
                PackageItemInfo::class.java.isAssignableFrom(p[3]) &&
                (p[4] == String::class.java || CharSequence::class.java.isAssignableFrom(p[4])) &&
                (p[5] == Boolean::class.javaPrimitiveType || p[5] == java.lang.Boolean::class.java)
        }?.also { it.isAccessible = true }
            ?: clazz.declaredConstructors.firstOrNull { it.parameterTypes.size == 6 }
                ?.also { it.isAccessible = true }
    }

    private fun Class<*>.findMethodByName(name: String): Method? {
        var c: Class<*>? = this
        while (c != null && c != Any::class.java) {
            c.declaredMethods.firstOrNull { it.name == name }?.let { return it.apply { isAccessible = true } }
            c = c.superclass
        }
        return null
    }

    private fun Class<*>.findFieldByName(name: String): Field? {
        var c: Class<*>? = this
        while (c != null && c != Any::class.java) {
            c.declaredFields.firstOrNull { it.name == name }?.let { return it }
            c = c.superclass
        }
        return null
    }

    private fun Class<*>.findFieldOfType(type: Class<*>): Field? {
        var c: Class<*>? = this
        while (c != null && c != Any::class.java) {
            c.declaredFields.firstOrNull { type.isAssignableFrom(it.type) }
                ?.let { return it.apply { isAccessible = true } }
            c = c.superclass
        }
        return null
    }
}
