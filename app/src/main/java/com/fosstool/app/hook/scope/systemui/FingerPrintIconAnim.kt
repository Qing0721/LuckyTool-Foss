package com.fosstool.app.hook.scope.systemui

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.utils.ModulePrefs

object FingerPrintIconAnim : YukiBaseHooker() {
    override fun onHook() {
        val removeMode = prefs(ModulePrefs).getString("remove_fingerprint_icon_mode", "0")
        val isReplaceIcon = prefs(ModulePrefs).getBoolean("replace_fingerprint_icon_switch", false)
        val iconPath = prefs(ModulePrefs).getString("replace_fingerprint_icon_path", "")

        VariousClass(
            "com.oplusos.systemui.keyguard.onscreenfingerprint.OnScreenFingerprintOpticalAnimCtrl",
            "com.oplus.systemui.keyguard.finger.onscreenfingerprint.OnScreenFingerprintUiMech",
            "com.oplus.systemui.biometrics.finger.udfps.OnScreenFingerprintUiMach",
            "com.oplus.systemui.biometrics.finger.udfps.OnScreenFingerprintUiMech"
        ).toClass().apply {
            method { name = "loadAnimDrawables" }.hook {
                when (removeMode) {
                    "0" -> if (isReplaceIcon) after {
                        instance.setCustomDrawable(
                            iconPath, true
                        )
                    }

                    "1" -> after { instance.setCustomDrawable(null, true) }
                    "2" -> after {
                        instance.removePressAnim()
                        if (isReplaceIcon) instance.setCustomDrawable(iconPath, true)
                    }

                    "3" -> intercept()
                }
            }
            method { name = "startFadeInAnimation" }.hook {
                if (isReplaceIcon) replaceUnit {
                    instance.setCustomDrawable(iconPath, false)
                } else if (removeMode == "1" || removeMode == "3") intercept()
            }
            method { name = "startFadeOutAnimation" }.hook {
                if (isReplaceIcon) intercept()
                else if (removeMode == "1" || removeMode == "3") intercept()
            }
        }
    }

    private fun Any.setCustomDrawable(iconPath: String?, update: Boolean) {
        this.current {
            val mContext = field { name = "mContext" }.cast<Context>()
            val getCurrentUserContext =
                method { name = "getCurrentUserContext" }.invoke<Context>(mContext) ?: return
            val drawable = if (iconPath.isNullOrEmpty()) null else BitmapDrawable(
                getCurrentUserContext.resources, BitmapFactory.decodeFile(iconPath)
            )
            if (drawable == null) {
                field { name = "mFadeInAnimDrawable" }.setNull()
                field { name = "mFadeOutAnimDrawable" }.setNull()
            }
            field { name = "mImMobileDrawable" }.set(drawable)
            field { name = "mFpIcon" }.cast<ImageView>()?.setImageDrawable(drawable)
            if (update) method { name = "updateFpIconColor" }.call()
        }
    }

    private fun Any.removePressAnim() {
        this.current().field { name = "mPressedAnimDrawable" }.setNull()
        this.current().field { name = "mPressedAnimDrawableTmp" }.setNull()
    }
}
