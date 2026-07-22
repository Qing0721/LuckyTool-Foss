package com.fosstool.app.hook.utils

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.IBinder
import android.view.Display
import com.highcapable.yukihookapi.hook.factory.current
import com.highcapable.yukihookapi.hook.factory.extends
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.IBinderClass
import com.highcapable.yukihookapi.hook.type.java.LongType
import com.fosstool.app.hook.scope.systemui.FingerPrintIconAnim.toClass


@Suppress("unused", "MemberVisibilityCanBePrivate")
class DisplayManagerUtils(val classLoader: ClassLoader?) {

    val clazz = "android.hardware.display.DisplayManager".toClass(classLoader)
    val displayClazz = "android.view.Display".toClass(classLoader)
    val displayInfoClazz = "android.view.DisplayInfo".toClass(classLoader)
    val addressPhysicalClazz = "android.view.DisplayAddress\$Physical".toClass(classLoader)
    val surfaceControlClazz = "android.view.SurfaceControl".toClass(classLoader)

    fun getService(context: Context): DisplayManager {
        return context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    }

    fun Display.getDisplayInfo(outDisplayInfo: Any?): Boolean? {
        return displayClazz.method {
            name = "getDisplayInfo"
            param(displayInfoClazz)
        }.get(this).invoke<Boolean>(outDisplayInfo)
    }

    fun getDynamicDisplayInfo(displayInfo: Any): Any? {
        val address = displayInfo.current().field { name = "address" }.any() ?: return null
        val extend = (address.javaClass.extends(addressPhysicalClazz))
        val params = surfaceControlClazz.method { name = "getDynamicDisplayInfo";paramCount = 1 }
            .give()?.parameterTypes ?: return null
        val physicalDisplayId = getPhysicalDisplayId(address)
        val displayTokenOrId: Any? = when (params[0]) {
            LongType -> if (extend) physicalDisplayId else 0
            IBinderClass -> if (extend) getPhysicalDisplayToken(physicalDisplayId)
            else getInternalDisplayToken()

            else -> return null
        }
        return surfaceControlClazz.method {
            name = "getDynamicDisplayInfo"
            paramCount = 1
        }.get().call(displayTokenOrId)
    }

    fun getPhysicalDisplayId(physical: Any?): Long? {
        return addressPhysicalClazz.method {
            name = "getPhysicalDisplayId"
            emptyParam()
        }.get(physical).invoke<Long>()
    }

    fun getPhysicalDisplayToken(physicalDisplayId: Long?): IBinder? {
        return surfaceControlClazz.method {
            name = "getPhysicalDisplayToken"
            param(LongType)
        }.get().invoke<IBinder>(physicalDisplayId)
    }

    fun getInternalDisplayToken(): IBinder? {
        return surfaceControlClazz.method {
            name = "getInternalDisplayToken"
            emptyParam()
        }.get().invoke<IBinder>()
    }
}
