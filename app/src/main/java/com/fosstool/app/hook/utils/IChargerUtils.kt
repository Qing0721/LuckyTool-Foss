package com.fosstool.app.hook.utils

import android.os.IBinder
import androidx.annotation.DeprecatedSinceApi
import com.highcapable.yukihookapi.hook.bean.VariousClass
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.type.android.IBinderClass

@Suppress("MemberVisibilityCanBePrivate", "unused")
class IChargerUtils(val classLoader: ClassLoader?) {

    val clazz: Class<*> = VariousClass(
        HIDL_CLASS_NAME,
        "vendor.oplus.hardware.charger.ICharger"
    ).get(classLoader)

    val stub: Class<*> by lazy { STUB_CLASS_NAME.toClass(classLoader) }

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

        if (clazz.name == HIDL_CLASS_NAME) return getInstanceC13()
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

    private companion object {
        const val HIDL_CLASS_NAME = "vendor.oplus.hardware.charger.V1_0.ICharger"
        const val STUB_CLASS_NAME = "vendor.oplus.hardware.charger.ICharger\$Stub"
    }
}
