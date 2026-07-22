package com.fosstool.app.hook.scope.otherapp

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.SharedPreferences
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.ActivityClass
import com.highcapable.yukihookapi.hook.type.android.HandlerClass
import com.highcapable.yukihookapi.hook.type.android.MediaPlayerClass
import com.highcapable.yukihookapi.hook.type.android.PendingIntentClass
import com.highcapable.yukihookapi.hook.type.android.SharedPreferencesClass
import com.highcapable.yukihookapi.hook.type.android.TypefaceClass
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.HashMapClass
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList
import com.fosstool.app.utils.ModulePrefs

object HookADM : YukiBaseHooker() {
    override fun onHook() {
        val isPro = prefs(ModulePrefs).getBoolean("adm_unlock_pro", false)
        if (!isPro) return
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                searchPackages("com.dv.get")
                matcher {
                    fields {
                        addForType(SharedPreferencesClass.name)
                        addForType(SharedPreferences.Editor::class.java.name)
                        addForType(HandlerClass.name)
                        addForType(HashMapClass.name)
                        addForType(TypefaceClass.name)
                        addForType(ClipboardManager::class.java.name)
                        addForType(AlarmManager::class.java.name)
                        addForType(NotificationManager::class.java.name)
                        addForType(MediaPlayerClass.name)
                        addForType(PendingIntentClass.name)
                    }
                    usingStrings("firebase.test.lab")
                }
            }.apply {
                checkDataList("HookADM")
                first().name.toClass().apply {
                    method {
                        modifiers { isStatic }
                        param(ActivityClass)
                        returnType = UnitType
                    }.hookAll {
                        after {
                            field {
                                name = "n"
                                modifiers { isStatic }
                                type(BooleanType)
                            }.get().setTrue()
                        }
                    }
                }
            }
        }
    }
}
