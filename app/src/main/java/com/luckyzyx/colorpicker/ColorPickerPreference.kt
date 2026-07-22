package com.luckyzyx.colorpicker

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.preference.Preference
import com.fosstool.app.R
import com.fosstool.app.utils.dialogCentered
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.max
import kotlin.math.min

class ColorPickerPreference(context: Context) : Preference(context) {

    private var color: Int = 0
    private var colorStr: String = "#00000000"
    private var previewView: View? = null

    init {
        widgetLayoutResource = R.layout.preference_widget_color_preview
    }

    override fun onGetDefaultValue(ta: android.content.res.TypedArray, index: Int): Any {
        return "#00000000"
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val v = if (defaultValue != null) defaultValue.toString() else "#00000000"
        setColorValue(v)
    }

    private fun setColorValue(value: String) {
        colorStr = value
        try {
            color = Color.parseColor(value)
        } catch (_: Throwable) {
            color = 0
            colorStr = "#00000000"
        }
        previewView?.setBackgroundColor(color)
        summary = context.getString(R.string.color_picker_current_color, colorStr)
    }

    override fun onBindViewHolder(view: androidx.preference.PreferenceViewHolder) {
        super.onBindViewHolder(view)
        val preview = view.findViewById(R.id.color_preview)
        previewView = preview
        preview?.setBackgroundColor(color)
        view.itemView.setOnClickListener { showPickerDialog() }
        preview?.setOnClickListener { showPickerDialog() }
    }

    private fun showPickerDialog() {
        val ctx = context
        val dialogView = LinearLayout(ctx).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }
        val gradientView = ColorGradientView(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
            )
        }
        val previewBar = View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 160
            )
            setBackgroundColor(color)
        }
        val hexText = TextView(ctx).apply {
            text = String.format("#%08X", color)
            textSize = 14f
            setPadding(0, 24, 0, 24)
        }
        val alphaRow = LinearLayout(ctx).apply {
            orientation = android.widget.LinearLayout.HORIZONTAL
            val label = TextView(ctx).apply {
                text = "Alpha"
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(120, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            val seekBar = SeekBar(ctx).apply {
                max = 255
                progress = Color.alpha(color)
                layoutParams = LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            val alphaText = TextView(ctx).apply {
                text = Color.alpha(color).toString()
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(120, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT)
            }
            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, b: Boolean) {
                    val newColor = (color and 0xFFFFFF) or (min(max(p, 0), 255) shl 24)
                    color = newColor
                    colorStr = String.format("#%08X", newColor)
                    previewBar.setBackgroundColor(newColor)
                    hexText.text = colorStr
                    alphaText.text = p.toString()
                    gradientView.setColor(newColor)
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
            addView(label)
            addView(seekBar)
            addView(alphaText)
        }
        gradientView.setColor(color)
        gradientView.setOnColorChangedListener { c ->
            color = c
            colorStr = String.format("#%08X", c)
            previewBar.setBackgroundColor(c)
            hexText.text = colorStr
        }
        dialogView.addView(gradientView)
        dialogView.addView(previewBar)
        dialogView.addView(hexText)
        dialogView.addView(alphaRow)

        MaterialAlertDialogBuilder(ctx, dialogCentered)
            .setTitle(R.string.color_picker_select_color)
            .setView(dialogView)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                if (shouldPersist()) {
                    persistInt(color)
                }
                notifyChanged()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
}
