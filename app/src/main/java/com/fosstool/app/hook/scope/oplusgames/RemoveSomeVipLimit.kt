package com.fosstool.app.hook.scope.oplusgames

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object RemoveSomeVipLimit : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.games.account.bean.VipInfoBean\$VipInfosDTO".toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "getVip" }.ignored().hook { replaceToTrue() }
            c.method { name = "getExpiredVip" }.ignored().hook { replaceToFalse() }
            c.method { name = "getExpireTime" }.ignored().hook { replaceTo("2999-12-31") }
            c.method { name = "getSign" }.ignored().hook { replaceToTrue() }
        }
        "com.oplus.games.account.bean.VipAccelearateResponse".toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "getSuperBooster" }.ignored().hook { replaceToTrue() }
            c.method { name = "isSuperBooster" }.ignored().hook { replaceToTrue() }
        }
        "com.oplus.games.account.bean.VIPStateBean".toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "getVipState" }.ignored().hook { replaceTo(5) }
            c.method { name = "getExpireTime" }.ignored().hook { replaceTo("2999-12-31") }
        }
        "com.coloros.gamespaceui.module.magicvoice.oplus.data.UserInfo".toClassOrNull(appClassLoader)?.let { c ->
            c.method { name = "getExpireTime" }.ignored().hook { replaceTo("2999-12-31") }
            c.method { name = "getHasTrialQualifications" }.ignored().hook { replaceToTrue() }
            c.method { name = "getUserIdentity" }.ignored().hook { replaceTo(3) }
        }
    }
}
