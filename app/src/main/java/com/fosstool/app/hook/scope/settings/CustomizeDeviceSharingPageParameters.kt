package com.fosstool.app.hook.scope.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.forEach
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.method
import com.fosstool.app.hook.utils.appcompat.dialog.COUIAlertDialogBuilder

object CustomizeDeviceSharingPageParameters : YukiBaseHooker() {

    @SuppressLint("DiscouragedApi")
    override fun onHook() {
        "com.oplus.settings.feature.deviceinfo.aboutphone.ShareAboutPhoneActivity".toClass().apply {
            method { name = "onCreate" }.hook {
                after {
                    val activity = instance<Activity>()
                    val shareViewId = activity.resources.getIdentifier(
                        "share_view", "id",
                        this@CustomizeDeviceSharingPageParameters.packageName
                    ).takeIf { it != 0 } ?: return@after
                    val shareView = activity.findViewById<ViewGroup>(shareViewId)
                        ?: return@after

                    shareView.children.forEach {
                        if (it is ViewGroup) it.forEach { it2 ->
                            if (it2 is TextView) it2.setClickInfo()
                            else if (it2 is ViewGroup) it2.forEach { it3 ->
                                if (it3 is TextView) it3.setClickInfo()
                                else if (it3 is ViewGroup) it3.forEach { it4 ->
                                    if (it4 is TextView) it4.setClickInfo()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun TextView.setClickInfo() {
        setOnClickListener {
            COUIAlertDialogBuilder(context, "COUIAlertDialog.SingleInput", appClassLoader).apply {
                var editText: EditText? = null
                var dialog: Any? = null
                dialog = builder?.apply {
                    setTitle(text)
                    setNegativeButton(android.R.string.cancel, null)
                    setPositiveButton(android.R.string.ok) { _, _ ->
                        val newText = editText?.text as CharSequence
                        if (newText.isNotBlank()) text = newText
                        dialog?.dismiss()
                    }
                }?.show()
                editText = dialog?.getEditText("edit_text_1")
                editText?.setText(text)
            }
        }
    }
}
