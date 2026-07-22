package com.fosstool.app.hook.utils.appcompat.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.View
import android.widget.EditText
import com.highcapable.yukihookapi.hook.factory.buildOf
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.type.android.ContextClass
import com.highcapable.yukihookapi.hook.type.android.DialogInterface_OnClickListenerClass
import com.highcapable.yukihookapi.hook.type.android.ViewClass
import com.highcapable.yukihookapi.hook.type.java.CharSequenceClass
import com.highcapable.yukihookapi.hook.type.java.IntType

@Suppress("unused")
class COUIAlertDialogBuilder {

    private val clazzString = "com.coui.appcompat.dialog.COUIAlertDialogBuilder"
    var context: Context
    var clazz: Class<*>
    var builder: Any?

    constructor(context: Context, classloader: ClassLoader?) {
        this.context = context
        this.clazz = clazzString.toClass(classloader)
        builder = clazz.buildOf(context) {
            param(ContextClass)
        }
    }

    constructor(context: Context, themeResId: Int, classloader: ClassLoader?) {
        this.context = context
        this.clazz = clazzString.toClass(classloader)
        builder = clazz.buildOf(context, themeResId) {
            param(ContextClass, IntType)
        }
    }

    constructor(context: Context, themeResName: String, classloader: ClassLoader?) {
        this.context = context
        this.clazz = clazzString.toClass(classloader)
        val themeResId = getStyle(themeResName)
        builder = clazz.buildOf(context, themeResId) {
            param(ContextClass, IntType)
        }
    }

    constructor(context: Context, themeResId: Int, themeResId2: Int, classloader: ClassLoader?) {
        this.context = context
        this.clazz = clazzString.toClass(classloader)
        builder = clazz.buildOf(context, themeResId, themeResId2) {
            param(ContextClass, IntType, IntType)
        }
    }

    fun Any.setTitle(charSequence: CharSequence): Any? {
        return this.current().method {
            name = "setTitle"
            param(CharSequenceClass)
        }.call(charSequence)
    }

    fun Any.setTitle(int: Int): Any? {
        return this.current().method {
            name = "setTitle"
            param(IntType)
        }.call(int)
    }

    fun Any.setMessage(charSequence: CharSequence): Any? {
        return this.current().method {
            name = "setMessage"
            param(CharSequenceClass)
        }.call(charSequence)
    }

    fun Any.setMessage(int: Int): Any? {
        return this.current().method {
            name = "setMessage"
            param(IntType)
        }.call(int)
    }

    fun Any.setPositiveButton(
        charSequence: CharSequence, onClickListener: DialogInterface.OnClickListener?
    ): Any? {
        return this.current().method {
            name = "setPositiveButton"
            param(CharSequenceClass, DialogInterface_OnClickListenerClass)
        }.call(charSequence, onClickListener)
    }

    fun Any.setPositiveButton(int: Int, onClickListener: DialogInterface.OnClickListener?): Any? {
        return this.current().method {
            name = "setPositiveButton"
            param(IntType, DialogInterface_OnClickListenerClass)
        }.call(int, onClickListener)
    }

    fun Any.setNeutralButton(
        charSequence: CharSequence, onClickListener: DialogInterface.OnClickListener?
    ): Any? {
        return this.current().method {
            name = "setNeutralButton"
            param(CharSequenceClass, DialogInterface_OnClickListenerClass)
        }.call(charSequence, onClickListener)
    }

    fun Any.setNeutralButton(int: Int, onClickListener: DialogInterface.OnClickListener?): Any? {
        return this.current().method {
            name = "setNeutralButton"
            param(IntType, DialogInterface_OnClickListenerClass)
        }.call(int, onClickListener)
    }

    fun Any.setNegativeButton(
        charSequence: CharSequence, onClickListener: DialogInterface.OnClickListener?
    ): Any? {
        return this.current().method {
            name = "setNegativeButton"
            param(CharSequenceClass, DialogInterface_OnClickListenerClass)
        }.call(charSequence, onClickListener)
    }

    fun Any.setNegativeButton(int: Int, onClickListener: DialogInterface.OnClickListener?): Any? {
        return this.current().method {
            name = "setNegativeButton"
            param(IntType, DialogInterface_OnClickListenerClass)
        }.call(int, onClickListener)
    }

    fun Any.setView(int: Int): Any? {
        return this.current().method {
            name = "setView"
            param(IntType)
        }.call(int)
    }

    fun Any.setView(view: View): Any? {
        return this.current().method {
            name = "setView"
            param(ViewClass)
        }.call(view)
    }

    fun Any.create(): Any? {
        return this.current().method {
            name = "create"
            emptyParam()
        }.call()
    }

    fun Any.dismiss() {
        this.current().method {
            name = "dismiss"
            emptyParam()
            superClass()
        }.call()
    }

    fun Any.show(): Any? {
        return this.current().method {
            name = "show"
            emptyParam()
        }.call()
    }

    fun Any.findViewById(id: Int): View? {
        return this.current().method {
            name = "findViewById"
            param(IntType)
            superClass()
        }.invoke<View>(id)
    }

    @SuppressLint("DiscouragedApi")
    fun Any.findViewById(idName: String): View? {
        val id = context.resources.getIdentifier(idName, "id", context.packageName)
        return this.current().method {
            name = "findViewById"
            param(IntType)
            superClass()
        }.invoke<View>(id)
    }

    fun Any.getEditText(idName: String): EditText? {
        return findViewById(idName)?.current()?.method {
            name = "getEditText"
            emptyParam()
        }?.invoke<EditText>()
    }

    @SuppressLint("DiscouragedApi")
    fun getStyle(name: String): Int {
        if (name.isBlank()) return 0
        return context.resources.getIdentifier(name.let {
            it.takeIf { e -> e.contains("_") }?.replace("_", ".") ?: it
        }, "style", context.packageName)
    }

    val styleList = arrayOf(
        "COUIAlertDialog_Center",
        "COUIAlertDialog_SingleInput"
    )
}
