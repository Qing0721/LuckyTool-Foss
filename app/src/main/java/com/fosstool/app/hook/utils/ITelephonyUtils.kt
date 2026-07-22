package com.fosstool.app.hook.utils

import android.content.Context
import android.os.IBinder
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.factory.toClass
import com.highcapable.yukihookapi.hook.type.android.IBinderClass
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.LongType

@Suppress("unused", "MemberVisibilityCanBePrivate")
class ITelephonyUtils(val classLoader: ClassLoader?) {

    val clazz = "com.android.internal.telephony.ITelephony".toClass(classLoader)
    val stub = "com.android.internal.telephony.ITelephony\$Stub".toClass(classLoader)
    val manager = "android.telephony.TelephonyManager".toClass(classLoader)
    val constants = "com.android.internal.telephony.RILConstants".toClass(classLoader)

    fun getService(): IBinder? {
        return ServiceManagerUtils(null).getService(Context.TELEPHONY_SERVICE)
    }

    fun getInstance(iBinder: IBinder?): Any? {
        return stub.method {
            name = "asInterface"
            param(IBinderClass)
        }.get().call(iBinder)
    }

    val reasonUser = manager.field {
        name = "ALLOWED_NETWORK_TYPES_REASON_USER"
        type = IntType
    }.get().int()

    val bitMaskNR = manager.field {
        name = "NETWORK_TYPE_BITMASK_NR"
        type = LongType
    }.get().long()

    val modeLTE = constants.field {
        name = "NETWORK_MODE_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA"
        type = IntType
    }.get().int()

    val modeNR = constants.field {
        name = "NETWORK_MODE_NR_LTE_TDSCDMA_CDMA_EVDO_GSM_WCDMA"
        type = IntType
    }.get().int()

    fun getAllowedNetworkTypesForReason(instance: Any?, subId: Int, reason: Int): Long? {
        return clazz.method {
            name = "getAllowedNetworkTypesForReason"
            param(IntType, IntType)
        }.get(instance).invoke<Long>(subId, reason)
    }

    fun setAllowedNetworkTypesForReason(
        instance: Any?,
        subId: Int,
        reason: Int,
        allowedNetworkTypes: Long
    ): Boolean? {
        return clazz.method {
            name = "setAllowedNetworkTypesForReason"
            param(IntType, IntType, LongType)
        }.get(instance).invoke<Boolean>(subId, reason, allowedNetworkTypes)
    }

    fun getPreferredNetworkType(instance: Any?, subId: Int): Int? {
        return clazz.method {
            name = "getPreferredNetworkType"
            param(IntType)
        }.get(instance).invoke<Int>(subId)
    }

    fun setPreferredNetworkType(instance: Any?, subId: Int, networkType: Int): Boolean? {
        return clazz.method {
            name = "setPreferredNetworkType"
            param(IntType, IntType)
        }.get(instance).invoke<Boolean>(subId, networkType)
    }
}
