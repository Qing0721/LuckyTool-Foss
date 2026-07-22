package com.fosstool.app.hook.hooker

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.fosstool.app.hook.scope.gallery.HookConfigAbility
import com.fosstool.app.hook.scope.gallery.HookFunctionManager
import com.fosstool.app.hook.scope.gallery.HookSystemStorage
import com.fosstool.app.hook.scope.gallery.RemoveAigcEliminationLimit
import com.fosstool.app.hook.scope.gallery.RemoveGalleryWatermarkWordLimit

object HookGallery : YukiBaseHooker() {
    override fun onHook() {

        loadHooker(HookSystemStorage)
        loadHooker(HookConfigAbility)
        loadHooker(HookFunctionManager)
        loadHooker(RemoveAigcEliminationLimit)
        loadHooker(RemoveGalleryWatermarkWordLimit)
    }
}
