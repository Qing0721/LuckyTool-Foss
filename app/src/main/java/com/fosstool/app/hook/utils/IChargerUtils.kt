package com.fosstool.app.hook.utils

import android.os.IBinder
import androidx.annotation.DeprecatedSinceApi
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.IBinderClass
import com.fosstool.app.hook.scope.systemui.StatusBarBatteryInfoNotify.toClass

@Suppress("MemberVisibilityCanBePrivate", "unused")
class IChargerUtils(val classLoader: ClassLoader?) {

    val clazz = VariousClass(
        "vendor.oplus.hardware.charger.V1_0.ICharger",
        "vendor.oplus.hardware.charger.ICharger"
    ).toClass(classLoader)

    val stub = "${clazz.canonicalName}\$Stub".toClass(classLoader)
    val serviceName = "vendor.oplus.hardware.charger.ICharger/default"

    @DeprecatedSinceApi(34, "不支持在ColorOS14中使用")
    fun getInstanceC13(): Any? {
        return clazz.method {
            name = "getService"
            emptyParam()
        }.get().call()
    }

    fun getService(): IBinder? {
        return ServiceManagerUtils(classLoader).getService(serviceName)
    }

    fun getInstance(): Any? {
        return stub.method {
            name = "asInterface"
            param(IBinderClass)
        }.get().call(getService())
    }

    fun queryChargeInfo(ins: Any?): String? {
        return clazz.method {
            name = "queryChargeInfo"
            emptyParam()
        }.get(ins).invoke<String>()
    }

    fun getUIsohValue(ins: Any?): Int? {
        return clazz.method {
            name = "getUIsohValue"
            emptyParam()
        }.get(ins).invoke<Int>()
    }
}
