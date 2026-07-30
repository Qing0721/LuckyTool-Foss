package com.fosstool.app.hook.scope.pictorial

import android.widget.LinearLayout
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.constructor
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveVideoSaveWaterMark : YukiBaseHooker() {
    override fun onHook() {
        "com.heytap.pictorial.data.VideoWaterMarkView".toClassOrNull(appClassLoader)?.apply {
            constructor().hook {
                after { instance<LinearLayout>().removeAllViews() }
            }
        }
    }
}
