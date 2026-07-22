@file:Suppress("unused")

package com.fosstool.app.ui.refactor

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment

typealias OnBackPressedTypeAlias = () -> Unit

fun Fragment.setOnBackPressed(type: Boolean = true, callback: OnBackPressedTypeAlias? = null) {
    requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (type) {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                } else {
                    callback?.invoke()
                }
            }
        })
}
