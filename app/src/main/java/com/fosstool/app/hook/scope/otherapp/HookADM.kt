package com.fosstool.app.hook.scope.otherapp

import android.app.Activity
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Method

object HookADM : YukiBaseHooker() {
    override fun onHook() {
        val isPro = prefs(ModulePrefs).getBoolean("adm_unlock_pro", false)
        if (!isPro) return

        val main = "com.dv.get.Main".toClassOrNull(appClassLoader) ?: return
        val onCreate = main.findMethod("onCreate") ?: return
        runCatching {
            XposedBridge.hookMethod(onCreate, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val activity = param.thisObject as? Activity ?: return
                    unlockProPrefs(activity)
                }
            })
        }
    }

    private fun unlockProPrefs(activity: Activity) {
        runCatching {
            val name = activity.packageName + "_preferences"
            activity.getSharedPreferences(name, 0).edit()
                .putBoolean("EVENT_DISA", false)
                .putBoolean("hua_voices", false)
                .commit()
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
