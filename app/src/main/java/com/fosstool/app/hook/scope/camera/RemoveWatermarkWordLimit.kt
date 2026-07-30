package com.fosstool.app.hook.scope.camera

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.A13
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import com.fosstool.app.utils.SDK
import org.luckypray.dexkit.query.matchers.MethodMatcher

object RemoveWatermarkWordLimit : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->

            dexKitBridge.findMethod {
                matcher {
                    name = "filter"
                    returnType = "java.lang.CharSequence"
                    paramTypes(
                        "java.lang.CharSequence",
                        "int",
                        "int",
                        "android.text.Spanned",
                        "int",
                        "int"
                    )
                    usingStrings("")
                    addInvoke(MethodMatcher().paramCount(2..3).returnType("void"))
                }
            }.apply {

                val onlyOne = SDK >= A13
                checkDataList("RemoveWatermarkWordLimit", onlyOne)
                val targets =
                    if (!onlyOne && size == 2) toList()
                    else listOfNotNull(firstOrNullSafe())
                targets.forEach { member ->
                    member.className.toClassOrNull(appClassLoader)
                        ?.method { name = "filter";paramCount = 6 }
                        ?.ignored()
                        ?.hook {
                            before {
                                result = args.getOrNull(0)
                            }
                        }
                }
            }
        }
    }
}
