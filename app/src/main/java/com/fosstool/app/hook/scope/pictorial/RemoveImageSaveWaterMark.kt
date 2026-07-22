package com.fosstool.app.hook.scope.pictorial

import android.graphics.Bitmap
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.BitmapClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.HandlerClass
import com.highcapable.yukihookapi.hook.type.defined.VagueType
import com.highcapable.yukihookapi.hook.type.java.BooleanType
import com.highcapable.yukihookapi.hook.type.java.FileClass
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.highcapable.yukihookapi.hook.type.java.StringClass
import com.fosstool.app.utils.DexkitUtils
import com.fosstool.app.utils.DexkitUtils.checkDataList

object RemoveImageSaveWaterMark : YukiBaseHooker() {
    override fun onHook() {
        DexkitUtils.create(appInfo.sourceDir) { dexKitBridge ->
            dexKitBridge.findClass {
                matcher {
                    fields {
                        addForType(FileClass.name)
                        addForType(HandlerClass.name)
                        addForType(LongType.name)
                        addForType(BooleanType.name)
                        addForType(StringClass.name)
                    }
                    methods {
                        add { returnType(HandlerClass.name) }
                        add { returnType(BitmapClass.name) }
                        add { returnType(BooleanType.name) }
                        add { paramTypes(ContextClass.name) }
                        add { paramCount(5);returnType(BitmapClass.name) }
                        add { paramTypes("com.heytap.pictorial.core.bean.BasePictorialData") }
                    }
                }
            }.apply {
                checkDataList("RemoveImageSaveWaterMark")
                first().name.toClass().apply {
                    method {
                        param(BooleanType, VagueType, BitmapClass, BooleanType)
                        returnType = BitmapClass
                    }.hook {
                        after { result = args(2).cast<Bitmap>() ?: return@after }
                    }
                }
            }
        }
    }
}
