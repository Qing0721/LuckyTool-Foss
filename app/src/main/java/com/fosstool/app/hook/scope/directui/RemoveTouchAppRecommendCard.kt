package com.fosstool.app.hook.scope.directui

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Method

object RemoveTouchAppRecommendCard : YukiBaseHooker() {
    override fun onHook() {
        "com.coloros.directui.repository.datasource.AppBean".toClassOrNull(appClassLoader)
            ?.findMethod("toCardUIInfo")
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
