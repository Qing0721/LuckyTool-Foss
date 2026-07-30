package com.fosstool.app.hook.scope.externalstorage

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog

object RemoveStorageLimit : YukiBaseHooker() {
    override fun onHook() {
        val clazz = "com.android.externalstorage.ExternalStorageProvider".toClassOrNull(appClassLoader)
        if (clazz == null) {
            YLog.error("RemoveStorageLimit: ExternalStorageProvider not found", tag = "LuckyTool")
            return
        }
        clazz.method { name = "shouldBlockDirectoryFromTree" }
            .ignored()
            .hook { replaceToFalse() }
    }
}
