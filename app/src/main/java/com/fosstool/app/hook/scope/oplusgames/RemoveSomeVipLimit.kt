package com.fosstool.app.hook.scope.oplusgames

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method

object RemoveSomeVipLimit : YukiBaseHooker() {
    override fun onHook() {
        "com.oplus.games.account.bean.VipInfoBean\$VipInfosDTO".toClass().apply {
            method { name = "getVip" }.hook {
                replaceToTrue()
            }
            method { name = "getExpiredVip" }.hook {
                replaceToFalse()
            }
            method { name = "getExpireTime" }.hook {
                replaceTo("2999-12-31")
            }
            method { name = "getSign" }.hook {
                replaceToTrue()
            }
        }
        "com.oplus.games.account.bean.VipAccelearateResponse".toClass().apply {
            method { name = "getSuperBooster" }.hook {
                replaceToTrue()
            }
            method { name = "isSuperBooster" }.hook {
                replaceToTrue()
            }
        }
        "com.oplus.games.account.bean.VIPStateBean".toClass().apply {
            method { name = "getVipState" }.hook {
                replaceTo(5)
            }
            method { name = "getExpireTime" }.hook {
                replaceTo("2999-12-31")
            }
        }
        "com.coloros.gamespaceui.module.magicvoice.oplus.data.UserInfo".toClass().apply {
            method { name = "getExpireTime" }.hook {
                replaceTo("2999-12-31")
            }
            method { name = "getHasTrialQualifications" }.hook {
                replaceToTrue()
            }
            method { name = "getUserIdentity" }.hook {
                replaceTo(3)
            }
        }
    }
}
