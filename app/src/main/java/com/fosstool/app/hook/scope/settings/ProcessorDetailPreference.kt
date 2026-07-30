package com.fosstool.app.hook.scope.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.fosstool.app.hook.utils.appcompat.dialog.COUIAlertDialogBuilder
import com.fosstool.app.utils.ModulePrefs
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClassOrNull

object ProcessorDetailPreference : YukiBaseHooker() {
    @SuppressLint("DiscouragedApi")
    override fun onHook() {
        val isImageSwitch =
            prefs(ModulePrefs).getBoolean("custom_processor_image_path_switch", false)
        val imagePath = prefs(ModulePrefs).getString("customize_processor_image_path", "")
        val isTextSwitch =
            prefs(ModulePrefs).getBoolean("custom_processor_introduction_text", false)

        if (!isImageSwitch && !isTextSwitch) return

        val hasValidPath = !imagePath.isNullOrEmpty() &&
            imagePath != "null" && imagePath != "Null"

        "com.oplus.settings.feature.deviceinfo.processordetail.ProcessorDetailPreference"
            .toClassOrNull(appClassLoader)
            ?.method { name = "onBindViewHolder"; paramCount = 1 }
            ?.ignored()
            ?.hook {
                after {
                    val holder = args.getOrNull(0) ?: return@after
                    val itemView = runCatching {
                        holder.javaClass.getDeclaredField("itemView").apply { isAccessible = true }
                            .get(holder) as? View
                    }.getOrNull() ?: return@after
                    val context = itemView.context
                    val settingsPkg = context.packageName
                    val textPrefs = context.getSharedPreferences(
                        "${settingsPkg}_lt_preferences", Context.MODE_PRIVATE
                    )

                    if (isImageSwitch && hasValidPath) {
                        applyImage(itemView, context, settingsPkg, imagePath!!)
                    }

                    if (isTextSwitch) {
                        applyText(itemView, context, settingsPkg, textPrefs)
                    }
                }
            }
    }

    private fun applyImage(
        itemView: View, context: Context, settingsPkg: String, imagePath: String
    ) {
        val ivTopId = context.resources.getIdentifier("iv_top", "id", settingsPkg)
        if (ivTopId == 0) return
        val imageView = itemView.findViewById<ImageView>(ivTopId) ?: return
        val bitmap = BitmapFactory.decodeFile(imagePath) ?: return
        val drawable = RoundedBitmapDrawableFactory.create(context.resources, bitmap).apply {
            setAntiAlias(true)
            cornerRadius = 12f * context.resources.displayMetrics.density
        }
        imageView.setImageDrawable(drawable)
    }

    @SuppressLint("DiscouragedApi")
    private fun applyText(
        itemView: View, context: Context, settingsPkg: String, textPrefs: SharedPreferences
    ) {
        val resNames = listOf(
            "tv_processor_description_1", "tv_processor_description_2", "tv_processor_description_3"
        )
        for (resName in resNames) {
            val tvId = context.resources.getIdentifier(resName, "id", settingsPkg)
            if (tvId == 0) continue
            val textView = itemView.findViewById<TextView>(tvId) ?: continue
            val customText = textPrefs.getString(resName, null)
            if (!customText.isNullOrEmpty()) {
                textView.text = customText
            }
            textView.setOnClickListener {
                showEditDialog(textView, textPrefs, resName)
            }
            textView.setOnLongClickListener {
                textPrefs.edit().remove(resName).commit()
                true
            }
        }
    }

    private fun showEditDialog(
        textView: TextView, textPrefs: SharedPreferences, key: String
    ) {
        val context = textView.context
        COUIAlertDialogBuilder(context, "COUIAlertDialog.SingleInput", appClassLoader).apply {
            var editText: EditText? = null
            var dialog: Any? = null
            dialog = builder?.apply {
                setTitle(textView.text)
                setNegativeButton(android.R.string.cancel, null)
                setPositiveButton(android.R.string.ok) { _, _ ->
                    val newText = editText?.text as? CharSequence
                    if (!newText.isNullOrBlank()) {
                        textView.text = newText
                        textPrefs.edit().putString(key, newText.toString()).commit()
                    }
                    dialog?.dismiss()
                }
            }?.show()
            editText = dialog?.getEditText("edit_text_1")
            editText?.apply {
                setSingleLine(false)
                maxLines = 5
                setText(textView.text)
            }
        }
    }
}
