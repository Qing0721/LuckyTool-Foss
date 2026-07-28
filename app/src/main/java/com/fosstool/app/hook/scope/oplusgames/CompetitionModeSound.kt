package com.fosstool.app.hook.scope.oplusgames

import android.media.AudioManager
import android.media.SoundPool
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.SparseIntArrayClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.DexkitUtils.firstOrNullSafe
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge

object CompetitionModeSound : YukiBaseHooker() {
    const val key = "remove_competition_mode_sound"
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(ContextClass.name)
                        addForType(BooleanType.name)
                        addForType(SoundPool::class.java.name)
                        addForType(AudioManager::class.java.name)
                        addForType(SparseIntArrayClass.name)
                    }
                    methods {
                        add {
                            paramCount(0)
                            returnType(UnitType.name)
                        }
                        add {
                            paramTypes(IntType.name)
                            returnType(UnitType.name)
                        }
                    }
                }
            }.apply {
                checkDataList("CompetitionModeSound")
                val cls = (firstOrNullSafe()?.name ?: return@apply).toClassOrNull(appClassLoader) ?: return@apply
                for (m in cls.declaredMethods) {
                    if (m.parameterCount == 1 && m.parameterTypes[0] == Int::class.javaPrimitiveType) {
                        runCatching {
                            XposedBridge.hookMethod(m, object : XC_MethodHook() {
                                override fun beforeHookedMethod(param: MethodHookParam) {
                                    if (param.args.getOrNull(0) as? Int == 9) {
                                        param.result = null
                                    }
                                }
                            })
                        }
                    }
                }
            }
        }
    }
}
