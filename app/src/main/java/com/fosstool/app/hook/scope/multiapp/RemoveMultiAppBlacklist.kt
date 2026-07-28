package com.fosstool.app.hook.scope.multiapp

import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.getOSVersionCode
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Method

object RemoveMultiAppBlacklist : YukiBaseHooker() {
    override fun onHook() {
        if (getOSVersionCode < 31) return
        if (!prefs(ModulePrefs).getBoolean("remove_multi_app_blacklist", false)) return
        "com.oplus.multiapp.utils.MultiAppBlackListUpdateHelper".toClassOrNull(appClassLoader)
            ?.findMethod("loadMultiappBlackListConfig")
            ?.let { runCatching { XposedBridge.hookMethod(it, XC_MethodReplacement.returnConstant(null)) } }
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
