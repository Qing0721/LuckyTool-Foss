package com.fosstool.app.hook.scope.systemui

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Message
import android.view.View
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import com.fosstool.app.utils.ModulePrefs
import com.fosstool.app.utils.dp
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Field

object VolumeDialogWhiteBackground : YukiBaseHooker() {

    private var customAlpha = -1

    private var onShowHooked = false

    override fun onHook() {
        customAlpha = prefs(ModulePrefs).getInt("custom_volume_dialog_background_transparency", -1)
        dataChannel.wait<Int>("custom_volume_dialog_background_transparency") { customAlpha = it }
        if (customAlpha < 0) return

        val dialogImpl = VariousClass(
            "com.oplusos.systemui.volume.VolumeDialogImplEx",
            "com.oplus.systemui.volume.OplusVolumeDialogImpl"
        ).toClassOrNull(appClassLoader)
        if (dialogImpl == null) {
            YLog.error("VolumeDialogWhiteBackground: VolumeDialogImplEx not found")
            return
        }

        dialogImpl.method { name = "isSurrealQualityOn" }.ignored().hook { replaceToFalse() }

        dialogImpl.method { name { it.contains("initDialog") } }.ignored().hook {
            after {
                if (onShowHooked) return@after
                val self = instance ?: return@after
                val dialog = self.readField("mDialog") as? Dialog ?: return@after
                val showMessage = dialog.readField("mShowMessage") as? Message ?: return@after
                val listener = showMessage.obj ?: return@after
                onShowHooked = true
                hookOnShow(listener.javaClass, dialogImpl)
            }
        }

        val rowsMapField = dialogImpl.findFieldByName("mVerticalRowsLayerDrawableMap")
        if (rowsMapField != null) {
            dialogImpl.method { name = "updateRowsH" }.ignored().hook {
                before {
                    val map = runCatching { rowsMapField.get(instance) }.getOrNull() as? MutableMap<*, *>
                        ?: return@before
                    map.values.forEach { applyLayer(it) }
                }
            }
        }

        dialogImpl.method { name = "expandPanel" }.ignored().hook {
            before {
                val self = instance ?: return@before
                val layer = self.readField("mVolumeBackgroundLayerDrawable")
                if (layer is LayerDrawable) applyLayer(layer)
                else (self.readField("mVolumeBackgroundBlurDrawable") as? Drawable)
                    ?.alpha = customAlpha * 25
            }
        }

        "com.oplus.systemui.volume.OplusVolumeSeekBar".toClassOrNull(appClassLoader)
            ?.declaredConstructors?.forEach { ctor ->
                runCatching {
                    XposedBridge.hookMethod(ctor, object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            runCatching {
                                XposedHelpers.callMethod(
                                    param.thisObject, "setProgressColor",
                                    ColorStateList.valueOf(HALF_WHITE)
                                )
                            }
                        }
                    })
                }
            }

        "com.oplus.systemui.volume.utils.VolumeBlurManager".toClassOrNull(appClassLoader)?.let { m ->
            val hasBlurDrawable = m.declaredMethods.any { it.name == "getBackgroundBlurDrawable" }
            if (hasBlurDrawable) {
                m.method { name = "getBackgroundBlurDrawable" }.ignored().hook {
                    after { (result as? Drawable)?.setBlurRadiusCompat((customAlpha * 25).dp) }
                }
            } else {
                m.method { name = "getVolumeBarBackground" }.ignored().hook {
                    after { applyLayer(result) }
                }
            }
        }
    }

    private fun hookOnShow(listenerClass: Class<*>, dialogImpl: Class<*>) {
        val outerField = listenerClass.findFieldByType(dialogImpl)
        listenerClass.method { name = "onShow" }.ignored().hook {
            before {
                if (customAlpha < 0) return@before
                val alpha = customAlpha * 25
                val target = (outerField?.let { runCatching { it.get(instance) }.getOrNull() })
                    ?: instance ?: return@before

                applyLayer(target.readField("mVerticalRowsLayerDrawable"))

                val more = target.readField("mVolumeMoreLayerDrawable")
                if (more is LayerDrawable) applyLayer(more)
                else (target.readField("mMoreRowStreamLl") as? View)?.background?.alpha = 255 - alpha

                applyLayer(target.readField("mVolumeAppAdjustLayerDrawable"))
                applyLayer(target.readField("mVolumeCaptionLayerDrawable"))

                val background = target.readField("mVolumeBackgroundLayerDrawable")
                if (background is LayerDrawable) applyLayer(background)
                else (target.readField("mVolumeBackgroundBlurDrawable") as? Drawable)?.alpha = alpha

                val btn = target.readField("mVolumeBtnDrawable") as? Drawable
                if (btn != null) {
                    btn.alpha = 255 - alpha
                    return@before
                }
                (target.readField("mAppVolumeAdjustFl") as? View)?.background?.alpha = 255 - alpha
                (target.readField("mDoubleEarView") as? View)?.background?.alpha = 255 - alpha
                (target.readField("mODICaptionsView") as? View)?.background?.alpha = 255 - alpha
            }
        }
    }

    private fun applyLayer(any: Any?) {
        val layer = any as? LayerDrawable ?: return
        val value = customAlpha * 25
        runCatching { layer.getDrawable(0)?.setBlurRadiusCompat(value.dp) }
        runCatching { layer.getDrawable(1)?.alpha = value }
    }

    private fun Drawable.setBlurRadiusCompat(blurRadius: Int) {
        if (!javaClass.name.contains("BackgroundBlurDrawable")) return
        runCatching { XposedHelpers.callMethod(this, "setBlurRadius", blurRadius) }
    }

    private fun Any.readField(name: String): Any? =
        runCatching { javaClass.findFieldByName(name)?.get(this) }.getOrNull()

    private fun Class<*>.findFieldByName(name: String): Field? {
        var cls: Class<*>? = this
        while (cls != null && cls != Any::class.java) {
            cls.declaredFields.firstOrNull { it.name == name }
                ?.let { return it.apply { isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }

    private fun Class<*>.findFieldByType(type: Class<*>): Field? {
        var cls: Class<*>? = this
        while (cls != null && cls != Any::class.java) {
            cls.declaredFields.firstOrNull { type.isAssignableFrom(it.type) }
                ?.let { return it.apply { isAccessible = true } }
            cls = cls.superclass
        }
        return null
    }

    private val HALF_WHITE = 0x80FFFFFF.toInt()
}
