package com.fosstool.app.hook.scope.oplusgames

import android.view.View
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.type.defined.VagueType
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.ListClass
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import org.luckypray.dexkit.query.enums.StringMatchType

object RemoveWelfarePage : YukiBaseHooker() {

    private const val TAG = "LuckyTool"
    private const val MAIN_PANEL_VIEW = "business.mainpanel.MainPanelView"
    private const val MAIN_PANEL_FRAGMENT = "business.mainpanel.main.MainPanelFragment"
    private const val NAV_RADIO_BUTTON = "business.mainpanel.view.NavigationRadioButton"
    private const val WELFARE = "welfare"

    override fun onHook() {
        val mainPanelView = MAIN_PANEL_VIEW.toClassOrNull(appClassLoader)
        if (mainPanelView != null) {
            hookMainPanelView(mainPanelView)
            return
        }
        val fragment = MAIN_PANEL_FRAGMENT.toClassOrNull(appClassLoader)
        if (fragment == null) {
            YLog.error("RemoveWelfarePage -> neither $MAIN_PANEL_VIEW nor $MAIN_PANEL_FRAGMENT found", tag = TAG)
            return
        }
        hookMainPanelFragment(fragment)
        hookNavigationRadioButton()
    }

    private fun hookMainPanelView(clazz: Class<*>) {
        clazz.method {
            param { it[0] == ListClass && it[1] == BooleanType }
            paramCount(2..3)
            returnType = UnitType
        }.hook {
            before {
                val first = args().first().list<Any>().firstOrNull() ?: return@before
                args().first().set(arrayListOf(first))
            }
        }
    }

    private fun hookMainPanelFragment(clazz: Class<*>) {

        clazz.method { name = "addRadioButton" }.ignored().hook {
            before {
                val key = args.getOrNull(0) as? String ?: ""
                if (key == WELFARE) resultNull()
            }
        }

        clazz.method { name = "initView" }.ignored().hook {
            after {
                @Suppress("UNCHECKED_CAST")
                val map = clazz.field { name = "navButtonMap" }
                    .ignored().get(instance).any() as? HashMap<Any?, Any?>
                if (map == null) {
                    YLog.error("RemoveWelfarePage -> field navButtonMap not found on $MAIN_PANEL_FRAGMENT", tag = TAG)
                    return@after
                }
                map.remove(WELFARE)
            }
        }
    }

    private fun hookNavigationRadioButton() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            val classes = dexKitBridge.findClass {
                matcher { className(NAV_RADIO_BUTTON, StringMatchType.Equals) }
            }.checkDataList("RemoveWelfarePage NavigationRadioButton", onlyOne = false)
            if (classes.isEmpty()) return@create

            val fieldData = classes.findField {
                matcher {
                    type = StringClass.name
                    addReadMethod {
                        paramTypes(null, BooleanType.name, BooleanType.name)
                        returnType(UnitType.name)
                        usingStrings("perf", "tool")
                    }
                    declaredClass { usingStrings("NavigationRadioButton") }
                }
            }.firstOrNull()
            if (fieldData == null) {
                YLog.error("RemoveWelfarePage -> NavigationRadioButton key field not found", tag = TAG)
                return@create
            }

            val owner = fieldData.declaredClassName.toClassOrNull(appClassLoader) ?: run {
                YLog.error("RemoveWelfarePage -> ${fieldData.declaredClassName} not loadable", tag = TAG)
                return@create
            }
            val keyFieldName = fieldData.fieldName

            owner.method {
                param(VagueType, BooleanType, BooleanType)
                returnType = UnitType
            }.ignored().hook {
                before {
                    val key = owner.field { name = keyFieldName; type = StringClass }
                        .ignored().get(instance).any() as? String ?: return@before
                    if (key == WELFARE) {
                        (instance as? View)?.visibility = View.GONE
                    }
                }
            }
        }
    }
}
