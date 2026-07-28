package com.fosstool.app.hook.scope.quicksearchbox

import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.java.StringClass
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import java.lang.reflect.Method

object SearchboxDefaultSearchLocalTab : YukiBaseHooker() {
    override fun onHook() {
        val hooked = "com.heytap.quicksearchbox.ui.fragment.SearchResultFragment"
            .toClassOrNull(appClassLoader)
            ?.findMethod("getDefaultTabId")
            ?.also { runCatching { XposedBridge.hookMethod(it, XC_MethodReplacement.returnConstant("local")) } } != null
        if (hooked) return

        DexkitUtils.create(appInfo.sourceDir) { bridge ->

            val classes = bridge.findClass {
                matcher { className("com.heytap.quicksearchbox.ui.fragment.SearchResultFragment") }
            }.checkDataList("SearchboxDefaultSearchLocalTab SearchResultFragment", onlyOne = false)
            if (classes.isEmpty()) return@create

            bridge.findMethod {
                searchInClass(classes)
                matcher {
                    paramCount = 0
                    returnType(StringClass.name)
                    addUsingField {
                        type("com.heytap.quicksearchbox.core.localsearch.SearchParams")
                    }
                    addUsingField {
                        type("com.heytap.common.bean.TabItems")
                    }

                    addCaller {
                        paramCount = 0
                        returnType("void")
                    }
                }
            }.apply {
                checkDataList("SearchboxDefaultSearchLocalTab getDefaultTabId", onlyOne = false)
                firstOrNullSafe()?.apply {
                    className.toClassOrNull(appClassLoader)
                        ?.method { name = methodName; emptyParam(); returnType = StringClass }
                        ?.ignored()
                        ?.hook { replaceTo("local") }
                }
            }
        }
    }

    private fun Class<*>.findMethod(name: String): Method? {
        var c: Class<*>? = this
        while (c != null && c != Any::class.java) {
            c.declaredMethods.firstOrNull { it.name == name && it.parameterCount == 0 }
                ?.let { return it.apply { isAccessible = true } }
            c = c.superclass
        }
        return null
    }
}
