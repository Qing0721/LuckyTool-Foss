package com.fosstool.app.hook.scope.systemui

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.fosstool.app.utils.ModulePrefs
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

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
        ).toClassOrNull(appClassLoader)?.let { c ->
            when (removeMode) {
                "0" -> if (isReplaceIcon) c.method { name = "loadAnimDrawables" }.ignored().hook {
                    after {
                        instance.setCustomDrawable(iconPath, true)
                    }
                }

                "1" -> c.method { name = "loadAnimDrawables" }.ignored().hook {
                    after {
                        instance.setCustomDrawable(null, true)
                    }
                }

                "2" -> c.method { name = "loadAnimDrawables" }.ignored().hook {
                    after {
                        instance.removePressAnim()
                        if (isReplaceIcon) instance.setCustomDrawable(iconPath, true)
                    }
                }

                "3" -> c.method { name = "loadAnimDrawables" }.ignored().hook { intercept() }
            }
            if (isReplaceIcon) {
                c.method { name = "startFadeInAnimation" }.ignored().hook {
                    before {
                        instance.setCustomDrawable(iconPath, false)
                        result = null
                    }
                }
            } else if (removeMode == "1" || removeMode == "3") {
                c.method { name = "startFadeInAnimation" }.ignored().hook { intercept() }
            }
            if (isReplaceIcon || removeMode == "1" || removeMode == "3") {
                c.method { name = "startFadeOutAnimation" }.ignored().hook { intercept() }
            }
        }
    }

    private fun Any.setCustomDrawable(iconPath: String?, update: Boolean) {
        val clazz = javaClass

        val userCtx = (clazz.findField("mContext")?.get(this) as? Context)
            ?: (clazz.findFieldOfType(Context::class.java)?.get(this) as? Context)
            ?: return
        val drawable = if (iconPath.isNullOrEmpty()) null else BitmapDrawable(
            userCtx.resources, BitmapFactory.decodeFile(iconPath)
        )
        if (drawable == null) {
            clazz.findField("mFadeInAnimDrawable")?.set(this, null)
            clazz.findField("mFadeOutAnimDrawable")?.set(this, null)
        }
        clazz.findField("mImMobileDrawable")?.set(this, drawable)
        (clazz.findField("mFpIcon")?.get(this) as? ImageView)?.setImageDrawable(drawable)
        if (update) runCatching {
            XposedHelpers.callMethod(this, "updateFpIconColor")
        }
    }

    private fun Any.removePressAnim() {
        javaClass.findField("mPressedAnimDrawable")?.set(this, null)
        javaClass.findField("mPressedAnimDrawableTmp")?.set(this, null)
    }

    private fun Class<*>.findFieldOfType(type: Class<*>): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            cls.declaredFields.firstOrNull { type.isAssignableFrom(it.type) }
                ?.let { f -> return f.also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }

    private fun Class<*>.findField(name: String): Field? {
        var cls: Class<*>? = this
        while (cls != null) {
            runCatching { return cls.getDeclaredField(name).also { it.isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }
}
