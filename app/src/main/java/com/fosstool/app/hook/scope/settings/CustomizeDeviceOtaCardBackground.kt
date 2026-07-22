package com.fosstool.app.hook.scope.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.BitmapFactory
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.fosstool.app.hook.utils.OplusBuildUtlils
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.log.YLog

object CustomizeDeviceOtaCardBackground : YukiBaseHooker() {

    private val HIDE_VIEW_NAMES = setOf(
        "logo_view",
        "linearLayout",
        "model_name",
        "model_ai",
        "model_description",
        "coloros_logo",
        "model_number",
        "img_intent_ota",
        "update_linear",
        "lin_center",
        "device_market_name",
        "update_text",
        "update_find",
    )

    @SuppressLint("DiscouragedApi")
    override fun onHook() {
        val path = prefs(ModulePrefs).getString("customize_device_ota_card_background_path", "")
            ?.takeIf { it.isNotBlank() && it != "null" && it != "Null" }
        val hideText = prefs(ModulePrefs).getBoolean("hide_ota_card_top_text", false)

        if (path == null && !hideText) return

        val className = "com.oplus.settings.widget.preference.AboutDeviceOtaUpdatePreference"
        try {
            className.toClass().apply {
                method { name = "onBindViewHolder"; paramCount = 1 }.hook {
                    before {
                        val holder = args().first().any() ?: return@before
                        val itemView = field { name = "itemView" }.get(holder).cast<View>()
                            ?: return@before
                        val context = itemView.context
                        val settingsPkg = context.packageName

                        if (path != null && itemView is RelativeLayout) {
                            applyBackground(itemView, context, settingsPkg, path)
                        }

                        if (hideText && itemView is ViewGroup) {
                            hideTopText(itemView)
                        }
                    }
                }
            }
        } catch (e: Throwable) {
            YLog.error(
                "CustomizeDeviceOtaCardBackground: $className not found",
                tag = "LuckyTool"
            )
        }

        val applySharing = prefs(ModulePrefs).getBoolean("apply_device_parameter_sharing_page", false)
        val osVersionCode = try { OplusBuildUtlils().getOSVersionCode ?: 0 } catch (_: Throwable) { 0 }
        if (applySharing && osVersionCode >= 34 && path != null) {
            val sharingClassName = "com.oplus.settings.feature.deviceinfo.aboutphone.ShareAboutPhoneActivity"
            try {
                sharingClassName.toClass().apply {
                    method { name = "updateOsVersion" }.hook {
                        before {
                            val activity = instance<Activity>() ?: return@before
                            applySharingBackground(activity, path)
                        }
                    }
                }
            } catch (e: Throwable) {
                YLog.error(
                    "CustomizeDeviceOtaCardBackground: $sharingClassName not found",
                    tag = "LuckyTool"
                )
            }
        }
    }

    private fun applyBackground(
        itemView: RelativeLayout, context: android.content.Context, settingsPkg: String, path: String
    ) {
        val bgId = context.resources.getIdentifier("about_device_top_bg", "id", settingsPkg)
        val maskId = context.resources.getIdentifier("about_device_top_video_mask", "id", settingsPkg)
        val bitmap = BitmapFactory.decodeFile(path) ?: return
        val drawable = RoundedBitmapDrawableFactory.create(context.resources, bitmap).apply {
            setAntiAlias(true)
            cornerRadius = 12f * context.resources.displayMetrics.density
        }
        if (bgId != 0) {
            itemView.findViewById<ImageView>(bgId)?.setImageDrawable(drawable)
        }
        if (maskId != 0) {
            itemView.findViewById<ImageView>(maskId)?.setImageDrawable(drawable)
        }
    }

    private fun hideTopText(itemView: ViewGroup) {
        for (i in 0 until itemView.childCount) {
            val child = itemView.getChildAt(i) ?: continue
            val resName = try {
                child.resources.getResourceEntryName(child.id)
            } catch (_: Throwable) {
                null
            }
            if (resName != null && resName in HIDE_VIEW_NAMES) {
                child.visibility = View.GONE
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun applySharingBackground(activity: Activity, path: String) {
        val settingsPkg = activity.packageName
        val layoutId = activity.resources.getIdentifier("parent_relativeLayout", "id", settingsPkg)
        if (layoutId == 0) return
        val relativeLayout = activity.findViewById<RelativeLayout>(layoutId) ?: return
        val bitmap = BitmapFactory.decodeFile(path) ?: return
        val drawable = RoundedBitmapDrawableFactory.create(activity.resources, bitmap).apply {
            setAntiAlias(true)
            cornerRadius = 12f * activity.resources.displayMetrics.density
        }
        val bgId = activity.resources.getIdentifier("about_device_top_bg", "id", settingsPkg)
        if (bgId != 0) {
            val bgView = activity.findViewById<View>(bgId)
            if (bgView != null) relativeLayout.removeView(bgView)
        }
        relativeLayout.background = drawable
    }
}
